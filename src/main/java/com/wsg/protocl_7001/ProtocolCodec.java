package com.wsg.protocl_7001;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * 连接建立请求报文
 * +---------+------+--------------+----------------+-------------+----------+
 * | Version | Type | Reserved Bit | Reserved Field | Terminal ID | CheckSum |
 * | (3bit)  |(3bit)|    (2bit)    |    (1Byte)     |   (6Byte)   |  (4Byte) |
 * +---------+------+--------------+----------------+-------------+----------+
 * 说明：
 * Version：001
 * Type：001
 * Reserved Bit：00
 * Reserved Field: 0x00
 * Terminal ID：终端唯一标识
 * CheckSum：先计算MD5(Version+Type+Reserved+TerminalID+SharedKey)，计算结果每四字节异或
 * 
 * 连接建立响应报文
 * +---------+------+--------------+------------+------------+---------+
 * | Version | Type | Reserved Bit | Arithmetic | Key Length |  Key    |
 * | (3bit)  |(3bit)|    (2bit)    |  (1Byte)   |  (2Byte)   | (nByte) |
 * +---------+------+--------------+------------+------------+---------+
 * 说明：
 * Version：001
 * Type：001
 * Reserved Bit：00
 * Arithmetic：0x01，当前采用RC4算法
 * Key Length：密钥字段长度
 * Key：随机生成的密钥
 * 
 * 数据报文
 * +---------+------+----------+---------+----------+---------+---------+
 * | Version | Type | Compress | Encrypt | Sequence | Length  |  Body   |
 * | (3bit)  |(3bit)|  (1bit)  | (1bit)  |  (1Byte) | (2Byte) | (nByte) |
 * +---------+------+----------+---------+----------+---------+---------+
 * 说明：
 * Version：001
 * Type：终端发到平台002，平台发到终端003
 * Compress：1为开启压缩，0为禁用压缩
 * Encrypt：1为开启加密，0为禁用加密
 * Sequence：每个报文的唯一ID，回复报文必须带相同的ID
 * Length：Body内容体长度
 * Body：内容，body为空的报文为链路通断测试报文，直接回复原报文
 * 
 * @author chenfan
 *
 */
public class ProtocolCodec extends ByteToMessageCodec<Message> {
	private static Log logger = LogFactory.getLog(ProtocolCodec.class);

	private static byte[] SHARED_KEY = "4ia09rjI81Jerf3Hnvjdqr8eio43rwtbf4214grfafrwq4313re124f929Efhaf1h3nfdsncdkjauhrhwq4321nnfjd"
			.getBytes(Charset.forName("ascii"));

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		in.markReaderIndex();
		byte head = in.readByte();
		if ((head & 0xe0) != 0x20) {
			if(logger.isErrorEnabled()){
				logger.error("Invalid protocol version [0x"+Integer.toHexString(head)+"]: "+ctx.channel());
			}
			ctx.close();
			return;
		}
		byte type = (byte) (head & 0x1c);
		Message message = new Message();
		message.setType(type);
		if (message.isType(Message.TYPE_CONNECTION_ESTABLISH)) {
			//建立连接请求报文
			if (in.readableBytes() < 11) {
				in.resetReaderIndex();
				return;
			}
			byte reserved = in.readByte();
			byte[] terminalIdBytes = new byte[6];
			in.readBytes(terminalIdBytes);
			byte[] checksum = new byte[4];
			in.readBytes(checksum);
			if (!Arrays.equals(checksum, getChecksum(head, reserved, terminalIdBytes))) {
				if(logger.isErrorEnabled()){
					logger.error("Checksum error, close connection: "+ ctx.channel());
				}
				ctx.close();
				return;
			}
			String terminalId = Hex.encodeHexString(terminalIdBytes);
			message.setTerminalId(terminalId);
			out.add(message);
		} else if (message.isType(Message.TYPE_TERMINAL_SEND) || message.isType(Message.TYPE_PLATFORM_SEND)) {
			if (in.readableBytes() < 3) {
				in.resetReaderIndex();
				return;
			}
			byte sequence = in.readByte();
			message.setSequence(sequence);
			short length = in.readShort();
			if (length < 0 || length > 102400) {
				if(logger.isErrorEnabled()){
					logger.error("Error! message length("+length+") is invalid, close connection: "+  ctx.channel());
				}
				ctx.close();
				return;
			}
			if (in.readableBytes() < length) {
				in.resetReaderIndex();
				return;
			}
			if ((head & 0x01) == 0x01) {
				message.setEncrypt(true);
			} else {
				message.setEncrypt(false);
			}
			byte[] data = null;
			if (length > 0) {
				data = new byte[length];
				in.readBytes(data);
			}
			message.setData(data);
			out.add(message);
		} else {
			if(logger.isErrorEnabled()){
				logger.error("Invalid protocol type[0x"+Integer.toHexString(head)+"]: "+ ctx.channel());
			}
			ctx.close();
			return;
		}
	}

	private byte[] getChecksum(byte head, byte reserved, byte[] terminalIdBytes) {
		byte[] src = new byte[2 + terminalIdBytes.length + SHARED_KEY.length];
		src[0] = head;
		src[1] = reserved;
		System.arraycopy(terminalIdBytes, 0, src, 2, terminalIdBytes.length);
		System.arraycopy(SHARED_KEY, 0, src, 2 + terminalIdBytes.length, SHARED_KEY.length);
		byte[] md5 = HashUtil.md5(src);
		byte[] checksum = new byte[4];
		for (int i = 0; i < 4; i++) {
			checksum[i] = (byte) (md5[i] ^ md5[i + 4] ^ md5[i + 8] ^ md5[i + 12]);
		}
		return checksum;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {
		if (message == null) {
			return;
		}

		if (message.isType(Message.TYPE_CONNECTION_ESTABLISH)) {
			// 连接建立回应报文
			out.writeByte(0x24);
			out.writeByte(message.getArithmetic());
			byte[] key = message.getKey();
			out.writeShort(key.length);
			out.writeBytes(key);
		} else if (message.isType(Message.TYPE_TERMINAL_SEND) || message.isType(Message.TYPE_PLATFORM_SEND)) {
			if (message.isType(Message.TYPE_TERMINAL_SEND)) {
				out.writeByte(0x29);
			} else {
				out.writeByte(0x2d);
			}
			out.writeByte(message.getSequence());
			byte[] data = message.getData();
			if (data == null || data.length == 0) {
				out.writeShort(0);
			} else {
				//data = codec.encrypt(key, data);
				out.writeShort(data.length);
				out.writeBytes(data);
			}
		} else {
			if(logger.isErrorEnabled()){
				logger.error("Invalid message type, ignore: "+ ctx.channel());
			}
		}
	}

}

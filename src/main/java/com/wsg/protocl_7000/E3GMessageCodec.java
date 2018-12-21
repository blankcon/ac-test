package com.wsg.protocl_7000;

import com.wsg.protocl_7001.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 
 * +------+------+------+--------+----------+ | Type | Flag | ID | Length |
 * Content | | (1B) | (1B) | (6B) | (4B) | (Length) |
 * +------+------+------+--------+----------+
 * 
 * @author chenfan
 * 
 */
public class E3GMessageCodec extends ByteToMessageCodec<Message> {
	private static Logger logger = LoggerFactory.getLogger(E3GMessageCodec.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// 头部最小长度为6字节
		if (in.readableBytes() < 6) {
			return;
		}
		in.markReaderIndex();
		byte type = in.readByte();
		byte optionalFlag = in.readByte();
		byte[] id = null;
		// 标志位1，有唯一标识字段
		if ((optionalFlag & 0x01) == 0x01) {
			// 头部长度不足，返回
			if (in.readableBytes() < 10) {
				in.resetReaderIndex();
				return;
			}
			id = new byte[6];
			in.readBytes(id);
		}

		Message msg = new Message();
		msg.setType(type);
		if (id != null) {
			String terminalId = Hex.encodeHexString(id);
			msg.setTerminalId(terminalId);
		}
		int length = in.readInt();
		if (length == 0) {
			out.add(msg);
			return;
		}
		if (length < 0 || length > 102400) {
			logger.error("Error! packet length({}) is invalid, close connection: {}", length, ctx.channel());
			ctx.close();
			return;
		}
		if (in.readableBytes() < length) {
			in.resetReaderIndex();
			return;
		}
		byte[] data = new byte[length];
		in.readBytes(data);
		msg.setData(data);
		out.add(msg);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {
		if (message == null) {
			return;
		}
		byte type = message.getType();
		if (type == Message.TYPE_PLATFORM_SEND) {
			type = (byte)0x81;
		}
		out.writeByte(type);
		String terminalId = message.getTerminalId();
		byte[] id = null;
		if (terminalId != null) {
			try {
				id = Hex.decodeHex(terminalId.toCharArray());
			} catch (DecoderException e) {
				id = null;
			}
		}
		if (id != null && id.length == 6) {
			out.writeByte(0x01);
			out.writeBytes(id);
		} else {
			out.writeByte(0x00);
		}
		byte[] data = message.getData();
		if (data == null) {
			out.writeInt(0);
		} else {
			out.writeInt(data.length);
			out.writeBytes(data);
		}
	}

}

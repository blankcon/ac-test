package com.wsg.protocl_7001;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class MessageHandler extends ChannelInboundHandlerAdapter {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MessageCodecFactory messageCodecFactory;


	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		byte[] keys = new byte[16];
		for (int i = 0; i < 16; i++) {
			keys[i] = 0x1;
		}
		Message message = (Message)msg;
		if (message.isType(Message.TYPE_CONNECTION_ESTABLISH)) {
			String terminalId = message.getTerminalId();
			byte arithmetic = 0x01;
			KeyGenerator keyGenerator = new KeyGenerator();
			byte[] key = keyGenerator.generateRandomKey(16);

			ctx.attr(ConnectionAttributes.TERMINAL_ID).set(terminalId);
			ctx.attr(ConnectionAttributes.ENCRYPT_ARITHMETIC).set((int) arithmetic);//rc4加密
			ctx.attr(ConnectionAttributes.ENCRYPT_KEY).set(key);

			message.setArithmetic(arithmetic);
			message.setKey(key);
			StringBuilder builder = new StringBuilder();
			for (byte k:key) {
				builder.append(k);
			}
			System.err.println("AC的key："+builder.toString());
			ctx.writeAndFlush(message);
			return;
		}
		String terminalId = ctx.attr(ConnectionAttributes.TERMINAL_ID).get();
		if (terminalId == null) {
			logger.error("Terminal Id not found, close connection: {}", ctx.channel());
			ctx.close();
			return;
		}
		message.setTerminalId(terminalId);

		if (message.getData() == null || message.getData().length == 0) {
			//探测链路报文，直接回复
			ctx.writeAndFlush(message);
			return;
		}

		if (message.isEncrypt()) {
			//已启用加密
			Integer arithmetic = ctx.attr(ConnectionAttributes.ENCRYPT_ARITHMETIC).get();
			if (arithmetic == null) {
				logger.error("Could not determine the encrypt arithmetic, close connection: {}", ctx.channel());
				ctx.close();
				return;
			}
			MessageCodec codec = messageCodecFactory.getMessageCodec(arithmetic);
			byte[] key = ctx.attr(ConnectionAttributes.ENCRYPT_KEY).get();
			if (key == null) {
				logger.error("Could not determine the decrypt key, close connection: {}", ctx.channel());
				ctx.close();
				return;
			}
			try {
				message.setData(codec.decrypt(key, message.getData()));
			} catch (Exception ex) {
				logger.error("Decrypt Falied, close connection: {}", ctx.channel(), ex);
				ctx.close();
				return;
			}
		}
		String msgS = new String(message.getData(),"utf-8");
		System.err.println(msgS);
		ctx.writeAndFlush(message);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx + "id:" + ctx.channel().id());
		System.out.println("7001的设备上线了");

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx);
		System.out.println("7001的设备离线了");
	}

}
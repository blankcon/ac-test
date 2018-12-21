package com.wsg.protocl_7001;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
public class MessageCodecFactory {
	private static Map<Integer, MessageCodec> messageCodecMap = new HashMap<Integer, MessageCodec>();
	private MessageCodec defaultMessageCodec = new DefaultMessageCodec();

	static {
		messageCodecMap.put(0x01, new RC4MessageCodec());
	}

	public MessageCodec getMessageCodec(int type) {
		MessageCodec codec = messageCodecMap.get(type);
		if (codec != null) {
			return codec;
		}
		return defaultMessageCodec;
	}
}

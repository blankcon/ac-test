package com.wsg.protocl_7001;

import io.netty.util.AttributeKey;

public interface ConnectionAttributes {
	public static final AttributeKey<String> TERMINAL_ID = AttributeKey.valueOf("terminal.id");
	public static final AttributeKey<Integer> ENCRYPT_ARITHMETIC = AttributeKey.valueOf("encrypt.arithmetic");
	public static final AttributeKey<byte[]> ENCRYPT_KEY = AttributeKey.valueOf("encrypt.key");
	public static final AttributeKey<Long> RECV_TIMESTAMP = AttributeKey.valueOf("recv.timestamp");
	public static final AttributeKey<Boolean> HTTP_KEEPALIVE = AttributeKey.valueOf("http.keepAlive");
}

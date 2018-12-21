package com.wsg.protocl_7001;

public interface MessageCodec {
	byte[] encrypt(byte[] key, byte[] content) throws Exception;

	byte[] decrypt(byte[] key, byte[] content) throws Exception;
}

package com.wsg.protocl_7001;

public class DefaultMessageCodec implements MessageCodec {

	@Override
	public byte[] encrypt(byte[] key, byte[] content) {
		return content;
	}

	@Override
	public byte[] decrypt(byte[] key, byte[] content) {
		return content;
	}

}

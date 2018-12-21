package com.wsg.protocl_7001;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class RC4MessageCodec implements MessageCodec {
	private final String ENCRYPT_ALGORITHM = "RC4";

	@Override
	public byte[] encrypt(byte[] key, byte[] content) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(key, ENCRYPT_ALGORITHM);
		Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return cipher.doFinal(content);
	}

	@Override
	public byte[] decrypt(byte[] key, byte[] content) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(key, ENCRYPT_ALGORITHM);
		Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(content);
	}
}

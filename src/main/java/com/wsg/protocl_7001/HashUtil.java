package com.wsg.protocl_7001;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
	public static byte[] md5(byte[] src) {
		return hash(src, "MD5");
	}

	private static byte[] hash(byte[] src, String algorithm) {
		MessageDigest messageDigest;

		try {
			messageDigest = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("No such algorithm [" + algorithm + "]");
		}

		return messageDigest.digest(src);
	}
}

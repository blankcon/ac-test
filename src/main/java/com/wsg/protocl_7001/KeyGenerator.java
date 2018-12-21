package com.wsg.protocl_7001;

import java.util.Random;

public class KeyGenerator {
	public byte[] generateRandomKey(int length) {
		byte[] result = new byte[length];
		new Random().nextBytes(result);
		return result;
	}
}

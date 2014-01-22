package io.trigger.forge.android.modules.tcp.util;

import java.io.UnsupportedEncodingException;

public class Util {
	
	public static String byteArrayToString(byte[] byteArray, int length, String charset)
			throws UnsupportedEncodingException {
		byte[] receivedCorrect = new byte[length];

		for (int i = 0; i < length; i++) {
			receivedCorrect[i] = byteArray[i];
		}

		return new String(receivedCorrect, charset);
	}
	
}

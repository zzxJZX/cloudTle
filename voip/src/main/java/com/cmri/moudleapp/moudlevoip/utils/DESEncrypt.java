package com.cmri.moudleapp.moudlevoip.utils;

import java.security.Key;

import javax.crypto.Cipher;

public class DESEncrypt {
	private static Cipher encryptCipher;
	private static Cipher decryptCipher;

	private static byte[] createKeyBytes() {
		byte[] keys = new byte[16];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = (byte) (8 + 2 * i);
		}
		return keys;
	}

	static {
		try {
			byte[] keys = createKeyBytes();

			// Security.addProvider(new com.sun.crypto.provider.SunJCE());
			Key key = getKey(keys);

			encryptCipher = Cipher.getInstance("DES");
			encryptCipher.init(Cipher.ENCRYPT_MODE, key);

			decryptCipher = Cipher.getInstance("DES");
			decryptCipher.init(Cipher.DECRYPT_MODE, key);
		} catch (Exception e) {
		}
	}

	public static String byteArr2HexStr(byte[] arrB) throws Exception {
		int iLen = arrB.length;
		StringBuffer sb = new StringBuffer(iLen * 2);
		for (int i = 0; i < iLen; i++) {
			int intTmp = arrB[i];
			while (intTmp < 0) {
				intTmp = intTmp + 256;
			}
			// add 0 at the begin of number if it is smaller than 0F
			if (intTmp < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString();
	}

	public static byte[] hexStr2ByteArr(String strIn) throws Exception {
		byte[] arrB = strIn.getBytes("UTF-8");
		int iLen = arrB.length;

		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2,"UTF-8");
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}

	private static byte[] encrypt(byte[] arrB) throws Exception {
		return encryptCipher.doFinal(arrB);
	}

	public static String encrypt(String strIn) throws Exception {
			return byteArr2HexStr(encrypt(strIn.getBytes("UTF-8")));
	}

	public static byte[] decrypt(byte[] arrB) throws Exception {
		return decryptCipher.doFinal(arrB);
	}

	public static String decrypt(String strIn) throws Exception {
		return new String(decrypt(hexStr2ByteArr(strIn)),"UTF-8");
	}

	private static Key getKey(byte[] arrBTmp) throws Exception {
		// create an empty btye array
		byte[] arrB = new byte[8];

		for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
			arrB[i] = arrBTmp[i];
		}

		// create the key
		Key key = new javax.crypto.spec.SecretKeySpec(arrB, "DES");

		return key;
	}
}
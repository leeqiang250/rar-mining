package com.mining.mining.file;

import java.io.*;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

public class File {

	public static String getMD5(String path) {
		String value = null;
		FileInputStream inputStream = null;

		java.io.File file = new java.io.File(path);
		if (!file.exists()) {
			return null;
		}

		try {
			inputStream = new FileInputStream(file);

			MappedByteBuffer buffer = inputStream.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(buffer);

			BigInteger bi = new BigInteger(1, md5.digest());
			value = bi.toString(16);
			while (value.length() < 32) {
				value = "0" + value;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//
		} finally {
			if (null != inputStream) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return value;
	}

	public static boolean WriteFile(byte[] content, String path) {
		boolean result = false;

		if (null == content || (new java.io.File(path).exists() && !new java.io.File(path).delete())) {
			//
			return result;
		}

		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(path);
			outputStream.write(content);
		} catch (Exception e) {
			e.printStackTrace();
			//
		} finally {
			if (null != outputStream) {
				try {
					outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

}
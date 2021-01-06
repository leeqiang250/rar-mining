package com.mining.mining.file;

import java.io.*;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;

public class RARFile {

	public static File RARFile = null;

	public static String ResourcePath = null;
	public static String RARFileName = null;

	public static String TestPath = null;
	public static String KeyPath = null;
	public static String RARPath = null;

	public static void Init(String resourcePath, String rarFileName) {
		ResourcePath = resourcePath;
		RARFileName = rarFileName;

		TestPath = ResourcePath + "/test";
		KeyPath = ResourcePath + "/key.key";
		RARPath = ResourcePath + "/" + RARFileName;
	}

	public static String RARFileMD5() {
		String value = null;
		FileInputStream inputStream = null;

		File file = new File(RARPath);
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

	public static boolean DownloadRARFile(byte[] content) {
		boolean result = false;

		if (null == content || (new File(RARPath).exists() && !new File(RARPath).delete())) {
			//
			return result;
		}

		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(RARPath);
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

	public static boolean TestFile() {
		boolean result = false;

		if (new File(TestPath).exists()) {
			if (!new File(TestPath).delete()) {
				//
				return result;
			}
		}

		FileWriter writer = null;
		try {
			writer = new FileWriter(new File(TestPath), true);
			writer.write("\n");

			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			//
		} finally {
			if (null != writer) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}
}
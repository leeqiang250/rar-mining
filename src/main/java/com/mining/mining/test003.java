package com.mining.mining;

import com.mining.mining.rar.Archive;
import com.mining.mining.rar.rarfile.FileHeader;

import java.io.File;
import java.util.zip.ZipOutputStream;

public class test003 implements test {
	@Override
	public void start() {
		String filename = "/rar/test001.rar";
		File f = new File(filename);
		Archive a = null;
		boolean result = false;

		boolean test = true;// test mode

		if (test) {
			String[] pwds = {"114", "1234", "sdfsdfsdfsdf"};

			for (int i = 0; i < pwds.length; i++) {
				long start = System.currentTimeMillis();
				try {
					a = new Archive(f, pwds[i], true);  //test mode
					if (a != null && a.isPass()) {
						result = true;
					} else {
						result = false;
					}

					if (!a.getMainHeader().isEncrypted()) {
						// result = true;
						FileHeader fh = a.nextFileHeader();
						try {
							// while(fh!=null){
							a.extractFile(fh, null);
							fh = a.nextFileHeader();
							// }
							result = true;
						} catch (Exception e) {
							// e.printStackTrace();
							result = false;
						}
					}
				} catch (Exception e) {
					result = false;
				}

				System.out.println("PWD[" + i + "]:" + pwds[i] + "=" + result
						+ "/" + (System.currentTimeMillis() - start) + "ms");
			}
		}
	}
}
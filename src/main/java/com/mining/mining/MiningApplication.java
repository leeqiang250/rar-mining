package com.mining.mining;

import com.mining.mining.rar.RarDecompressionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
public class MiningApplication {

	//private static Logger log = LoggerFactory.getLogger("MiningApplication");

	public static void main(String[] args) {

//		{
//			FileWriter writer = null;
//			try {
//				new File("/resource/password/1.key.end").delete();
//				writer = new FileWriter(new File("/resource/password/1.key.end"), true);
//				int i = 0;
//				while (i < 200) {
//					i++;
//					writer.write("\n");
//					writer.write(i + "");
////					writer.write("64876655");
//				}
//				writer.flush();
//				writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}

		String destPath = "/resource/";
		File rarFile = new File(destPath + "resource.rar");
		String passwordPath = destPath + "password/";
		String keyFile = destPath + "key.txt";

		checkRarFile(rarFile);
		testFile(destPath + "test.txt", passwordPath);

		int n = Runtime.getRuntime().availableProcessors() * 10;
		LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(n * 100);
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(n);
		while (n > 0) {
			fixedThreadPool.execute(() -> {
				System.out.println("Calc Thread:" + Thread.currentThread().getName());

				ByteArrayOutputStream stream = new ByteArrayOutputStream();

				while (true) {
					RarDecompressionUtil.unrar_v2(rarFile, destPath, keyFile, queue, stream);
				}
			});
			n--;
		}
		start(queue, passwordPath);

		SpringApplication.run(MiningApplication.class, args);
	}

	private static void checkRarFile(File rarFile) {
		if (!rarFile.exists()) {
			System.out.println("Calc Thread:");
			System.exit(0);
		}
	}

	private static void start(LinkedBlockingQueue<String> queue, String passwordPath) {
		new Thread(() -> {
			while (true) {
				String file = null;
				List<String> passwords = null;
				for (File e : new File(passwordPath).listFiles((dir, name) -> name.endsWith(".key"))) {
					if (e.getName().endsWith(".key")) {
						passwords = readPassWords(e);
						if (null != passwords) {
							file = e.getPath();
							break;
						}
					}
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				if (null != passwords) {
					for (int i = 0; i < passwords.size(); i++) {
						String password = passwords.get(i);
						if (!"".equals(password)) {
							try {
								queue.put(password);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}

					try {
						while (0 < queue.size()) {
							Thread.sleep(1000L);
						}
						new File(file).renameTo(new File(file + ".end"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}).start();
	}

	private static void testFile(String testFile, String passwordPath) {
		File test = new File(testFile);
		File password = new File(passwordPath);
		if (test.exists()) {
			if (!test.delete()) {
				System.out.println("Calc Thread:");
				System.exit(0);
			}
		}

		try {
			if (!password.exists()) {
				if (!password.mkdir()) {
					System.out.println("Calc Thread:");
					System.exit(0);
				}
			}

			FileWriter writer = new FileWriter(test, true);
			writer.write("\n");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Calc Thread:");
			System.exit(0);
		}
	}

	private static List<String> readPassWords(File file) {
		boolean error = false;
		List list = new ArrayList<>();
		try {
			BufferedReader bw = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = bw.readLine()) != null) {
				list.add(line);
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			error = true;
		}
		return error ? null : list;
	}
}
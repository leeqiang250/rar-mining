package com.mining.mining;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class test004 {

	public void start(String group, List<String> pwd) throws Exception {
		String name = "./" + group + ".sh";

		FileWriter writer = new FileWriter(name, true);
		writer.write("#!/bin/sh");

		int len = pwd.size();
		for (int i = 0; i < len; i++) {
			if (i % 10 == 0) {
				writer.write("\n");
				writer.write("\n");
				writer.write("echo {\"name\":\"" + name + "\",\"index\":\"" + i + "\"}");
				writer.write("\n");
				writer.write("\n");
			}

			writer.write("\n");
			writer.write("./u t -p" + i + " ./f");
		}

		writer.flush();
		writer.close();

		Runtime runtime = Runtime.getRuntime();
		runtime.exec("chmod +x " + "./" + name);
		Process process = runtime.exec("./" + name);
		InputStream inputStream = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		String line;
		String ok = "OK";
		while (null != (line = reader.readLine())) {
			System.out.println(line);
			if (0 <= line.indexOf(group) || 0 <= line.indexOf(ok)) {
				System.out.println("line:" + line);
			}
		}
//System.pro
///r/c a -p1 /f/f.rar
///r/u t -p1 /f/f.rar
///r/unrar t -p18 /f/f.rar
	}

}
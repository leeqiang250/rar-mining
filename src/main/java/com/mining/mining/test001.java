package com.mining.mining;

import java.io.*;

public class test001 implements test {

	@Override
	public void start() {
		try {

			new File("/rar/start.sh").delete();

			for (int  i = 0;i<50;i++) {
				StringBuilder builder = new StringBuilder();
				builder.append("echo group" + i + " begin `date +\"%Y-%m-%d %H:%M:%S\"` >> info.log");
				builder.append("\n");

				String[] cmds = new String[5000];
				for (int j = 0; j < cmds.length; j++) {
					if (j % 100 == 0 && j != 0) {
						builder.append("\n");
						builder.append("\n");
						builder.append("echo group" + i + " " + j + " doing `date +\"%Y-%m-%d %H:%M:%S\"` >> info.log");
						builder.append("\n");
					}
					builder.append("\n");
					builder.append("/rarcode/rar t -p" + j + " /rar/test001.rar");
				}

				builder.append("\n");
				builder.append("\n");
				builder.append("echo group" + i + " end `date +\"%Y-%m-%d %H:%M:%S\"` >> info.log");

				{
					new File("/rar/group" + i + ".sh").delete();

					FileWriter writer = new FileWriter(new File("/rar/group" + i + ".sh"), true);
					writer.write(builder.toString());
					writer.flush();
					writer.close();
				}
				{
					FileWriter writer = new FileWriter(new File("/rar/start.sh"), true);
					writer.write("\n");
					writer.write("sh /rar/group" + i + ".sh &");
					writer.flush();
					writer.close();
				}
			}


			long ts = System.currentTimeMillis();
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(new String[]{"/Users/liqiang/Desktop/tmp/test001.sh"});
			String line = null, result = "";

			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = input.readLine()) != null) {
				//System.out.println(line);
			}
			input.close();

			System.out.println(System.currentTimeMillis() - ts);
			//System.out.println(new String(process.getErrorStream().readAllBytes()));
			//System.out.println(new String(process.getInputStream().readAllBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
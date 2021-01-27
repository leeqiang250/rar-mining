package com.mining.mining.mining;

import com.mining.mining.dto.Dto;
import com.mining.mining.dto.TaskDto;
import com.mining.mining.http.Http;
import com.mining.mining.http.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
public class TestController {

	private String ok = "All OK";

	@GetMapping("/v1/test")
	public String Test() {
		new Thread(() -> {
			BufferedReader reader = null;
			InputStream inputStream = null;
			Process process = null;
			String line = null;
			Dto<TaskDto> taskDto = null;
			FileWriter writer = null;
			String[] passwords = null;
			File file = null;
			int len = 0;

			try {
				taskDto = Http.DispatchGet(Constant.DispatchHost + Path.TASK_TEST, TaskDto.class);
				if (Dto.success(taskDto)) {
					if (null != taskDto.data) {
						String name = "test-" + taskDto.data.group + ".sh";
						file = new File(name);

						writer = new FileWriter(name, true);
						writer.write("#!/bin/sh");

						passwords = taskDto.data.text.split(",");
						len = passwords.length;
						for (int i = 0; i < len; i++) {
							writer.write("\n");
							writer.write("./u t -p" + i + " f");
						}

						writer.flush();
						writer.close();

						log.info("test start authorize:{}", name);
						Runtime.getRuntime().exec("chmod +x " + name);
						log.info("test end authorize:{}", name);

						process = new ProcessBuilder(file.getAbsolutePath()).redirectErrorStream(true).start();
						inputStream = process.getInputStream();
						reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

						log.info("test start mining:{}", name);

						while (null != (line = reader.readLine())) {
							if (0 <= line.indexOf(ok)) {
								log.info("test discover:{}", name);
							}
						}
						log.info("test end mining:{}", name);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Exception {}", e);
			} finally {
				if (null != reader) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (null != inputStream) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (null != process) {
					process.destroy();
				}
				if (null != writer) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (null != file) {
					try {
						file.delete();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		return "received request:/v1/test";
	}
}
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
		String result = null;
		BufferedReader reader = null;
		InputStream inputStream = null;
		Process process = null;
		String line = null;
		Dto<TaskDto> taskDto = null;
		StringBuilder builder = new StringBuilder();
		String[] passwords = null;
		int len = 0;

		try {
			taskDto = Http.DispatchGet(Constant.DispatchHost + Path.TASK_TEST, TaskDto.class);
			if (Dto.success(taskDto)) {
				if (null != taskDto.data) {
					builder.delete(0, builder.length());

					passwords = taskDto.data.text.split(",");
					len = passwords.length;
					for (int i = 0; i < len; i++) {
						builder.append("\n");
						builder.append("./u t -p" + passwords[i] + " f");
					}

					log.info("test starting mining:{}", taskDto.data.group);
					result = "test starting mining:" + taskDto.data.group;

					process = new ProcessBuilder("/bin/sh", "-c", builder.toString()).redirectErrorStream(true).start();
					inputStream = process.getInputStream();
					reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

					log.info("test started mining:{}", taskDto.data.group);
					result = "test started mining:" + taskDto.data.group;

					boolean isOk = false;
					while (null != (line = reader.readLine())) {
						if (0 <= line.indexOf(ok)) {
							isOk = true;
							log.info("test discover:{}", taskDto.data.group);
							result = "test discover:" + taskDto.data.group;
						}
					}

					if (!isOk) {
						log.info("test end mining:{}", taskDto.data.group);
						result = "test end mining:" + taskDto.data.group;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = e.getLocalizedMessage();
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
			builder.delete(0, builder.length());
		}

		return result;
	}
}
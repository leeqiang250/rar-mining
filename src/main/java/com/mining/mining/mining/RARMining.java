package com.mining.mining.mining;

import com.mining.mining.dto.Dto;
import com.mining.mining.dto.TaskDto;
import com.mining.mining.http.Path;
import com.mining.mining.http.Http;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RARMining {

	private String group = null;
	private String dispatchHost;
	private String ok = "All OK";

	public RARMining(String dispatchHost, String group) {
		this.dispatchHost = dispatchHost;
		this.group = group;
	}

	public void start(int interval) {
		BufferedReader reader = null;
		InputStream inputStream = null;
		Process process = null;
		String line = null;
		Dto<TaskDto> taskDto = null;
		Dto<Boolean> confirmDto = null;
		Dto<Boolean> completeDto = null;
		FileWriter writer = null;
		String[] passwords = null;
		File file = null;
		int len = 0;
		while (true) {
			try {
				taskDto = Http.DispatchGet(dispatchHost + Path.TASK_GET, TaskDto.class);
				if (Dto.success(taskDto) && null != taskDto.data) {
					confirmDto = Http.DispatchGet(dispatchHost + String.format(Path.TASK_CONFIRM, taskDto.data.group), Boolean.class);
					if (Dto.success(confirmDto) && confirmDto.data) {
						String name = taskDto.data.group + ".sh";
						file = new File(name);

						writer = new FileWriter(name, true);
						writer.write("#!/bin/sh");

						passwords = taskDto.data.text.split(",");
						len = passwords.length;
						for (int i = 0; i < len; i++) {
							if (i % interval == 0) {
								writer.write("\n");
								writer.write("echo \"group=" + taskDto.data.group + "&index=" + i + "\"");
							}

							writer.write("\n");
							writer.write("u t -p" + i + " f");
						}

						writer.flush();
						writer.close();

						Runtime.getRuntime().exec("chmod +x " + name);
						process = new ProcessBuilder(file.getAbsolutePath()).redirectErrorStream(true).start();
						inputStream = process.getInputStream();
						reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
						while (null != (line = reader.readLine())) {
							if (0 <= line.indexOf(taskDto.data.group)) {
								report(line);

								log.info("line:{}", line);
							}

							if (0 <= line.indexOf(ok)) {
								while (!discover(taskDto.data.group)) {
									sleep();
								}

								log.info("line:{}", line);
								log.info("discover:{}", line);
							}
						}

						while (true) {
							try {
								completeDto = Http.DispatchGet(dispatchHost + String.format(Path.TASK_COMPLETE, taskDto.data.group), Boolean.class);
								if (Dto.success(completeDto) && completeDto.data) {
									break;
								} else {
									sleep();
								}
							} catch (Exception e) {
								e.printStackTrace();
								log.error("Exception {}", e);
							}
						}

						file.delete();
					} else {
						sleep();
					}
				} else {
					sleep();
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Exception {}", e);

				try {
					sleep();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					log.error("Exception {}", e1);
				}
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
			}
		}
	}


	private void sleep() throws InterruptedException {
		Thread.sleep(5000L);
	}

	private boolean report( String line) {
		try {
			Dto<Boolean> reportDto = Http.DispatchGet(
					dispatchHost + String.format(Path.MINING_RUN_REPORT, InetAddress.getLocalHost().getHostAddress(), group) + line,
					Boolean.class);
			if (Dto.success(reportDto) && reportDto.data) {
				return true;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			log.error("Exception {}", e);
		}

		return false;
	}

	private boolean discover(String group) {
		try {
			Dto<Boolean> reportDto = Http.DispatchGet(dispatchHost + String.format(Path.TASK_DISCOVER, group), Boolean.class);
			if (Dto.success(reportDto) && reportDto.data) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Exception {}", e);
		}

		return false;
	}
}
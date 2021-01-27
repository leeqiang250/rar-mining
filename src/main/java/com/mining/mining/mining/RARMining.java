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
	private String ok = "All OK";

	public RARMining(String group) {
		this.group = group;
	}

	public void start(int interval) {
		BufferedReader reader = null;
		InputStream inputStream = null;
		Process process = null;
		String line = null;
		Dto<TaskDto> taskDto = null;
		StringBuilder builder = new StringBuilder();
		Dto<Boolean> confirmDto = null;
		Dto<Boolean> completeDto = null;
		String[] passwords = null;
		int len = 0;
		long ts = 0L;

		while (true) {
			try {
				taskDto = Http.DispatchGet(Constant.DispatchHost + Path.TASK_GET, TaskDto.class);
				if (Dto.success(taskDto)) {
					if (null != taskDto.data) {
						confirmDto = Http.DispatchGet(Constant.DispatchHost + String.format(Path.TASK_CONFIRM, taskDto.data.group), Boolean.class);
						if (Dto.success(confirmDto) && confirmDto.data) {
							builder.delete(0, builder.length());

							passwords = taskDto.data.text.split(",");
							len = passwords.length;
							for (int i = 0; i < len; i++) {
								if (i % interval == 0) {
									builder.append("\n");
									builder.append("echo \"group=" + taskDto.data.group + "&index=" + i + "\"");
								}

								builder.append("\n");
								builder.append("./u t -p" + i + " f");
							}

							ts = System.currentTimeMillis();

							process = new ProcessBuilder("/bin/sh", "-c", builder.toString()).redirectErrorStream(true).start();
							inputStream = process.getInputStream();
							reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
							while (null != (line = reader.readLine())) {
								if (0 <= line.indexOf(taskDto.data.group)) {
									report(line);

									log.info("{}", line);
								}

								if (0 <= line.indexOf(ok)) {
									while (!discover(taskDto.data.group)) {
										sleep();
									}

									log.info("line:{}", line);
									log.info("discover:{}", taskDto.data.text);
								}
							}

							log.info("mining len {} cost ts {}", len, (System.currentTimeMillis() - ts) / 1000L);

							while (true) {
								try {
									completeDto = Http.DispatchGet(Constant.DispatchHost + String.format(Path.TASK_COMPLETE, taskDto.data.group), Boolean.class);
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
						} else {
							sleep();
						}
					} else {
						log.info("taskDto {}", taskDto);
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
				builder.delete(0, builder.length());
			}
		}
	}

	private void sleep() throws InterruptedException {
		Thread.sleep(5000L);
	}

	private boolean report(String line) {
		try {
			Dto<Boolean> reportDto = Http.DispatchGet(Constant.DispatchHost + String.format(Path.MINING_RUN_REPORT, InetAddress.getLocalHost().getHostAddress(), group) + line, Boolean.class);
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
			Dto<Boolean> reportDto = Http.DispatchGet(Constant.DispatchHost + String.format(Path.TASK_DISCOVER, group), Boolean.class);
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
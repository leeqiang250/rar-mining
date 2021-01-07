package com.mining.mining.mining;

import com.mining.mining.dto.Dto;
import com.mining.mining.dto.TaskDto;
import com.mining.mining.file.RARFile;
import com.mining.mining.http.DispatchPath;
import com.mining.mining.http.Http;
import com.mining.mining.rar.RarDecompressionUtil;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RARMining {

	private String group = null;
	private String dispatchHost;
	private long lastReportTs = 0L;
	private long reportInterval = 1000L * 60L;

	public RARMining(String dispatchHost, String group) {
		this.dispatchHost = dispatchHost;
		this.group = group;
	}

	public void start() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		while (true) {
			try {
				if (System.currentTimeMillis() - lastReportTs > reportInterval) {
					report("");
				}

				Dto<TaskDto> taskDto = Http.DispatchGet(dispatchHost + DispatchPath.TASK_GET, TaskDto.class);
				if (Dto.success(taskDto) && null != taskDto.data) {
					Dto<Boolean> confirmDto = Http.DispatchGet(
							dispatchHost + String.format(DispatchPath.TASK_CONFIRM, taskDto.data.group),
							Boolean.class);
					if (Dto.success(confirmDto) && confirmDto.data) {
						for (String password : taskDto.data.text.split(",")) {
							if (!"".equals(password)) {
								if (RarDecompressionUtil.unRAR_V2(RARFile.RARFile, RARFile.KeyPath, taskDto.data.group, password, stream)) {
									while (true) {
										try {
											Dto<Boolean> discoverDto = Http.DispatchGet(
													dispatchHost + String.format(
															DispatchPath.TASK_DISCOVER,
															taskDto.data.group,
															password),
													Boolean.class);
											if (Dto.success(discoverDto) && discoverDto.data) {
												break;
											} else {
												sleep();
											}
										} catch (Exception e) {
											e.printStackTrace();
											//
										}
									}
								}
								if (System.currentTimeMillis() - lastReportTs > reportInterval) {
									report(password);
								}
							}
						}

						while (true) {
							try {
								Dto<Boolean> completeDto = Http.DispatchGet(
										dispatchHost + String.format(DispatchPath.TASK_COMPLETE, taskDto.data.group),
										Boolean.class);
								if (Dto.success(completeDto) && completeDto.data) {
									break;
								} else {
									sleep();
								}
							} catch (Exception e) {
								e.printStackTrace();
								//
							}
						}
					} else {
						sleep();
					}
				} else {
					sleep();
				}
			} catch (Exception e) {
				e.printStackTrace();
				//
				sleep();
			}
		}
	}

	private void sleep() {
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void report(String password) {
		try {
			Dto<Boolean> reportDto = Http.DispatchGet(
					dispatchHost + String.format(DispatchPath.MINING_RUN_REPORT, InetAddress.getLocalHost().getHostAddress(), group, password),
					Boolean.class);
			if (Dto.success(reportDto) && reportDto.data) {
				lastReportTs = System.currentTimeMillis();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			//
		}
	}

}
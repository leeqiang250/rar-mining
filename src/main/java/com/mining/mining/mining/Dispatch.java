package com.mining.mining.mining;

import com.mining.mining.dto.Dto;
import com.mining.mining.dto.MiningInfoDto;
import com.mining.mining.http.Path;
import com.mining.mining.http.Http;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Dispatch {

	private String dispatchHost;

	public Dispatch(String dispatchHost) {
		this.dispatchHost = dispatchHost;
	}

	public void start() throws InterruptedException {
		while (true) {
			try {
				Dto<MiningInfoDto> dto = Http.DispatchGet(dispatchHost + Path.MINING_INFO, MiningInfoDto.class);
				if (Dto.success(dto)) {
					DownloadFile(dto.data.rarFilePath, dto.data.rarFilePathMD5);
					DownloadFile(dto.data.programPath, dto.data.programPathMD5);

					Runtime.getRuntime().exec("chmod +x " + dto.data.rarFilePath);
					Runtime.getRuntime().exec("chmod +x " + dto.data.programPath);

					int count = Runtime.getRuntime().availableProcessors() * (dto.data.coreThreadCount() > 0 ? dto.data.coreThreadCount() : 10);
					ExecutorService fixedThreadPool = Executors.newFixedThreadPool(count);
					while (count > 0) {
						fixedThreadPool.execute(() -> {
							new RARMining(dispatchHost, UUID.randomUUID().toString()).start(dto.data.reportInterval);
						});
						count--;
					}
					break;
				} else {
					Thread.sleep(5000L);
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Exception {}", e);
			} finally {
				Thread.sleep(5000L);
			}
		}
	}

	private void DownloadFile(String path, String md5) throws InterruptedException {
		new java.io.File(path).delete();

		String fileMD5 = com.mining.mining.file.File.getMD5(path);
		while (StringUtils.isEmpty(fileMD5) || !fileMD5.equals(md5)) {
			com.mining.mining.file.File.WriteFile(Http.Get(dispatchHost + String.format(Path.TASK_DOWNLOAD_FILE, md5)), path);
			fileMD5 = com.mining.mining.file.File.getMD5(path);
			Thread.sleep(1000L);
		}
	}

}
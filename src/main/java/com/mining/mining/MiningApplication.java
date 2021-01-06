package com.mining.mining;

import com.mining.mining.dto.Dto;
import com.mining.mining.dto.MiningInfoDto;
import com.mining.mining.file.RARFile;
import com.mining.mining.http.DispatchPath;
import com.mining.mining.http.Http;
import com.mining.mining.mining.RARMining;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class MiningApplication implements ApplicationRunner {

	@Value("${dispatch-host}")
	private String dispatchHost;

	@Value("${resource-path}")
	private String resourcePath;

	@Value("${rar-file}")
	private String rarFileName;


	public static void main(String[] args) {
		SpringApplication.run(MiningApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		RARFile.Init(resourcePath, rarFileName);

		if (!RARFile.TestFile()) {
			//
			System.exit(0);
		}

		while (true) {
			try {
				Dto<MiningInfoDto> dto = Http.DispatchGet(dispatchHost + DispatchPath.MINING_INFO, MiningInfoDto.class);
				if (Dto.success(dto)) {
					String rarFileMD5 = RARFile.RARFileMD5();
					while (StringUtils.isEmpty(rarFileMD5) || !rarFileMD5.equals(dto.data().rarMD5())) {
						RARFile.DownloadRARFile(Http.Get(dispatchHost + DispatchPath.TASK_DOWNLOAD_RAR_FILE));
						rarFileMD5 = RARFile.RARFileMD5();
						Thread.sleep(5000L);
					}

					RARFile.RARFile = new File(RARFile.RARPath);

					int count = Runtime.getRuntime().availableProcessors() * (dto.data.coreThreadCount() > 0 ? dto.data.coreThreadCount() : 10);
					ExecutorService fixedThreadPool = Executors.newFixedThreadPool(count);
					while (count > 0) {
						fixedThreadPool.execute(() -> {
							String group = UUID.randomUUID().toString();
							new RARMining(dispatchHost, group).start();
						});
						count--;
					}
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Thread.sleep(5000L);
			}
		}
	}
}
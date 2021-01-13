package com.mining.mining;

import com.mining.mining.dto.Dto;
import com.mining.mining.dto.MiningInfoDto;
import com.mining.mining.file.RARFile;
import com.mining.mining.http.DispatchPath;
import com.mining.mining.http.Http;
import com.mining.mining.mining.RARMining;
import com.mining.mining.rar.RarDecompressionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
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


	private static final AtomicLong COUNT = new AtomicLong(0);

	private boolean test(String id, int index) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		//File file = new File("/resource/123456.rar");
		File file = new File("/resource/data.rar");
		//File file = new File("/resource/ziliao.rar");

		long c = Long.MAX_VALUE;

		long ts = System.currentTimeMillis();
		long last = c;

		long interval = 0L;
		while (c > 0) {
			interval = System.currentTimeMillis() - ts;
			if (index == 0 && interval > 1000L) {
				//System.out.println("id:" + id + " interval:" + interval + " count:" + (last - c));
				System.out.println("id:" + id + " interval:" + interval + " count:" + (COUNT.get() - last) + " total:" + COUNT.get());

				ts = System.currentTimeMillis();

				//last = c;
				last = COUNT.get();
			}
			RarDecompressionUtil.unRAR_V3(file,
					"/resource/123456.key",
					id,
					UUID.randomUUID().toString(),
					stream);
			c--;
			COUNT.getAndIncrement();
		}
		return true;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
//		{
//			int count = Runtime.getRuntime().availableProcessors() * 4;
//			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(count);
//			while (count > 0) {
//				fixedThreadPool.execute(() -> {
//					test(UUID.randomUUID().toString(), 1);
//				});
//				count--;
//			}
//		}
		if (test(UUID.randomUUID().toString(), 0)) {
			return;
		}

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
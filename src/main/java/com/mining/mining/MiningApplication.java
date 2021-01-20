package com.mining.mining;

import com.mining.mining.mining.Dispatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class MiningApplication implements ApplicationRunner {

	@Value("${dispatch-host}")
	private String dispatchHost;

	public static void main(String[] args) {
		SpringApplication.run(MiningApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		new Dispatch(dispatchHost).start();
	}

}
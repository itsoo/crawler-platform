package com.sncfc.crawler.worker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sncfc.crawler.worker.start.WorkerStart;

@SpringBootApplication
public class SpringBootWorkerApplication implements CommandLineRunner {
	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(
				SpringBootWorkerApplication.class);
		app.setBannerMode(Mode.OFF);
		app.run(args);
	}

	@Autowired
	WorkerStart workerStart;

	public void run(String... arg0) throws Exception {
		workerStart.start();

		Thread.currentThread().join();
	}
}
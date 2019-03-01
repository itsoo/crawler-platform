package com.sncfc.crawler.manage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sncfc.crawler.manage.start.ManageStart;

@SpringBootApplication
public class SpringBootManageApplication implements CommandLineRunner {
	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(
				SpringBootManageApplication.class);
		app.setBannerMode(Mode.OFF);
		app.run(args);
	}

	@Autowired
	ManageStart manageStart;

	@Override
	public void run(String... arg0) throws Exception {
		manageStart.start();

		Thread.currentThread().join();
	}
}
package com.sncfc.crawler.manage.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.sncfc.crawler.manage.curator.CuratorManage;
import com.sncfc.crawler.manage.task.dao.ITaskDetailDao;
import com.sncfc.crawler.manage.task.dao.impl.OracleTaskDetailDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
@PropertySource({ "classpath:config.properties" })
public class AppConfig {
	@Value("${config.zookeeper.hostPort}")
	public String hostPort;

	@Value("${config.timerInterval}")
	public long timerInterval;

	@Bean("timerInterval")
	public long getTimerInterval() {
		// 配置文件内的时间单位是“分钟”，这里要转换成“毫秒”
		return timerInterval * 60 * 1000;
	}

	@ConfigurationProperties(prefix = "config.datasource.sql")
	@Bean
	public DataSource masterDataSource() {
		System.out.println("---------- crawler datasource init ----------");
		return DataSourceBuilder.create().type(DruidDataSource.class).build();
	}

	@Bean
	@Autowired
	public ITaskDetailDao taskDetailDao(DataSource masterDataSource) {
//		return new MysqlTaskDetailDao(masterDataSource);
		return new OracleTaskDetailDao(masterDataSource);
	}

	@Bean
	public CuratorManage curatorManage() {
		return new CuratorManage(hostPort);
	}
}

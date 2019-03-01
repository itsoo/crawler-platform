package com.sncfc.crawler.store.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.sncfc.crawler.store.dao.IStoreDao;
import com.sncfc.crawler.store.dao.impl.OracleStoreDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AppConfig {
	@ConfigurationProperties(prefix = "config.datasource.sql")
	@Bean
	public DataSource storeDataSource() {
		System.out.println("---------- crawler datasource init ----------");
		return DataSourceBuilder.create().type(DruidDataSource.class).build();
	}

	@Bean
	@Autowired
	public IStoreDao storeDao(DataSource storeDataSource) {
//		return new MysqlStoreDao(storeDataSource);
		return new OracleStoreDao(storeDataSource);
	}
}

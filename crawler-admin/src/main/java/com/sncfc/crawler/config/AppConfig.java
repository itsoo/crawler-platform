package com.sncfc.crawler.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.sncfc.crawler.curator.CuratorManage;
import com.sncfc.crawler.tactics.dao.ITacticsDao;
import com.sncfc.crawler.tactics.dao.impl.OracleTacticsDao;
import com.sncfc.crawler.task.dao.ITaskDao;
import com.sncfc.crawler.task.dao.ITaskDetailDao;
import com.sncfc.crawler.task.dao.ITaskTagDao;
import com.sncfc.crawler.task.dao.impl.OracleTaskDao;
import com.sncfc.crawler.task.dao.impl.OracleTaskDetailDao;
import com.sncfc.crawler.task.dao.impl.OracleTaskTagDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
@PropertySource({"classpath:config.properties"})
public class AppConfig {
    @Value("${config.zookeeper.hostPort}")
    public String hostPort;

    @ConfigurationProperties(prefix = "config.datasource.sql")
    @Bean
    public DataSource masterDataSource() {
        System.out.println("---------- crawler datasource init ----------");
        return DataSourceBuilder.create().type(DruidDataSource.class).build();
    }

    @Bean
    @Autowired
    public ITacticsDao tacticsDao(DataSource masterDataSource) {
//		return new MysqlTacticsDao(masterDataSource);
        return new OracleTacticsDao(masterDataSource);
    }

    @Bean
    @Autowired
    public ITaskTagDao taskTagDao(DataSource masterDataSource) {
//        return new MysqlTaskTagDao(masterDataSource);
        return new OracleTaskTagDao(masterDataSource);
    }

    @Bean
    @Autowired
    public ITaskDao taskDao(DataSource masterDataSource) {
//        return new MysqlTaskDao(masterDataSource);
        return new OracleTaskDao(masterDataSource);
    }

    @Bean
    @Autowired
    public ITaskDetailDao taskDetailDao(DataSource masterDataSource) {
//        return new MysqlTaskDetailDao(masterDataSource);
        return new OracleTaskDetailDao(masterDataSource);
    }

    @Bean
    public CuratorManage curatorManage() {
        return new CuratorManage(hostPort);
    }
}

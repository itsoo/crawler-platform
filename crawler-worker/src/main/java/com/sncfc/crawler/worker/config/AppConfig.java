package com.sncfc.crawler.worker.config;

import com.sncfc.crawler.worker.loader.MyClassLoader;
import com.sncfc.crawler.worker.mq.IMQClient;
import com.sncfc.crawler.worker.mq.impl.RabbitMQClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${config.zookeeper.hostPort}")
    private String hostPort;

    @Value("${config.threadPool.corePoolSize}")
    private int corePoolSize;

    @Value("${config.threadPool.maximumPoolSize}")
    private int maximumPoolSize;

    @Value("${config.classLoader.path}")
    private String classLoaderPath;

    @Bean("hostPort")
    public String getHostPort() {
        return hostPort;
    }

    @Bean("corePoolSize")
    public int getCorePoolSize() {
        return corePoolSize;
    }

    @Bean("maximumPoolSize")
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    @Bean("mqClient")
    public IMQClient mqClient() {
        return new RabbitMQClient();
    }

    @Bean("myClassLoader")
    public MyClassLoader myClassLoader() {
        return new MyClassLoader(classLoaderPath);
    }
}

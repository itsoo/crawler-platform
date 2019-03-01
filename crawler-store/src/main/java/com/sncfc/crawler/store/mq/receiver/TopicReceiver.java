package com.sncfc.crawler.store.mq.receiver;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sncfc.crawler.store.bean.ResultInfo;
import com.sncfc.crawler.store.service.StoreService;
import com.sncfc.crawler.store.util.GsonUtils;

@Component
@RabbitListener(queues = "${config.rabbitmq.queue.name}")
public class TopicReceiver {

	@Autowired
	private StoreService storeService;

	@RabbitHandler
	public void process(String message) {
		try {
			ResultInfo resultInfo = GsonUtils.get(message, ResultInfo.class);
//			System.err.println(message);
			storeService.storeResult(resultInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

package com.sncfc.crawler.worker.spider.tactics;

import com.sncfc.crawler.worker.bean.ResultInfo;
import com.sncfc.crawler.worker.bean.TaskUnitInfo;
import com.sncfc.crawler.worker.mq.IMQClient;
import com.sncfc.crawler.worker.util.GsonUtils;

/**
 * 采集策略的父类，抽象类，定义执行任务的抽象方法。 其子类都是单例，所以不要在子类中定义非常量“属性”
 * 
 * @author a
 *
 */
public abstract class Tactics {

	/**
	 * 消息队列操作类
	 */
	private IMQClient mqClient;

	public Tactics(IMQClient mqClient) {
		this.mqClient = mqClient;
	}

	/**
	 * 执行采集任务的抽象方法，需要子类来实现
	 */
	public abstract void execute(TaskUnitInfo taskUnit);

	/**
	 * 获取采集结果后的处理
	 */
	protected void dealResultInfo(ResultInfo resultInfo) {
		String message = GsonUtils.toJson(resultInfo);
		mqClient.sendMessage(message);
	}
}

package com.sncfc.crawler.worker.mq;

public interface IMQClient {
	public void sendMessage(String message);
}

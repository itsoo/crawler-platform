package com.sncfc.crawler.worker.bean;

public class WorkerInfo {
	/**
	 * 所在服务器的IP
	 */
	private String selfIp;

	/**
	 * 该服务器对应的“任务单元”节点的路径
	 */
	private String taskUnitPath;

	public String getSelfIp() {
		return selfIp;
	}

	public void setSelfIp(String selfIp) {
		this.selfIp = selfIp;
	}

	public String getTaskUnitPath() {
		return taskUnitPath;
	}

	public void setTaskUnitPath(String taskUnitPath) {
		this.taskUnitPath = taskUnitPath;
	}

}

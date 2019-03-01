package com.sncfc.crawler.bean;

public class MessageInfo {
	private int code;

	private int taskStatus;

	private String msg;

	public MessageInfo(int code) {
		this.code = code;
	}

	public MessageInfo(int code, int taskStatus) {
		this.code = code;
		this.taskStatus = taskStatus;
	}

	public MessageInfo(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(int taskStatus) {
		this.taskStatus = taskStatus;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}

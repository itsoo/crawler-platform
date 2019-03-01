package com.sncfc.crawler.manage.bean;

public class TaskDetailBean {

	private long detailId;

	private long taskId;

	private long tacticsId;

	private String charset;

	private String urlHost;

	private String urlPath;

	private String urlParams;

	private String requestHeader;

	private String startTime;

	private String endTime;

	private long sleepTime;

	private int parseType;

	private String parseDetail;

	private String stopFloor;

	public long getDetailId() {
		return detailId;
	}

	public void setDetailId(long detailId) {
		this.detailId = detailId;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public long getTacticsId() {
		return tacticsId;
	}

	public void setTacticsId(long tacticsId) {
		this.tacticsId = tacticsId;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getUrlHost() {
		return urlHost;
	}

	public void setUrlHost(String urlHost) {
		this.urlHost = urlHost;
	}

	public String getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}

	public String getUrlParams() {
		return urlParams;
	}

	public void setUrlParams(String urlParams) {
		this.urlParams = urlParams;
	}

	public String getRequestHeader() {
		return requestHeader;
	}

	public void setRequestHeader(String requestHeader) {
		this.requestHeader = requestHeader;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public long getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public int getParseType() {
		return parseType;
	}

	public void setParseType(int parseType) {
		this.parseType = parseType;
	}

	public String getParseDetail() {
		return parseDetail;
	}

	public void setParseDetail(String parseDetail) {
		this.parseDetail = parseDetail;
	}

	public String getStopFloor() {
		return stopFloor;
	}

	public void setStopFloor(String stopFloor) {
		this.stopFloor = stopFloor;
	}

}

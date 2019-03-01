package com.sncfc.crawler.manage.bean;

import java.util.Map;

/**
 * 爬虫模块处理的“任务单元”的描述信息类
 * 
 * @author a
 *
 */
public class TaskUnitInfo {

	private long taskId;

	private long tacticsId;

	private String charset;

	private String urlHost;

	private String url;

	private int parseType;

	private String parseDetail;

	private long sleepTime;

	/**
	 * 爬取网站时，需要的额外消息头
	 */
	private String requestHeader;

	/**
	 * 从数据库读取的参数，存储额外需要的数据
	 */
	private Map<String, String> params;

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public long getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public String getRequestHeader() {
		return requestHeader;
	}

	public void setRequestHeader(String requestHeader) {
		this.requestHeader = requestHeader;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

}

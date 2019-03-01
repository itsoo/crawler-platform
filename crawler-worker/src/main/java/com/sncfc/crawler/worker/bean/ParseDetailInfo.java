package com.sncfc.crawler.worker.bean;

import java.util.Map;

/**
 * 网页源码解析规则详情
 * 
 * @author a
 *
 */
public class ParseDetailInfo {

	private String urlListXpath;

	private String urlRegex;

	private String urlAdd;

	private String tableName;

	private Map<String, String> itemXpath;

	public String getUrlListXpath() {
		return urlListXpath;
	}

	public void setUrlListXpath(String urlListXpath) {
		this.urlListXpath = urlListXpath;
	}

	public String getUrlRegex() {
		return urlRegex;
	}

	public void setUrlRegex(String urlRegex) {
		this.urlRegex = urlRegex;
	}

	public String getUrlAdd() {
		return urlAdd;
	}

	public void setUrlAdd(String urlAdd) {
		this.urlAdd = urlAdd;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Map<String, String> getItemXpath() {
		return itemXpath;
	}

	public void setItemXpath(Map<String, String> itemXpath) {
		this.itemXpath = itemXpath;
	}

}

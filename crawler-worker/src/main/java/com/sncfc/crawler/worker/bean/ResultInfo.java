package com.sncfc.crawler.worker.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 网页的最终解析结果
 * 
 * @author a
 *
 */
public class ResultInfo {

	private int code;

	private long taskId;

	private int resultType;

	private String urlPath;

	private String tableName;

	private String[] updateKeys;
	
	private List<Map<String, String>> columns;
	
	private String desc;

	public ResultInfo() {
		this.resultType = 0;
		this.columns = new ArrayList<Map<String, String>>();
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public int getResultType() {
		return resultType;
	}

	public void setResultType(int resultType) {
		this.resultType = resultType;
	}

	public String getUrlPath() {
		return urlPath;
	}

	public void setUrlPath(String urlPath) {
		this.urlPath = urlPath;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String[] getUpdateKeys() {
		return updateKeys;
	}

	public void setUpdateKeys(String[] updateKeys) {
		this.updateKeys = updateKeys;
	}

	public List<Map<String, String>> getColumns() {
		return columns;
	}

	public void setColumns(List<Map<String, String>> columns) {
		this.columns = columns;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}

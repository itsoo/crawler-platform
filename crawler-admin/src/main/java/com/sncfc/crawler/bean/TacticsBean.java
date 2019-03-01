package com.sncfc.crawler.bean;

public class TacticsBean {
	
	private long tacticsId;

	private String tacticsName;

	private int status;

	private String createTime;

	private String jarPath;

	private String className;

	private String describe;

	public long getTacticsId() {
		return tacticsId;
	}

	public void setTacticsId(long tacticsId) {
		this.tacticsId = tacticsId;
	}

	public String getTacticsName() {
		return tacticsName;
	}

	public void setTacticsName(String tacticsName) {
		this.tacticsName = tacticsName;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getJarPath() {
		return jarPath;
	}

	public void setJarPath(String jarPath) {
		this.jarPath = jarPath;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

}

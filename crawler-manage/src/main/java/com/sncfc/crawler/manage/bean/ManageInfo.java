package com.sncfc.crawler.manage.bean;

/**
 * “控制中心”节点的描述信息类
 * 
 * @author a
 *
 */
public class ManageInfo {
	/**
	 * 所在服务器的IP
	 */
	private String selfIp;

	/**
	 * 该服务器对应mission节点的路径
	 */
	private String messionPath;

	public ManageInfo(String selfIp, String messionPath) {
		this.selfIp = selfIp;
		this.messionPath = messionPath;
	}

	public String getSelfIp() {
		return selfIp;
	}

	public void setSelfIp(String selfIp) {
		this.selfIp = selfIp;
	}

	public String getMessionPath() {
		return messionPath;
	}

	public void setMessionPath(String messionPath) {
		this.messionPath = messionPath;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ManageInfo) {
			return this.hashCode() == obj.hashCode();
		}

		return super.equals(obj);
	}

	@Override
	public String toString() {
		return "{\"selfIp\":\"" + selfIp + "\", \"messionPath\":\""
				+ messionPath + "\"}";
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

}

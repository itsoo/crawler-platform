package com.sncfc.crawler.db;

public class SelectParam {
	private String whereClause;
	private String[] params;
	private String orderby = "";
	public String getWhereClause() {
		return whereClause;
	}
	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}
	public String[] getParams() {
		return params;
	}
	public void setParams(String[] params) {
		this.params = params;
	}
	public String getOrderby() {
		return orderby;
	}
	public void setOrderby(String orderby) {
		this.orderby = orderby;
	}
	
	public String getParamsStr() {
		String str = "";
		for (Object obj : params) {
			str += obj.toString() + ",";
		}
		return str;
	}
}

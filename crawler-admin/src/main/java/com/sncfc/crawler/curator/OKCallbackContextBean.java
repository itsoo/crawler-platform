package com.sncfc.crawler.curator;

public class OKCallbackContextBean {
	/**
	 * 操作数据库返回的状态码
	 */
	private int code;

	/**
	 * 操作任务节点的结果
	 */
	private boolean result;

	private String msg;

	public OKCallbackContextBean(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
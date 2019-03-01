package com.sncfc.crawler.bean;

public class Result<T> {
	/**
	 * 获取结果集的状态码
	 */
	private int code;

	/**
	 * 提示信息，一般只在失败时给出提示信息
	 */
	private String msg;

	/**
	 * 分页查询时的总数据量，用于计算总页数
	 */
	private long count;

	/**
	 * 获取的结果集，如果失败，该字段为null
	 */
	private T data;

	public Result() {

	}

	/**
	 * 该构造方法用于获取结果集时只需要“状态码”和“提示信息”的情况
	 * 
	 * @param code
	 * @param msg
	 */
	public Result(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	/**
	 * 该构造方法用于获取结果集成功时只需要“状态码”、“总数”和“结果集”的情况
	 * 
	 * @param code
	 * @param count
	 * @param data
	 */
	public Result(int code, long count, T data) {
		this.code = code;
		this.count = count;
		this.data = data;
	}

	/**
	 * 包含所有参数的构造方法
	 * 
	 * @param code
	 * @param msg
	 * @param count
	 * @param data
	 */
	public Result(int code, String msg, long count, T data) {
		this.code = code;
		this.msg = msg;
		this.count = count;
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}

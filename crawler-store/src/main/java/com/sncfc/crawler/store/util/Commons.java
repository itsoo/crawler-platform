package com.sncfc.crawler.store.util;

/**
 * 
 * 常量类
 * 
 * @author a
 *
 */
public class Commons {
	/**
	 * 访问网页获取正确结果的返回码
	 */
	public static final int RESULT_CODE_OK = 0;

	/**
	 * 访问网页获取的响应为空时的错误码
	 */
	public static final int RESULT_CODE_ERROR = -200;

	/**
	 * 访问网页获取到源码，但是没有解析出列表时的错误码
	 */
	public static final int RESULT_CODE_NO_URLS = -201;

	/**
	 * 访问网页获取到源码，但是没有解析出详情时的错误码
	 */
	public static final int RESULT_CODE_NO_ITEM = -202;

	/**
	 * 采集结果表都要有的字段：信息所在网址
	 */
	public static final String RESULT_BD_COLUMN_URL_PATH = "url_path";

	/**
	 * 采集结果表都要有的字段：信息所属任务的ID
	 */
	public static final String RESULT_BD_COLUMN_TASK_ID = "task_id";

	/**
	 * 任务的采集结果的类型：普通类型，只关心采集结果，将采集结果插入新表
	 */
	public static final int RESULT_INFO_TYPE_NORMAL = 0;

	/**
	 * 任务的采集结果的类型：插入新表，要插入采集结果和源数据的一些字段信息
	 */
	public static final int RESULT_INFO_TYPE_INSERT = 1;

	/**
	 * 任务的采集结果的类型：更新原表，将采集结果更新到原表的对应字段
	 */
	public static final int RESULT_INFO_TYPE_UPDATE = 2;
}

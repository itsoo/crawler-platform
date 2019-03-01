package com.sncfc.crawler.worker.util;

/**
 * 
 * 常量类
 * 
 * @author a
 *
 */
public class Commons {
	/**
	 * zookeeper节点“任务单元”的总节点目录名
	 */
	public static final String PARENT_TU_NODE_PATH = "/taskUnits";

	/**
	 * zookeeper节点“任务单元”的总节点目录名
	 */
	public static final String TU_NODE_PATH = PARENT_TU_NODE_PATH
			+ "/taskUnit-";

	/**
	 * zookeeper节点标识“爬取模块”的总结点目录名
	 */
	public static final String PARENT_WORKER_NODE_PATH = "/workers";

	/**
	 * zookeeper节点标识“控制中心”总节点下的子节点目录名
	 */
	public static final String WORKER_NODE_PATH = PARENT_WORKER_NODE_PATH
			+ "/worker-";

	/**
	 * JS方式解析网页源码时，如果parseDetail是以var urlRegex开头，则说明当前是列表页，否则是详情页
	 */
	public static final String PAESE_DETAIL_LIST_START = "var urlRegex";

	/**
	 * JS方式解析网页源码时，用来替换接收列表页urls的变量名
	 */
	public static final String PAESE_VARIABLE_NAME_LIST = "urlList";

	/**
	 * JS方式解析网页源码时，解析列表页生成urls的方法名
	 */
	public static final String PAESE_FUNCTION_NAME_LIST = "getUrlList";

	/**
	 * JS方式解析网页源码时，用来替换接收网页详情的变量名
	 */
	public static final String PAESE_VARIABLE_NAME_ITEM = "itemInfo";

	/**
	 * JS方式解析网页源码时，解析详情页页生成结果的方法名
	 */
	public static final String PAESE_FUNCTION_NAME_ITEM = "getItemInfo";

	/**
	 * 任务结果的解析方式：Xpath
	 */
	public static final int PARSE_TYPE_XPATH = 0;

	/**
	 * 任务结果的解析方式：JS
	 */
	public static final int PARSE_TYPE_JS = 1;

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
	 * 内置策略的最大ID，小于等于该ID的策略不可被修改，不可被删除
	 */
	public static final int DEFAULT_TACTICS_ID_MAX = 10;
}

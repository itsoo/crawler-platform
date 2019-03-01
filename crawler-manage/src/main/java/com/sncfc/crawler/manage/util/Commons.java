package com.sncfc.crawler.manage.util;

/**
 * 
 * 常量类
 * 
 * @author a
 *
 */
public class Commons {
	/**
	 * zookeeper节点“上线任务”的总节点目录名
	 */
	public static final String PARENT_TASK_NODE_PATH = "/tasks";

	/**
	 * zookeeper节点标识“控制中心”的总节点目录名
	 */
	public static final String PARENT_MANAGE_NODE_PATH = "/manages";

	/**
	 * zookeeper节点标识“控制中心”总节点下的子节点目录名
	 */
	public static final String MANAGE_NODE_PATH = PARENT_MANAGE_NODE_PATH
			+ "/manage-";

	/**
	 * zookeeper节点标识“控制中心”对应的自身任务总节点目录名
	 */
	public static final String PARENT_MISSION_NODE_PATH = "/missions";

	/**
	 * zookeeper节点标识“控制中心”对应的自身任务总节点下的子节点目录名
	 */
	public static final String MISSION_NODE_PATH = PARENT_MISSION_NODE_PATH
			+ "/mission-";

	/**
	 * zookeeper节点标记为“主节点”的节点目录名
	 */
	public static final String MSATER_NODE_PATH = "/master";

	/**
	 * zookeeper节点标识“爬取模块”的总节点目录名
	 */
	public static final String PARENT_WORKER_NODE_PATH = "/workers";

	/**
	 * “上线任务”的状态码：执行（“上线”）
	 */
	public static final int TASK_STATUS_EXECUTORY = 2;

	/**
	 * “上线任务”的状态码：暂停
	 */
	public static final int TASK_STATUS_PAUSE = 3;

	/**
	 * “上线任务”的状态码：完成
	 */
	public static final int TASK_STATUS_FINISH = 4;

	/**
	 * “上线任务”的类型码：单页任务
	 */
	public static final int TASK_TYPE_SINGLE = 0;

	/**
	 * “上线任务”的类型码：多页任务
	 */
	public static final int TASK_TYPE_MULTIPLE = 1;

	/**
	 * “上线任务”是否循环执行：不循环
	 */
	public static final int TASK_CIRCULATION_FALSE = 0;

	/**
	 * “上线任务”是否循环执行：循环
	 */
	public static final int TASK_CIRCULATION_TRUE = 1;

	/**
	 * 多页任务中参数的类型：单值替换
	 */
	public static final int TASK_PARAMS_TYPE_SINGLE = 0;

	/**
	 * 多页任务中参数的类型：范围取值，比如页码 1-20 页
	 */
	public static final int TASK_PARAMS_TYPE_BETWEEN = 1;

	/**
	 * 多页任务中参数的类型：数据库读取，不仅要做参数替换，还要存储源数据的某些字段值，加入到最终的采集结果
	 */
	public static final int TASK_PARAMS_TYPE_DB_NOMAL = 2;

	/**
	 * 多页任务中参数的类型：数据库读取，类似百度ak类型。只做参数替换，不存储源数据的其他字段值
	 */
	public static final int TASK_PARAMS_TYPE_DB_AK = 3;

	/**
	 * 任务反馈的初始值
	 */
	public static final int TASK_FEEDBACK_ORIGINAL = 0;

	/**
	 * 从数据库读取网址构造参数时，参数的分隔符
	 */
	public static final String TASK_PARAMS_KEY_SEPARATOR = ",";

}

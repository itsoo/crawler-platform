package com.sncfc.crawler.util;

/**
 * 常量类
 *
 * @author a
 */
public class Commons {
    /**
     * 用于权限验证的Cookie字段名
     */
    public static final String COOKIE_TOKEN = "token";

    /**
     * Result结果类code字段成功的状态码
     */
    public static final int RESULT_CODE_OK = 0;

    /**
     * Result结果类code字段失败的状态码
     */
    public static final int RESULT_CODE_FAILED = -1;

    /**
     * “上线任务”的状态码：停止
     */
    public static final int TASK_STATUS_STOP = 0;

    /**
     * “上线任务”的状态码：启用
     */
    public static final int TASK_STATUS_START = 1;

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
     * 如果操作任务节点出现了问题，就返回该状态码
     */
    public static final int TASK_NODE_ERROR = -200;

    /**
     * 检查节点存在不存在的状态码：存在
     */
    public static final int TASK_NODE_EXISTS = 1;

    /**
     * 检查节点存在不存在的状态码：不存在
     */
    public static final int TASK_NODE_NONE = 0;

    /**
     * zookeeper节点“上线任务”的总节点目录名
     */
    public static final String PARENT_TASK_NODE_PATH = "/tasks";

    /**
     * zookeeper节点“上线任务”下的子节点目录名
     */
    public static final String BASE_TASK_NODE_PATH = PARENT_TASK_NODE_PATH
            + "/task-";

    /**
     * 任务反馈的初始值
     */
    public static final int TASK_FEEDBACK_ORIGINAL = 0;

    /**
     * 内置策略的最大ID，小于等于该ID的策略不可被修改，不可被删除
     */
    public static final int DEFAULT_TACTICS_ID_MAX = 10;
}

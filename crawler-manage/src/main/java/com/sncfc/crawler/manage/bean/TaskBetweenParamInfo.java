package com.sncfc.crawler.manage.bean;

/**
 * 任务参数类TaskParamInfo的包装类
 * 
 * @author a
 *
 */
public class TaskBetweenParamInfo {

	private TaskParamInfo taskParamInfo;

	private TaskBetweenParamInfo nextTaskBetweenParamInfo;

	public TaskParamInfo getTaskParamInfo() {
		return taskParamInfo;
	}

	public void setTaskParamInfo(TaskParamInfo taskParamInfo) {
		this.taskParamInfo = taskParamInfo;
	}

	public TaskBetweenParamInfo getNextTaskBetweenParamInfo() {
		return nextTaskBetweenParamInfo;
	}

	public void setNextTaskBetweenParamInfo(
			TaskBetweenParamInfo nextTaskBetweenParamInfo) {
		this.nextTaskBetweenParamInfo = nextTaskBetweenParamInfo;
	}

}

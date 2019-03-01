package com.sncfc.crawler.manage.bean;

/**
 * "上线任务"节点值转换后的Task 任务自身的数据只关注taskId、status、taskType和circulation这四个字段
 * 
 * @author a
 *
 */
public class TaskBean {

	/**
	 * 任务的ID
	 */
	private long taskId;

	/**
	 * 任务的状态码
	 */
	private int status;

	/**
	 * 任务的类型：单页或多页
	 */
	private int taskType;

	/**
	 * 是否是循环任务的标识码
	 */
	private int circulation;

	private String startTime;

	private String endTime;

	/**
	 * 该任务在tasks节点下的目录名
	 */
	private String taskNodePath;

	/**
	 * 该任务被分派到的目标mission的目录值
	 */
	private String missionNodePath;

	/**
	 * 用来记录该任务被处理时的时间戳
	 */
	private long lastDealTimestamp;
	
	/**
	 * 用来记录“控制中心”对“上线任务”的处理结果，用来反馈给“用户系统”
	 */
	private int feedback;

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getTaskType() {
		return taskType;
	}

	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}

	public int getCirculation() {
		return circulation;
	}

	public void setCirculation(int circulation) {
		this.circulation = circulation;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getTaskNodePath() {
		return taskNodePath;
	}

	public void setTaskNodePath(String taskNodePath) {
		this.taskNodePath = taskNodePath;
	}

	public String getMissionNodePath() {
		return missionNodePath;
	}

	public void setMissionNodePath(String missionNodePath) {
		this.missionNodePath = missionNodePath;
	}

	public long getLastDealTimestamp() {
		return lastDealTimestamp;
	}

	public void setLastDealTimestamp(long lastDealTimestamp) {
		this.lastDealTimestamp = lastDealTimestamp;
	}

	public int getFeedback() {
		return feedback;
	}

	public void setFeedback(int feedback) {
		this.feedback = feedback;
	}

}

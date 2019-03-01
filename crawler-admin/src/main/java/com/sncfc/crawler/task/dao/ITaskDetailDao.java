package com.sncfc.crawler.task.dao;

import com.sncfc.crawler.bean.TaskDetailBean;

public interface ITaskDetailDao {
	public TaskDetailBean searchTaskDetail(long taskId);

	public int insertTaskDetail(TaskDetailBean taskDeatil);

	public int updateTaskDetail(TaskDetailBean taskDeatil);

	public int searchCountByTacticsId(long tacticsId);
	
	public String searchTaskIdsByTacticsId(long tacticsId);
}
package com.sncfc.crawler.manage.curator;

import com.sncfc.crawler.manage.bean.TaskBean;
import com.sncfc.crawler.manage.bean.TaskUnitInfo;

public interface OkCallback {
	public void dealNewTask(TaskBean newTask);

	public void dealUpdatedTask(TaskBean updatedTask);

	public TaskUnitInfo getNextTaskUnitInfo(long taskId, int workerCount);
}
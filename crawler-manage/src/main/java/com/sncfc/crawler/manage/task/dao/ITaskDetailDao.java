package com.sncfc.crawler.manage.task.dao;

import java.util.List;
import java.util.Map;

import com.sncfc.crawler.manage.bean.TaskDetailBean;
import com.sncfc.crawler.manage.bean.TaskParamInfo;

public interface ITaskDetailDao {
	public TaskDetailBean searchTaskDetail(long taskId);

	public int updateTaskDetail(long taskId, String stoopFloor);

	public List<Map<String, String>> getParamsFromBD(
            TaskParamInfo taskParamInfo, int limit);

	public List<Map<String, String>> getAksFromBD(String sql);
}
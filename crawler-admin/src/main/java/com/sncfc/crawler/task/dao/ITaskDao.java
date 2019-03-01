package com.sncfc.crawler.task.dao;

import java.util.List;

import com.sncfc.crawler.bean.TaskBean;
import com.sncfc.crawler.bean.UpdateTaskInfo;
import com.sncfc.crawler.db.Filter;

public interface ITaskDao {
	public UpdateTaskInfo searchUpdateTaskInfoById(Long taskId);

	public List<UpdateTaskInfo> searchUpdateTaskInfoByStatus(Integer status);

	public List<TaskBean> searchTask(List<Filter> filters, Integer start,
                                     Integer limit);

	public long searchTaskCount(List<Filter> filters);

	public int insertTask(TaskBean task);

	public int deleteTask(Long taskId);

	public int updateTaskStatus(Long taskId, Integer status);

	public int updateTaskDesc(Long taskId, String desc);
}

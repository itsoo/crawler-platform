package com.sncfc.crawler.task.dao;

import java.util.List;

import com.sncfc.crawler.bean.TaskTagBean;

public interface ITaskTagDao {
	public List<TaskTagBean> searchTaskTag();

	public int insertTaskTag(String tagName);

	public int deleteTaskTag(Long tagId);
}

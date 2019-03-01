package com.sncfc.crawler.curator;

import com.sncfc.crawler.bean.UpdateTaskInfo;

public interface OkCallback {
	public void execute(UpdateTaskInfo updatedTask);
}
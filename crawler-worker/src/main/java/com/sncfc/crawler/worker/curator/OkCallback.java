package com.sncfc.crawler.worker.curator;

import com.sncfc.crawler.worker.bean.TaskUnitInfo;

public interface OkCallback {
	public void dealTaskUnit(TaskUnitInfo newTask);
}
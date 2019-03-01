package com.sncfc.crawler.store.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sncfc.crawler.store.bean.ResultInfo;
import com.sncfc.crawler.store.dao.IStoreDao;
import com.sncfc.crawler.store.util.Commons;

@Service
public class StoreService {
	@Autowired
	private IStoreDao storeDao;

	public void storeResult(ResultInfo resultInfo) {
		// 先处理结果的状态码，如果为0：正常，才做入库处理，否则
		int code = resultInfo.getCode();
		if (Commons.RESULT_CODE_OK == code) {
			String tableName = resultInfo.getTableName();

			String taskId = String.valueOf(resultInfo.getTaskId());
			String urlPath = resultInfo.getUrlPath();

			int resultType = resultInfo.getResultType();
			if (Commons.RESULT_INFO_TYPE_NORMAL == resultType) {
				List<Map<String, String>> columns = resultInfo.getColumns();
				for (Map<String, String> map : columns) {
					map.put(Commons.RESULT_BD_COLUMN_TASK_ID, taskId);
					map.put(Commons.RESULT_BD_COLUMN_URL_PATH, urlPath);
				}

				storeDao.insertTable(tableName, columns);
			} else if (Commons.RESULT_INFO_TYPE_INSERT == resultType) {
				List<Map<String, String>> columns = resultInfo.getColumns();
				storeDao.insertTable(tableName, columns);
			} else if (Commons.RESULT_INFO_TYPE_UPDATE == resultType) {
				List<Map<String, String>> columns = resultInfo.getColumns();
				String[] updateKeys = resultInfo.getUpdateKeys();
				storeDao.updateTable(tableName, columns, updateKeys);
			}

		} else {
			storeDao.insertErrorInfo(resultInfo);
		}
	}
}

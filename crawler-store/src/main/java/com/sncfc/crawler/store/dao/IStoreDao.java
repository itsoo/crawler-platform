package com.sncfc.crawler.store.dao;

import java.util.List;
import java.util.Map;

import com.sncfc.crawler.store.bean.ResultInfo;

public interface IStoreDao {
	public int insertTable(String table, List<Map<String, String>> datas);

	public int updateTable(String table, List<Map<String, String>> datas,
                           String[] updateKeys);

	public int insertErrorInfo(ResultInfo resultInfo);
}

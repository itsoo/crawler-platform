package com.sncfc.crawler.tactics.dao;

import java.util.List;

import com.sncfc.crawler.bean.TacticsBean;
import com.sncfc.crawler.db.Filter;

public interface ITacticsDao {
	public List<TacticsBean> searchTacticses(List<Filter> filters, int start,
                                             int limit);

	public List<TacticsBean> searchTacticsesOptions();
	
	public long searchTacticsCount(List<Filter> filters);

	public int insertTactics(TacticsBean tactics);

	public int updateTactics(long tacticsId, int status);

	public int deleteTactics(long tacticsId);
}

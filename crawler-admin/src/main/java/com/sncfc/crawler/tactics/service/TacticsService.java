package com.sncfc.crawler.tactics.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sncfc.crawler.bean.TacticsBean;
import com.sncfc.crawler.db.Filter;
import com.sncfc.crawler.tactics.dao.ITacticsDao;
import com.sncfc.crawler.task.dao.ITaskDetailDao;

@Service
public class TacticsService {
	@Autowired
	private ITacticsDao tacticsDao;

	@Autowired
	private ITaskDetailDao taskDetailDao;

	public List<TacticsBean> searchTactics(List<Filter> filters, int pageNo,
			int pageSize) {
		int start = (pageNo - 1) * pageSize;

		List<TacticsBean> list = tacticsDao.searchTacticses(filters, start,
				pageSize);

		return list;
	}

	public List<TacticsBean> searchTacticsOptions() {
		return tacticsDao.searchTacticsesOptions();
	}

	public long searchTacticsCount(List<Filter> filters) {
		return tacticsDao.searchTacticsCount(filters);
	}

	public int addTactics(TacticsBean tactics) {
		return tacticsDao.insertTactics(tactics);
	}

	public int deleteTactics(long tacticsId) {
		return tacticsDao.deleteTactics(tacticsId);
	}

	public int updateTactics(long tacticsId, int status) {
		return tacticsDao.updateTactics(tacticsId, status);
	}

	public int searchTaskCountByTacticsId(long tacticsId) {
		return taskDetailDao.searchCountByTacticsId(tacticsId);
	}

	public String searchTaskIdsByTacticsId(long tacticsId) {
		return taskDetailDao.searchTaskIdsByTacticsId(tacticsId);
	}
}
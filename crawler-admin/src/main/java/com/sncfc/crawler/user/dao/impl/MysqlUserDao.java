package com.sncfc.crawler.user.dao.impl;

import com.sncfc.crawler.db.MysqlBaseTableDao;

import javax.sql.DataSource;

public class MysqlUserDao extends MysqlBaseTableDao {

	public MysqlUserDao(DataSource dataSource) {
		super(dataSource);
	}

}

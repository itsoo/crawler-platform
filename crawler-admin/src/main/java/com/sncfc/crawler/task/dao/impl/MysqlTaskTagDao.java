package com.sncfc.crawler.task.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.sncfc.crawler.bean.TaskTagBean;
import com.sncfc.crawler.db.MysqlBaseTableDao;
import com.sncfc.crawler.task.dao.ITaskTagDao;

public class MysqlTaskTagDao extends MysqlBaseTableDao implements ITaskTagDao {
	private static final Logger logger = Logger
			.getLogger(MysqlTaskTagDao.class);

	public MysqlTaskTagDao(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public List<TaskTagBean> searchTaskTag() {
		List<TaskTagBean> taskTagList = null;

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "select * from cs_mp_task_tag";
			stat = conn.prepareStatement(sql);

			rs = stat.executeQuery();
			taskTagList = new ArrayList<TaskTagBean>();
			while (rs.next()) {
				TaskTagBean taskTag = new TaskTagBean();
				taskTag.setTagId(rs.getLong("tag_id"));
				taskTag.setTagName(rs.getString("tag_name"));
				taskTag.setCreateTime(rs.getString("create_time"));

				taskTagList.add(taskTag);
			}
		} catch (Exception e) {
			logger.error("查询任务标签时异常了···", e);
		} finally {
			// 关闭结果集
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			// 关闭预编译
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			// 关闭连接
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}
		
		return taskTagList;
	}

	@Override
	public int insertTaskTag(String taskTagName) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "insert into cs_mp_task_tag(tag_name) values(?)";
			stat = conn.prepareStatement(sql);
			stat.setString(1, taskTagName);

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("插入任务标签时异常了···", e);
		} finally {
			// 关闭预编译
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			// 关闭连接
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}

		return -1;
	}

	@Override
	public int deleteTaskTag(Long tagId) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "delete from cs_mp_task_tag where tag_id = ?";
			stat = conn.prepareStatement(sql);
			stat.setLong(1, tagId);

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("删除任务标签时异常了···", e);
		} finally {
			// 关闭预编译
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			// 关闭连接
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		}

		return -1;
	}
}

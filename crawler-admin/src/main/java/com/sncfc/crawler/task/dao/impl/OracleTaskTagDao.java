package com.sncfc.crawler.task.dao.impl;

import com.sncfc.crawler.bean.TaskTagBean;
import com.sncfc.crawler.db.OracleBaseTableDao;
import com.sncfc.crawler.task.dao.ITaskTagDao;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OracleTaskTagDao extends OracleBaseTableDao implements ITaskTagDao {
	private static final Logger logger = Logger
			.getLogger(OracleTaskTagDao.class);

	public OracleTaskTagDao(DataSource dataSource) {
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
			String sql = "select * from SC_TASK_TAG";
			stat = conn.prepareStatement(sql);

			rs = stat.executeQuery();
			taskTagList = new ArrayList<TaskTagBean>();
			while (rs.next()) {
				TaskTagBean taskTag = new TaskTagBean();
				taskTag.setTagId(rs.getLong("TAG_ID"));
				taskTag.setTagName(rs.getString("TAG_NAME"));
				taskTag.setCreateTime(rs.getString("CREATE_TIME"));

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
			if(stat != null){
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
			String sql = "insert into SC_TASK_TAG(TAG_ID,TAG_NAME) values(SEQ_SC_TASK_TAG.NEXTVAL,?)";
			stat = conn.prepareStatement(sql);
			stat.setString(1, taskTagName);

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("插入任务标签时异常了···", e);
		} finally {
			// 关闭预编译
			if(stat != null){
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
			String sql = "delete from SC_TASK_TAG where tag_id = ?";
			stat = conn.prepareStatement(sql);
			stat.setLong(1, tagId);

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("删除任务标签时异常了···", e);
		} finally {
			// 关闭预编译
			if(stat != null){
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

package com.sncfc.crawler.task.dao.impl;

import com.sncfc.crawler.bean.TaskDetailBean;
import com.sncfc.crawler.db.OracleBaseTableDao;
import com.sncfc.crawler.task.dao.ITaskDetailDao;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OracleTaskDetailDao extends OracleBaseTableDao implements ITaskDetailDao {
	private static final Logger logger = Logger
			.getLogger(OracleTaskDetailDao.class);

	public OracleTaskDetailDao(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public TaskDetailBean searchTaskDetail(long taskId) {
		TaskDetailBean taskDetail = null;

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "select * from SC_TASK_DETAIL where TASK_ID = ?";
			stat = conn.prepareStatement(sql);
			stat.setLong(1, taskId);

			rs = stat.executeQuery();
			if (rs.next()) {
				taskDetail = new TaskDetailBean();

				taskDetail.setDetailId(rs.getLong("DETAIL_ID"));
				taskDetail.setTaskId(rs.getLong("TASK_ID"));
				taskDetail.setTacticsId(rs.getLong("TACTICS_ID"));
				taskDetail.setCharset(rs.getString("CHARSET"));
				taskDetail.setUrlHost(rs.getString("URL_HOST"));
				taskDetail.setUrlPath(rs.getString("URL_PATH"));
				taskDetail.setUrlParams(rs.getString("URL_PARAMS"));
				taskDetail.setRequestHeader(rs.getString("REQUEST_HEADER"));
				taskDetail.setStartTime(rs.getString("START_TIME"));
				taskDetail.setEndTime(rs.getString("END_TIME"));
				taskDetail.setSleepTime(rs.getLong("SLEEP_TIME"));
				taskDetail.setParseType(rs.getInt("PARSE_TYPE"));
				taskDetail.setParseDetail(rs.getString("PARSE_DETAIL"));
				taskDetail.setStopFloor(rs.getString("STOP_FLOOR"));
			}
		} catch (Exception e) {
			logger.error("查询策略时异常了···", e);
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

		return taskDetail;
	}

	@Override
	public int insertTaskDetail(TaskDetailBean taskDetail) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "insert into SC_TASK_DETAIL(DETAIL_ID,TASK_ID,TACTICS_ID,CHARSET,URL_HOST,URL_PATH,URL_PARAMS,REQUEST_HEADER,START_TIME,END_TIME,SLEEP_TIME,PARSE_TYPE,PARSE_DETAIL,STOP_FLOOR) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			stat = conn.prepareStatement(sql);
			int index = 1;
			stat.setLong(index++, taskDetail.getTaskId());
			stat.setLong(index++, taskDetail.getTaskId());
			stat.setLong(index++, taskDetail.getTacticsId());
			stat.setString(index++, taskDetail.getCharset());
			stat.setString(index++, taskDetail.getUrlHost());
			stat.setString(index++, taskDetail.getUrlPath());
			stat.setString(index++, taskDetail.getUrlParams());
			stat.setString(index++, taskDetail.getRequestHeader());
			stat.setString(index++, taskDetail.getStartTime());
			stat.setString(index++, taskDetail.getEndTime());
			stat.setLong(index++, taskDetail.getSleepTime());
			stat.setInt(index++, taskDetail.getParseType());
			stat.setString(index++, taskDetail.getParseDetail());
			stat.setString(index++, taskDetail.getStopFloor());

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("插入策略时异常了···", e);
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
	public int updateTaskDetail(TaskDetailBean taskDetail) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "update SC_TASK_DETAIL set TACTICS_ID=?,CHARSET=?,URL_HOST=?,URL_PATH=?,URL_PARAMS=?,REQUEST_HEADER=?,START_TIME=?,END_TIME=?,SLEEP_TIME=?,PARSE_TYPE=?,PARSE_DETAIL=?,STOP_FLOOR=? where DETAIL_ID = ?";
			stat = conn.prepareStatement(sql);
			int index = 1;
			stat.setLong(index++, taskDetail.getTacticsId());
			stat.setString(index++, taskDetail.getCharset());
			stat.setString(index++, taskDetail.getUrlHost());
			stat.setString(index++, taskDetail.getUrlPath());
			stat.setString(index++, taskDetail.getUrlParams());
			stat.setString(index++, taskDetail.getRequestHeader());
			stat.setString(index++, taskDetail.getStartTime());
			stat.setString(index++, taskDetail.getEndTime());
			stat.setLong(index++, taskDetail.getSleepTime());
			stat.setInt(index++, taskDetail.getParseType());
			stat.setString(index++, taskDetail.getParseDetail());
			stat.setString(index++, taskDetail.getStopFloor());
			stat.setLong(index++, taskDetail.getDetailId());

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("更新策略时异常了···", e);
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

	public int searchCountByTacticsId(long tacticsId) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();

			String sql = "select count(*) from SC_TASK_DETAIL where TACTICS_ID = ?";
			stat = conn.prepareStatement(sql);
			stat.setLong(1, tacticsId);

			rs = stat.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception e) {
			logger.error("查询任务数量时异常了···", e);
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

		return -1;
	}

	public String searchTaskIdsByTacticsId(long tacticsId) {
		StringBuilder taskIds = new StringBuilder();

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();

			String sql = "select TASK_ID from SC_TASK_DETAIL where TACTICS_ID = ? ORDER BY TASK_ID";
			stat = conn.prepareStatement(sql);
			stat.setLong(1, tacticsId);

			rs = stat.executeQuery();
			while (rs.next()) {
				long taskId = rs.getLong(1);
				taskIds.append(taskId).append(",");
			}
		} catch (Exception e) {
			logger.error("查询任务数量时异常了···", e);
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

		return taskIds.toString();
	}
}

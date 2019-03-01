package com.sncfc.crawler.task.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.sncfc.crawler.bean.TaskDetailBean;
import com.sncfc.crawler.db.MysqlBaseTableDao;
import com.sncfc.crawler.task.dao.ITaskDetailDao;

public class MysqlTaskDetailDao extends MysqlBaseTableDao implements ITaskDetailDao {
	private static final Logger logger = Logger
			.getLogger(MysqlTaskDetailDao.class);

	public MysqlTaskDetailDao(DataSource dataSource) {
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
			String sql = "select * from cs_mp_task_detail where task_id = ?";
			stat = conn.prepareStatement(sql);
			stat.setLong(1, taskId);

			rs = stat.executeQuery();
			if (rs.next()) {
				taskDetail = new TaskDetailBean();

				taskDetail.setDetailId(rs.getLong("detail_id"));
				taskDetail.setTaskId(rs.getLong("task_id"));
				taskDetail.setTacticsId(rs.getLong("tactics_id"));
				taskDetail.setCharset(rs.getString("charset"));
				taskDetail.setUrlHost(rs.getString("url_host"));
				taskDetail.setUrlPath(rs.getString("url_path"));
				taskDetail.setUrlParams(rs.getString("url_params"));
				taskDetail.setRequestHeader(rs.getString("request_header"));
				taskDetail.setStartTime(rs.getString("start_time"));
				taskDetail.setEndTime(rs.getString("end_time"));
				taskDetail.setSleepTime(rs.getLong("sleep_time"));
				taskDetail.setParseType(rs.getInt("parse_type"));
				taskDetail.setParseDetail(rs.getString("parse_detail"));
				taskDetail.setStopFloor(rs.getString("stop_floor"));
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

		return taskDetail;
	}

	@Override
	public int insertTaskDetail(TaskDetailBean taskDeatil) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "insert into cs_mp_task_detail(task_id,tactics_id,charset,url_host,url_path,url_params,request_header,start_time,end_time,sleep_time,parse_type,parse_detail,stop_floor) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			stat = conn.prepareStatement(sql);
			stat.setLong(1, taskDeatil.getTaskId());
			stat.setLong(2, taskDeatil.getTacticsId());
			stat.setString(3, taskDeatil.getCharset());
			stat.setString(4, taskDeatil.getUrlHost());
			stat.setString(5, taskDeatil.getUrlPath());
			stat.setString(6, taskDeatil.getUrlParams());
			stat.setString(7, taskDeatil.getRequestHeader());
			stat.setString(8, taskDeatil.getStartTime());
			stat.setString(9, taskDeatil.getEndTime());
			stat.setLong(10, taskDeatil.getSleepTime());
			stat.setInt(11, taskDeatil.getParseType());
			stat.setString(12, taskDeatil.getParseDetail());
			stat.setString(13, taskDeatil.getStopFloor());

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("插入策略时异常了···", e);
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
	public int updateTaskDetail(TaskDetailBean taskDeatil) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "update cs_mp_task_detail set tactics_id=?,charset=?,url_host=?,url_path=?,url_params=?,request_header=?,start_time=?,end_time=?,sleep_time=?,parse_type=?,parse_detail=?,stop_floor=? where detail_id = ?";
			stat = conn.prepareStatement(sql);
			stat.setLong(1, taskDeatil.getTacticsId());
			stat.setString(2, taskDeatil.getCharset());
			stat.setString(3, taskDeatil.getUrlHost());
			stat.setString(4, taskDeatil.getUrlPath());
			stat.setString(5, taskDeatil.getUrlParams());
			stat.setString(6, taskDeatil.getRequestHeader());
			stat.setString(7, taskDeatil.getStartTime());
			stat.setString(8, taskDeatil.getEndTime());
			stat.setLong(9, taskDeatil.getSleepTime());
			stat.setInt(10, taskDeatil.getParseType());
			stat.setString(11, taskDeatil.getParseDetail());
			stat.setString(12, taskDeatil.getStopFloor());
			stat.setLong(13, taskDeatil.getDetailId());

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("更新策略时异常了···", e);
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

	public int searchCountByTacticsId(long tacticsId) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();

			String sql = "select count(*) from `cs_mp_task_detail` where `tactics_id` = ?";
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

	public String searchTaskIdsByTacticsId(long tacticsId) {
		StringBuilder taskIds = new StringBuilder();

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();

			String sql = "select `task_id` from `cs_mp_task_detail` where `tactics_id` = ?";
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

		return taskIds.toString();
	}
}

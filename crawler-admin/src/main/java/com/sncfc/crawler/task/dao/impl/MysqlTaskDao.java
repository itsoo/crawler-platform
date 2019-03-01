package com.sncfc.crawler.task.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.sncfc.crawler.bean.TaskBean;
import com.sncfc.crawler.bean.UpdateTaskInfo;
import com.sncfc.crawler.db.MysqlBaseTableDao;
import com.sncfc.crawler.db.Filter;
import com.sncfc.crawler.db.SelectParam;
import com.sncfc.crawler.task.dao.ITaskDao;

public class MysqlTaskDao extends MysqlBaseTableDao implements ITaskDao {
	private static final Logger logger = Logger.getLogger(MysqlTaskDao.class);

	public MysqlTaskDao(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public UpdateTaskInfo searchUpdateTaskInfoById(Long taskId) {
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();

			String sql = "select a.task_id,a.task_type,a.circulation,a.status,b.start_time,b.end_time from `cs_mp_task` a join `cs_mp_task_detail` b on a.task_id=b.task_id and a.task_id=?";

			stat = conn.prepareStatement(sql);
			stat.setLong(1, taskId);

			rs = stat.executeQuery();
			if (rs.next()) {
				UpdateTaskInfo task = new UpdateTaskInfo();
				task.setTaskId(rs.getLong(1));
				task.setTaskType(rs.getInt(2));
				task.setCirculation(rs.getInt(3));
				task.setStatus(rs.getInt(4));
				task.setStartTime(rs.getString(5));
				task.setEndTime(rs.getString(6));

				return task;
			}
		} catch (Exception e) {
			logger.error("查询任务时异常了···", e);
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

		return null;
	}

	@Override
	public List<UpdateTaskInfo> searchUpdateTaskInfoByStatus(Integer status) {
		List<UpdateTaskInfo> updateTaskList = new ArrayList<UpdateTaskInfo>();

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();

			String sql = "select a.task_id,a.task_type,a.circulation,a.status,b.start_time,b.end_time from `cs_mp_task` a join `cs_mp_task_detail` b on a.task_id=b.task_id and a.status=?";

			stat = conn.prepareStatement(sql);
			stat.setInt(1, status);

			rs = stat.executeQuery();
			while (rs.next()) {
				UpdateTaskInfo task = new UpdateTaskInfo();
				task.setTaskId(rs.getLong(1));
				task.setTaskType(rs.getInt(2));
				task.setCirculation(rs.getInt(3));
				task.setStatus(rs.getInt(4));
				task.setStartTime(rs.getString(5));
				task.setEndTime(rs.getString(6));

				updateTaskList.add(task);
			}
		} catch (Exception e) {
			logger.error("查询任务时异常了···", e);
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

		return updateTaskList;
	}

	@Override
	public List<TaskBean> searchTask(List<Filter> filters, Integer start,
			Integer limit) {
		List<TaskBean> taskList = null;

		SelectParam sp = getSelectParam(filters, null, null);
		String limitByCluase = getLimitClause(start + "," + limit);

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();

			String sql = "select * from `cs_mp_task`" + sp.getWhereClause()
					+ limitByCluase;
			stat = conn.prepareStatement(sql);

			String[] params = sp.getParams();
			if (params != null) {
				int index = 1;
				for (String param : params) {
					stat.setString(index++, param);
				}
			}

			rs = stat.executeQuery();
			taskList = new ArrayList<TaskBean>();
			while (rs.next()) {
				TaskBean task = new TaskBean();
				task.setTaskId(rs.getLong("task_id"));
				task.setTagId(rs.getLong("tag_id"));
				task.setTaskName(rs.getString("task_name"));
				task.setTaskType(rs.getInt("task_type"));
				task.setCreateTime(rs.getString("create_time"));
				task.setCirculation(rs.getInt("circulation"));
				task.setStatus(rs.getInt("status"));
				task.setDescribe(rs.getString("describe"));

				taskList.add(task);
			}
		} catch (Exception e) {
			logger.error("查询任务时异常了···", e);
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

		return taskList;
	}

	@Override
	public long searchTaskCount(List<Filter> filters) {
		SelectParam sp = getSelectParam(filters, null, null);

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();

			String sql = "select count(*) from `cs_mp_task` "
					+ sp.getWhereClause();
			stat = conn.prepareStatement(sql);

			String[] params = sp.getParams();
			if (params != null) {
				int index = 1;
				for (String param : params) {
					stat.setString(index++, param);
				}
			}

			rs = stat.executeQuery();
			if (rs.next()) {
				return rs.getLong(1);
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

	@Override
	public int insertTask(TaskBean task) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "insert into `cs_mp_task`(`tag_id`,`task_name`,`task_type`,`circulation`,`status`,`describe`) values(?,?,?,?,?,?)";
			stat = conn.prepareStatement(sql);
			stat.setLong(1, task.getTagId());
			stat.setString(2, task.getTaskName());
			stat.setInt(3, task.getTaskType());
			stat.setInt(4, task.getCirculation());
			stat.setInt(5, task.getStatus());
			stat.setString(6, task.getDescribe());

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("插入任务时异常了···", e);
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
	public int deleteTask(Long taskId) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "delete from `cs_mp_task` where `task_id`=?";
			stat = conn.prepareStatement(sql);
			stat.setLong(1, taskId);

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("删除任务时异常了···", e);
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
	public int updateTaskStatus(Long taskId, Integer status) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "update `cs_mp_task` set `status`=? where `task_id`=?";
			stat = conn.prepareStatement(sql);
			stat.setInt(1, status);
			stat.setLong(2, taskId);

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("更新任务状态时异常了···", e);
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
	public int updateTaskDesc(Long taskId, String desc) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "update `cs_mp_task` set `describe`=? where `task_id`=?";
			stat = conn.prepareStatement(sql);
			stat.setString(1, desc);
			stat.setLong(2, taskId);

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("更新任务状态时异常了···", e);
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

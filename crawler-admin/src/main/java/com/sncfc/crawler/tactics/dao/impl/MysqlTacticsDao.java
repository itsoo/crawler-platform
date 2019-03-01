package com.sncfc.crawler.tactics.dao.impl;

import com.sncfc.crawler.bean.TacticsBean;
import com.sncfc.crawler.db.Filter;
import com.sncfc.crawler.db.MysqlBaseTableDao;
import com.sncfc.crawler.db.SelectParam;
import com.sncfc.crawler.tactics.dao.ITacticsDao;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MysqlTacticsDao extends MysqlBaseTableDao implements ITacticsDao {
	private static final Logger logger = Logger
			.getLogger(MysqlTacticsDao.class);

	public MysqlTacticsDao(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public List<TacticsBean> searchTacticses(List<Filter> filters, int start,
			int limit) {
		List<TacticsBean> tacticsList = null;

		SelectParam sp = getSelectParam(filters, null, null);
		String limitByCluase = getLimitClause(start + "," + limit);

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();

			String sql = "select * from `cs_mp_tactics`" + sp.getWhereClause()
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
			tacticsList = new ArrayList<TacticsBean>();
			while (rs.next()) {
				TacticsBean tactics = new TacticsBean();
				tactics.setTacticsId(rs.getLong("tactics_id"));
				tactics.setTacticsName(rs.getString("tactics_name"));
				tactics.setStatus(rs.getInt("status"));
				tactics.setJarPath(rs.getString("jar_path"));
				tactics.setClassName(rs.getString("class_name"));
				tactics.setCreateTime(rs.getString("create_time"));
				tactics.setDescribe(rs.getString("describe"));

				tacticsList.add(tactics);
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

		return tacticsList;
	}

	@Override
	public List<TacticsBean> searchTacticsesOptions() {
		List<TacticsBean> tacticsList = null;

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();

			// 如果查询的策略名为空就查询所有
			String sql = "select `tactics_id`,`tactics_name`,`status` from `cs_mp_tactics`";
			stat = conn.prepareStatement(sql);

			rs = stat.executeQuery();
			tacticsList = new ArrayList<TacticsBean>();
			while (rs.next()) {
				TacticsBean tactics = new TacticsBean();
				tactics.setTacticsId(rs.getLong("tactics_id"));
				tactics.setTacticsName(rs.getString("tactics_name"));
				tactics.setStatus(rs.getInt("status"));

				tacticsList.add(tactics);
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

		return tacticsList;
	}

	@Override
	public long searchTacticsCount(List<Filter> filters) {
		SelectParam sp = getSelectParam(filters, null, null);

		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();

			String sql = "select count(*) from `cs_mp_tactics` "
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
			logger.error("查询策略数量时异常了···", e);
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
	public int insertTactics(TacticsBean tactics) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "insert into `cs_mp_tactics`(`tactics_name`,`jar_path`,`class_name`,`describe`) values(?,?,?,?)";
			stat = conn.prepareStatement(sql);
			stat.setString(1, tactics.getTacticsName());
			stat.setString(2, tactics.getJarPath());
			stat.setString(3, tactics.getClassName());
			stat.setString(4, tactics.getDescribe());

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("插入策略时异常了···" + e.getMessage());
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
	public int deleteTactics(long tacticsId) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "delete from `cs_mp_tactics` where `tactics_id` = ?";
			stat = conn.prepareStatement(sql);
			stat.setLong(1, tacticsId);

			return stat.executeUpdate();
		} catch (Exception e) {
			logger.error("删除策略时异常了···", e);
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
	public int updateTactics(long tacticsId, int status) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = dataSource.getConnection();
			String sql = "update `cs_mp_tactics` set `status` = ? where `tactics_id` = ?";
			stat = conn.prepareStatement(sql);
			stat.setInt(1, status);
			stat.setLong(2, tacticsId);

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
}
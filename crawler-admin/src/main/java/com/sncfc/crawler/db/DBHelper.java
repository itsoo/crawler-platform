package com.sncfc.crawler.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class DBHelper {
	private final static Logger logger = Logger.getLogger(DBHelper.class);

	private DBHelper() {
	}

	public static int execute(Connection conn, String sql) throws SQLException {
		return execute(conn, sql, null);
	}

	public static int execute(Connection conn, String sql, String[] params)
			throws SQLException {
		PreparedStatement stat = null;
		logger.debug("execute,sql=" + sql);
		stat = conn.prepareStatement(sql);
		if (params != null) {
			int index = 1;
			for (String param : params) {
				stat.setString(index++, param);
			}
		}
		return stat.executeUpdate();
	}

	public static int queryForInt(Connection conn, String sql)
			throws SQLException {
		return queryForInt(conn, sql, null);
	}

	public static int queryForInt(Connection conn, String sql, String[] params)
			throws SQLException {
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			logger.debug("query,sql=" + sql);
			stat = conn.prepareStatement(sql);
			if (params != null) {
				int index = 1;
				for (String param : params) {
					stat.setString(index++, param);
				}
			}
			rs = stat.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
		} finally {
			if (stat != null) {
				stat.close();
			}
			if (rs != null) {
				rs.close();
			}
		}

		return -1;
	}

	public static long queryForLong(Connection conn, String sql, String[] params)
			throws SQLException {
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			logger.debug("query,sql=" + sql);
			stat = conn.prepareStatement(sql);
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
		} finally {
			if (stat != null) {
				stat.close();
			}
			if (rs != null) {
				rs.close();
			}
		}

		return -1;
	}

	public static Map<String, String> queryForMap(Connection conn, String sql)
			throws SQLException {
		return queryForMap(conn, sql, null);
	}

	public static Map<String, String> queryForMap(Connection conn, String sql,
			String[] params) throws SQLException {
		List<Map<String, String>> list = query(conn, sql, params);
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	public static List<Map<String, String>> query(Connection conn, String sql)
			throws SQLException {
		return query(conn, sql, null);
	}

	public static List<Map<String, String>> query(Connection conn, String sql,
			String[] params) throws SQLException {
		List<Map<String, String>> datas = new ArrayList<Map<String, String>>();
		ResultSet rs = null;
		PreparedStatement stat = null;
		try {
			// rs = getResultSet(conn, sql, params);
			logger.debug("query,sql=" + sql);
			stat = conn.prepareStatement(sql);
			if (params != null) {
				int index = 1;
				for (String param : params) {
					stat.setString(index++, param);
				}
			}
			rs = stat.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();

			while (rs.next()) {
				Map<String, String> data = new HashMap<String, String>();
				for (int i = 1; i <= columnCount; i++) {
					if (rs.getString(i) != null) {
						data.put(metaData.getColumnLabel(i), rs.getString(i));
					}
				}
				datas.add(data);
			}
		} finally {
			if (stat != null) {
				stat.close();
			}
			if (rs != null) {
				rs.close();
			}
		}
		return datas;
	}
}

package com.sncfc.crawler.store.dao.impl;

import com.sncfc.crawler.store.bean.ResultInfo;
import com.sncfc.crawler.store.dao.IStoreDao;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OracleStoreDao implements IStoreDao {
    private static final Logger logger = Logger.getLogger(OracleStoreDao.class);

    private DataSource dataSource;

    public OracleStoreDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insertTable(String table, List<Map<String, String>> datas) {
        if (datas == null || datas.size() == 0) {
            return 0;
        }
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            int result = 0;
            for (Map<String, String> map : datas) {
                result += this.insertTable(conn, table, map);
            }
            conn.commit();
            return result;
        } catch (Exception e) {
            logger.error("insert错误:", e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("关闭数据库连接的时候出现异常了", e);
            }
        }
        return -1;
    }

    public int updateTable(String table, List<Map<String, String>> datas,
                           String[] updateKeys) {
        if (datas == null || datas.size() == 0) {
            return 0;
        }
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            int result = this.updateTable(conn, table, datas, updateKeys);
            conn.commit();
            return result;
        } catch (Exception e) {
            logger.error("update错误:" + e.getLocalizedMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            } catch (SQLException e) {
                // 关闭数据库连接的时候出现异常了
                logger.error("关闭数据库连接的时候出现异常了:" + e.getLocalizedMessage());
            }
        }
        return -1;
    }

    private int updateTable(Connection conn, String table,
                            List<Map<String, String>> datas, String[] updateKeys)
            throws SQLException {
        int result = 0;
        for (Map<String, String> map : datas) {
            result += this.updateTable(conn, table, map, updateKeys);
        }
        return result;
    }

    private int updateTable(Connection conn, String table,
                            Map<String, String> data, String[] updateKeys) throws SQLException {
        if (data == null) {
            return 0;
        }

        String where = "";
        String[] params = new String[updateKeys.length];
        for (int i = 0; i < updateKeys.length; i++) {
            where += updateKeys[i] + "=?";
            params[i] = data.remove(updateKeys[i]);
            if (i < updateKeys.length - 1) {
                where += " and ";
            }
        }

        return this.updateTable(conn, table, where, params, data);
    }

    private int updateTable(Connection conn, String table, String whereClause,
                            String[] whereParams, Map<String, String> data) throws SQLException {
        if (data == null) {
            return 0;
        }

        if (!(whereClause == null || whereClause.equals(""))) {
            whereClause = " where " + whereClause;
        }

        String[] params = new String[whereParams.length + data.keySet().size()];

        String columnStr = " set ";
        int index = 0;
        for (String key : data.keySet()) {
            columnStr += key + "=?,";
            params[index++] = data.get(key);
        }
        if (whereParams != null) {
            for (String param : whereParams) {
                params[index++] = param;
            }
        }
        columnStr = columnStr.substring(0, columnStr.length() - 1);
        String sql = "update " + table + columnStr + whereClause;

        return execute(conn, sql, params);

    }

    private int insertTable(Connection conn, String table,
                            Map<String, String> data) throws SQLException {
        if (data == null) {
            return 0;
        }

        String columnStr = "";
        String valueStr = "";
        String[] params = new String[data.keySet().size()];
        int index = 0;
        for (String key : data.keySet()) {
            columnStr += key + ",";
            valueStr += "?,";
            params[index++] = data.get(key);
        }
        columnStr = columnStr.substring(0, columnStr.length() - 1);
        valueStr = valueStr.substring(0, valueStr.length() - 1);

        String sql = "insert into " + table + "(" + columnStr + ")"
                + " values (" + valueStr + ")";

        return execute(conn, sql, params);
    }

    private int execute(Connection conn, String sql, String[] params) {
        PreparedStatement stat = null;
        try {
            stat = conn.prepareStatement(sql);

            if (params != null) {
                int index = 1;
                for (String param : params) {
                    stat.setString(index++, param);
                }
            }
            return stat.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭预编译
            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return -1;
    }

    public int insertErrorInfo(ResultInfo resultInfo) {
        Connection conn = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();
            String sql = "insert into SC_ERROR_INFO(CODE,TASK_ID,RESULT_TYPE,URL_PATH,TABLE_NAME,DESCRIBE) values(?,?,?,?,?,?)";
            stat = conn.prepareStatement(sql);
            stat.setInt(1, resultInfo.getCode());
            stat.setLong(2, resultInfo.getTaskId());
            stat.setInt(3, resultInfo.getResultType());
            stat.setString(4, resultInfo.getUrlPath());
            stat.setString(5, resultInfo.getTableName());
            stat.setString(6, resultInfo.getDesc());

            return stat.executeUpdate();
        } catch (Exception e) {
            logger.error("插入采集错误信息时异常了···" + e.getMessage());
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

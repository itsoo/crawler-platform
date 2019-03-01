package com.sncfc.crawler.manage.task.dao.impl;

import com.sncfc.crawler.manage.bean.TaskDetailBean;
import com.sncfc.crawler.manage.bean.TaskParamInfo;
import com.sncfc.crawler.manage.task.dao.ITaskDetailDao;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OracleTaskDetailDao implements ITaskDetailDao {
    private static final Logger logger = Logger
            .getLogger(OracleTaskDetailDao.class);
    /**
     * 多页任务中，从数据库读取参数的基本SQL语句
     */
    private static final String TASK_PARAMS_BASE_SQL = "select t.* from (select t1.*,rownum num from(${sql}) t1 where rownum <= ?) t where num > ?";

    private DataSource dataSource;

    public OracleTaskDetailDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

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

    public int updateTaskDetail(long taskId, String stoopFloor) {
        Connection conn = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();
            String sql = "update SC_TASK_DETAIL set STOP_FLOOR=? where TASK_ID = ?";
            stat = conn.prepareStatement(sql);
            stat.setString(1, stoopFloor);
            stat.setLong(2, taskId);

            return stat.executeUpdate();
        } catch (Exception e) {
            logger.error("更新策略stop_floor字段时异常了···", e);
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

    public List<Map<String, String>> getParamsFromBD(
            TaskParamInfo taskParamInfo, int limit) {
        List<Map<String, String>> datas = new ArrayList<Map<String, String>>();

        Connection conn = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = TASK_PARAMS_BASE_SQL.replace("${sql}", taskParamInfo.getValue());
            stat = conn.prepareStatement(sql);
            stat.setInt(1, taskParamInfo.getFrom() + limit);
            stat.setInt(2, taskParamInfo.getFrom());

            rs = stat.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, String> data = new HashMap<String, String>();
                for (int i = 1; i <= columnCount; i++) {
                    if (rs.getString(i) != null) {
                        data.put("${" + metaData.getColumnLabel(i) + "}",
                                rs.getString(i));
                    }
                }
                datas.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

        return datas;
    }

    public List<Map<String, String>> getAksFromBD(String sql) {
        List<Map<String, String>> datas = new ArrayList<Map<String, String>>();

        Connection conn = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stat = conn.prepareStatement(sql);

            rs = stat.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, String> data = new HashMap<String, String>();
                for (int i = 1; i <= columnCount; i++) {
                    if (rs.getString(i) != null) {
                        data.put("${" + metaData.getColumnLabel(i) + "}",
                                rs.getString(i));
                    }
                }
                datas.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

        return datas;
    }
}
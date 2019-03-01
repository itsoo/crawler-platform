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

public class MysqlTaskDetailDao implements ITaskDetailDao {
    private static final Logger logger = Logger
            .getLogger(MysqlTaskDetailDao.class);
    /**
     * 多页任务中，从数据库读取参数的基本SQL语句
     */
    private static final String TASK_PARAMS_BASE_SQL = "select * from(${sql}) t limit ?,?";

    private DataSource dataSource;

    public MysqlTaskDetailDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

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

    public int updateTaskDetail(long taskId, String stoopFloor) {
        Connection conn = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();
            String sql = "update cs_mp_task_detail set stop_floor=? where task_id = ?";
            stat = conn.prepareStatement(sql);
            stat.setString(1, stoopFloor);
            stat.setLong(2, taskId);

            return stat.executeUpdate();
        } catch (Exception e) {
            logger.error("更新策略stop_floor字段时异常了···", e);
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

    public List<Map<String, String>> getParamsFromBD(
            TaskParamInfo taskParamInfo, int limit) {
        List<Map<String, String>> datas = new ArrayList<Map<String, String>>();

        Connection conn = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = TASK_PARAMS_BASE_SQL.replace("${sql}",
                    taskParamInfo.getValue());
            stat = conn.prepareStatement(sql);
            stat.setInt(1, taskParamInfo.getFrom());
            stat.setInt(2, limit);

            rs = stat.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            while (rs.next()) {
                HashMap<String, String> data = new HashMap<String, String>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
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

        return datas;
    }

    public List<Map<String, String>> getAksFromBD(String sql) {
        ArrayList<Map<String, String>> datas = new ArrayList<Map<String, String>>();

        Connection conn = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stat = conn.prepareStatement(sql);

            rs = stat.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();

            while (rs.next()) {
                HashMap<String, String> data = new HashMap<String, String>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
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

        return datas;
    }
}
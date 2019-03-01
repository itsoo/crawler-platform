package com.sncfc.crawler.task.dao.impl;

import com.sncfc.crawler.bean.TaskBean;
import com.sncfc.crawler.bean.UpdateTaskInfo;
import com.sncfc.crawler.db.Filter;
import com.sncfc.crawler.db.OracleBaseTableDao;
import com.sncfc.crawler.db.SelectParam;
import com.sncfc.crawler.task.dao.ITaskDao;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OracleTaskDao extends OracleBaseTableDao implements ITaskDao {
    private static final Logger logger = Logger.getLogger(OracleTaskDao.class);

    public OracleTaskDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public UpdateTaskInfo searchUpdateTaskInfoById(Long taskId) {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();

            String sql = "select a.TASK_ID,a.TASK_TYPE,a.CIRCULATION,a.STATUS,b.START_TIME,b.END_TIME from SC_TASK a join SC_TASK_DETAIL b on a.TASK_ID=b.TASK_ID and a.TASK_ID=?";

            stat = conn.prepareStatement(sql);
            stat.setLong(1, taskId);

            rs = stat.executeQuery();
            if (rs.next()) {
                UpdateTaskInfo task = new UpdateTaskInfo();
                task.setTaskId(rs.getLong("TASK_ID"));
                task.setTaskType(rs.getInt("TASK_TYPE"));
                task.setCirculation(rs.getInt("CIRCULATION"));
                task.setStatus(rs.getInt("STATUS"));
                task.setStartTime(rs.getString("START_TIME"));
                task.setEndTime(rs.getString("END_TIME"));

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

            String sql = "select a.TASK_ID,a.TASK_TYPE,a.CIRCULATION,a.STATUS,b.START_TIME,b.END_TIME from SC_TASK a join SC_TASK_DETAIL b on a.TASK_ID=b.TASK_ID and a.STATUS=?";

            stat = conn.prepareStatement(sql);
            stat.setInt(1, status);

            rs = stat.executeQuery();
            while (rs.next()) {
                UpdateTaskInfo task = new UpdateTaskInfo();
                task.setTaskId(rs.getLong("TASK_ID"));
                task.setTaskType(rs.getInt("TASK_TYPE"));
                task.setCirculation(rs.getInt("CIRCULATION"));
                task.setStatus(rs.getInt("STATUS"));
                task.setStartTime(rs.getString("START_TIME"));
                task.setEndTime(rs.getString("END_TIME"));

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

        return updateTaskList;
    }

    @Override
    public List<TaskBean> searchTask(List<Filter> filters, Integer start,
                                     Integer limit) {
        List<TaskBean> taskList = null;

        SelectParam sp = getSelectParam(filters, null, null);

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();

            String sql = "select * from SC_TASK" + sp.getWhereClause()+ " ORDER BY TASK_ID";
            String finalSql = BSAE_QUERY_SQL.replace("${sql}", sql);

            stat = conn.prepareStatement(finalSql);

            String[] params = sp.getParams();
            int index = 1;
            if (params != null) {
                for (String param : params) {
                    stat.setString(index++, param);
                }
            }
            stat.setInt(index++, start + limit);
            stat.setInt(index++, start);

            rs = stat.executeQuery();
            taskList = new ArrayList<TaskBean>();
            while (rs.next()) {
                TaskBean task = new TaskBean();
                task.setTaskId(rs.getLong("TASK_ID"));
                task.setTagId(rs.getLong("TAG_ID"));
                task.setTaskName(rs.getString("TASK_NAME"));
                task.setTaskType(rs.getInt("TASK_TYPE"));
                task.setCreateTime(rs.getString("CREATE_TIME"));
                task.setCirculation(rs.getInt("CIRCULATION"));
                task.setStatus(rs.getInt("STATUS"));
                task.setDescribe(rs.getString("DESCRIBE"));

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

            String sql = "select count(*) from SC_TASK "
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
    public int insertTask(TaskBean task) {
        Connection conn = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();
            String sql = "insert into SC_TASK(TASK_ID,TAG_ID,TASK_NAME,TASK_TYPE,CIRCULATION,STATUS,DESCRIBE) values(SEQ_SC_TASK.NEXTVAL,?,?,?,?,?,?)";
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
    public int deleteTask(Long taskId) {
        Connection conn = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();
            String sql = "delete from SC_TASK where TASK_ID=?";
            stat = conn.prepareStatement(sql);
            stat.setLong(1, taskId);

            return stat.executeUpdate();
        } catch (Exception e) {
            logger.error("删除任务时异常了···", e);
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
    public int updateTaskStatus(Long taskId, Integer status) {
        Connection conn = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();
            String sql = "update SC_TASK set STATUS=? where TASK_ID=?";
            stat = conn.prepareStatement(sql);
            stat.setInt(1, status);
            stat.setLong(2, taskId);

            return stat.executeUpdate();
        } catch (Exception e) {
            logger.error("更新任务状态时异常了···", e);
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
    public int updateTaskDesc(Long taskId, String desc) {
        Connection conn = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();
            String sql = "update SC_TASK set DESCRIBE=? where TASK_ID=?";
            stat = conn.prepareStatement(sql);
            stat.setString(1, desc);
            stat.setLong(2, taskId);

            return stat.executeUpdate();
        } catch (Exception e) {
            logger.error("更新任务状态时异常了···", e);
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

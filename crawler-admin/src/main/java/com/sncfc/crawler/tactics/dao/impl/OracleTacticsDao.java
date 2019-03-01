package com.sncfc.crawler.tactics.dao.impl;

import com.sncfc.crawler.bean.TacticsBean;
import com.sncfc.crawler.db.Filter;
import com.sncfc.crawler.db.OracleBaseTableDao;
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

public class OracleTacticsDao extends OracleBaseTableDao implements ITacticsDao {
    private static final Logger logger = Logger
            .getLogger(OracleTacticsDao.class);

    public OracleTacticsDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<TacticsBean> searchTacticses(List<Filter> filters, int start,
                                             int limit) {
        List<TacticsBean> tacticsList = new ArrayList<TacticsBean>();

        SelectParam sp = getSelectParam(filters, null, null);

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();

            String sql = "select * from SC_TACTICS" + sp.getWhereClause() + " ORDER BY TACTICS_ID";
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
            while (rs.next()) {
                TacticsBean tactics = new TacticsBean();
                tactics.setTacticsId(rs.getLong("TACTICS_ID"));
                tactics.setTacticsName(rs.getString("TACTICS_NAME"));
                tactics.setStatus(rs.getInt("STATUS"));
                tactics.setJarPath(rs.getString("JAR_PATH"));
                tactics.setClassName(rs.getString("CLASS_NAME"));
                tactics.setCreateTime(rs.getString("CREATE_TIME"));
                tactics.setDescribe(rs.getString("DESCRIBE"));

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
            String sql = "select TACTICS_ID,TACTICS_NAME,STATUS from SC_TACTICS  ORDER BY TACTICS_ID";
            stat = conn.prepareStatement(sql);

            rs = stat.executeQuery();
            tacticsList = new ArrayList<TacticsBean>();
            while (rs.next()) {
                TacticsBean tactics = new TacticsBean();
                tactics.setTacticsId(rs.getLong("TACTICS_ID"));
                tactics.setTacticsName(rs.getString("TACTICS_NAME"));
                tactics.setStatus(rs.getInt("STATUS"));

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

            String sql = "select count(*) from SC_TACTICS "
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
    public int insertTactics(TacticsBean tactics) {
        Connection conn = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();
            String sql = "insert into SC_TACTICS(TACTICS_ID,TACTICS_NAME,JAR_PATH,CLASS_NAME,DESCRIBE) values(SEQ_SC_TACTICS.NEXTVAL,?,?,?,?)";
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
            String sql = "delete from SC_TACTICS where TACTICS_ID = ?";
            stat = conn.prepareStatement(sql);
            stat.setLong(1, tacticsId);

            return stat.executeUpdate();
        } catch (Exception e) {
            logger.error("删除策略时异常了···", e);
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
    public int updateTactics(long tacticsId, int status) {
        Connection conn = null;
        PreparedStatement stat = null;
        try {
            conn = dataSource.getConnection();
            String sql = "update SC_TACTICS set STATUS = ? where TACTICS_ID = ?";
            stat = conn.prepareStatement(sql);
            stat.setInt(1, status);
            stat.setLong(2, tacticsId);

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
}
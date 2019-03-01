package com.sncfc.crawler.db;

import javax.sql.DataSource;
import java.util.List;

public class OracleBaseTableDao {
    protected static final String BSAE_QUERY_SQL = "select t.* from (select t1.*,rownum num from(${sql}) t1 where rownum <= ?) t where num > ?";

    protected DataSource dataSource;

    public OracleBaseTableDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected SelectParam getSelectParam(List<Filter> filters, String where,
                                         String[] params) {
        SelectParam sp = Filter.getFilterParams(filters);
        sp.setWhereClause(getWhereClause(sp.getWhereClause(), where));
        sp.setParams(getParams(sp.getParams(), params));
        return sp;
    }

    private String getWhereClause(String filterWhere, String where) {
        String whereClause = "";
        if (where == null || where.equals("")) {
            if (!filterWhere.equals("")) whereClause = filterWhere;
        } else {
            if (!filterWhere.equals("")) {
                whereClause = filterWhere + " and " + where;
            } else {
                whereClause = " where " + where;
            }
        }
        return whereClause;
    }

    private String[] getParams(String[] filterParams, String[] params) {
        String[] newParams;
        if (params != null && params.length != 0) {
            newParams = new String[filterParams.length + params.length];
            int index = 0;
            for (String str : filterParams) {
                newParams[index++] = str;
            }
            for (String str : params) {
                newParams[index++] = str;
            }
        } else {
            newParams = filterParams;
        }
        return newParams;
    }

}

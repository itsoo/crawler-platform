package com.sncfc.crawler.db;

import java.util.ArrayList;
import java.util.List;

public class Filter {
	public final static int Op_Bigger = 1;
    public final static int Op_Equal = 2;
    public final static int Op_Smaller = 3;
    public final static int Op_Like = 4;
    public final static int Op_BiggerEqual = 5;
    public final static int Op_SmallerEqual = 6;
    public final static int Op_NotEqual = 7;
    public final static int Op_In = 8;
    public final static int Op_And = 9;
    public final static int Op_NotLike = 10;
    public final static int Op_Null = 11;
    public final static int Op_Between = 12;
    public final static int Op_NotIn = 13;

    //And, Or
    public final static int Relation_And = 1;
    public final static int Relation_Or = 2;

    //Other, Text, Date, ComboBox
    public final static int FieldType_Default = 0;
    public final static int FieldType_Text = 1;
    public final static int FieldType_Date = 2;
    public final static int FieldType_ComboBox = 3;
    public final static int FieldType_CheckBox = 4;
    public final static int FieldType_Int = 5;
    
    public final static int State_Enable = 1;
    public final static int State_Disable = 0;
    
    public final static int ValueType_Normal = 0;
    public final static int ValueType_Complex = 1;
 
    /**
     * 获取比较字符串
     * @param op
     * @return
     */
    public static String getOpStr(int op)
    {
        String comparationStr = "=";
        switch (op)
        {
            case Op_Bigger:
                comparationStr = " > ";
                break;
            case Op_Equal:
                comparationStr = " = ";
                break;
            case Op_NotEqual:
                comparationStr = " != ";
                break;
            case Op_Smaller:
                comparationStr = " < ";
                break;
            case Op_Like:
                comparationStr = " like ";
                break;
            case Op_NotLike:
                comparationStr = " not like ";
                break;
            case Op_BiggerEqual:
                comparationStr = " >= ";
                break;
            case Op_SmallerEqual:
                comparationStr = " <= ";
                break;
            case Op_In:
                comparationStr = " in ";
                break;
            case Op_NotIn:
                comparationStr = " not in ";
                break;
            case Op_And:
                comparationStr = " & ";
                break;
        }
        return comparationStr;
    }
    
    public static String getRelationStr(int relation)
    {
        switch (relation)
        {
            case Relation_And:
                return " and ";
            case Relation_Or:
                return " or ";
        }
        return "";
    }
    
    public static SelectParam getFilterParams(Filter filter) {
    	 SelectParam sqlParam = new SelectParam();
         List<String> sqliteParams = new ArrayList<String>();
         sqlParam.setWhereClause(getWhereClause(filter, sqliteParams));
         if (!sqlParam.getWhereClause().equals(""))
         {
             sqlParam.setWhereClause(" where " + sqlParam.getWhereClause());
         }
         sqlParam.setParams(sqliteParams.toArray(new String[0]));
         return sqlParam;
    }
    
    private static String getWhereClause(Filter filter, List<String> sqliteParams) {
    	if (filter == null) {
            return "";
        }
        String whereClause = "(";
        
        if (filter.isRoot()) {
        	whereClause += generateClause(filter, sqliteParams);
        } else {
        	if (filter.getSubFilters() != null ) {
        		int index = 0;
        		for (Filter item : filter.getSubFilters()) {
        			if (index++ != 0){
                        whereClause += getRelationStr(item.getRelation());
                    }
        			whereClause += getWhereClause(item, sqliteParams);	
				}
                
            }
        }
        whereClause = whereClause + ")";
        return whereClause;
    }
    
    public static SelectParam getFilterParams(List<Filter> filters)
    {
        SelectParam sqlParam = new SelectParam();
        List<String> sqliteParams = new ArrayList<String>();
        sqlParam.setWhereClause(getClause(filters, sqliteParams));
        if (!sqlParam.getWhereClause().equals(""))
        {
            sqlParam.setWhereClause(" where " + sqlParam.getWhereClause());
        }
        sqlParam.setParams(sqliteParams.toArray(new String[0]));
        return sqlParam;
    }

    /// <summary>
    /// 递归方式根据传入的filters生成where语句及sqliteParams
    /// </summary>
    /// <param name="filters"></param>
    /// <param name="sqliteParams"></param>
    /// <returns></returns>
    private static String getClause(List<Filter> filters, List<String> sqliteParams) {
        if (filters == null) {
            return "";
        }
        if (filters.size() == 0) {
            return "";
        }
        String whereClause = "(";
        int index = 0;
        for (Filter item : filters) {
        	if (item.getState() == Filter.State_Disable) continue;
            if (index++ != 0) {
                whereClause += getRelationStr(item.getRelation());
            }
            if (item.isRoot()) {
                whereClause += generateClause(item, sqliteParams);
            }
            else {
                if (item.getSubFilters() != null && item.getSubFilters().size() > 0) {
                    whereClause += getClause(item.getSubFilters(), sqliteParams);
                }
            }
            
        }
        return whereClause + ")";
    }

    public static String generateNormalClause(Filter filter, String value, List<String> sqliteParams) {
    	String sqlParam = null;
    	String comparationStr = getOpStr(filter.getOp());
        String clause = "";
        
        if (filter.getFieldType() == FieldType_Date) {
        	if (filter.getOp() == Op_Between) {
        		String[] strs = filter.getValue().split("\\|");
        		if (strs.length == 1) {
        			sqliteParams.add(strs[0]);
        			clause = filter.getDBColumnName() + " " + getOpStr(Op_BiggerEqual) + " ? ";
        		} else {
        			if (strs[0].equals("")) {
            			sqliteParams.add(strs[1] + " 23:59:59");
            			clause = filter.getDBColumnName() + " " + getOpStr(Op_SmallerEqual) + " ? ";
            		} else {
	        			sqliteParams.add(strs[0]);
	            		sqliteParams.add(strs[1] + " 23:59:59");
	            		clause = filter.getDBColumnName() + " between ? and ? ";
            		} 	
        		}
        	} else {
				sqlParam = filter.getOp() == Op_SmallerEqual ? value
						+ " 23:59:59" : value;
				sqliteParams.add(sqlParam);
				clause = filter.getDBColumnName() + " " + comparationStr + " ? ";
        	}
        } else if (filter.getOp() == Op_Between) {
    		String[] strs = filter.getValue().split("\\|");
    		if (strs.length == 1) {
    			sqliteParams.add(strs[0]);
    			clause = filter.getDBColumnName() + " " + getOpStr(Op_BiggerEqual) + " ? ";
    		} else {
    			if (strs[0].equals("")) {
        			sqliteParams.add(strs[1]);
        			clause = filter.getDBColumnName() + " " + getOpStr(Op_SmallerEqual) + " ? ";
        		} else {
        			sqliteParams.add(strs[0]);
            		sqliteParams.add(strs[1]);
            		clause = filter.getDBColumnName() + " between ? and ? ";
        		} 	
    		}
        } else {
        	if (filter.getOp() == Op_Like || filter.getOp() == Op_NotLike) {
                sqlParam = "%" + value.trim() + "%";
            } else {
                sqlParam = value.trim();
            }
        	if (filter.getOp() == Op_In || filter.getOp() == Op_NotIn) {
//            	String params = "";
            	StringBuffer sb = new StringBuffer();
            	sb.append(filter.getDBColumnName() + " " + comparationStr + "  (");
            	boolean first = true;
            	for (String str : value.split(",")) {
            		sqliteParams.add(str);
            		if (first) {
            			sb.append("?");
            			first = false;
            		} else {
            			sb.append(",?");	
            		}
    			}
            	sb.append(")");
            	clause = sb.toString();
            } else if (filter.getOp() == Op_Null) {
            	clause = filter.getDBColumnName() + " is null ";
            } else if (filter.getOp() == Op_And) {
            	clause = "(" + filter.getDBColumnName() + " " + comparationStr + " ? > 0 )";
            	sqliteParams.add(sqlParam);
            } else {
            	clause = filter.getDBColumnName() + " " + comparationStr + " ? ";
            	sqliteParams.add(sqlParam);
            }
        }
        
        if (filter.isReverse()) {
        	return "not " + clause;
        }
        
        return clause;
    }
    
    /// <summary>
    /// 生成根节点的where语句
    /// </summary>
    /// <param name="filter"></param>
    /// <param name="sqliteParams"></param>
    /// <returns></returns>
    private static String generateClause(Filter filter, List<String> sqliteParams) {

        int index = sqliteParams.size();

        if (filter.getValueType() == Filter.ValueType_Complex) {
            SqlParamsComplexProcess process = new SqlParamsComplexProcess(filter, index);
            ComplexValueConverter customFilterConvertor = new ComplexValueConverter(process);
            customFilterConvertor.convert(filter.getValue());
            sqliteParams.addAll(process.SqlParams);
            
            return "(" + process.Result + ")";
        }
        else {
        	return generateNormalClause(filter, filter.getValue(), sqliteParams);
        }
        
    }
    
    public boolean satify(String value) {
    	if (this.valueType == ValueType_Complex) {
    		SatifyComplexProcess f = new SatifyComplexProcess(value);
            ComplexValueConverter c = new ComplexValueConverter(f);
            c.convert(this.getValue());
            return f.Result;
    	} else {
    		switch (this.op) {
			case Op_Equal:
				return this.value.equals(value);
			case Op_In:
				return ("," + this.value + ",").contains("," + value + ",");
			case Op_NotIn:
				return !("," + this.value + ",").contains("," + value + ",");
			case Op_And:
				return (Integer.parseInt(this.value) & Integer.parseInt(value)) > 0;
			case Op_Between:
				String[] strs = this.value.split("\\|");
        		if (strs.length == 1) {
        			return Double.parseDouble(strs[0]) <= Double.parseDouble(value);
        		} else {
        			if (strs[0].equals("")) {
        				return Double.parseDouble(strs[1]) >= Double.parseDouble(value);
            		} else {
            			return Double.parseDouble(strs[0]) <= Double.parseDouble(value) &&
            					Double.parseDouble(strs[1]) >= Double.parseDouble(value);
            		} 	
        		}
			default:
				break;
			}
    	}
        return false;
    }
    
	private int id;
	private String field;
	private boolean columnComplex;
	private String columnName;
	private int op;
	private String value;
	private int valueType;
	private int fieldType = FieldType_Default;
	private String tableName;
	private boolean root = true;
	private boolean reverse = false;
	private int relation = Relation_And;
	private int state = State_Enable;
	private List<Filter> subFilters;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public int getOp() {
		return op;
	}
	public void setOp(int op) {
		this.op = op;
	}
	
	
	public boolean isColumnComplex() {
		return columnComplex;
	}

	public void setColumnComplex(boolean columnComplex) {
		this.columnComplex = columnComplex;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public int getValueType() {
		return valueType;
	}

	public void setValueType(int valueType) {
		this.valueType = valueType;
	}

	public int getFieldType() {
		return fieldType;
	}
	public void setFieldType(int fieldType) {
		this.fieldType = fieldType;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public List<Filter> getSubFilters() {
		return subFilters;
	}
	public void setSubFilters(List<Filter> subFilters) {
		this.subFilters = subFilters;
	}
	public boolean isRoot() {
		return root;
	}
	public void setRoot(boolean root) {
		this.root = root;
	}
	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public int getRelation() {
		return relation;
	}
	public void setRelation(int relation) {
		this.relation = relation;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public String getDBColumnName() {
		if (columnComplex) {
			return columnName;
		}
		if (this.tableName != null && !this.tableName.equals("")) {
			return this.tableName + "." + columnName;
		}
		return columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	
	
}
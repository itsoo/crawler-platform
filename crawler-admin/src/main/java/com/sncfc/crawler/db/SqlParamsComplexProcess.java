package com.sncfc.crawler.db;

import java.util.ArrayList;
import java.util.List;

public class SqlParamsComplexProcess extends BaseComplexProcess implements
		IComplexProcess {
	public String Result;
	public String WhereClause = "";
	public List<String> SqlParams = new ArrayList<String>();
	Filter filter;
	String comparationStr = "";
	int rIndex;

	public SqlParamsComplexProcess(Filter filter, int startIndex) {
		Result = "";

		rIndex = startIndex;
		this.filter = filter;
		this.comparationStr = Filter.getOpStr(filter.getOp());
	}

	public void reset() {
		Result = "";
	}

	public void begin(boolean reverse, int relation) {
		super.begin(reverse, relation);
		String not = reverse ? "not" : "";
		if (relation == Filter.Relation_And) {
			Result += " and " + not + " (";
		} else if (relation == Filter.Relation_Or) {
			Result += " or " + not + " (";
		} else {
			Result += not + "(";
		}
	}

	public void end(boolean reverse) {
		super.end(reverse);
		Result += ")";
	}

	public boolean process(boolean reverse, int relation, String value) {
		super.process(reverse, relation, value);
		if (relation == Filter.Relation_And) {
			Result += " and ";
		} else if (relation == Filter.Relation_Or) {
			Result += " or ";
		}

		List<String> params = new ArrayList<String>();
		Result += Filter.generateNormalClause(filter, value, params);

		SqlParams.addAll(params);

		rIndex++;
		return false;
	}
}

package com.sncfc.crawler.db;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class SatifyComplexProcess extends BaseComplexProcess implements IComplexProcess{
	boolean outFlag = false;

	Stack<Boolean> resultStack = new Stack<Boolean>();
	Stack<Boolean> reverseStack = new Stack<Boolean>();
	Set<String> containSet = new HashSet<String>();

	public boolean Result = false;

	String str = "";

	public SatifyComplexProcess(String str) {
		this.str = str.toLowerCase();
	}

	public void reset(String str) {
		this.str = str;
		Result = false;
		outFlag = false;
		resultStack.clear();
		reverseStack.clear();
	}

	public boolean goout() {
		if (outFlag) {
			outFlag = false;
			return true;
		}
		return false;
	}

	int deep;
	int lastDeep;

	boolean lastResult = false;

	public void nextRelation(boolean reverse, int relation) {
		if (relation == Filter.Relation_Or) {
			if (resultStack.pop()) {
				outFlag = true;
				resultStack.push(true);
			}
		} else {
			if (!(boolean) resultStack.pop()) {
				outFlag = true;
				resultStack.push(false);
			}
		}
	}

	public void begin(boolean reverse, int relation) {
		// reverseStack.Push(reverse);
	}

	public void end(boolean reverse) {
		// boolean result = (boolean)deepStack.Pop();
		// deepStack.Push(result);
		if (reverse) {
			resultStack.push(!resultStack.pop());
		}

	}

	public boolean process(boolean reverse, int relation, String value) {
		boolean result = str.contains(value);

		if (result) {
			containSet.add(value);
		}
		// resultStack.Push((boolean)(resultStack.Pop()) && (reverse ? !result :
		// result));
		resultStack.push(reverse ? !result : result);
		return true;

	}

	public void processStart() {
		// resultStack.Push(true);
	}

	public void processEnd() {
		if (resultStack.size() > 0) {
			Result = resultStack.pop();
		}

	}
}

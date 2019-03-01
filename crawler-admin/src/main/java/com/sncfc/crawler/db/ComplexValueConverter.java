package com.sncfc.crawler.db;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class ComplexValueConverter {
	private final static Logger logger = Logger.getLogger(ComplexValueConverter.class);

//	private static Pattern p = Pattern.compile("[)(+|-]");
	private static Pattern p = Pattern.compile("\\[|\\(|\\)|\\+|-|\\||\\]");


	int start = 0;
	int end;
	String str;
	Stack<Integer> kuohaoStack = new Stack<Integer>();

	private IComplexProcess process;

	public ComplexValueConverter(IComplexProcess process) {
		this.process = process;
	}

	public void convert(String s) {
		this.str = s.toLowerCase()
				.replaceAll("\\[", "(")
				.replaceAll("【", "(")
				.replaceAll("】", ")")
				.replaceAll("\\]", ")")
				.replaceAll("（", "(")
				.replaceAll("－", "-")
				.replaceAll("＋", "+")
				.replaceAll("）", ")")
				.replaceAll("｜", "|");
		this.start = 0;
		this.getFilterStr();
		process.processEnd();
	}

	private int findSubEnd(int index, String str) {
//		int start = str.indexOf('(', index);
		if (kuohaoStack.isEmpty()) {
			return str.length();
		}
		int start = kuohaoStack.pop();
		int end = str.indexOf(')', index);
		
		int count = 1;
		int left = 0;
		int right = 0;
		while (count > 0) {
			left = str.indexOf("(", start + 1);
			right = str.indexOf(")", start + 1);
			if (right < left || left == -1) {
				start = right;
				count--;
			} else {
				start = left;
				count++;
			}
			
		}
		
		
		end = right;
		
		
//		while (end > start && start != -1) {
//
//			start = str.indexOf('(', end + 1);
//			end = str.indexOf(')', end + 1);
//
//		}
		// if (end == -1)
		// {
		// return str.Length;
		// }
		return end + 1;
	}

	private void getFilterStr() {
		String first = "";
		boolean flag = true;
		int currentRelation = 0;
		boolean reverse = false;
		while (flag) {
			
			if (process.goout()) {
				logger.debug("go out ----------------------:" + start);
				start = findSubEnd(start, str);
				break;
			}
			//将==改为>=
			if (start >= str.length()) {
				break;
			}

			first = str.substring(start, start + 1);

			logger.debug("word:" + first);
			
			if (first.equals("(")) {
				logger.debug("go in start:" + start);
				kuohaoStack.push(start);
				start += 1;
				logger.debug("begin:" + start);
				process.begin(reverse, currentRelation);
				this.getFilterStr();
				process.end(reverse);
				reverse = false;
				currentRelation = 999;
			} else if (first.equals("+")) {
				start += 1;
				currentRelation = Filter.Relation_And;
				process.nextRelation(reverse, currentRelation);
			} else if (first.equals("|")) {
				start += 1;
				currentRelation = Filter.Relation_Or;
				process.nextRelation(reverse, currentRelation);
			} else if (first.equals("-")) {
				start += 1;

				// if (currentRelation == 0)
				// {

				// currentRelation = Filter.Relation_And;
				// }
				if (currentRelation == 999) {
					currentRelation = Filter.Relation_And;
					// process.nextRelation(reverse, currentRelation);
				} else {
					// process.nextRelation(reverse, currentRelation);
				}
				reverse = true;
			} else if (first.equals(")")) {
				start += 1;
				kuohaoStack.pop();
//				logger.debug("end:" + start + "," + str.charAt(start));
				flag = false;
			} else {
				Matcher m = p.matcher(str.substring(start));
				String value = "";

				if (m.find()) {

					end = m.start();

					value = str.substring(start, start + end);
					// System.Console.WriteLine(value);
					start = start + end;
				} else {
					value = str.substring(start);

					start = str.length();

				}

				process.process(reverse, currentRelation, value);

				reverse = false;
				currentRelation = 999;
			}
		}
	} // end method
	
	public static void main(String[] args) {
		String a = "|";
		System.out.println(a);
		System.out.println("|".equals(a));
		
		String ab = "ICBC就在你身边".toLowerCase();
		
		System.out.println(ab);
		
		
		
		
		
	}
}

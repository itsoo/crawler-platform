package com.sncfc.crawler.util;

import java.text.ParseException;
import java.util.Date;

import org.quartz.CronExpression;
import org.springframework.util.StringUtils;

public class TimeUtils {

	public static boolean isDealTime(String startTime, String endTime) {
		try {
			// 得到当天凌晨时的Date：dayStart
			long now = System.currentTimeMillis() / 1000l;
			long daySecond = 60 * 60 * 24;
			long dayTime = now - (now + 8 * 3600) % daySecond;
			Date dayStart = new Date(dayTime * 1000);

			// 当前时间
			Date curDate = new Date();

			// 先处理开始时间
			boolean afterStartTime = false;
			if (StringUtils.isEmpty(startTime)) {
				afterStartTime = true;
			} else {
				CronExpression startExpression = new CronExpression(startTime);
				// 找到当天凌晨之后的第一个满足的开始时间
				Date nextDate = startExpression.getNextValidTimeAfter(dayStart);
				// 判断当前时间是否处于开始时间之后
				afterStartTime = curDate.after(nextDate);
			}

			// 再处理结束时间
			boolean beforeEndTime = false;
			if (StringUtils.isEmpty(endTime)) {
				beforeEndTime = true;
			} else {
				CronExpression endExpression = new CronExpression(endTime);
				// 找到当天凌晨之后的第一个满足的结束时间
				Date endDate = endExpression.getNextValidTimeAfter(dayStart);
				// 判断任务是否处于结束时间之前
				beforeEndTime = curDate.before(endDate);
			}

			return afterStartTime && beforeEndTime;
		} catch (ParseException e) {
			return false;
		}
	}
}

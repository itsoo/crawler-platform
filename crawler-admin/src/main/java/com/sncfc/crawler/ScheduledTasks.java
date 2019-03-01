package com.sncfc.crawler;

import com.sncfc.crawler.bean.MessageInfo;
import com.sncfc.crawler.bean.UpdateTaskInfo;
import com.sncfc.crawler.task.dao.ITaskDao;
import com.sncfc.crawler.task.service.TaskService;
import com.sncfc.crawler.util.Commons;
import com.sncfc.crawler.util.TimeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledTasks {
	private static final Logger logger = Logger.getLogger(ScheduledTasks.class);

	@Autowired
	private ITaskDao taskDao;

	@Autowired
	private TaskService taskService;

	/**
	 * 每十分钟轮询一次
	 */
	@Scheduled(fixedDelay = 600000)
	public void startMonitor() {
		logger.info("每隔十分钟检查一次状态为“启用”(status=1)的定时任务");

		synchronized (taskService.LOCK) {
			List<UpdateTaskInfo> taskList = taskService
					.searchUpdateTaskInfoByStatus(Commons.TASK_STATUS_START);

			for (UpdateTaskInfo task : taskList) {
				dealNewTask(task);
			}
		}
	}

	private void dealNewTask(UpdateTaskInfo updateTask) {
		// 如果任务满足了时间策略，就将任务“上线”
		if (TimeUtils.isDealTime(updateTask.getStartTime(),
				updateTask.getEndTime())) {

			long taskId = updateTask.getTaskId();
			MessageInfo message = taskService.startTaskToNode(updateTask);

			if (message.getCode() < 0) {
				logger.error(message.getMsg());
			} else {
				int count = taskDao.updateTaskStatus(taskId,
						Commons.TASK_STATUS_EXECUTORY);
				if (count < 0) {
					logger.error("当前任务已“上线”，但将任务的状态修改为“执行中”时失败了，taskId="
							+ taskId);
				}

			}
		}
	}

}
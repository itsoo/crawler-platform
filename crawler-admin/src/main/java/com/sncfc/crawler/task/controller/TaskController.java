package com.sncfc.crawler.task.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.reflect.TypeToken;
import com.sncfc.crawler.bean.MessageInfo;
import com.sncfc.crawler.bean.Result;
import com.sncfc.crawler.bean.TaskBean;
import com.sncfc.crawler.bean.TaskDetailBean;
import com.sncfc.crawler.bean.TaskTagBean;
import com.sncfc.crawler.db.Filter;
import com.sncfc.crawler.task.service.TaskService;
import com.sncfc.crawler.util.Commons;
import com.sncfc.crawler.util.GsonUtils;

@RestController
@RequestMapping("task")
public class TaskController {
	private static final Logger logger = Logger.getLogger(TaskController.class);

	@Autowired
	TaskService taskService;

	// 采集任务的增删改查
	@RequestMapping("/search")
	public Result<List<TaskBean>> getTask(
			@RequestParam("item") final String item,
			@RequestParam("pageNo") final int pageNo,
			@RequestParam("pageSize") final int pageSize,
			final HttpServletRequest request) {

		logger.info("/task/search?item=" + item);

		List<Filter> searchFs = GsonUtils.getGson().fromJson(item,
				new TypeToken<List<Filter>>() {
				}.getType());

		List<TaskBean> list = taskService
				.searchTask(searchFs, pageNo, pageSize);
		long count = taskService.searchTaskCount(searchFs);

		// 对于获取结果集成功与否做相应的处理
		if (list == null) {
			return new Result<List<TaskBean>>(Commons.RESULT_CODE_FAILED,
					"获取任务结果集失败了");
		} else {
			return new Result<List<TaskBean>>(Commons.RESULT_CODE_OK, count,
					list);
		}
	}

	@RequestMapping("/add")
	public Result<Integer> addTask(@RequestParam("item") final String item,
			final HttpServletRequest request) {

		logger.info("/task/add?item=" + item);

		TaskBean task = GsonUtils.getGson().fromJson(item,
				new TypeToken<TaskBean>() {
				}.getType());

		int count = taskService.addTask(task);

		// 对于获取结果集成功与否做相应的处理
		if (count < 0) {
			return new Result<Integer>(Commons.RESULT_CODE_FAILED, "插入任务失败了");
		} else {
			return new Result<Integer>(Commons.RESULT_CODE_OK, count, count);
		}
	}

	@RequestMapping("/delete")
	public Result<Integer> deleteTask(
			@RequestParam("taskId") final long taskId,
			final HttpServletRequest request) {

		logger.info("/task/delete?taskId=" + taskId);

		int count = taskService.deleteTask(taskId);

		// 对于获取结果集成功与否做相应的处理
		if (count < 0) {
			return new Result<Integer>(Commons.RESULT_CODE_FAILED, "删除任务失败了");
		} else {
			return new Result<Integer>(Commons.RESULT_CODE_OK, count, count);
		}
	}

	@RequestMapping("/start")
	public Result<Integer> startTask(@RequestParam("taskId") final long taskId,
			final HttpServletRequest request) {

		logger.info("/task/start?taskId=" + taskId);

		MessageInfo message = taskService.startTask(taskId);

		if (message.getCode() < 0) {
			return new Result<Integer>(message.getCode(), message.getMsg());
		} else {
			return new Result<Integer>(Commons.RESULT_CODE_OK, 1,
					message.getTaskStatus());
		}

	}

	@RequestMapping("/stop")
	public Result<Integer> stopTask(@RequestParam("taskId") final long taskId,
			final HttpServletRequest request) {

		logger.info("/task/stop?taskId=" + taskId);

		// 返回处理的结果码
		MessageInfo message = taskService.stopTask(taskId);

		if (message.getCode() < 0) {
			return new Result<Integer>(message.getCode(), message.getMsg());
		} else {
			return new Result<Integer>(Commons.RESULT_CODE_OK, 1,
					message.getTaskStatus());
		}
	}

	// 任务标签的增删查
	@RequestMapping("/tag/search")
	public Result<List<TaskTagBean>> getTaskTag(final HttpServletRequest request) {

		logger.info("/task/tag/search");

		List<TaskTagBean> list = taskService.searchTaskTag();

		// 对于获取结果集成功与否做相应的处理
		if (list == null) {
			return new Result<List<TaskTagBean>>(Commons.RESULT_CODE_FAILED,
					"获取任务标签结果集失败了");
		} else {
			return new Result<List<TaskTagBean>>(Commons.RESULT_CODE_OK,
					list.size(), list);
		}
	}

	@RequestMapping("/tag/add")
	public Result<Integer> addTaskTag(
			@RequestParam("tagName") final String tagName,
			final HttpServletRequest request) {

		logger.info("/task/tag/add?tagName=" + tagName);

		int count = taskService.addTaskTag(tagName);

		// 对于获取结果集成功与否做相应的处理
		if (count < 0) {
			return new Result<Integer>(Commons.RESULT_CODE_FAILED, "插入任务标签失败了");
		} else {
			return new Result<Integer>(Commons.RESULT_CODE_OK, count, count);
		}
	}

	@RequestMapping("/tag/delete")
	public Result<Integer> deleteTaskTag(
			@RequestParam("tagId") final long tagId,
			final HttpServletRequest request) {

		logger.info("/task/tag/delete?tagId=" + tagId);

		int count = taskService.deleteTaskTag(tagId);

		// 对于获取结果集成功与否做相应的处理
		if (count < 0) {
			return new Result<Integer>(Commons.RESULT_CODE_FAILED, "删除任务标签失败了");
		} else {
			return new Result<Integer>(Commons.RESULT_CODE_OK, count, count);
		}
	}

	// 采集任务详情的增改查
	@RequestMapping("/detail/search")
	public Result<TaskDetailBean> getTaskDetail(
			@RequestParam("taskId") final long taskId,
			final HttpServletRequest request) {

		logger.info("/task/detail/search?taskId=" + taskId);

		TaskDetailBean taskDeatil = taskService.searchTaskDetail(taskId);

		// 对于获取结果集成功与否做相应的处理
		if (taskDeatil == null) {
			return new Result<TaskDetailBean>(Commons.RESULT_CODE_FAILED,
					"没有获取到该任务的任务详情，新建任务请添加任务详情");
		} else {
			return new Result<TaskDetailBean>(Commons.RESULT_CODE_OK, 1,
					taskDeatil);
		}
	}

	@RequestMapping("/detail/add")
	public Result<Integer> addTaskDetail(
			@RequestParam("item") final String item,
			final HttpServletRequest request) {

		logger.info("/task/detail/add?item=" + item);

		TaskDetailBean taskDeatil = GsonUtils.getGson().fromJson(item,
				new TypeToken<TaskDetailBean>() {
				}.getType());

		int count = taskService.addTaskDetail(taskDeatil);

		// 对于获取结果集成功与否做相应的处理
		if (count < 0) {
			return new Result<Integer>(Commons.RESULT_CODE_FAILED, "插入任务详情失败了");
		} else {
			return new Result<Integer>(Commons.RESULT_CODE_OK, count, count);
		}
	}

	@RequestMapping("/detail/update")
	public Result<Integer> updateTaskDetail(
			@RequestParam("item") final String item,
			final HttpServletRequest request) {

		logger.info("/task/detail/update?item=" + item);

		TaskDetailBean taskDeatil = GsonUtils.getGson().fromJson(item,
				new TypeToken<TaskDetailBean>() {
				}.getType());

		int count = taskService.updateTaskDetail(taskDeatil);

		// 对于获取结果集成功与否做相应的处理
		if (count < 0) {
			return new Result<Integer>(Commons.RESULT_CODE_FAILED, "更新任务详情失败了");
		} else {
			return new Result<Integer>(Commons.RESULT_CODE_OK, count, count);
		}
	}
}

package com.sncfc.crawler.task.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sncfc.crawler.bean.MessageInfo;
import com.sncfc.crawler.bean.TaskBean;
import com.sncfc.crawler.bean.TaskDetailBean;
import com.sncfc.crawler.bean.TaskTagBean;
import com.sncfc.crawler.bean.UpdateTaskInfo;
import com.sncfc.crawler.curator.CuratorManage;
import com.sncfc.crawler.curator.OkCallback;
import com.sncfc.crawler.db.Filter;
import com.sncfc.crawler.task.dao.ITaskDao;
import com.sncfc.crawler.task.dao.ITaskDetailDao;
import com.sncfc.crawler.task.dao.ITaskTagDao;
import com.sncfc.crawler.util.Commons;
import com.sncfc.crawler.util.DomainUtils;
import com.sncfc.crawler.util.GsonUtils;
import com.sncfc.crawler.util.TimeUtils;

@Service
public class TaskService {
	/**
	 * TaskService的所对象，用于对状态为“启用”（status=1）的任务做同步操作
	 */
	public final Object LOCK = new Object();

	@Autowired
	private ITaskDao taskDao;

	@Autowired
	private ITaskTagDao taskTagDao;

	@Autowired
	private ITaskDetailDao taskDetailDao;

	private CuratorManage curatorManage;

	@Autowired
	public TaskService(CuratorManage curatorManage) {
		this.curatorManage = curatorManage;

		curatorManage.tasksPathChildrenCache(okCallback);
	}

	private OkCallback okCallback = new OkCallback() {

		@Override
		public void execute(UpdateTaskInfo updatedTask) {
			long taskId = updatedTask.getTaskId();
			int status = updatedTask.getStatus();

			if (Commons.TASK_STATUS_FINISH == status) {
				synchronized (LOCK) {
					taskDao.updateTaskStatus(taskId, status);
					curatorManage.deleteTaskNode(updatedTask.getTaskNodePath());
				}
			}

			String desc = GsonUtils.getTaskDesc(updatedTask);
			taskDao.updateTaskDesc(taskId, desc);
		}
	};

	// 采集任务相关
	public List<TaskBean> searchTask(List<Filter> filters, int pageNo,
			int pageSize) {
		int start = (pageNo - 1) * pageSize;

		return taskDao.searchTask(filters, start, pageSize);
	}

	public long searchTaskCount(List<Filter> filters) {
		return taskDao.searchTaskCount(filters);
	}

	public int addTask(TaskBean task) {
		return taskDao.insertTask(task);
	}

	public int deleteTask(long taskId) {
		return taskDao.deleteTask(taskId);
	}

	public MessageInfo startTask(long taskId) {
		synchronized (LOCK) {
			// 先处理特殊情况
			UpdateTaskInfo updateTask = taskDao
					.searchUpdateTaskInfoById(taskId);
			if (updateTask == null) {
				return new MessageInfo(Commons.RESULT_CODE_FAILED,
						"没有查询到该任务的任务详情，请为该任务添加详情");
			}
			// 如果任务的状态为“启用”或“执行中”，就返回错误信息
			int curStatus = updateTask.getStatus();
			if (Commons.TASK_STATUS_START == curStatus
					|| Commons.TASK_STATUS_EXECUTORY == curStatus) {
				return new MessageInfo(Commons.RESULT_CODE_FAILED,
						"该任务已处于开启状态，无需再次启用，status=" + curStatus);
			}
			// 开始处理业务逻辑
			// 先判断当前时间是否满足该任务的时间策略。如果满足，就通知操作任务节点
			// 如果不满足，就将任务的状态置为“启用”，等待定时器处理
			if (TimeUtils.isDealTime(updateTask.getStartTime(),
					updateTask.getEndTime())) {

				MessageInfo message = startTaskToNode(updateTask);
				if (message.getCode() < 0) {
					return message;
				} else {
					int count = taskDao.updateTaskStatus(taskId,
							Commons.TASK_STATUS_EXECUTORY);
					if (count < 0) {
						return new MessageInfo(Commons.RESULT_CODE_FAILED,
								"当前任务已“上线”，但将任务的状态修改为“执行中”时失败了，taskId="
										+ taskId);
					} else {
						return new MessageInfo(Commons.RESULT_CODE_OK,
								Commons.TASK_STATUS_EXECUTORY);
					}
				}
			} else {
				int count = taskDao.updateTaskStatus(taskId,
						Commons.TASK_STATUS_START);
				if (count < 0) {
					return new MessageInfo(Commons.RESULT_CODE_FAILED,
							"当前时间不满足任务的时间策略，将任务的状态修改为“启用”时失败了，taskId=" + taskId);
				} else {
					return new MessageInfo(Commons.RESULT_CODE_OK,
							Commons.TASK_STATUS_START);
				}
			}
		}
	}

	public MessageInfo stopTask(long taskId) {
		synchronized (LOCK) {
			// 先处理特殊情况
			UpdateTaskInfo updateTask = taskDao
					.searchUpdateTaskInfoById(taskId);
			if (updateTask == null) {
				return new MessageInfo(Commons.RESULT_CODE_FAILED,
						"没有查询到该任务，请刷新后再试");
			}
			// 如果任务的状态为“停止”、“暂停”或“已完成”时，就返回错误信息
			int curStatus = updateTask.getStatus();
			if (Commons.TASK_STATUS_STOP == curStatus
					|| Commons.TASK_STATUS_PAUSE == curStatus
					|| Commons.TASK_STATUS_FINISH == curStatus) {
				return new MessageInfo(Commons.RESULT_CODE_FAILED,
						"该任务已处于停用状态，无需再次停用，status=" + curStatus);
			}
			// 开始处理业务逻辑
			MessageInfo existMessage = curatorManage
					.checkTaskNodeExists(taskId);
			// 如果节点存在，就修改节点状态；如果节点不存在，就将任务的状态置为“停止”
			if (Commons.TASK_NODE_EXISTS == existMessage.getCode()) {
				MessageInfo message = curatorManage.changeTaskNodeStatus(
						taskId, Commons.TASK_STATUS_PAUSE);
				if (message.getCode() < 0) {
					return message;
				} else {
					int count = taskDao.updateTaskStatus(taskId,
							Commons.TASK_STATUS_PAUSE);
					if (count < 0) {
						return new MessageInfo(Commons.RESULT_CODE_FAILED,
								"任务已暂停，但将任务的状态修改为“停止”时失败了，taskId=" + taskId);
					} else {
						return new MessageInfo(Commons.RESULT_CODE_OK,
								Commons.TASK_STATUS_PAUSE);
					}
				}
			} else if (Commons.TASK_NODE_NONE == existMessage.getCode()) {
				int count = taskDao.updateTaskStatus(taskId,
						Commons.TASK_STATUS_STOP);

				if (count < 0) {
					return new MessageInfo(Commons.RESULT_CODE_FAILED,
							"将任务的状态修改为“停止”时失败了，taskId=" + taskId);
				} else {
					return new MessageInfo(Commons.RESULT_CODE_OK);
				}
			} else {
				return existMessage;
			}
		}
	}

	/**
	 * 此方法用于通知任务节点，执行任务
	 */
	public MessageInfo startTaskToNode(UpdateTaskInfo taskBean) {
		MessageInfo existMessage = curatorManage.checkTaskNodeExists(taskBean
				.getTaskId());

		// 如果节点不存在，就新建；如果节点存在就修改节点值
		if (Commons.TASK_NODE_NONE == existMessage.getCode()) {
			taskBean.setStatus(Commons.TASK_STATUS_EXECUTORY);
			String nodeValue = GsonUtils.toJson(taskBean);
			return curatorManage
					.createTaskNode(taskBean.getTaskId(), nodeValue);
		} else if (Commons.TASK_NODE_EXISTS == existMessage.getCode()) {
			return curatorManage.changeTaskNodeStatus(taskBean.getTaskId(),
					Commons.TASK_STATUS_EXECUTORY);
		} else {
			return existMessage;
		}

	}

	// 任务标签相关
	public List<TaskTagBean> searchTaskTag() {
		return taskTagDao.searchTaskTag();
	}

	public int addTaskTag(String tagName) {
		return taskTagDao.insertTaskTag(tagName);
	}

	public int deleteTaskTag(long tagId) {
		return taskTagDao.deleteTaskTag(tagId);
	}

	// 采集任务详情相关
	public TaskDetailBean searchTaskDetail(long taskId) {
		return taskDetailDao.searchTaskDetail(taskId);
	}

	public int addTaskDetail(TaskDetailBean taskDeatil) {
		String urlPath = taskDeatil.getUrlPath();
		String urlHost = DomainUtils.getHost(urlPath);
		taskDeatil.setUrlHost(urlHost);
		return taskDetailDao.insertTaskDetail(taskDeatil);
	}

	public int updateTaskDetail(TaskDetailBean taskDeatil) {
		String urlPath = taskDeatil.getUrlPath();
		String urlHost = DomainUtils.getHost(urlPath);
		taskDeatil.setUrlHost(urlHost);
		return taskDetailDao.updateTaskDetail(taskDeatil);
	}

	public List<UpdateTaskInfo> searchUpdateTaskInfoByStatus(int status) {
		return taskDao.searchUpdateTaskInfoByStatus(status);
	}
}

package com.sncfc.crawler.curator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import com.sncfc.crawler.bean.MessageInfo;
import com.sncfc.crawler.bean.UpdateTaskInfo;
import com.sncfc.crawler.util.Commons;
import com.sncfc.crawler.util.GsonUtils;

/**
 * 通过Curator操作zookeeper的功能类
 * 
 * @author a
 *
 */
public class CuratorManage {
	private static final Logger logger = Logger.getLogger(CuratorManage.class);

	private CuratorFramework client;

	private ExecutorService es;

	public CuratorManage(String hostPort) {
		es = Executors.newFixedThreadPool(5);

		RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

		client = CuratorFrameworkFactory.builder().connectString(hostPort)
				.sessionTimeoutMs(5000).connectionTimeoutMs(5000)
				.retryPolicy(retryPolicy).build();

		client.start();
	}

	/**
	 * 检查任务节点是否存在
	 */
	public MessageInfo checkTaskNodeExists(long taskId) {
		String taskNodePath = Commons.BASE_TASK_NODE_PATH + taskId;

		try {
			Stat s = client.checkExists().forPath(taskNodePath);

			if (s == null) {
				return new MessageInfo(Commons.TASK_NODE_NONE);
			} else {
				return new MessageInfo(Commons.TASK_NODE_EXISTS);
			}
		} catch (Exception e) {
			String errMsg = "检查节点存在时异常了：" + e.getMessage();
			logger.error(errMsg);
			return new MessageInfo(Commons.TASK_NODE_ERROR, errMsg);
		}
	}

	/**
	 * 创建任务节点，当用户“启动”某个采集任务时调用
	 */
	public MessageInfo createTaskNode(long taskId, String nodeValue) {
		logger.info("上线了一个新任务：taskId=" + taskId);
		String taskNodePath = Commons.BASE_TASK_NODE_PATH + taskId;

		try {
			client.create().creatingParentsIfNeeded()
					.withMode(CreateMode.PERSISTENT)
					.forPath(taskNodePath, nodeValue.getBytes());

			return new MessageInfo(Commons.RESULT_CODE_OK);
		} catch (Exception e) {
			String errMsg = "创建任务节点时异常了：" + e.getMessage();
			logger.error(errMsg);
			return new MessageInfo(Commons.TASK_NODE_ERROR, errMsg);
		}
	}

	/**
	 * 修改已“上线”任务的状态
	 */
	public MessageInfo changeTaskNodeStatus(long taskId, int status) {
		logger.info("修改上线任务的状态：taskId=" + taskId + ", status=" + status);

		try {
			// 先获取节点的值，再判断前后状态的值
			byte[] ret = client.getData().forPath(
					Commons.BASE_TASK_NODE_PATH + taskId);
			UpdateTaskInfo taskBean = GsonUtils.get(new String(ret),
					UpdateTaskInfo.class);

			// 如果此时是“暂停”操作，且该节点的任务状态为“已完成”，那么就直接返回“已完成”信息
			if (Commons.TASK_STATUS_PAUSE == status
					&& Commons.TASK_STATUS_FINISH == taskBean.getStatus()) {
				return new MessageInfo(Commons.TASK_NODE_ERROR,
						"当前任务已经完成，不需要暂停");
			}

			// 修改任务的“状态”，将“反馈”值置为0，并将修改后的节点值更新到任务节点
			taskBean.setStatus(status);
			taskBean.setFeedback(Commons.TASK_FEEDBACK_ORIGINAL);
			String newValue = GsonUtils.toJson(taskBean);

			client.setData().forPath(Commons.BASE_TASK_NODE_PATH + taskId,
					newValue.getBytes());

			// 正常操作完成后，返回OK状态码
			return new MessageInfo(Commons.RESULT_CODE_OK);
		} catch (Exception e) {
			logger.error("修改任务状态时异常了：taskId=" + taskId + e.getMessage());
			return new MessageInfo(Commons.TASK_NODE_ERROR, e.getMessage());
		}
	}

	/**
	 * 删除“上线”任务
	 */
	public void deleteTaskNode(String taskNodePath) {
		logger.info("删除上线任务：taskNodePath=" + taskNodePath);

		try {
			client.delete().guaranteed().forPath(taskNodePath);
		} catch (Exception e) {
			logger.error("删除任务异常了：taskNodePath=" + taskNodePath, e);
		}
	}

	/**
	 * 监听Tasks“上线任务”总节点下的子节点
	 */
	public void tasksPathChildrenCache(final OkCallback okCallback) {
		@SuppressWarnings("resource")
		final PathChildrenCache cache = new PathChildrenCache(client,
				Commons.PARENT_TASK_NODE_PATH, true);

		cache.getListenable().addListener(new PathChildrenCacheListener() {

			public void childEvent(CuratorFramework client,
					PathChildrenCacheEvent event) throws Exception {
				switch (event.getType()) {
				case CHILD_UPDATED:
					// 每当有一个任务被修改后，就将该任务的状态取出来
					UpdateTaskInfo updatedTask = GsonUtils.get(new String(event
							.getData().getData()), UpdateTaskInfo.class);

					if (updatedTask != null) {
						// 只关心“控制中心”反馈过来的信息，当任务的“反馈”值不为初始值时，需要处理
						if (updatedTask.getFeedback() != Commons.TASK_FEEDBACK_ORIGINAL) {
							okCallback.execute(updatedTask);
						}
					}
					break;
				default:
					break;
				}
			}
		}, es);

		try {
			cache.start();
		} catch (Exception e) {
			logger.error("监听manages节点下子节点时异常了：" + e.getMessage());
		}
	}

}
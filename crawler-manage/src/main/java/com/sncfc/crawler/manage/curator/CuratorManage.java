package com.sncfc.crawler.manage.curator;

import com.sncfc.crawler.manage.bean.ManageInfo;
import com.sncfc.crawler.manage.bean.TaskBean;
import com.sncfc.crawler.manage.bean.TaskUnitInfo;
import com.sncfc.crawler.manage.bean.WorkerInfo;
import com.sncfc.crawler.manage.util.Commons;
import com.sncfc.crawler.manage.util.GsonUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.Code;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CuratorManage {
    private static final Logger logger = Logger.getLogger(CuratorManage.class);

    private CuratorFramework client;

    /**
     * 本机IP，用来标识节点
     */
    private String selfIp;

    /**
     * 本机对应的待处理任务节点的名称
     */
    private String selfMissionNodePath;

    /**
     * 本机对应的机器节点的名称
     */
    private String selfNodePath;

    /**
     * 本机对应的机器节点名称去掉父节点
     */
    private String myNodePath;

    /**
     * “控制中心”服务器对应的mission节点路径的缓存
     */
    private Set<String> missionPaths;

    /**
     * “爬取模块”服务器对应的taskUnitNode节点路径的缓存
     */
    private Set<String> taskUnitPaths;

    /**
     * 用来处理异步操作的线程池
     */
    private ExecutorService es;

    /**
     * “任务单元”缓存
     */
    private ConcurrentLinkedQueue<String> queue;

    public CuratorManage(String hostPort) {
        try {
            selfIp = InetAddress.getLocalHost().getHostAddress();
            logger.info("本机IP：" + selfIp);
        } catch (UnknownHostException e) {
            logger.error("获取本机IP时异常了，" + e.getMessage());
        }

        missionPaths = new HashSet<String>();

        taskUnitPaths = new HashSet<String>();

        es = Executors.newFixedThreadPool(5);

        queue = new ConcurrentLinkedQueue<String>();

        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

        client = CuratorFrameworkFactory.builder().connectString(hostPort)
                .sessionTimeoutMs(5000).connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy).build();

        client.start();
    }

    /**
     * 创建自身对应的待处理任务节点，这是一个永久的、有序的节点
     */
    public void createSelfMissionNode() {
        try {
            selfMissionNodePath = client.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(Commons.MISSION_NODE_PATH);

            logger.info("创建自身对应的待处理任务节点成功：" + selfMissionNodePath);
        } catch (Exception e) {
            logger.error("创建自身对应的待处理任务节点时异常了,重试中···：" + e.getMessage());
            createSelfMissionNode();
        }
    }

    /**
     * 在“控制中心”总节点manages下创建自己的标识节点，这是一个临时的、有序的节点
     */
    public void createSelfNode() {
        try {
            String selfNodeValue = GsonUtils.getSelfNodeValue(selfIp,
                    selfMissionNodePath);

            selfNodePath = client
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(Commons.MANAGE_NODE_PATH, selfNodeValue.getBytes());

            myNodePath = selfNodePath.replace(Commons.PARENT_MANAGE_NODE_PATH,
                    "");

            logger.info("创建自身标识节点成功：" + selfNodePath);
        } catch (Exception e) {
            logger.error("创建自身标识节点时异常了,重试中···：" + e.getMessage());
            createSelfNode();
        }
    }

    /**
     * 通过LeaderLatch的方式来做主节点竞选
     */
    @SuppressWarnings("resource")
    public void runForMaster() {
        LeaderLatch leaderLatch = new LeaderLatch(client,
                Commons.MSATER_NODE_PATH, selfNodePath);

        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                logger.info(selfNodePath + "成为主节点，开始执行主节点职责···");

                takeLeadership();
            }

            @Override
            public void notLeader() {
                logger.info(selfNodePath + "失去主节点···");

                // 失去leader地位后要将mission缓存清空
                synchronized (missionPaths) {
                    missionPaths.clear();
                }
            }
        });

        try {
            leaderLatch.start();
        } catch (Exception e) {
            logger.error("启动master主节点竞选LeaderLatch时异常了,重试中···："
                    + e.getMessage());
            runForMaster();
        }
    }

    /**
     * 将“上线任务”解析成“任务单元”，并派发给workers去执行爬取操作
     */
    public void assigningTaskToWorkers(OkCallback okCallback) {
        // 先监听“爬取模块”集群的服务器，确定“任务单元”可以被分派到的目标
        workerPathChildrenCache(okCallback);

        // 监听自身对应的mission节点，以处理新任务
        missionPathChildrenCache(okCallback);
    }

    /**
     * 重新启动任务处理
     */
    public void restartTask(TaskBean taskBean, OkCallback okCallback) {
        dealTaskUnit(taskBean, okCallback);
    }

    /**
     * “上线任务”的“任务单元”被执行完之后，先将mission节点下的任务节点删除后再将该任务的状态修改为“已完成”
     */
    public void updateTaskStatusToFinish(TaskBean taskBean) {
        taskBean.setStatus(Commons.TASK_STATUS_FINISH);
        taskBean.setFeedback(Commons.TASK_STATUS_FINISH);

        String finishTaskNodeValue = GsonUtils.toJson(taskBean);

        try {
            client.inTransaction()
                    .delete()
                    .forPath(taskBean.getMissionNodePath())
                    .and()
                    .setData()
                    .forPath(taskBean.getTaskNodePath(),
                            finishTaskNodeValue.getBytes()).and().commit();
        } catch (Exception e) {
            logger.error("操作节点时异常了···" + e.getMessage());
        }
    }

    /**
     * 定时任务再次采集时要将原始任务的反馈置为“执行中”
     */
    public void updateTaskFeedback(TaskBean taskBean, int feedback) {
        taskBean.setFeedback(feedback);

        String finishTaskNodeValue = GsonUtils.toJson(taskBean);

        try {
            client.setData().forPath(taskBean.getTaskNodePath(),
                    finishTaskNodeValue.getBytes());
        } catch (Exception e) {
            logger.error("修改节点内容时异常了···" + e.getMessage());
        }
    }

    /**
     * 竞选成为主节点后，执行主节点职责
     */
    private void takeLeadership() {
        // 监控“控制中心”manages总节点下的子节点，以获取服务器列表
        managesPathChildrenCache();

        // 监控“上线任务”tasks总节点下的子节点，并处理用户提交的“上线任务”
        tasksPathChildrenCache();
    }

    /**
     * 监听manages“控制中心”总节点下的子节点
     */
    private void managesPathChildrenCache() {
        @SuppressWarnings("resource")
        final PathChildrenCache cache = new PathChildrenCache(client,
                Commons.PARENT_MANAGE_NODE_PATH, true);

        cache.getListenable().addListener(new PathChildrenCacheListener() {

            public void childEvent(CuratorFramework client,
                                   PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        // 每当有一个新的“控制中心”机器加入集群，就将该机器对应的mission节点名加入缓存
                        ManageInfo manageAdd = GsonUtils.get(new String(event
                                .getData().getData()), ManageInfo.class);
                        String missionPath = manageAdd.getMessionPath();

                        synchronized (missionPaths) {
                            if (!StringUtils.isEmpty(missionPath)) {
                                missionPaths.add(missionPath);
                            } else {
                                logger.error("master：新加入manage的missionPath为空，不做处理，IP："
                                        + manageAdd.getSelfIp());
                            }
                        }

                        logger.info("master：有新的manage加入，IP："
                                + manageAdd.getSelfIp());
                        break;
                    case CHILD_REMOVED:
                        // 每当有一个“控制中心”机器退出集群，就将该机器对应的mission节点名从缓存中删除
                        ManageInfo manageDel = GsonUtils.get(new String(event
                                .getData().getData()), ManageInfo.class);

                        String missionPathDel = manageDel.getMessionPath();

                        synchronized (missionPaths) {
                            if (!StringUtils.isEmpty(missionPathDel)) {
                                missionPaths.remove(missionPathDel);
                            }
                        }

                        logger.info("master：有manage退出，IP：" + manageDel.getSelfIp());
                        break;
                    default:
                        break;
                }
            }
        }, es);

        try {
            cache.start();
        } catch (Exception e) {
            logger.error("master：监听manages节点下子节点时异常了：" + e.getMessage());
        }
    }

    /**
     * 监听Tasks“上线任务”总节点下的子节点
     */
    private void tasksPathChildrenCache() {
        @SuppressWarnings("resource")
        final PathChildrenCache cache = new PathChildrenCache(client,
                Commons.PARENT_TASK_NODE_PATH, true);

        cache.getListenable().addListener(new PathChildrenCacheListener() {

            public void childEvent(CuratorFramework client,
                                   PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        // 每当有一个新的任务被用户“上线”后，就将该任务分派出去
                        ChildData child = event.getData();
                        TaskBean newTask = GsonUtils.get(
                                new String(child.getData()), TaskBean.class);

                        // 该newTask的状态是“执行”且没有被分配过,则执行新任务的分配
                        // 该newTask的missionNodePath字段值为空则表示该任务没有被分配过
                        if (Commons.TASK_STATUS_EXECUTORY == newTask.getStatus()
                                && StringUtils.isEmpty(newTask.getMissionNodePath())) {
                            newTask.setTaskNodePath(child.getPath());

                            logger.info("master：有新任务“上线”了，taskNodePath="
                                    + newTask.getTaskNodePath());

                            // 将新任务分派出去
                            distributeNewTask(newTask);
                        }
                        break;
                    case CHILD_UPDATED:
                        // 每当有一个任务被修改后，就将该任务对应的节点更新
                        TaskBean updatedTask = GsonUtils.get(new String(event
                                .getData().getData()), TaskBean.class);

                        // 该任务已被分配出去了，才会做处理
                        // 当任务的“反馈”值为初始值时，表示此次更新是由“用户系统”发起的，需要处理
                        if (!StringUtils.isEmpty(updatedTask.getMissionNodePath())
                                && updatedTask.getFeedback() == Commons.TASK_FEEDBACK_ORIGINAL) {

                            // 如果用户将“上线任务”的状态置为“暂停”，就通知slave停止执行该任务
                            if (Commons.TASK_STATUS_PAUSE == updatedTask
                                    .getStatus()) {
                                logger.info("master：任务taskNodePath="
                                        + updatedTask.getTaskNodePath() + " 被暂停");

                                stopDistributedTask(updatedTask);
                            }

                            // 如果用户将“上线任务”的状态置为“执行”，就通知slave去执行该任务
                            if (Commons.TASK_STATUS_EXECUTORY == updatedTask
                                    .getStatus()) {
                                logger.info("master：任务taskNodePath="
                                        + updatedTask.getTaskNodePath() + " 重新启用");

                                restartDistributedTask(updatedTask);
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
            logger.error("master：监听tasks节点下子节点时异常了：" + e.getMessage());
        }
    }

    /**
     * 分派新上线的任务到“控制中心”集群下的slave机器
     */
    private void distributeNewTask(TaskBean newTask) {
        // 先获取目标节点
        String missionPath = getNextMissionPath();

        // 如果正确得到了目标节点，就执行任务分派
        if (!StringUtils.isEmpty(missionPath)) {
            // 根据taskId构造子节点目录名
            String nodePath = missionPath + "/task-" + newTask.getTaskId();
            newTask.setMissionNodePath(nodePath);

            dealNewTaskInTransaction(newTask);
        }
    }

    /**
     * 取出missions节点下的一个子节点，用来接收新“上线任务”
     */
    private String getNextMissionPath() {
        List<String> missionList = new ArrayList<String>();

        // 为了避免死锁，将missionPaths里面的missionPath取出来放入一个临时的List中
        synchronized (missionPaths) {
            for (String path : missionPaths) {
                missionList.add(path);
            }
        }

        String finalPath = "";
        int minCount = Integer.MAX_VALUE;

        // 用遍历这个临时List的方式代替遍历missionPaths。查询每个missionPath下的子节点的数量
        for (String missionPath : missionList) {
            try {
                List<String> cList = client.getChildren().forPath(missionPath);
                int count = cList.size();

                // 找到子节点数最小的那个节点
                if (count < minCount) {
                    finalPath = missionPath;
                    minCount = count;
                }
            } catch (Exception e) {
                logger.error("master：获取mission下子节点的数量时异常了，missionPath="
                        + missionPath + "：" + e.getMessage());
            }
        }

        return finalPath;
    }

    /**
     * 这里用到“事物操作”来完成新任务的分派和更新操作
     */
    private void dealNewTaskInTransaction(TaskBean newTask) {
        // 任务分派的目标节点目录名
        String messionNodePath = newTask.getMissionNodePath();
        // 任务分派的目标节点的值
        String messionNodeValue = GsonUtils.toJson(newTask);

        // 任务原始的目录名
        String taskNodePath = newTask.getTaskNodePath();
        // 将任务的状态作为反馈更新到“上线任务”节点，表示该任务已被“控制中心”处理了
        newTask.setFeedback(newTask.getStatus());
        String taskNodeValue = GsonUtils.toJson(newTask);

        try {
            // 先将任务分配到目标节点后再更新原目标节点，增加任务被分配到的mission节点路径
            client.inTransaction().create().withMode(CreateMode.PERSISTENT)
                    .forPath(messionNodePath, messionNodeValue.getBytes())
                    .and().setData()
                    .forPath(taskNodePath, taskNodeValue.getBytes()).and()
                    .commit();

            logger.info("master：新任务 " + taskNodePath + " 被分派到了 "
                    + messionNodePath);
        } catch (Exception e) {
            logger.error("master：新任务 " + taskNodePath + " 分派时异常了："
                    + e.getMessage());
        }
    }

    /**
     * 当用户暂停一个“上线任务”时，调用该方法去同步更新mission节点下的对应任务节点的值
     */
    private void stopDistributedTask(TaskBean updatedTask) {
        try {
            // 任务的新状态，用来告诉用户该任务“已被停用”
            updateDistributedTask(updatedTask, Commons.TASK_STATUS_PAUSE);

            logger.info("master：已通知将任务暂停： " + updatedTask.getTaskNodePath());
        } catch (Exception e) {
            logger.error("master：通知任务：" + updatedTask.getTaskNodePath()
                    + " 暂停时异常了：" + e.getMessage());
        }
    }

    /**
     * 当用户重新启用一个“上线任务”时，调用该方法去同步更新mission节点下的对应任务节点的值
     */
    private void restartDistributedTask(TaskBean updatedTask) {
        try {
            // 任务的新状态，用来告诉用户该任务“已被重新执行”
            updateDistributedTask(updatedTask, Commons.TASK_STATUS_EXECUTORY);

            logger.info("master：已通知将任务重启： " + updatedTask.getTaskNodePath());
        } catch (Exception e) {
            logger.error("master：通知任务：" + updatedTask.getTaskNodePath()
                    + " 重启时异常了：" + e.getMessage());
        }

    }

    /**
     * 该方法用于处理用户更新“上线任务”的操作
     */
    private void updateDistributedTask(TaskBean updatedTask, int newStatus)
            throws Exception {
        String messionNodePath = updatedTask.getMissionNodePath();
        // 先获取要更新的mission节点的新值
        String missionNodeValue = GsonUtils.toJson(updatedTask);

        String taskNodePath = updatedTask.getTaskNodePath();
        // 再将新的状态码作为反馈更新到“上线任务”节点，表示该任务已被“控制中心”处理了
        updatedTask.setFeedback(newStatus);
        String taskNodeValue = GsonUtils.toJson(updatedTask);

        // 先更新目标mission节点后再更新原目标节点，告诉原目标节点该任务已经被处理了
        client.inTransaction().setData()
                .forPath(messionNodePath, missionNodeValue.getBytes()).and()
                .setData().forPath(taskNodePath, taskNodeValue.getBytes())
                .and().commit();
    }

    /**
     * 监听“爬取模块”集群的服务器，并持续更新
     */
    private void workerPathChildrenCache(final OkCallback okCallback) {
        @SuppressWarnings("resource")
        final PathChildrenCache cache = new PathChildrenCache(client,
                Commons.PARENT_WORKER_NODE_PATH, true);

        cache.getListenable().addListener(new PathChildrenCacheListener() {

            public void childEvent(CuratorFramework client,
                                   PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        // 每当有一个新的“爬取模块”机器加入集群，就将该机器对应的worker节点名加入缓存
                        WorkerInfo workerAdd = GsonUtils.get(new String(event
                                .getData().getData()), WorkerInfo.class);
                        String taskUnitPath = workerAdd.getTaskUnitPath();

                        if (!StringUtils.isEmpty(taskUnitPath)) {
                            String myTaskUnitPath = taskUnitPath + myNodePath;

                            synchronized (taskUnitPaths) {
                                taskUnitPaths.add(myTaskUnitPath);
                                queue.offer(myTaskUnitPath);

                                logger.info("slave：有新的worker加入，IP："
                                        + workerAdd.getSelfIp());
                            }

                            // 每当有一个新的采集机器加入进来，就要监听其对应的“任务单元”节点
                            taskUnitPathChildrenCache(myTaskUnitPath, okCallback);
                        } else {
                            logger.error("slave：新加入的worker的taskUnitPath为空，不作处理，IP："
                                    + workerAdd.getSelfIp());
                        }

                        break;
                    case CHILD_REMOVED:
                        // 每当有一个“爬取模块”机器退出集群，就将该机器对应的worker节点名从缓存中删除
                        WorkerInfo workerDel = GsonUtils.get(new String(event
                                .getData().getData()), WorkerInfo.class);
                        String taskUnitPathDel = workerDel.getTaskUnitPath();

                        synchronized (taskUnitPaths) {
                            if (!StringUtils.isEmpty(taskUnitPathDel)) {
                                String myTaskUnitPathDel = taskUnitPathDel
                                        + myNodePath;
                                taskUnitPaths.remove(myTaskUnitPathDel);
                            }
                        }

                        logger.info("slave：有worker退出，IP：" + workerDel.getSelfIp());
                        break;
                    default:
                        break;
                }
            }
        }, es);

        try {
            cache.start();
        } catch (Exception e) {
            logger.error("slave：监听workers节点下子节点时异常了：" + e.getMessage());
        }
    }

    /**
     * slave监听自身对应的mission节点下的任务子节点
     */
    private void missionPathChildrenCache(final OkCallback okCallback) {
        @SuppressWarnings("resource")
        final PathChildrenCache cache = new PathChildrenCache(client,
                selfMissionNodePath, true);

        cache.getListenable().addListener(new PathChildrenCacheListener() {

            public void childEvent(CuratorFramework client,
                                   PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        // 每当有一个新的“上线任务”被master分派过来，本slave就得做处理
                        ChildData child = event.getData();
                        TaskBean newTask = GsonUtils.get(
                                new String(child.getData()), TaskBean.class);

                        // 该newTask的状态是“执行”,则执行新任务的分配
                        if (Commons.TASK_STATUS_EXECUTORY == newTask.getStatus()) {
                            logger.info("slave：开始处理一个新任务，missionNodePath="
                                    + newTask.getMissionNodePath());

                            // 将任务构造出“任务单元”
                            okCallback.dealNewTask(newTask);

                            // 将构造出的“任务单元”分配到每个worker对应的任务单元节点
                            dealTaskUnit(newTask, okCallback);
                        }
                        break;
                    case CHILD_UPDATED:
                        // 每当有一个任务被修改后，就将该任务做相应的更新处理
                        TaskBean updatedTask = GsonUtils.get(new String(event
                                .getData().getData()), TaskBean.class);

                        logger.info("slave：处理修改的“上线任务”，taskNodePath="
                                + updatedTask.getTaskNodePath());

                        // 根据“上线任务”的状态来“停止”或“执行”任务
                        okCallback.dealUpdatedTask(updatedTask);
                        break;
                    default:
                        break;
                }
            }
        }, es);

        try {
            cache.start();
        } catch (Exception e) {
            logger.error("slave：监听workers节点下子节点时异常了：" + e.getMessage());
        }
    }

    private void dealTaskUnit(TaskBean taskBean, OkCallback okCallback) {
        List<String> taskUnitList = new ArrayList<String>();

        // 为了避免死锁，将taskUnitPaths里面的taskUnitPath取出来放入一个临时的List中
        synchronized (taskUnitPaths) {
            for (String path : taskUnitPaths) {
                taskUnitList.add(path);
            }
        }

        // 如果是单页任务，在分配任务时应该相对的均衡的分配给采集机器
        // 利用queue的FIFO特性实现所有采集机器在分配任务时的均衡性
        if (Commons.TASK_TYPE_SINGLE == taskBean.getTaskType()) {
            String taskUnitPath = queue.poll();
            while (true) {
                // 如果taskUnitPath为null，说明这个queue空了，没有可以分配到的采集机器了
                if (taskUnitPath == null) {
                    logger.error("slave：采集机器的缓存queue为空，无法分配“任务单元”···");
                    break;
                }

                // 如果该taskUnitPath包含在taskUnitList里面，说明它是有效的，要给他分配“任务单元”
                // “任务单元”分配完成之后，将该taskUnitPath插入queue继续排队
                if (taskUnitList.contains(taskUnitPath)) {

                    TaskUnitInfo taskUnitInfo = okCallback.getNextTaskUnitInfo(
                            taskBean.getTaskId(), taskUnitList.size());

                    createTaskUnitPath(taskUnitPath, taskUnitInfo);

                    queue.offer(taskUnitPath);

                    break;
                } else {
                    logger.info("slave：任务单元路径失效，将被从待选队列中删除taskUnitPath=" + taskUnitPath);
                    taskUnitPath = queue.poll();
                }
            }
        } else if (Commons.TASK_TYPE_MULTIPLE == taskBean.getTaskType()) {
            // 用遍历这个临时List的方式代替遍历taskUnitPaths。为每个“任务单元”节点分配一个“任务单元”
            for (String taskUnitPath : taskUnitList) {
                TaskUnitInfo taskUnitInfo = okCallback.getNextTaskUnitInfo(
                        taskBean.getTaskId(), taskUnitList.size());

                // 如果获取的“任务单元”为空，就说明该任务已经被处理完了，直接结束遍历
                if (taskUnitInfo == null) {
                    break;
                }

                createTaskUnitPath(taskUnitPath, taskUnitInfo);
            }
        }
    }

    /**
     * 在采集机器对应的“任务单元”节点下创建“任务单元”
     */
    private void createTaskUnitPath(final String taskUnitPath,
                                    final TaskUnitInfo taskUnitInfo) {
        // 如果获取的“任务单元”不为null，则执行；如果为空，说明该任务被处理完了
        if (taskUnitInfo != null) {
            String tuNodeValue = GsonUtils.toJson(taskUnitInfo);

            try {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .inBackground(new BackgroundCallback() {

                            @Override
                            public void processResult(CuratorFramework arg0,
                                                      CuratorEvent arg1) throws Exception {
                                switch (Code.get(arg1.getResultCode())) {
                                    case OK:
                                        break;
                                    case NODEEXISTS:
                                        logger.error("slave：创建“任务单元”节点时发现已存在，当前任务将丢失···："
                                                + GsonUtils.toJson(taskUnitPath));
                                        break;
                                    default:
                                        logger.error("slave：创建“任务单元”节点" + taskUnitPath + "/taskUnit-" + taskUnitInfo
                                                .getTaskId() + "失败···Code=" + arg1.getResultCode());
                                        createTaskUnitPath(taskUnitPath, taskUnitInfo);
                                        break;
                                }
                            }

                        })
                        .forPath(
                                taskUnitPath + "/taskUnit-"
                                        + taskUnitInfo.getTaskId(),
                                tuNodeValue.getBytes());
            } catch (Exception e) {
                logger.error("slave：创建“任务单元”在采集服务器下对应的节点时异常了···："
                        + e.getMessage());
            }
        }
    }

    private void taskUnitPathChildrenCache(final String taskUnitNodePath,
                                           final OkCallback okCallback) {
        logger.info("slave：监听worker任务节点下该manage对应的taskUnit节点，taskUnitNodePath="
                + taskUnitNodePath);

        @SuppressWarnings("resource")
        final PathChildrenCache cache = new PathChildrenCache(client,
                taskUnitNodePath, true);

        cache.getListenable().addListener(new PathChildrenCacheListener() {

            public void childEvent(CuratorFramework client,
                                   PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_REMOVED:
                        // 每当有一个“任务单元”被删除后，就说明该“任务单元”已被处理，需要再分配一个
                        TaskUnitInfo removedTaskUnitInfo = GsonUtils.get(
                                new String(event.getData().getData()),
                                TaskUnitInfo.class);

                        int workerCount = 0;
                        synchronized (taskUnitPaths) {
                            workerCount = taskUnitPaths.size();
                        }

                        TaskUnitInfo taskUnitInfo = okCallback.getNextTaskUnitInfo(
                                removedTaskUnitInfo.getTaskId(), workerCount);

                        createTaskUnitPath(taskUnitNodePath, taskUnitInfo);
                        break;
                    default:
                        break;
                }
            }
        }, es);

        try {
            cache.start();
        } catch (Exception e) {
            logger.error("slave：监听taskUnitNode节点下子节点时异常了：" + e.getMessage());
        }
    }
}
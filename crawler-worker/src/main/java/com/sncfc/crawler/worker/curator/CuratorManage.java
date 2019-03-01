package com.sncfc.crawler.worker.curator;

import com.sncfc.crawler.worker.bean.TaskUnitInfo;
import com.sncfc.crawler.worker.util.Commons;
import com.sncfc.crawler.worker.util.GsonUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
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
    private String selfTaskUnitNodePath;

    /**
     * 本机对应的机器节点的名称
     */
    private String selfNodePath;

    /**
     * 用来处理异步操作的线程池
     */
    private ExecutorService es;

    @Autowired
    public CuratorManage(String hostPort) {
        try {
            selfIp = InetAddress.getLocalHost().getHostAddress();
            logger.info("本机IP：" + selfIp);
        } catch (UnknownHostException e) {
            logger.error("获取本机IP时异常了，" + e.getMessage());
        }

        es = Executors.newFixedThreadPool(5);

        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

        client = CuratorFrameworkFactory.builder().connectString(hostPort)
                .sessionTimeoutMs(5000).connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy).build();

        client.start();
    }

    /**
     * 创建自身对应的待处理“任务单元”节点，这是一个永久的、有序的节点
     */
    public void createSelfTaskUnitNode() {
        try {
            selfTaskUnitNodePath = client.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(Commons.TU_NODE_PATH);

            logger.info("创建自身对应的待处理任务节点成功：" + selfTaskUnitNodePath);
        } catch (Exception e) {
            logger.error("创建自身对应的待处理任务节点时异常了,重试中···：" + e.getMessage());
            createSelfTaskUnitNode();
        }
    }

    /**
     * 在“爬取模块”总节点workers下创建自己的标识节点，这是一个临时的、有序的节点
     */
    public void createSelfNode() {
        try {
            String selfNodeValue = GsonUtils.getSelfNodeValue(selfIp,
                    selfTaskUnitNodePath);

            selfNodePath = client
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(Commons.WORKER_NODE_PATH, selfNodeValue.getBytes());

            logger.info("创建自身标识节点成功：" + selfNodePath);
        } catch (Exception e) {
            logger.error("创建自身标识节点时异常了,重试中···：" + e.getMessage());
            createSelfNode();
        }
    }

    /**
     * 监听采集机器对应的“任务单元”节点，如果有新的“控制中心”增加，就监听新增节点
     */
    public void taskUnitPathChildrenCache(final OkCallback okCallback) {
        @SuppressWarnings("resource")
        final PathChildrenCache cache = new PathChildrenCache(client,
                selfTaskUnitNodePath, true);

        cache.getListenable().addListener(new PathChildrenCacheListener() {

            public void childEvent(CuratorFramework client,
                                   PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        // 每当有一个新的“控制中心”来分配了任务，就要监听该“控制中心”对应的“任务单元”节点
                        String taskUnitManagePath = event.getData().getPath();

                        taskUnitManagePathChildrenCache(taskUnitManagePath,
                                okCallback);
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

    /**
     * 每个“控制中心”都会在采集机器对应的taskUnit节点下新建一个对应的manage节点，用来分配“任务单元”。
     * 采集机器需要监听/taskUnit/manage-xx节点下的子节点，这些子节点就是“任务单元”
     */
    private void taskUnitManagePathChildrenCache(String taskUnitManagePath,
                                                 final OkCallback okCallback) {

        @SuppressWarnings("resource")
        final PathChildrenCache cache = new PathChildrenCache(client,
                taskUnitManagePath, true);

        cache.getListenable().addListener(new PathChildrenCacheListener() {

            public void childEvent(CuratorFramework client,
                                   PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        // 每当有一个新的“任务单元”被分配过来，就要去执行它
                        ChildData child = event.getData();

                        TaskUnitInfo taskUnitAdd = GsonUtils.get(
                                new String(child.getData()), TaskUnitInfo.class);
                        taskUnitAdd.setTaskUnitNodePath(child.getPath());

                        logger.info("worker：新增了一个任务单元，开始处理taskId=" + taskUnitAdd.getTaskId());
                        okCallback.dealTaskUnit(taskUnitAdd);
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

    /**
     * 当一个“任务单元”被完成后，要删除它对应的节点。用来通知“控制中心”再给我分配一个
     */
    public void deleteTaskUnit(TaskUnitInfo taskUnit) {
        String taskUnitPath = taskUnit.getTaskUnitNodePath();
        logger.info("“任务单元”处理完毕，删除该节点：taskUnitPath=" + taskUnitPath);

        try {
            client.delete().guaranteed().forPath(taskUnitPath);
        } catch (Exception e) {
            logger.error("删除任务异常了：taskUnitPath=" + taskUnitPath);
        }
    }
}
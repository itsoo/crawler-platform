package com.sncfc.crawler.worker.start;

import com.sncfc.crawler.worker.bean.TaskUnitInfo;
import com.sncfc.crawler.worker.curator.CuratorManage;
import com.sncfc.crawler.worker.curator.OkCallback;
import com.sncfc.crawler.worker.spider.SpiderWorker;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkerStart {
    private final static Logger logger = Logger.getLogger(WorkerStart.class);

    @Autowired
    private CuratorManage curatorManage;

    @Autowired
    private SpiderWorker spiderWorker;

    public void start() {
        logger.info("爬取模块启动···");

        // 先执行初始化操作
        init();
        logger.info("爬取模块初始化完成···");

        // 开始处理“任务单元”，执行采集
        dealTaskUnit();
    }

    private void init() {
        // 创建自身对应的“任务单元”节点
        curatorManage.createSelfTaskUnitNode();

        // 创建自身标识节点
        curatorManage.createSelfNode();
    }

    private void dealTaskUnit() {
        OkCallback okCallback = new OkCallback() {

            @Override
            public void dealTaskUnit(TaskUnitInfo newTask) {
                spiderWorker.crawlTaskUnit(newTask);
            }

        };
		
        curatorManage.taskUnitPathChildrenCache(okCallback);
    }
}

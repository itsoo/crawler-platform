package com.sncfc.crawler.worker.spider;

import com.sncfc.crawler.worker.bean.TaskUnitInfo;
import com.sncfc.crawler.worker.curator.CuratorManage;
import com.sncfc.crawler.worker.loader.MyClassLoader;
import com.sncfc.crawler.worker.mq.IMQClient;
import com.sncfc.crawler.worker.spider.tactics.*;
import com.sncfc.crawler.worker.util.Commons;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class SpiderWorker {
    private final static Logger logger = Logger.getLogger(SpiderWorker.class);

    private final static String BASE_TACTICS_NAME = "com.sncfc.crawler.worker.spider.tactics.custom.Tactics";

    @Autowired
    private CuratorManage curatorManage;

    @Autowired
    private MyClassLoader myClassLoader;

    private ExecutorService threadPool;

    private Map<Long, Tactics> tacticsMap;

    private IMQClient mqClient;

    @Autowired
    public SpiderWorker(int corePoolSize, int maximumPoolSize,
                        IMQClient mqClient) {

        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        this.mqClient = mqClient;

        tacticsMap = new ConcurrentHashMap<Long, Tactics>();
        tacticsMap.put(Tactics1.TACTICS_NUM, new Tactics1(mqClient));
        tacticsMap.put(Tactics2.TACTICS_NUM, new Tactics2(mqClient));
        tacticsMap.put(Tactics3.TACTICS_NUM, new Tactics3(mqClient));
        tacticsMap.put(Tactics4.TACTICS_NUM, new Tactics4(mqClient));
    }

    public void crawlTaskUnit(TaskUnitInfo taskUnit) {
        long tacticsId = taskUnit.getTacticsId();
        Tactics tactics = tacticsMap.get(tacticsId);

        // 如果策略ID大于内置策略的最大ID，就说明这是一个自定义策略，需要处理
        if (tactics == null && tacticsId > Commons.DEFAULT_TACTICS_ID_MAX) {
            makeCustomTactics(tacticsId);
            tactics = tacticsMap.get(tacticsId);
        }
        if (tactics != null) {
            Runnable thread = new DealTaskUnitRunnable(tactics, taskUnit,
                    curatorManage);
            threadPool.execute(thread);
        } else {
            logger.error("该采集策略不存在，tacticsId=" + tacticsId);
        }
    }

    // 后续扩展采集策略，但内置的两个策略不能被替换
    public boolean addTacticsToMap(long tacticsId, Tactics tactics) {
        if (tacticsId > Commons.DEFAULT_TACTICS_ID_MAX) {
            tacticsMap.put(tacticsId, tactics);

            return true;
        }

        logger.error("扩展的采集策略的ID与内置采集策略重复，不予扩展···");
        return false;
    }

    // 删除扩展的采集策略，但内置的策略不能被删除
    public boolean removeTacticsFromMap(long tacticsId) {
        if (tacticsId > Commons.DEFAULT_TACTICS_ID_MAX) {
            tacticsMap.remove(tacticsId);

            return true;
        }

        logger.error("删除的采集策略的ID与内置采集策略重复，不予删除···");
        return false;
    }

    private synchronized void makeCustomTactics(long tacticsId) {
        // 每次构造之前先判断是否存在，防止重复执行
        Tactics tactics = tacticsMap.get(tacticsId);
        if (tactics != null) {
            return;
        }

        String name = BASE_TACTICS_NAME + tacticsId;
        try {
            Class<?> clazz = myClassLoader.loadClass(name);
            Constructor<?> constructor = clazz.getConstructor(IMQClient.class);
            Tactics newTactics = (Tactics) constructor.newInstance(mqClient);

            tacticsMap.put(tacticsId, newTactics);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}

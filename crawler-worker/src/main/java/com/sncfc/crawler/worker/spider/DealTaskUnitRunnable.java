package com.sncfc.crawler.worker.spider;

import com.sncfc.crawler.worker.bean.TaskUnitInfo;
import com.sncfc.crawler.worker.curator.CuratorManage;
import com.sncfc.crawler.worker.spider.tactics.Tactics;

public class DealTaskUnitRunnable implements Runnable {

    /**
     * 采集策略类，执行对应的“采集任务单元”
     */
    private Tactics tactics;

    /**
     * 要执行的“采集任务单元”
     */
    private TaskUnitInfo taskUnit;

    /**
     * 要执行的“采集任务单元”
     */
    private CuratorManage curatorManage;

    public DealTaskUnitRunnable(Tactics tactics, TaskUnitInfo taskUnit,
                                CuratorManage curatorManage) {
        this.tactics = tactics;
        this.taskUnit = taskUnit;
        this.curatorManage = curatorManage;
    }

    @Override
    public void run() {
		tactics.execute(taskUnit);

        try {
            Thread.sleep(taskUnit.getSleepTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        curatorManage.deleteTaskUnit(taskUnit);
    }
}

package com.sncfc.crawler.manage.start;

import com.sncfc.crawler.manage.bean.TaskBean;
import com.sncfc.crawler.manage.bean.TaskUnitInfo;
import com.sncfc.crawler.manage.curator.CuratorManage;
import com.sncfc.crawler.manage.curator.OkCallback;
import com.sncfc.crawler.manage.task.dao.ITaskDetailDao;
import com.sncfc.crawler.manage.util.Commons;
import org.apache.log4j.Logger;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.*;

@Service
public class ManageStart {
    private final static Logger logger = Logger.getLogger(ManageStart.class);

    @Autowired
    private long timerInterval;

    @Autowired
    private CuratorManage curatorManage;

    @Autowired
    private ITaskDetailDao taskDetailDao;

    /**
     * 循环任务集合，用来存储循环任务，然后交由定时器统一处理
     */
    private Map<Long, TaskBean> cirTaskBeans;

    /**
     * 任务用来生成“任务单元”的处理类
     */
    private Map<Long, ManageTaskCache> taskCaches;

    /**
     * 用来处理“循环任务”的定时器
     */
    private Timer timer;

    public void start() {
        logger.info("控制中心启动···");

        // 先执行初始化操作
        init();
        logger.info("控制中心初始化完成···");

        // 执行slave职责，处理任务。生成“任务单元”并分派到workers
        assigningTaskToWorkers();
    }

    /**
     * 控制中心平台启动时需要做一些初始化工作，方法内的初始化工作执行顺序不能变
     */
    private void init() {
        cirTaskBeans = new HashMap<Long, TaskBean>();

        taskCaches = new HashMap<Long, ManageTaskCache>();

        timer = new Timer();

        // 创建自身对应的待处理任务节点
        curatorManage.createSelfMissionNode();

        // 创建自身标识节点
        curatorManage.createSelfNode();

        // 做竞选主节点相关的处理
        curatorManage.runForMaster();
    }

    private void assigningTaskToWorkers() {
        logger.info("控制中心开始执行slave的职责···");

        OkCallback okCallback = new OkCallback() {

            @Override
            public void dealNewTask(TaskBean newTask) {
                // 解析任务，将任务处理成“任务单元”
                makeTaskCache(newTask, false);

                // 如果是循环任务，就将该任务添加到“循环任务集合”，交由定时器处理
                if (Commons.TASK_CIRCULATION_TRUE == newTask.getCirculation()) {
                    synchronized (cirTaskBeans) {
                        cirTaskBeans.put(newTask.getTaskId(), newTask);
                    }
                }
            }

            @Override
            public void dealUpdatedTask(TaskBean updatedTask) {
                ManageTaskCache taskCache = taskCaches.get(updatedTask
                        .getTaskId());
                if (taskCache != null) {
                    // 如果任务的状态为“执行”，就将任务对应的处理开启
                    if (Commons.TASK_STATUS_EXECUTORY == updatedTask
                            .getStatus()) {
                        // 先将该任务的处理类重启
                        taskCache.restart();

                        // 重启之后继续向worker分发任务
                        curatorManage.restartTask(updatedTask, this);
                    } else if (Commons.TASK_STATUS_PAUSE == updatedTask
                            .getStatus()) {
                        // 如果任务的状态为“暂停”，就将任务对应的处理关闭
                        taskCache.stop();
                    } else {
                        logger.error("任务的状态不在处理的范围 status="
                                + updatedTask.getStatus());
                    }
                } else {
                    if (Commons.TASK_CIRCULATION_TRUE == updatedTask
                            .getCirculation()) {
                        synchronized (cirTaskBeans) {
                            cirTaskBeans.put(updatedTask.getTaskId(),
                                    updatedTask);
                        }
                    } else {
                        synchronized (cirTaskBeans) {
                            cirTaskBeans.remove(updatedTask.getTaskId());
                        }
                    }
                }
            }

            @Override
            public TaskUnitInfo getNextTaskUnitInfo(long taskId, int workerCount) {
                ManageTaskCache taskCache = taskCaches.get(taskId);
                // 当前taskCache存在，且处于“运行”状态，才会去获取一个“任务单元”
                if (taskCache != null && taskCache.isRunning()) {
                    TaskUnitInfo taskUnitInfo = taskCache
                            .getNextTaskUnitInfo(workerCount);
                    // 如果获取的“任务单元”为空，说明该任务所有的“任务单元”都被处理了，要将当前taskCache删除掉
                    if (taskUnitInfo == null) {
                        taskCaches.remove(taskId);
                        // 然后修改tasks节点下该原始“上线任务”的反馈信息置为“已完成”
                        TaskBean finishTaskBean = taskCache.getTaskBeanCopy();

                        // 如果不是循环任务，采集完成后将原始任务的状态置为“已完成”
                        if (Commons.TASK_CIRCULATION_FALSE == finishTaskBean
                                .getCirculation()) {
                            curatorManage
                                    .updateTaskStatusToFinish(finishTaskBean);
                        } else {
                            curatorManage.updateTaskFeedback(finishTaskBean,
                                    Commons.TASK_STATUS_FINISH);
                        }

                        logger.info("slave：本轮任务执行完毕missionNodePath="
                                + finishTaskBean.getMissionNodePath());
                    }

                    return taskUnitInfo;
                } else {
                    return null;
                }
            }
        };

        // 监听“爬取模块”服务器节点，确定“任务单元”可以被分派到的目标
        curatorManage.assigningTaskToWorkers(okCallback);

        // 处理循环任务
        dealCirTasksByTimer(okCallback);
    }

    /**
     * 生成任务对应的“任务单元”处理类。 由定时器循环执行的任务，cirExe传值true
     */
    private void makeTaskCache(TaskBean newTask, boolean cirExe) {
        // 先更新任务被处理时的时间戳
        newTask.setLastDealTimestamp(System.currentTimeMillis());

        ManageTaskCache manageCache = new ManageTaskCache(newTask,
                taskDetailDao);
        if (cirExe) {
            manageCache.refreshParamLists();
        }

        taskCaches.put(newTask.getTaskId(), manageCache);
    }

    /**
     * 处理“循环任务”
     */
    private void dealCirTasksByTimer(final OkCallback okCallback) {
        timer.schedule(new TimerTask() {
            public void run() {
                synchronized (cirTaskBeans) {
                    for (Long taskId : cirTaskBeans.keySet()) {
                        // 如果“任务单元”处理类已存在，就跳过
                        if (taskCaches.containsKey(taskId)) {
                            logger.info("任务还在进行中，本次循环不执行···taskId=" + taskId);
                            continue;
                        } else {
                            logger.info("循环任务开始···taskId=" + taskId);
                            makeCirTaskCache(cirTaskBeans.get(taskId),
                                    okCallback);
                        }
                    }
                }
            }
        }, timerInterval, timerInterval);
    }

    /**
     * 该任务的“任务单元”处理类不存在，就要根据“循环条件”来处理
     */
    private void makeCirTaskCache(TaskBean cirTask, OkCallback okCallback) {
        if (Commons.TASK_STATUS_EXECUTORY == cirTask.getStatus()) {
            try {
                if (isDealTime(cirTask)) {
                    makeTaskCache(cirTask, true);
                    curatorManage.updateTaskFeedback(cirTask,
                            Commons.TASK_STATUS_EXECUTORY);
                    curatorManage.restartTask(cirTask, okCallback);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("该任务不在“执行”状态，本次循环不执行···taskId=" + cirTask.getTaskId());
        }
    }

    private boolean isDealTime(TaskBean cirTask) throws ParseException {
        // 上一次执行的时间
        long lastDealTimestamp = cirTask.getLastDealTimestamp();
        Date lastDate = new Date(lastDealTimestamp);

        // 当前时间
        Date curDate = new Date();

        // 先处理开始时间
        boolean afterStartTime = false;
        String startTime = cirTask.getStartTime();
        if (StringUtils.isEmpty(startTime)) {
            afterStartTime = true;
        } else {
            CronExpression startExpression = new CronExpression(startTime);
            Date nextDate = startExpression.getNextValidTimeAfter(lastDate);
            afterStartTime = curDate.after(nextDate);
        }

        // 再处理结束时间
        boolean beforeEndTime = false;
        String endTime = cirTask.getEndTime();
        if (StringUtils.isEmpty(endTime)) {
            beforeEndTime = true;
        } else {
            CronExpression endExpression = new CronExpression(endTime);
            Date endDate = endExpression.getNextValidTimeAfter(lastDate);
            beforeEndTime = curDate.before(endDate);
        }

        return afterStartTime && beforeEndTime;
    }
}

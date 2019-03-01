package com.sncfc.crawler.manage.start;

import com.sncfc.crawler.manage.bean.*;
import com.sncfc.crawler.manage.task.dao.ITaskDetailDao;
import com.sncfc.crawler.manage.util.Commons;
import com.sncfc.crawler.manage.util.GsonUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * “上线任务”处理类，负责处理“上线任务”，生成“任务单元”
 *
 * @author a
 */
public class ManageTaskCache {
    /**
     * 用来标记当前任务是否处于“启用”状态
     */
    private boolean running;

    /**
     * 用来标记当前任务是否已经生成全部的“任务单元”
     */
    private boolean done;

    /**
     * queue中缓存的“任务单元”数量等于worker的数量乘以该“倍数”
     */
    private final static int MULTIPLE = 1000;

    private TaskBean taskBean;

    private TaskDetailBean taskDetailBean;

    private List<List<TaskParamInfo>> paramLists;

    /**
     * task_detail表对应的数据库操作类，用来记录当前任务被处理的进度
     */
    private ITaskDetailDao taskDetailDao;

    /**
     * “任务单元”缓存
     */
    private ConcurrentLinkedQueue<TaskUnitInfo> queue;

    /**
     * 类似百度ak类型的参数缓存
     */
    private Map<String, ConcurrentLinkedQueue<Map<String, String>>> akQueueMap;

    public ManageTaskCache(TaskBean taskBean, ITaskDetailDao taskDetailDao) {
        this.taskBean = taskBean;
        this.taskDetailDao = taskDetailDao;

        this.queue = new ConcurrentLinkedQueue<TaskUnitInfo>();

        this.akQueueMap = new HashMap<String, ConcurrentLinkedQueue<Map<String, String>>>();

        restart();
    }

    /**
     * 重启该任务
     */
    public void restart() {
        this.running = true;

        this.taskDetailBean = taskDetailDao.searchTaskDetail(taskBean
                .getTaskId());
        this.taskBean.setStartTime(taskDetailBean.getStartTime());
        this.taskBean.setEndTime(taskDetailBean.getEndTime());

        // 如果该任务的stopFloor字段为空，通过urlParams字段来构造参数
        if (StringUtils.isEmpty(taskDetailBean.getStopFloor())) {
            this.paramLists = GsonUtils.getTaskParamsList(taskDetailBean
                    .getUrlParams());
        } else {
            // 如果该任务的stopFloor字段不为空，为断点续采，所以要通过stopFloor字段来构造参数
            this.paramLists = GsonUtils.getTaskParamsList(taskDetailBean
                    .getStopFloor());
        }
    }

    /**
     * 循环任务，每次循环执行时，需要从头开始，要将参数置为原始参数而不是用stoopFloor
     */
    public void refreshParamLists() {
        this.paramLists = GsonUtils.getTaskParamsList(taskDetailBean
                .getUrlParams());
    }

    /**
     * 停止该任务
     */
    public void stop() {
        this.running = false;
    }

    /**
     * 返回该任务是否处于“运行”状态
     */
    public boolean isRunning() {
        return running;
    }

    public TaskBean getTaskBeanCopy() {
        TaskBean taskBeanCopy = new TaskBean();
        taskBeanCopy.setTaskId(taskBean.getTaskId());
        taskBeanCopy.setStatus(taskBean.getStatus());
        taskBeanCopy.setTaskType(taskBean.getTaskType());
        taskBeanCopy.setCirculation(taskBean.getCirculation());
        taskBeanCopy.setTaskNodePath(taskBean.getTaskNodePath());
        taskBeanCopy.setMissionNodePath(taskBean.getMissionNodePath());

        return taskBeanCopy;
    }

    /**
     * 获取“任务单元”，并根据采集机器的数量来缓存相应数量的“任务单元”
     */
    public synchronized TaskUnitInfo getNextTaskUnitInfo(int workerCount) {
        if (queue.size() == 0) {
            makeTaskUnitToQueue();
        }

        return queue.poll();
    }

    private void makeTaskUnitToQueue() {
        // 任务类型，“单页”或是“多页”的标识码
        int taskType = taskBean.getTaskType();
        if (Commons.TASK_TYPE_SINGLE == taskType) {
            if (!done) {
                TaskUnitInfo taskUnit = new TaskUnitInfo();

                taskUnit.setTaskId(taskDetailBean.getTaskId());
                taskUnit.setTacticsId(taskDetailBean.getTacticsId());
                taskUnit.setCharset(taskDetailBean.getCharset());
                taskUnit.setUrlHost(taskDetailBean.getUrlHost());
                taskUnit.setUrl(taskDetailBean.getUrlPath());
                taskUnit.setParseType(taskDetailBean.getParseType());
                taskUnit.setParseDetail(taskDetailBean.getParseDetail());
                taskUnit.setSleepTime(taskDetailBean.getSleepTime());

                queue.offer(taskUnit);

                done = true;
            }
        } else if (Commons.TASK_TYPE_MULTIPLE == taskType) {
            List<TaskUnitInfo> taskUnits = getAllFinalUrls();
            for (TaskUnitInfo taskUnit : taskUnits) {
                queue.offer(taskUnit);
            }
        } else {
            // do something
        }

    }

    private List<TaskUnitInfo> getAllFinalUrls() {
        List<TaskUnitInfo> taskUnitAll = new ArrayList<TaskUnitInfo>();

        // 每次执行URL构建时，都要将上一次构建到的位置信息保存到任务的stopFloor字段
        taskDetailDao.updateTaskDetail(taskBean.getTaskId(),
                GsonUtils.toJson(paramLists));

        for (List<TaskParamInfo> paramList : paramLists) {
            List<TaskUnitInfo> taskUnits = getParamFinalUrl(paramList);

            taskUnitAll.addAll(taskUnits);
        }

        return taskUnitAll;
    }

    private List<TaskUnitInfo> getParamFinalUrl(List<TaskParamInfo> paramList) {
        List<TaskUnitInfo> finalTaskUnits = new ArrayList<TaskUnitInfo>();
        if (paramList == null || paramList.size() == 0) {
            return finalTaskUnits;
        }

        List<String> finalUrls = new ArrayList<String>();

        String url = taskDetailBean.getUrlPath();

        TaskBetweenParamInfo betweenParamInfo = null;
        TaskBetweenParamInfo lastBetweenParamInfo = null;
        Map<String, List<Map<String, String>>> dbParams = new HashMap<String, List<Map<String, String>>>();
        for (TaskParamInfo taskParamInfo : paramList) {
            // 获取参数的类型
            int paramType = taskParamInfo.getType();
            if (Commons.TASK_PARAMS_TYPE_SINGLE == paramType) {
                url = url.replace(taskParamInfo.getKey(),
                        taskParamInfo.getValue());
                taskParamInfo.setFrom(taskParamInfo.getFrom() + 1);
            } else if (Commons.TASK_PARAMS_TYPE_BETWEEN == paramType) {
                TaskBetweenParamInfo nextTaskBetweenParamInfo = new TaskBetweenParamInfo();

                if (betweenParamInfo == null) {
                    betweenParamInfo = new TaskBetweenParamInfo();
                    lastBetweenParamInfo = betweenParamInfo;
                }

                lastBetweenParamInfo.setTaskParamInfo(taskParamInfo);
                lastBetweenParamInfo
                        .setNextTaskBetweenParamInfo(nextTaskBetweenParamInfo);

                lastBetweenParamInfo = nextTaskBetweenParamInfo;
            } else if (Commons.TASK_PARAMS_TYPE_DB_NOMAL == paramType) {
                List<Map<String, String>> dbParamList = getParamsFromDB(taskParamInfo);
                dbParams.put(taskParamInfo.getKey(), dbParamList);
            } else if (Commons.TASK_PARAMS_TYPE_DB_AK == paramType) {
                Map<String, String> akMap = getAkMapFromDB(taskParamInfo);
                for (String key : akMap.keySet()) {
                    url = url.replace(key, akMap.get(key));
                }
            } else {
                // do something
            }
        }
        finalUrls.add(url);

        // 如果存在参数类型为1（范围取值）的参数，就通过这些参数构造网址
        if (betweenParamInfo != null) {
            dealNextBetweemParam(betweenParamInfo, finalUrls, true);
        }

        // 如果参数参数类型为2（从数据库读取）的参数，就通过这些参数来构造带附加参数的“任务单元”
        // 否则就构造不带附加参数的普通“任务单元”
        if (dbParams.size() > 0) {
            finalTaskUnits.addAll(dealDBParams(dbParams, finalUrls));
        } else {
            if (betweenParamInfo == null) {
                TaskParamInfo taskParamInfo = paramList.get(0);
                if (taskParamInfo.getFrom() > 1) {
                    finalUrls.clear();
                }
            }

            for (String finalUrl : finalUrls) {
                TaskUnitInfo taskUnit = getOneTaskUnitInfo(finalUrl);

                finalTaskUnits.add(taskUnit);
            }
        }

        return finalTaskUnits;
    }

    /**
     * 挨个处理可变参数
     * betweenParam 本轮参数;
     * finalUrls 构建网址的结果集;
     * resume 上一轮参数是否重新开始计数;
     */
    private void dealNextBetweemParam(TaskBetweenParamInfo taskBetweenParam,
                                      List<String> finalUrls, boolean lastResume) {
        // 获取下一个包装参数
        TaskBetweenParamInfo nextTaskBetweenParamInfo = taskBetweenParam
                .getNextTaskBetweenParamInfo();
        // 根据下一个包装参数获取下一个参数
        TaskParamInfo nextTaskParamInfo = nextTaskBetweenParamInfo
                .getTaskParamInfo();

        // 获取当前参数
        TaskParamInfo taskParamInfo = taskBetweenParam.getTaskParamInfo();

        // 当前参数是否需要重新计数
        boolean curResume = false;

        int interval = taskParamInfo.getInterval();
        String[] pages = taskParamInfo.getValue().split("-");
        int start = Integer.parseInt(pages[0]);
        int end = Integer.parseInt(pages[1]);

        // 获取本轮当前参数的开始值
        int from = taskParamInfo.getFrom() >= start ? taskParamInfo.getFrom()
                : start;

        // 当from大于end时，说明当前参数在本轮所有的可能性都被处理过了
        if (from > end) {
            // 如果还存在下一个参数，则当前参数要从头开始
            if (nextTaskParamInfo != null) {
                from = start;
            } else {
                // 如果不存在下一个参数。则说明全部参数的全部可能性都被处理过了，应该清空结果集后返回
                finalUrls.clear();
                return;
            }
        }

        // 生成本轮当前参数的结束值
        // 如果该值大于本轮当前参数的最大值，则说明本轮是该参数的最后一轮
        int to = from + MULTIPLE;
        if (to > end) {
            to = end + 1;

            curResume = true;
        } else {
            curResume = false;
        }

        List<String> paramUrls = new ArrayList<String>();
        for (int i = from; i < to; i++) {
            for (String finalUrl : finalUrls) {
                paramUrls.add(finalUrl.replace(taskParamInfo.getKey(),
                        String.valueOf(i * interval)));
            }
        }

        finalUrls.clear();
        finalUrls.addAll(paramUrls);

        // 如果本轮的上一个参数执行了最后一轮，那么当前参数要记录已处理到的位置
        if (lastResume) {
            taskParamInfo.setFrom(to);
        }

        if (nextTaskParamInfo != null) {
            dealNextBetweemParam(nextTaskBetweenParamInfo, finalUrls, curResume);
        }
    }

    private List<Map<String, String>> getParamsFromDB(
            TaskParamInfo taskParamInfo) {
        List<Map<String, String>> dbParams = taskDetailDao.getParamsFromBD(
                taskParamInfo, MULTIPLE);

        int count = dbParams.size();
        if (count > 0) {
            int from = taskParamInfo.getFrom();
            taskParamInfo.setFrom(from + count);
        }

        return dbParams;
    }

    private List<TaskUnitInfo> dealDBParams(
            Map<String, List<Map<String, String>>> dbParams,
            List<String> finalUrls) {
        List<TaskUnitInfo> finalTaskUnits = new ArrayList<TaskUnitInfo>();

        for (String finalUrl : finalUrls) {
            TaskUnitInfo taskUnit = getOneTaskUnitInfo(finalUrl);

            finalTaskUnits.add(taskUnit);
        }

        for (String key : dbParams.keySet()) {
            List<Map<String, String>> singDBParams = dbParams.get(key);

            String[] keyArr = key.split(Commons.TASK_PARAMS_KEY_SEPARATOR);

            dealFinalUrls(keyArr, singDBParams, finalTaskUnits);
        }

        return finalTaskUnits;
    }

    private void dealFinalUrls(String[] keyArr,
                               List<Map<String, String>> singDBParams,
                               List<TaskUnitInfo> finalTaskUnits) {

        List<TaskUnitInfo> dealTaskUnits = new ArrayList<TaskUnitInfo>();

        for (TaskUnitInfo taskUnit : finalTaskUnits) {
            String url = taskUnit.getUrl();
            for (Map<String, String> dbParam : singDBParams) {
                String dealUrl = url;
                for (String singKey : keyArr) {
                    dealUrl = dealUrl.replace(singKey, dbParam.get(singKey));
                }

                TaskUnitInfo dealTaskUnit = getOneTaskUnitInfo(dealUrl);

                // 构造新的“任务单元”dealTaskUnit的新params
                Map<String, String> dealParams = new HashMap<String, String>();
                // 先判断原“任务单元”taskUnit有没有params，如果有就合并进来
                Map<String, String> params = taskUnit.getParams();
                if (params != null) {
                    for (String paramKey : params.keySet()) {
                        dealParams.put(paramKey, params.get(paramKey));

                    }
                }
                // 将当前的dbParam合并进来
                for (String dbKey : dbParam.keySet()) {
                    dealParams.put(dbKey, dbParam.get(dbKey));
                }

                dealTaskUnit.setParams(dealParams);

                dealTaskUnits.add(dealTaskUnit);
            }
        }

        finalTaskUnits.clear();
        finalTaskUnits.addAll(dealTaskUnits);
    }

    private Map<String, String> getAkMapFromDB(TaskParamInfo taskParamInfo) {
        String key = taskParamInfo.getKey();
        String value = taskParamInfo.getValue();

        ConcurrentLinkedQueue<Map<String, String>> curAkQueue = akQueueMap
                .get(key);

        if (curAkQueue == null) {
            curAkQueue = new ConcurrentLinkedQueue<Map<String, String>>();

            curAkQueue.addAll(taskDetailDao.getAksFromBD(value));
            akQueueMap.put(key, curAkQueue);
        }

        Map<String, String> akMap = curAkQueue.poll();
        if (akMap != null) {
            curAkQueue.offer(akMap);
        }

        return akMap;
    }

    private TaskUnitInfo getOneTaskUnitInfo(String finalUrl) {
        TaskUnitInfo taskUnit = new TaskUnitInfo();

        taskUnit.setUrl(finalUrl);
        taskUnit.setTaskId(taskDetailBean.getTaskId());
        taskUnit.setTacticsId(taskDetailBean.getTacticsId());
        taskUnit.setCharset(taskDetailBean.getCharset());
        taskUnit.setUrlHost(taskDetailBean.getUrlHost());
        taskUnit.setParseType(taskDetailBean.getParseType());
        taskUnit.setParseDetail(taskDetailBean.getParseDetail());
        taskUnit.setSleepTime(taskDetailBean.getSleepTime());
        taskUnit.setRequestHeader(taskDetailBean.getRequestHeader());

        return taskUnit;
    }
}

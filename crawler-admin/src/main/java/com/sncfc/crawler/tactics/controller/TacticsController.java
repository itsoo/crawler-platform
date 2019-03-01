package com.sncfc.crawler.tactics.controller;

import com.google.gson.reflect.TypeToken;
import com.sncfc.crawler.bean.Result;
import com.sncfc.crawler.bean.TacticsBean;
import com.sncfc.crawler.db.Filter;
import com.sncfc.crawler.tactics.service.TacticsService;
import com.sncfc.crawler.util.Commons;
import com.sncfc.crawler.util.GsonUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("tactics")
public class TacticsController {
    private static final Logger logger = Logger
            .getLogger(TacticsController.class);

    @Autowired
    TacticsService tacticsService;

    @RequestMapping("/search")
    public Result<List<TacticsBean>> getTactics(
            @RequestParam("item") final String item,
            @RequestParam("pageNo") final int pageNo,
            @RequestParam("pageSize") final int pageSize,
            final HttpServletRequest request) {

        logger.info("/tactics/search?item=" + item);

        List<Filter> searchFs = GsonUtils.getGson().fromJson(item,
                new TypeToken<List<Filter>>() {
                }.getType());

        List<TacticsBean> list = tacticsService.searchTactics(searchFs, pageNo,
                pageSize);
        long count = tacticsService.searchTacticsCount(searchFs);

        // 对于获取结果集成功与否做相应的处理
        if (list == null || count < 0) {
            return new Result<List<TacticsBean>>(Commons.RESULT_CODE_FAILED,
                    "获取策略结果集失败了");
        } else {
            return new Result<List<TacticsBean>>(Commons.RESULT_CODE_OK, count,
                    list);
        }
    }

    @RequestMapping("/searchOptions")
    public Result<List<TacticsBean>> getTacticsOptions(
            final HttpServletRequest request) {
        logger.info("/tactics/searchOptions");

        List<TacticsBean> list = tacticsService.searchTacticsOptions();

        // 对于获取结果集成功与否做相应的处理
        if (list == null) {
            return new Result<List<TacticsBean>>(Commons.RESULT_CODE_FAILED,
                    "获取策略选项集失败了");
        } else {
            return new Result<List<TacticsBean>>(Commons.RESULT_CODE_OK, 1,
                    list);
        }
    }

    @RequestMapping("/add")
    public Result<Integer> addTactics(@RequestParam("item") final String item,
                                      final HttpServletRequest request) {

        logger.info("/tactics/add?item=" + item);

        TacticsBean tacticsBean = GsonUtils.getGson().fromJson(item,
                new TypeToken<TacticsBean>() {
                }.getType());

        if (StringUtils.isEmpty(tacticsBean.getTacticsName())) {
            return new Result<Integer>(Commons.RESULT_CODE_FAILED, "策略名称不能为空");
        }

        int count = tacticsService.addTactics(tacticsBean);

        // 对于获取结果集成功与否做相应的处理
        if (count < 0) {
            return new Result<Integer>(Commons.RESULT_CODE_FAILED, "插入策略失败了");
        } else {
            return new Result<Integer>(Commons.RESULT_CODE_OK, count, count);
        }
    }

    @RequestMapping("/delete")
    public Result<Integer> deleteTactics(
            @RequestParam("tacticsId") final long tacticsId,
            final HttpServletRequest request) {

        logger.info("/tactics/delete?tacticsId=" + tacticsId);

        // 内置的策略是不能够删除的，如果待删除的策略的ID小于或等于内置策略ID的最大值，则直接返回
        if (tacticsId <= Commons.DEFAULT_TACTICS_ID_MAX) {
            return new Result<Integer>(Commons.RESULT_CODE_FAILED,
                    "内置策略不可以被删除");
        }

        // 先查看使用了该采集策略的任务的数量，如果为0才可以删除
        int taskCount = tacticsService.searchTaskCountByTacticsId(tacticsId);
        if (taskCount == 0) {
            int count = tacticsService.deleteTactics(tacticsId);

            // 对于获取结果集成功与否做相应的处理
            if (count < 0) {
                return new Result<Integer>(Commons.RESULT_CODE_FAILED,
                        "删除策略失败了");
            } else {
                return new Result<Integer>(Commons.RESULT_CODE_OK, "", 0, count);
            }
        } else {
            return new Result<Integer>(Commons.RESULT_CODE_FAILED,
                    "该策略正在被使用，不能被删除");
        }
    }

    @RequestMapping("/update")
    public Result<Integer> updateTactics(
            @RequestParam("tacticsId") final long tacticsId,
            @RequestParam("status") final int status,
            final HttpServletRequest request) {

        logger.info("/tactics/delete?tacticsId=" + tacticsId + ", status="
                + status);

        // 如果要停用某个采集策略，要先知道有没有任务使用了该策略，如果有就不可以停用该策略
        if (status == 0) {
            int taskCount = tacticsService
                    .searchTaskCountByTacticsId(tacticsId);
            if (taskCount > 0) {
                return new Result<Integer>(Commons.RESULT_CODE_FAILED,
                        "该策略正在被使用，不能被停止");
            }

            if (taskCount < 0) {
                return new Result<Integer>(Commons.RESULT_CODE_FAILED,
                        "查询使用该策略的任务数量时出错，请稍后再试");
            }
        }

        int count = tacticsService.updateTactics(tacticsId, status);

        // 对于获取结果集成功与否做相应的处理
        if (count < 0) {
            return new Result<Integer>(Commons.RESULT_CODE_FAILED, "更新策略失败了");
        } else {
            return new Result<Integer>(Commons.RESULT_CODE_OK, count, count);
        }
    }

    @RequestMapping("/getTaskIds")
    public Result<String> getTaskIds(
            @RequestParam("tacticsId") final long tacticsId,
            final HttpServletRequest request) {
        logger.info("/tactics/getTaskIds?tacticsId=" + tacticsId);

        String taskIds = tacticsService.searchTaskIdsByTacticsId(tacticsId);

        // 对于获取结果集成功与否做相应的处理
        return new Result<String>(Commons.RESULT_CODE_OK, 1, taskIds);
    }
}
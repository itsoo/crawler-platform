package com.sncfc.crawler.worker.spider.tactics;

import com.sncfc.crawler.worker.bean.ParseDetailInfo;
import com.sncfc.crawler.worker.bean.ResultInfo;
import com.sncfc.crawler.worker.bean.TaskUnitInfo;
import com.sncfc.crawler.worker.mq.IMQClient;
import com.sncfc.crawler.worker.spider.fetcher.AnalysisWebPage;
import com.sncfc.crawler.worker.spider.fetcher.MyHTMLFetcher;
import com.sncfc.crawler.worker.util.Commons;
import com.sncfc.crawler.worker.util.GsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * 内置采集策略一，通过httpClient处理get请求方式静态网页的采集
 *
 * @author a
 */
public class Tactics1 extends Tactics {
    private final static Logger logger = Logger.getLogger(Tactics1.class);

    /**
     * 采集策略的序号标记
     */
    public static final long TACTICS_NUM = 1;

    /**
     * 网页源码处理类
     */
    private AnalysisWebPage analysisWebPage;

    /**
     * 访问互联网的功能类
     */
    private MyHTMLFetcher fetcher;

    public Tactics1(IMQClient mqClient) {
        super(mqClient);
        this.analysisWebPage = new AnalysisWebPage();
        this.fetcher = new MyHTMLFetcher();
    }

    @Override
    public void execute(TaskUnitInfo taskUnit) {
        // 处理“任务单元”附带的信息
        int parseType = taskUnit.getParseType();

        long taskId = taskUnit.getTaskId();
        long sleepTime = taskUnit.getSleepTime();

        String url = taskUnit.getUrl();
        String charset = taskUnit.getCharset();
        String parseDetail = taskUnit.getParseDetail();

        Map<String, String> requestHeader = GsonUtils.getMap(taskUnit
                .getRequestHeader());
        // 先访问网路，获取网页源码
        byte[] result = fetcher.doGetMethod(url, requestHeader);

        // 如果采集的网页源码为空，则发送一个错误信息后直接结束该方法
        if (result == null) {
            ResultInfo resultInfo = new ResultInfo();
            resultInfo.setCode(Commons.RESULT_CODE_ERROR);
            resultInfo.setTaskId(taskId);
            resultInfo.setUrlPath(url);
            resultInfo.setDesc("没有获取到网页源码");

            dealResultInfo(resultInfo);

            return;
        }

        if (Commons.PARSE_TYPE_XPATH == parseType) {
            ParseDetailInfo parseDetailInfo = GsonUtils.get(parseDetail,
                    ParseDetailInfo.class);
            String tableName = parseDetailInfo.getTableName();

            Map<String, String> itemXpath = parseDetailInfo.getItemXpath();

            // 如果解析详情的urlListXpath字段值为空，则说明该网页不是列表页，只需采集本网页详情即可
            if (StringUtils.isEmpty(parseDetailInfo.getUrlListXpath())) {

                List<Map<String, String>> columns = analysisWebPage
                        .analysisItemByXpath(result, itemXpath, charset);

                ResultInfo resultInfo = new ResultInfo();
                resultInfo.setTaskId(taskId);
                resultInfo.setUrlPath(url);

                if (columns.size() == 0) {
                    resultInfo.setCode(Commons.RESULT_CODE_NO_ITEM);
                    try {
                        String desc = new String(result, charset);
                        resultInfo.setDesc(desc);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    resultInfo.setTableName(tableName);
                    resultInfo.setColumns(columns);
                    resultInfo.setCode(Commons.RESULT_CODE_OK);
                }

                dealResultInfo(resultInfo);
            } else {
                List<String> itemUrls = analysisWebPage.analysisListByXpath(
                        result, parseDetailInfo, url);

                // 如果没有得到列表，则发送一个错误信息接直接结束该方法
                if (itemUrls.size() == 0) {
                    ResultInfo resultInfo = new ResultInfo();
                    resultInfo.setCode(Commons.RESULT_CODE_NO_URLS);
                    resultInfo.setTaskId(taskId);
                    resultInfo.setUrlPath(url);

                    try {
                        String desc = new String(result, charset);
                        resultInfo.setDesc(desc);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    dealResultInfo(resultInfo);

                    return;
                }

                for (String itemUrl : itemUrls) {
                    // 网站的采集间隔，休息一下再爬取
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    byte[] itemResult = fetcher.doGetMethod(itemUrl,
                            requestHeader);
                    // 如果采集的网页源码为空，则发送一个错误信息后直接结束本轮,进入下一轮
                    if (itemResult == null) {
                        ResultInfo resultInfo = new ResultInfo();
                        resultInfo.setCode(Commons.RESULT_CODE_ERROR);
                        resultInfo.setTaskId(taskId);
                        resultInfo.setUrlPath(itemUrl);
                        resultInfo.setDesc("没有获取到网页源码");

                        dealResultInfo(resultInfo);

                        continue;
                    }

                    List<Map<String, String>> columns = analysisWebPage
                            .analysisItemByXpath(itemResult, itemXpath, charset);

                    ResultInfo resultInfo = new ResultInfo();
                    resultInfo.setTaskId(taskId);
                    resultInfo.setUrlPath(itemUrl);

                    if (columns.size() == 0) {
                        resultInfo.setCode(Commons.RESULT_CODE_NO_ITEM);
                        try {
                            String desc = new String(itemResult, charset);
                            resultInfo.setDesc(desc);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        resultInfo.setTableName(tableName);
                        resultInfo.setColumns(columns);
                        resultInfo.setCode(Commons.RESULT_CODE_OK);
                    }

                    dealResultInfo(resultInfo);
                }
            }
        } else if (Commons.PARSE_TYPE_JS == parseType) {
            if (parseDetail.trim().startsWith(Commons.PAESE_DETAIL_LIST_START)) {
                List<String> itemUrls = analysisWebPage.analysisListByJS(
                        result, charset, parseDetail);

                // 如果没有得到列表，则发送一个错误信息
                if (itemUrls.size() == 0) {
                    ResultInfo resultInfo = new ResultInfo();
                    resultInfo.setCode(Commons.RESULT_CODE_NO_URLS);
                    resultInfo.setTaskId(taskId);
                    resultInfo.setUrlPath(url);

                    try {
                        String desc = new String(result, charset);
                        resultInfo.setDesc(desc);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    dealResultInfo(resultInfo);

                    return;
                }

                for (String itemUrl : itemUrls) {
                    // 网站的采集间隔，休息一下再爬取
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    byte[] itemResult = fetcher.doGetMethod(itemUrl,
                            requestHeader);
                    // 如果采集的网页源码为空，则发送一个错误信息后直接结束本轮,进入下一轮
                    if (itemResult == null) {
                        ResultInfo resultInfo = new ResultInfo();
                        resultInfo.setCode(Commons.RESULT_CODE_ERROR);
                        resultInfo.setTaskId(taskId);
                        resultInfo.setUrlPath(itemUrl);
                        resultInfo.setDesc("没有获取到网页源码");

                        dealResultInfo(resultInfo);

                        continue;
                    }

                    ResultInfo resultInfo = analysisWebPage.analysisItemByJS(
                            itemResult, charset, parseDetail);
                    resultInfo.setTaskId(taskId);
                    resultInfo.setUrlPath(itemUrl);

                    List<Map<String, String>> columns = resultInfo.getColumns();
                    if (columns.size() == 0) {
                        resultInfo.setCode(Commons.RESULT_CODE_NO_ITEM);

                        try {
                            String desc = new String(result, charset);
                            resultInfo.setDesc(desc);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        resultInfo.setCode(Commons.RESULT_CODE_OK);
                    }

                    dealResultInfo(resultInfo);
                }
            } else {
                ResultInfo resultInfo = analysisWebPage.analysisItemByJS(
                        result, charset, parseDetail);
                resultInfo.setTaskId(taskId);
                resultInfo.setUrlPath(url);

                List<Map<String, String>> columns = resultInfo.getColumns();
                if (columns.size() == 0) {
                    resultInfo.setCode(Commons.RESULT_CODE_NO_ITEM);

                    try {
                        String desc = new String(result, charset);
                        resultInfo.setDesc(desc);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    resultInfo.setCode(Commons.RESULT_CODE_OK);
                }

                dealResultInfo(resultInfo);
            }
        } else {
            // 暂时打印一个错误日志，后续可扩展
            logger.error("错误的网页解析方式，parseType=" + parseType);
        }
    }
}

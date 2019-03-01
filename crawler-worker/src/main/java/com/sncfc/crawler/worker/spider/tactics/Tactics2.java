package com.sncfc.crawler.worker.spider.tactics;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.sncfc.crawler.worker.bean.ParseDetailInfo;
import com.sncfc.crawler.worker.bean.ResultInfo;
import com.sncfc.crawler.worker.bean.TaskUnitInfo;
import com.sncfc.crawler.worker.mq.IMQClient;
import com.sncfc.crawler.worker.util.Commons;
import com.sncfc.crawler.worker.util.DomainUtil;
import com.sncfc.crawler.worker.util.GsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 内置采集策略二，通过htmlunit处理动态网页的采集
 *
 * @author a
 */
public class Tactics2 extends Tactics {
    /**
     * 采集策略的序号标记
     */
    public static final long TACTICS_NUM = 2;

    /**
     * 抓取内容时要去掉某些节点
     */
    private HashSet<String> removeTags;

    /**
     * 换行的TAG
     */
    private HashSet<String> newLineTagsSet;

    public Tactics2(IMQClient mqClient) {
        super(mqClient);
        removeTags = new HashSet<String>();
        removeTags.add("SCRIPT");
        removeTags.add("STYLE");
        removeTags.add("FORM");

        newLineTagsSet = new HashSet<String>();

        // 换行的TAG
        String[] newLineTags = {"P", "BR", "TABLE", "UL", "LI", "TR", "DL",
                "DD", "DT", "DIV", "CENTER"};

        for (int i = 0; i < newLineTags.length; i++) {
            newLineTagsSet.add(newLineTags[i]);
        }
    }

    @Override
    public void execute(TaskUnitInfo taskUnit) {
        // 处理“任务单元”附带的信息
        int parseType = taskUnit.getParseType();

        long taskId = taskUnit.getTaskId();
        long sleepTime = taskUnit.getSleepTime();

        String url = taskUnit.getUrl();
        // String chatset = taskUnit.getCharset();
        String parseDetail = taskUnit.getParseDetail();

        // 访问网络
        WebClient webClient = new WebClient(BrowserVersion.CHROME);

        webClient.setJavaScriptTimeout(30 * 1000);
        // 设置连接超时时间,这里是10S。如果为0，则无限期等待
        webClient.getOptions().setTimeout(10000);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.waitForBackgroundJavaScript(20 * 1000);

        // 用来标记当前正在访问的网址，做错误信息记录
        String currentUrl = url;
        try {
            HtmlPage page = webClient.getPage(url);

            if (Commons.PARSE_TYPE_XPATH == parseType) {
                ParseDetailInfo parseDetailInfo = GsonUtils.get(parseDetail,
                        ParseDetailInfo.class);

                String tableName = parseDetailInfo.getTableName();

                Map<String, String> itemXpath = parseDetailInfo.getItemXpath();

                // 如果解析详情的urlListXpath字段值为空，则说明该网页不是列表页，只需采集本网页详情即可
                if (StringUtils.isEmpty(parseDetailInfo.getUrlListXpath())) {
                    List<Map<String, String>> columns = getColumns(page,
                            itemXpath);

                    ResultInfo resultInfo = new ResultInfo();
                    resultInfo.setTaskId(taskId);
                    resultInfo.setUrlPath(url);

                    if (columns.size() == 0) {
                        resultInfo.setCode(Commons.RESULT_CODE_NO_ITEM);
                        resultInfo.setDesc(page.asXml());
                    } else {
                        resultInfo.setTableName(tableName);
                        resultInfo.setColumns(columns);
                        resultInfo.setCode(Commons.RESULT_CODE_OK);
                    }

                    dealResultInfo(resultInfo);

                    page.cleanUp();
                } else {
                    List<String> itemUrls = getUrlList(page, parseDetailInfo);

                    // 如果没有得到列表，则发送一个错误信息接直接结束该方法
                    if (itemUrls.size() == 0) {
                        ResultInfo resultInfo = new ResultInfo();
                        resultInfo.setCode(Commons.RESULT_CODE_NO_URLS);
                        resultInfo.setTaskId(taskId);
                        resultInfo.setUrlPath(url);
                        resultInfo.setDesc(page.asXml());

                        dealResultInfo(resultInfo);

                        page.cleanUp();

                        return;
                    }

                    for (String itemUrl : itemUrls) {
                        // 网站的采集间隔，休息一下再爬取
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // 当前访问网址变更
                        currentUrl = itemUrl;

                        HtmlPage itemPage = webClient.getPage(itemUrl);

                        List<Map<String, String>> columns = getColumns(
                                itemPage, itemXpath);

                        ResultInfo resultInfo = new ResultInfo();
                        resultInfo.setTaskId(taskId);
                        resultInfo.setUrlPath(itemUrl);

                        if (columns.size() == 0) {
                            resultInfo.setCode(Commons.RESULT_CODE_NO_ITEM);
                            resultInfo.setDesc(itemPage.asXml());
                        } else {
                            resultInfo.setTableName(tableName);
                            resultInfo.setColumns(columns);
                            resultInfo.setCode(Commons.RESULT_CODE_OK);
                        }

                        dealResultInfo(resultInfo);

                        itemPage.cleanUp();
                    }
                }
            }
        } catch (Exception e) {
            ResultInfo resultInfo = new ResultInfo();
            resultInfo.setCode(Commons.RESULT_CODE_ERROR);
            resultInfo.setTaskId(taskId);
            resultInfo.setUrlPath(currentUrl);
            resultInfo.setDesc(e.getMessage());

            dealResultInfo(resultInfo);
        } finally {
            webClient.close();
        }
    }

    private List<Map<String, String>> getColumns(HtmlPage page,
                                                 Map<String, String> itemXpath) {
        List<Map<String, String>> columns = new ArrayList<Map<String, String>>();

        // 解析内容的中间缓存map，用来构造单页面多条数据时的每条数据的column
        Map<Integer, HashMap<String, String>> cacheMap = new HashMap<Integer, HashMap<String, String>>();
        // 是否需要等待页面渲染
        boolean wait = true;
        for (String xpathKey : itemXpath.keySet()) {
            try {
                List<Node> columnNodes = page.getByXPath(itemXpath
                        .get(xpathKey));

                // 给页面5s的缓冲时间，每隔0.5秒检查一次，如果获取到元素就向下执行
                for (int i = 0; i < 10; i++) {
                    if ((columnNodes == null || columnNodes.size() == 0)
                            && wait) {
                        Thread.sleep(500);
                        columnNodes = page.getByXPath(itemXpath.get(xpathKey));
                    } else {
                        break;
                    }
                }
                // 只等待一次，取后续其他节点时不需要再等待
                wait = false;

                for (int i = 0; i < columnNodes.size(); i++) {
                    HashMap<String, String> column = cacheMap.get(i);
                    if (column == null) {
                        column = new HashMap<String, String>();
                        cacheMap.put(i, column);
                    }
                    column.put(xpathKey, getContent(columnNodes.get(i)).trim());
                }
            } catch (Exception e) {
                // do nothing
            }
        }

        for (Integer i : cacheMap.keySet()) {
            columns.add(cacheMap.get(i));
        }

        return columns;
    }

    private List<String> getUrlList(HtmlPage page, ParseDetailInfo parseDetail) {
        List<String> itemList = new ArrayList<String>();

        try {
            List<Node> urls = page.getByXPath(parseDetail.getUrlListXpath());

            // 给页面5s的缓冲时间，每隔0.5秒检查一次，如果获取到元素就向下执行
            for (int i = 0; i < 10; i++) {
                if (urls == null || urls.size() == 0) {
                    Thread.sleep(500);
                    urls = page.getByXPath(parseDetail.getUrlListXpath());
                } else {
                    break;
                }
            }

            String urlRegex = parseDetail.getUrlRegex();

            for (Node url : urls) {
                String itemUrl = DomainUtil.getAbsoluteURL(url.getNodeValue(),
                        page.getBaseURI(), parseDetail.getUrlAdd());

                // 如果网址的匹配正则为空，且itemUrl不为空，就不需要对itemUrl做规则验证，直接将itemUrl加入结果集
                if (StringUtils.isEmpty(urlRegex)
                        && !StringUtils.isEmpty(itemUrl)) {
                    itemList.add(itemUrl);
                }

                // 如果匹配的正则不为空，需要itemUrl不为空且满足正则匹配才会加入结果集
                if (!StringUtils.isEmpty(urlRegex)
                        && !StringUtils.isEmpty(itemUrl)
                        && Pattern.matches(urlRegex, itemUrl)) {
                    itemList.add(itemUrl);
                }
            }
        } catch (Exception e) {
            // do nothing
        }

        return itemList;
    }

    private String getContent(Node node) {
        if (node == null) {
            return "";
        }

        // 如果当前节点是要被去除的，就不抽取该节点的值
        if (removeTags.contains(node.getNodeName().toUpperCase())) {
            return "";
        }

        // 对于隐藏的内容也不抽取
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null && nnm.getNamedItem("style") != null) {
            // 隐藏样式
            if (nnm.getNamedItem("style")
                    .getTextContent()
                    .matches(
                            "(?i).*?display\\s*:\\s*none.*|.*?size\\s*:\\s*0[^\\d]+.*")) {
                return "";
            }
        }

        StringBuilder contents = new StringBuilder();
        if (node.getNodeType() == Node.TEXT_NODE) {
            contents.append(node.getTextContent());
        } else if (newLineTagsSet.contains(node.getNodeName().toUpperCase())) {
            contents.append("\n");
        }

        // 当前节点的值抽取完之后，要处理该节点的子孙节点
        Node child = node.getFirstChild();
        while (child != null) {
            contents.append(getContent(child));

            // 当前子节点处理完之后要挨个向下处理该节点的兄弟节点
            child = child.getNextSibling();
        }

        String text = contents.toString().replace("^\\s+", "")
                .replaceAll("[\\u00A0]", "").replaceAll("\n{2,}", "\n")
                .replaceAll("^　*", "");
        if (text.trim().length() == 0) {
            text = "\n";
        }

        return text;
    }
}

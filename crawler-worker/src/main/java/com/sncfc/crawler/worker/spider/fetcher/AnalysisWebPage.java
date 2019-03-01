package com.sncfc.crawler.worker.spider.fetcher;

import com.sncfc.crawler.worker.bean.ParseDetailInfo;
import com.sncfc.crawler.worker.bean.ResultInfo;
import com.sncfc.crawler.worker.util.Commons;
import com.sncfc.crawler.worker.util.DomainUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.regex.Pattern;

public class AnalysisWebPage {
    private final static Logger logger = Logger
            .getLogger(AnalysisWebPage.class);

    /**
     * 抓取内容时要去掉某些节点
     */
    private HashSet<String> removeTags;

    /**
     * 换行的TAG
     */
    private HashSet<String> newLineTagsSet;

    public AnalysisWebPage() {
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

    /**
     * 通过Xpath的方式解析网页源码，处理列表页
     */
    public List<String> analysisListByXpath(byte[] result,
                                            ParseDetailInfo parseDetail, String srcUrl) {
        List<String> itemList = new ArrayList<String>();

        try {
            DOMParser parser = initDOMParser();

            InputSource inputSource = new InputSource(new ByteArrayInputStream(
                    result));

            parser.parse(inputSource);
            Document document = parser.getDocument();
            if (document != null) {
                document.normalize();
            }
            Node domTree = document.getDocumentElement();
            NodeList urlList = XPathAPI.selectNodeList(domTree,
                    parseDetail.getUrlListXpath());

            String urlRegex = parseDetail.getUrlRegex();

            for (int i = 0; i < urlList.getLength(); i++) {
                String itemUrl = DomainUtil.getAbsoluteURL(urlList.item(i)
                        .getTextContent(), srcUrl, parseDetail.getUrlAdd());

                // 如果匹配的正则为空，且itemUrl不为空，就不需要对itemUrl做规则验证，直接将itemUrl加入结果集
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
            logger.error("analysisListByXpath方法执行异常了···" + e.getMessage());
        }

        return itemList;
    }

    /**
     * 通过Xpath的方式解析网页源码，处理详情页
     */
    public List<Map<String, String>> analysisItemByXpath(byte[] result,
                                                         Map<String, String> itemXpath, String charset) {
        List<Map<String, String>> columns = new ArrayList<Map<String, String>>();

        try {
            DOMParser parser = initDOMParser();

            InputSource inputSource = new InputSource(new ByteArrayInputStream(
                    result));

            if (StringUtils.isNotBlank(charset)) {
                inputSource.setEncoding(charset);
            }

            parser.parse(inputSource);
            Document document = parser.getDocument();
            if (document != null) {
                document.normalize();
            }
            Node domTree = document.getDocumentElement();

            // 解析内容的中间缓存map，用来构造单页面多条数据时的每条数据的column
            Map<Integer, HashMap<String, String>> cacheMap = new HashMap<Integer, HashMap<String, String>>();
            for (String columnXpath : itemXpath.keySet()) {
                NodeList columnNode = XPathAPI.selectNodeList(domTree,
                        itemXpath.get(columnXpath));
                for (int i = 0; i < columnNode.getLength(); i++) {
                    HashMap<String, String> column = cacheMap.get(i);
                    if (column == null) {
                        column = new HashMap<String, String>();
                        cacheMap.put(i, column);
                    }
                    column.put(columnXpath, getContent(columnNode.item(i))
                            .trim());
                }
            }

            for (Integer i : cacheMap.keySet()) {
                columns.add(cacheMap.get(i));
            }
        } catch (Exception e) {
            logger.error("analysisItemByXpath方法执行异常了···" + e.getMessage());
        }

        return columns;
    }

    /**
     * 通过JavaScript的方式解析网页源码，处理列表页
     */
    public List<String> analysisListByJS(byte[] result, String charset,
                                         String jsStr) {
        List<String> urlList = new ArrayList<String>();

        ScriptEngine engine = new ScriptEngineManager()
                .getEngineByName("JavaScript");
        try {
            engine.eval(jsStr);
            engine.put(Commons.PAESE_VARIABLE_NAME_LIST, urlList);
            Invocable inv = (Invocable) engine;
            inv.invokeFunction(Commons.PAESE_FUNCTION_NAME_LIST, new String(
                    result, charset));

        } catch (Exception e) {
            logger.error("analysisListByJS方法执行异常了···" + e.getMessage());
        }

        return urlList;
    }

    /**
     * 通过JavaScript的方式解析网页源码,处理详情页
     */
    public ResultInfo analysisItemByJS(byte[] result, String charset,
                                       String jsStr) {
        ResultInfo itemInfo = new ResultInfo();

        ScriptEngine engine = new ScriptEngineManager()
                .getEngineByName("JavaScript");

        try {
            engine.eval(jsStr);
            engine.put(Commons.PAESE_VARIABLE_NAME_ITEM, itemInfo);
            Invocable inv = (Invocable) engine;
            inv.invokeFunction(Commons.PAESE_FUNCTION_NAME_ITEM, new String(
                    result, charset));

        } catch (Exception e) {
            logger.error("analysisItemByJS方法执行异常了···" + e.getMessage());
        }

        return itemInfo;
    }

    private DOMParser initDOMParser() {
        DOMParser parser = new DOMParser();

        try {
            parser.setProperty(
                    "http://cyberneko.org/html/properties/names/elems", "upper");
            parser.setProperty(
                    "http://cyberneko.org/html/properties/names/attrs", "lower");
            parser.setFeature(
                    "http://cyberneko.org/html/features/balance-tags", true);

            parser.setFeature("http://xml.org/sax/features/namespaces", false);
        } catch (SAXNotRecognizedException e) {
            logger.error(e.getMessage());
        } catch (SAXNotSupportedException e) {
            logger.error(e.getMessage());
        }

        return parser;
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

        String text = contents.toString().replaceAll("^\\s+", "").replaceAll("[\\u00A0]", "")
                .replaceAll("\n{2,}", "\n").replaceAll("^　*", "");
        if (text.trim().length() == 0) {
            text = "\n";
        }

        return text;
    }
}

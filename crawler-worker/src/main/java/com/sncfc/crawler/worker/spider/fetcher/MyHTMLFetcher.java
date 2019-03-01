package com.sncfc.crawler.worker.spider.fetcher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 获取URL内容
 */
public class MyHTMLFetcher {
    private final static Logger logger = Logger.getLogger(MyHTMLFetcher.class);

    public byte[] doGetMethod(String url) {
        return doGetMethod(url, null, null);
    }

    public byte[] doGetMethod(String url, HttpHost proHost) {
        return doGetMethod(url, proHost, null);
    }

    public byte[] doGetMethod(String url, Map<String, String> requestHeaders) {
        return doGetMethod(url, null, requestHeaders);
    }

    public byte[] doGetMethod(String url, HttpHost proHost,
                              Map<String, String> requestHeaders) {
        RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(10000)
                .setConnectTimeout(10000).setConnectionRequestTimeout(10000).build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();

        HttpGet request = new HttpGet(url);

        RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();
        request.setConfig(requestConfig);

        // 如果需要就设置代理
        if (proHost != null) {
            RequestConfig config = RequestConfig.custom().setProxy(proHost)
                    .build();
            request.setConfig(config);
        }

        // 设置消息头
        request.addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0");
        // 设置额外的消息头，某些站点可能需要
        if (requestHeaders != null) {
            for (String key : requestHeaders.keySet()) {
                request.addHeader(key, requestHeaders.get(key));
            }
        }

        byte[] result = null;

        try {
            HttpResponse response = httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                result = EntityUtils.toByteArray(response.getEntity());
            } else {
                logger.error("statusCode：" + statusCode + " URL：" + url);
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public byte[] doPostMethod(String url) {
        return doPostMethod(url, null, null);
    }

    public byte[] doPostMethod(String url, Map<String, String> requestHeaders) {
        return doPostMethod(url, null, requestHeaders);
    }

    public byte[] doPostMethod(String url, HttpHost proHost,
                               Map<String, String> requestHeaders) {

        RequestConfig defaultRequestConfig = RequestConfig.custom().setSocketTimeout(10000)
                .setConnectTimeout(10000).setConnectionRequestTimeout(10000).build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig).build();

        String[] paramsArray = url.split("\\?");

        HttpPost request = new HttpPost(paramsArray[0]);

        RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();
        request.setConfig(requestConfig);

        // 创建参数列表
        List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        if (paramsArray.length > 1) {
            String[] paArray = paramsArray[1].split("&");
            for (String temp : paArray) {
                String[] paramsMap = temp.split("=");
                if (paramsMap.length > 1) {
                    postParameters.add(new BasicNameValuePair(paramsMap[0],
                            paramsMap[1]));
                }

                if (paramsMap.length == 1) {
                    postParameters
                            .add(new BasicNameValuePair(paramsMap[0], ""));
                }
            }
        }

        // 如果需要就设置代理
        if (proHost != null) {
            RequestConfig config = RequestConfig.custom().setProxy(proHost)
                    .build();
            request.setConfig(config);
        }

        // 设置消息头
        request.addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0");
        // 设置额外的消息头，某些站点可能需要
        if (requestHeaders != null) {
            for (String key : requestHeaders.keySet()) {
                request.addHeader(key, requestHeaders.get(key));
            }
        }

        byte[] result = null;

        try {
            // 设置请求参数
            HttpEntity entity = new UrlEncodedFormEntity(postParameters,
                    "utf-8");
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                result = EntityUtils.toByteArray(response.getEntity());
            } else {
                logger.error("statusCode：" + statusCode + " URL：" + url);
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}

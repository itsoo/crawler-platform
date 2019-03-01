package com.sncfc.crawler.util;

import org.springframework.util.StringUtils;

/**
 * 对URL的一些操作集（提取主机、域名、相对URL转绝对URL）
 * 
 * @author LiWei
 * @see URL规则
 * 
 */
public class DomainUtils {

	/**
	 * 从URL中提取主机，如http://news.163.com/xxx/xxx.html --> news.163.com
	 * 
	 * @param url
	 *            输入的URL
	 * @return
	 */
	public static String getHost(String url) {
		if (url == null) {
			return null;
		}

		url = url.replaceAll("^https?://", "").replaceAll("([^/]+).*", "$1");

		return url;
	}

	/**
	 * 
	 * @param srcURL
	 *            需要转换的URL
	 * @param baseURL
	 *            srcURL所在的URL
	 * @return
	 */
	public static String getAbsoluteURL(String srcURL, String baseURL,
			String srcURLAdd) {
		if (baseURL == null || srcURL == null) {
			return srcURL;
		}
		if (srcURL.matches("(?i)mailto:.*"))
			return null;
		if (!StringUtils.isEmpty(srcURLAdd)) {
			srcURL = srcURLAdd + srcURL;
		}

		srcURL = srcURL.replaceAll("^(?:\"|\\./|\"\\./)|\"$", "");

		if (srcURL.startsWith("http://") || srcURL.startsWith("https://")) {
			return srcURL.trim();
		} else if (srcURL.matches("(?i)javascript:.*")) {
			return null;
		}

		String absoluteURL = null;

		String base_host = baseURL.replaceAll("(https?://[^/]+).*", "$1");
		String base_app = baseURL.replaceAll("https?://[^/]+(/(?:[^/]+/)*)?.*",
				"$1");
		if (!base_app.startsWith("/")) {
			base_app = "/" + base_app;
		}

		if (srcURL.startsWith("/")) {
			// add by dzy
			if (srcURL.startsWith("//")) {
				if (base_host.startsWith("https:")) {
					absoluteURL = "https:" + srcURL;
				} else {
					absoluteURL = "http:" + srcURL;
				}
			} else {
				absoluteURL = base_host + srcURL;
			}
		} else if (srcURL.startsWith("../")) {
			while (srcURL.startsWith("../")) {
				srcURL = srcURL.substring(3);
				base_app = base_app.replaceAll("[^/]+/$", "");
			}

			absoluteURL = base_host + base_app + srcURL;
		} else if (srcURL.startsWith("?")) {
			if (baseURL.indexOf("?") > 0) {
				absoluteURL = baseURL.substring(0, baseURL.indexOf("?"))
						+ srcURL;
			} else {
				absoluteURL = baseURL + srcURL;
			}
		} else {
			absoluteURL = base_host + base_app + srcURL;
		}

		return absoluteURL.trim();
	}

	/**
	 * 去除URL中不需要的参数信息
	 * 
	 * @param url
	 * @param params
	 * @return String
	 */
	public static String trimURL(String url, String params) {
		if (url == null || params == null) {
			return url;
		}
		StringBuilder urlBuilder = new StringBuilder();
		String[] urls = url.split("[&?]");
		for (int i = 0; i < urls.length; i++) {
			if (params.indexOf(urls[i].split("=")[0]) == -1) {
				if (i == 0) {
					urlBuilder.append(urls[i] + "?");
				} else {
					urlBuilder.append(urls[i] + "&");
				}
			}
		}
		if (urlBuilder.toString().endsWith("&")
				|| urlBuilder.toString().endsWith("?")) {
			urlBuilder.delete(urlBuilder.length() - 1, urlBuilder.length());
		}

		return urlBuilder.toString();
	}

	public static void main(String[] args) {
		System.out.println(DomainUtils.getAbsoluteURL("thread-3479351-1-1.html",
				"http://bbs.syweitao.com/thread/2", "/"));

	}
}

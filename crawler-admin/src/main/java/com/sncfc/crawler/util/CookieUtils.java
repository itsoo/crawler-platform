package com.sncfc.crawler.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * 处理Cookie的工具类
 * 
 * @author a
 *
 */
public class CookieUtils {

	/**
	 * 获取权限验证用的Cookie
	 * 
	 * @param tokenVale
	 * @return
	 */
	public static Cookie makeTokenCookie(String tokenVale) {
		return getCookie(Commons.COOKIE_TOKEN, tokenVale);
	}

	/**
	 * 获取Cookie中token字段的值
	 * 
	 * @param request
	 * @return
	 */
	public static String getTokenValue(final HttpServletRequest request) {
		return getCookieValueByName(request, Commons.COOKIE_TOKEN);
	}

	/**
	 * 用键值构造Cookie
	 * 
	 * @param tokenVale
	 * @return
	 */
	public static Cookie getCookie(String cookieName, String cookieValue) {
		return new Cookie(cookieName, cookieValue);
	}

	/**
	 * 根据字段名获取Cookie内的字段值
	 * 
	 * @param request
	 * @param name
	 * @return
	 */
	public static String getCookieValueByName(final HttpServletRequest request,
			String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie co : cookies) {
				if (co.getName().equals(name)) {
					return co.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * 根据token做权限鉴定，暂时不实现
	 * 
	 * @param tokenValue
	 * @return
	 */
	public static boolean checkToken(final HttpServletRequest request) {
		return true;
	}
}
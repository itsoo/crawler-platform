package com.sncfc.crawler.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

//import org.apache.log4j.Logger;

//import com.sncfc.crawler.util.Commons;
//import com.sncfc.crawler.util.CookieUtils;

@WebFilter(urlPatterns = "/*", filterName = "checkTokenFilter")
public class CheckTokenFilter implements Filter {
	// private static final Logger logger = Logger
	// .getLogger(CheckTokenFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
//		HttpServletRequest req = (HttpServletRequest) request;
//		HttpServletResponse resp = (HttpServletResponse) response;
//
//		String path = req.getRequestURI();
//		logger.info("========path==========" + path);
//
//		// 除了登陆接口不需要验证
//		if ((path.endsWith("test"))) {
//			chain.doFilter(request, response);
//			return;
//		}
//
//		// 其余接口都需要验证权限
//		String tokenValue = CookieUtil.getCookieValueByName(req,
//				Commons.COOKIE_TOKEN);
//		logger.info("========token==========" + tokenValue);
//		if ("qwe123".equals(tokenValue)) {
//			chain.doFilter(request, response);
//		} else {
//			logger.info("未授权用户");
//			resp.getWriter().write("拒绝访问");
//		}
	}

	@Override
	public void destroy() {

	}

}

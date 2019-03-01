package com.sncfc.crawler.user.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sncfc.crawler.bean.Result;
import com.sncfc.crawler.util.Commons;
import com.sncfc.crawler.util.CookieUtils;

@RestController
@RequestMapping("user")
public class UserController {
	int count = 0;

	@RequestMapping("/test")
	public Result<String> test(@RequestParam("item") final String item,
			final HttpServletRequest request, final HttpServletResponse response) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			Cookie cookie = new Cookie("token", "new person");
			response.addCookie(cookie);
			return new Result<String>(Commons.RESULT_CODE_FAILED, "没有Cookie");
		}

		for (Cookie co : cookies) {
			System.err.println(co.getName() + "=" + co.getValue());
		}

		Cookie cookie = CookieUtils.makeTokenCookie("qwe123abc" + count++);
		response.addCookie(cookie);
		return new Result<String>(Commons.RESULT_CODE_OK, "测试通过");
	}

}
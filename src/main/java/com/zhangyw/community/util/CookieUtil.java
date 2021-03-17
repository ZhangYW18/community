package com.zhangyw.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    public static String getValue(HttpServletRequest request, String name) throws Exception {
        if (request==null || name==null) {
            throw new IllegalArgumentException("Argument is empty");
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new IllegalArgumentException("Cookie is empty");
        }
        for (Cookie cookie:cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

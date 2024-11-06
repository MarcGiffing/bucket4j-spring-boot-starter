package com.giffing.bucket4j.spring.boot.starter.servlet;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class RequestUtils {

    public static String getIpFromRequest(HttpServletRequest request) {
        var ip = request.getHeader("x-forwarded-for");
        if (!StringUtils.hasText(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (!StringUtils.hasText(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

}

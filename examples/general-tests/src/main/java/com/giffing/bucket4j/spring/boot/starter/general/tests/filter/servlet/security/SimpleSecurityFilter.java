package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * A user authorized for the url /secure when the query parameter 'username' has the value 'admin'
 */
@Component
@Order(0)
public class SimpleSecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean isSecurePath = request.getRequestURI().equals("/secure");
        boolean isNotAdmin = !Objects.equals("admin", request.getParameter("username"));
        if(isSecurePath && isNotAdmin) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Hello World");
        } else {
            filterChain.doFilter(request, response);
        }
    }
}

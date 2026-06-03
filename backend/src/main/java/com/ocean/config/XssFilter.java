package com.ocean.config;

import com.ocean.utils.XssUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * XSS 过滤器 — 清洗所有请求参数
 */
@Component
@Order(1)
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }

    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            return XssUtil.clean(super.getParameter(name));
        }

        @Override
        public String[] getParameterValues(String name) {
            return XssUtil.clean(super.getParameterValues(name));
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> map = super.getParameterMap();
            if (map == null) return null;
            return map.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> XssUtil.clean(e.getValue())
                    ));
        }

        @Override
        public String getHeader(String name) {
            return XssUtil.clean(super.getHeader(name));
        }
    }
}

package com.ocean.utils;

/**
 * XSS 防护工具 — 转义 & 过滤 HTML 特殊字符
 */
public class XssUtil {

    /**
     * 过滤输入，转义 HTML 特殊字符
     */
    public static String clean(String value) {
        if (value == null || value.isEmpty()) return value;
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    /**
     * 批量过滤字符串数组
     */
    public static String[] clean(String... values) {
        if (values == null) return null;
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = clean(values[i]);
        }
        return result;
    }
}

package com.coursework.kinotinder.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

@Component
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        StringBuilder params = new StringBuilder();
        Enumeration<String> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = req.getParameterValues(paramName);
            params.append(paramName).append("=");
            if (paramValues != null) {
                for (int i = 0; i < paramValues.length; i++) {
                    params.append(paramValues[i]);
                    if (i < paramValues.length - 1) {
                        params.append(",");
                    }
                }
            }
            params.append("; ");
        }
        logger.info("Request: {} {}, parameters: {}", req.getMethod(), req.getRequestURI(), params);

        chain.doFilter(request, response);
    }
}
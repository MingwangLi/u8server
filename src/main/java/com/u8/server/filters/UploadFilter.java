package com.u8.server.filters;

import com.u8.server.log.Log;
import org.apache.struts2.dispatcher.StrutsRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class UploadFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Log.i("---------UploadFilter初始化");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(new StrutsRequestWrapper((HttpServletRequest) servletRequest), servletResponse);
    }

    @Override
    public void destroy() {
        Log.i("---------UploadFilter销毁");
    }
}

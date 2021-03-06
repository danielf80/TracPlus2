/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redxiii.tracplus.web.filters;

import java.io.IOException;
import java.util.Enumeration;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.web.context.AppSessionContext;

/**
 *
 * @author dfilgueiras
 */
@WebFilter(filterName = "authFilter", urlPatterns = "/app/*")
public class AuthFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Inject
    private AppSessionContext appCtx;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing filter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            
            if (req.getUserPrincipal() == null) {
                
                logger.info("User {} not logged-in. Trying to access {}", req.getRemoteAddr(), req.getRequestURI());
                
                if (!req.getRequestURI().endsWith("/login.jsf")) {
                    
                    saveRequestParameters(req);
                    
                    resp.sendRedirect(req.getContextPath() + "/expired.jsf");
                    return;
                }
            }
        } catch (ClassCastException e) {
            logger.warn("ClassCastException. Request: {} / Response: {}", request.getClass(), response.getClass());
            logger.error("Error", e);
        }
        
        chain.doFilter(request, response);
    }
    
    private void saveRequestParameters(HttpServletRequest request) {
        try {
            Enumeration<String> parameters = request.getParameterNames();
            while (parameters.hasMoreElements()) {
                String key = parameters.nextElement();
                logger.debug("Param: {} = {}", key, request.getParameter(key));
                
                if (appCtx != null)
                    appCtx.addParameter(key, request.getParameter(key));
            }

        } catch (Exception e) {
            logger.error("Error saving request parameters", e);
        }
    }

    @Override
    public void destroy() {
        logger.info("Destroy filter");
    }
}

package com.redxiii.tracplus.web.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dfilgueiras
 * 
 */
@WebFilter(filterName = "sslFilter", urlPatterns = "/app/search/*")
public class SSLFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("Initializing filter");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		//http://docs.oracle.com/javaee/5/tutorial/doc/bnagb.html
		chain.doFilter(request, response);
		
		try {
			HttpServletRequest req = (HttpServletRequest) request;
//			HttpServletResponse resp = (HttpServletResponse) response;
			if (!req.isSecure()) {
				logger.debug("Request was made on open (insecure) channel, such as HTTP: {}", req.getRequestURI());
			} else {
				logger.debug("Request was made using a secure channel, such as HTTPS");
			}
		} catch (ClassCastException e) {
			logger.warn("ClassCastException. Request: {} / Response: {}",
					request.getClass(), response.getClass());
			logger.error("Error", e);
		}
	}
	
	@Override
    public void destroy() {
        logger.info("Destroy filter");
    }
}

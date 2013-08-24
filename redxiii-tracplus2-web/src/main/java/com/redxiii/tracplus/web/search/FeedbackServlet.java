package com.redxiii.tracplus.web.search;

import java.io.IOException;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.openid4java.consumer.ConsumerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.search.SearchResult;
import com.redxiii.tracplus.ejb.util.UsageAnalysis;
import com.redxiii.tracplus.web.context.AppSessionContext;

@Dependent
@WebServlet(name = "SearchFeedback", urlPatterns = {"/sfb.jsf"})
public class FeedbackServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Inject
    private AppSessionContext appCtx;
    
    @Inject 
    private UsageAnalysis usageAnalysis;
    
    @Inject
    private SearchInfo searchInfo;
    
    @Inject
    private List<SearchResult> results;
    
    @Inject
    private AppSessionContext ctx;

    public FeedbackServlet() throws ConsumerException {
       
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handled(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handled(req, resp);
    }

    private void handled(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    	String user = ctx.getUser().getUsername();
    	String redirect = request.getParameter("redirectTo");
    	if (redirect != null) {
    		try {
				String url = new String(Hex.decodeHex(redirect.toCharArray()));
				logger.debug("User '" + user + "' has clicked at: {}", url);
				logger.debug("Redirecting to: {}", url);
				
				if (url.contains("/trac/")) {
					usageAnalysis.logClick(Integer.parseInt(request.getParameter("id")));
				}
				
				response.sendRedirect(url);
				return;
			} catch (DecoderException e) {
				logger.error("Invalid redirect url: {}", redirect, e);
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
    	}
        	
        response.sendError(HttpServletResponse.SC_ACCEPTED);
    }
}

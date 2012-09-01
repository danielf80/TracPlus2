package com.redxiii.tracplus.web;

import java.io.IOException;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.server.RealmVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.entity.User;
import com.redxiii.tracplus.ejb.util.AppConfiguration;

@Dependent
@WebServlet(name = "Authentication", urlPatterns = {"/auth.jsf"})
public class AuthServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String SEARCH_PAGE = "/search/search.jsf";
	private static final String LOGIN_PAGE = "/login.jsf";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private ConsumerManager manager;
	private String contextPath;

	private User currentUser;
	private boolean googleAuthentication;
	private String allowedDomain;
	
	@Inject 
	private SessionContext ctx;
	
	public AuthServlet() throws ConsumerException {
		
		googleAuthentication = AppConfiguration.getInstance().getBoolean("web.security.authentication.google", true);
		allowedDomain = AppConfiguration.getInstance().getString("web.security.domain");
		
		if (googleAuthentication) {
			RealmVerifier realmVerifier = new RealmVerifier(true);
			realmVerifier.setEnforceRpId(false);
			
			manager = new ConsumerManager();
			manager.setRealmVerifier(realmVerifier);
		}
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
		
		contextPath = request.getRequestURL().toString();
		
		String redirect = request.getContextPath() + getClass().getAnnotation(WebServlet.class).urlPatterns()[0];
		
		if (!googleAuthentication) {
			
			currentUser = new User();
			currentUser.setUsername("anonymous@gmail.com");
			currentUser.setName("anonymous");
			
			if (ctx != null) {
				ctx.setUser(currentUser);
				
				logger.info("User authentication done");
			} 
			
			redirect = request.getContextPath() + SEARCH_PAGE;
			
		} else if (request.getParameterMap().size() == 0){
			try {
				logger.info("Access required by ip: {}", request.getRemoteHost());
				redirect = makeAuthenticationRequest(request.getSession());
			} catch (ConsumerException e) {
				throw new ServletException(e);
			} catch (DiscoveryException e) {
				throw new ServletException(e);
			} catch (MessageException e) {
				throw new ServletException(e);
			}
		} else {
			try {
				Credentials credentials = verifyAuthenticationResponse(request, response);
				if (credentials != null && (allowedDomain == null || credentials.getEmail().endsWith(allowedDomain))) {

					currentUser = new User();
					currentUser.setUsername(credentials.getEmail());
					currentUser.setName(credentials.getEmail().split("@")[0]);
					
					if (ctx != null) {
						ctx.setUser(currentUser);
						
						logger.info("User authentication done");
					} else {
						logger.warn("SessionContext not set");
					}
					
					redirect = request.getContextPath() + SEARCH_PAGE;
				} else {
					redirect = request.getContextPath() + LOGIN_PAGE;
				}
			} catch (MessageException e) {
				throw new ServletException(e);
			} catch (DiscoveryException e) {
				throw new ServletException(e);
			} catch (AssociationException e) {
				throw new ServletException(e);
			}
		}
		
		response.sendRedirect(redirect);
	}
	
	private String makeAuthenticationRequest(HttpSession session) throws ConsumerException, DiscoveryException, MessageException {
		
		// perform discovery on the user-supplied identifier
	    List<?> discoveries = manager.discover("https://www.google.com/accounts/o8/id");

	    // attempt to associate with the OpenID provider
	    // and retrieve one service endpoint for authentication
	    DiscoveryInformation discovered = manager.associate(discoveries);

	    // store the discovery information in the user's session for later use
	    // leave out for stateless operation / if there is no session
	    
	    session.setAttribute("discovered", discovered);

	    // obtain a AuthRequest message to be sent to the OpenID provider
	    AuthRequest authReq = manager.authenticate(discovered, contextPath);

	    FetchRequest fetchRequest = FetchRequest.createFetchRequest();
	    fetchRequest.addAttribute("email", "http://axschema.org/contact/email", true);
//	    fetchRequest.addAttribute("firstName", "http://axschema.org/namePerson/first", true);
//	    fetchRequest.addAttribute("lastName", "http://axschema.org/namePerson/last", true);
	    
	    authReq.addExtension(fetchRequest);
	    
	    return authReq.getDestinationUrl(true);
	}
	
	private Credentials verifyAuthenticationResponse(HttpServletRequest request, HttpServletResponse response) throws MessageException, DiscoveryException, AssociationException {
		HttpSession session = request.getSession();
		
		// extract the parameters from the authentication response
	    // (which comes in as a HTTP request from the OpenID provider)
	    ParameterList openidResp = new ParameterList(request.getParameterMap());

	    // retrieve the previously stored discovery information
	    DiscoveryInformation discovered = (DiscoveryInformation) session.getAttribute("discovered");

	    // extract the receiving URL from the HTTP request
	    StringBuffer receivingURL = request.getRequestURL();
	    String queryString = request.getQueryString();
	    if (queryString != null && queryString.length() > 0)
	        receivingURL.append("?").append(request.getQueryString());

	    // verify the response
	    VerificationResult verification = manager.verify(receivingURL.toString(), openidResp, discovered);

	    // examine the verification result and extract the verified identifier
	    Identifier verified = verification.getVerifiedId();
	    
	    if (verified != null) {
	    	AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();
	    	if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
	    		FetchResponse fetchResponse = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
	    		
	    		Credentials credentials = new Credentials();
	    		
	    		List<?> emailsList = fetchResponse.getAttributeValues("email");
//	    		List<?> fnameList = fetchResponse.getAttributeValues("email");
//	    		List<?> lnameList = fetchResponse.getAttributeValues("email");
	    		
	    		credentials.setEmail((String)emailsList.get(0));
//	    		credentials.setFirstName((String)fnameList.get(0));
//	    		credentials.setLastName((String)lnameList.get(0));
	    		
	    		return credentials;
	    	}
	    } else {
	    	logger.info("Access denied response: {}", request.getRemoteHost());
	    }
	    
	    return null;
	}
}

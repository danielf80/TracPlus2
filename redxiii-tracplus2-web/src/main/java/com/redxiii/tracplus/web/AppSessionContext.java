package com.redxiii.tracplus.web;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.redxiii.tracplus.ejb.entity.User;
import java.util.HashMap;
import java.util.Map;

@Named
@SessionScoped
public class AppSessionContext implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private User user;
        private Map<String, String> parameters = new HashMap<String, String>();
	
	public void setUser(User user) {
		this.user = user;
	}
	public User getUser() {
		return user;
	}
        
        public void addParameter(String key, String value) {
            parameters.put(key, value);
        }
        
        public String getParameter(String key) {
            return parameters.get(key);
        }
        
        public void resetParameters() {
            parameters.clear();
        }
}

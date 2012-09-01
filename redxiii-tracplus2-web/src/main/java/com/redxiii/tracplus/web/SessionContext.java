package com.redxiii.tracplus.web;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.redxiii.tracplus.ejb.entity.User;

@Named
@SessionScoped
public class SessionContext implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private User user;
	
	public void setUser(User user) {
		this.user = user;
	}
	public User getUser() {
		return user;
	}
}

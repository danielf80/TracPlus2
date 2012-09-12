package com.redxiii.tracplus.web;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.redxiii.tracplus.ejb.entity.User;

@Named
@SessionScoped
public class AppSessionContext implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private User user;
	private boolean superuser;
	
	public void setUser(User user) {
		this.user = user;
	}
	public User getUser() {
		return user;
	}
	public void setSuperUser(boolean superuser) {
		this.superuser = superuser;
	}
	public boolean isSuperuser() {
		return superuser;
	}
}

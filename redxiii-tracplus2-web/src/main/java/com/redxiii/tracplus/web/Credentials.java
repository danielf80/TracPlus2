package com.redxiii.tracplus.web;


public class Credentials {

	private String email;
	
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}
	
	@Override
	public String toString() {
		return email;
	}
}

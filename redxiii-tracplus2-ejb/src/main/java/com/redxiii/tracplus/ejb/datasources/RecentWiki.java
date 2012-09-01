package com.redxiii.tracplus.ejb.datasources;

import java.io.Serializable;

public class RecentWiki implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private Number version;
	
	public RecentWiki() {
	}
	
	public RecentWiki(String name, Number version) {
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Number getVersion() {
		return version;
	}
	public void setVersion(Number version) {
		this.version = version;
	}
	@Override
	public String toString() {
		return "RecentWiki [name=" + name + ", version=" + version + "]";
	}
}

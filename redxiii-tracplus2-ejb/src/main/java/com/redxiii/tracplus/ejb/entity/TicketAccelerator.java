package com.redxiii.tracplus.ejb.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TicketAccelerator {

	private String id;
	private String category;
	private String name;
	
	private Map<String, Set<String>> properties = new HashMap<String, Set<String>>();

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Set<String>> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, Set<String>> properties) {
		this.properties = properties;
	}
}

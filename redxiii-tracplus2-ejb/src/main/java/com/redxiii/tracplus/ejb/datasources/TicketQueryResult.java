package com.redxiii.tracplus.ejb.datasources;

import com.redxiii.tracplus.ejb.entity.Ticket;

public class TicketQueryResult extends Ticket {
	
	private String newvalue;
	private Integer modified;
	
	public TicketQueryResult(){
		super();
	}
	
	public String getNewvalue() {
		return newvalue;
	}
	public void setNewvalue(String newValue) {
		this.newvalue = newValue;
	}
	public Integer getModified() {
		return modified;
	}
	public void setModified(Integer modified) {
		this.modified = modified;
	}
}

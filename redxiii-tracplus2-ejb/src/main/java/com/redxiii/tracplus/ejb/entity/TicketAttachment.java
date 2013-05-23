package com.redxiii.tracplus.ejb.entity;

public class TicketAttachment extends Attachment {

	private static final long serialVersionUID = 1L;

	private String	ticketType;
	private String	component;
	private String	milestone;
	private String	status;
	
	public String getTicketType() {
		return ticketType;
	}
	public void setTicketType(String ticketType) {
		this.ticketType = ticketType;
	}

	public String getComponent() {
		return component;
	}
	public void setComponent(String component) {
		this.component = component;
	}
	public String getMilestone() {
		return milestone;
	}
	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
}

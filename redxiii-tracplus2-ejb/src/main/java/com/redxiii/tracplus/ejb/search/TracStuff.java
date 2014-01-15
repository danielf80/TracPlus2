package com.redxiii.tracplus.ejb.search;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.redxiii.tracplus.ejb.entity.Ticket;
import com.redxiii.tracplus.ejb.entity.Wiki;
import com.redxiii.tracplus.ejb.util.AppConfiguration;

public class TracStuff {
	
	public static final SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private String id;
	private String url;
	private String author;
	private StringBuilder content;
	private Date createdDate;
	private String modifiedDate;
	private String description;
	private String tags = "";
	private String context = "";
	private String cc = "";
	private String status = "";	
	private long modifiedTimestamp;
	
	private TracStuff(){}
	
	private static String getBaseUrl() {
		String host = AppConfiguration.getInstance().getString("trac.web.url");
		if (host == null)
			host = "localhost";
		return "http://" + host + "/trac/"; 
	}
	
	public TracStuff(Ticket ticket){
		
		this(
				"ticket/" + ticket.getId().toString(), 			//ID
				"ticket/" + ticket.getId().toString(), 			//URL
				ticket.getReporter(), 							//AUTHOR 
				ticket.getDescription(),						//CONTENT
				new Date(1000L * (Integer)ticket.getTime()), 	//CREATED DATE
				1000L * (Integer)ticket.getChangetime(), 		//MODIFIED DATE
				ticket.getSummary(), 							//DESCRIPTION
				"ticket");										//CONTEXT
		
        this.status = ticket.getStatus();
        
        String owner = StringUtils.trimToEmpty(ticket.getOwner());
        String[] ccs = StringUtils.split(ticket.getCc(), ',');
        if (ccs != null) {
	        for (String copied : ccs) {
	        	if (!StringUtils.isEmpty(copied) && !copied.equals(owner)) {
	        		this.cc += copied + ",";
	        	}
	        }
        }
        
        this.cc += StringUtils.trimToEmpty(ticket.getOwner());
	}
	
	public TracStuff(Wiki wiki) {
		this(
				"wiki/" + wiki.getName(), 
				"wiki/" + wiki.getName(), 
				wiki.getAuthor(), 
				wiki.getText(), 
				new Date(1000L * wiki.getTime()), 
				1000L * wiki.getTime(), 
				wiki.getName(), 
				"wiki");
	}
	
	

	public TracStuff(String id, String url, String author,
			String content, Date createdDate, long modifiedTimestamp,
			String description, String context) {
		this();
		this.id = id;
		this.url = getBaseUrl() + url;
		this.author = author;
		this.content = new StringBuilder(description).append("\r\n ").append(content);
		this.createdDate = createdDate;
		this.modifiedTimestamp = modifiedTimestamp;
		this.modifiedDate = dtFormat.format(new Date(this.modifiedTimestamp));
		this.description = description;
		this.context = context;
	}

	public String getId() {
		return id;
	}
	
	public void addContent(String description, Integer time) {
		this.content.append("\r\n");
		this.content.append(description);
		this.modifiedDate = dtFormat.format(new Date(1000L * time));
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	public String getContent() {
		return content.toString();
	}

	public String getAuthor() {
		return author;
	}

	public String getUrl() {
		return url;
	}

	public String getDescription() {
		return description.toString();
	}

	public String getModifiedDate() {
		return modifiedDate;
	}
	
	public long getModifiedTimestamp() {
		return modifiedTimestamp;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public String getTags() {
		return tags;
	}

	public String getContext() {
		return context;
	}

    public String getCc() {
        return cc;
    }
    
    public String getStatus() {
		return status;
	}
    
    public void setStatus(String status) {
		this.status = status;
	}

}

package com.redxiii.tracplus.ejb.search;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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
	private long modifiedTimestamp;
	
	private TracStuff(){}
	
	private static String getBaseUrl() {
		String host = AppConfiguration.getInstance().getString("web.trac.host-url");
		if (host == null)
			host = "localhost";
		return "http://" + host + "/trac/"; 
	}
	
	public TracStuff(Ticket ticket){
		
		this(
				"ticket/" + ticket.getId().toString(), 						//ID
				"ticket/" + ticket.getId().toString(), 			//URL
				ticket.getReporter(), ticket.getDescription(),	//AUTHOR 
				new Date(1000L * (Integer)ticket.getTime()), 	//CONTENT
				1000L * (Integer)ticket.getChangetime(), 		//CREATED DATE
				ticket.getSummary(), 							//MODIFIED DATE
				"ticket");										//CONTEXT
		
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
		this.id = id;
		this.url = getBaseUrl() + url;
		this.author = author;
		this.content = new StringBuilder(content);
		this.createdDate = createdDate;
		this.modifiedTimestamp = modifiedTimestamp;
		this.modifiedDate = dtFormat.format(new Date(this.modifiedTimestamp));
		this.description = description;
		this.context = context;
	}

	public static TracStuff createInstanceWiki(Map<String, Object> dbTuple) {
		
		TracStuff stuff = new TracStuff();
		stuff.id = dbTuple.get("name").toString();
		stuff.url = getBaseUrl() + "wiki/" + dbTuple.get("name").toString();
		stuff.author = dbTuple.get("author").toString();
		stuff.content = new StringBuilder(dbTuple.get("text").toString());
		stuff.createdDate = new Date(1000L * (Integer)dbTuple.get("time"));
		stuff.modifiedTimestamp = 1000L * (Integer)dbTuple.get("time");
		stuff.modifiedDate = dtFormat.format(new Date(stuff.modifiedTimestamp));
		stuff.description = dbTuple.get("name").toString();
		stuff.context = "wiki";
		
		return stuff;
	}
	
	public static TracStuff createInstanceTicket(Map<String, Object> dbTuple) {
		
		TracStuff stuff = new TracStuff();
		stuff.id = dbTuple.get("id").toString();
		stuff.url = getBaseUrl() + "ticket/" + dbTuple.get("id").toString();
		stuff.author = dbTuple.get("reporter").toString();
		stuff.content = new StringBuilder(dbTuple.get("description").toString());
		stuff.createdDate = new Date(1000L * (Integer)dbTuple.get("time"));
		stuff.modifiedTimestamp = 1000L * (Integer)dbTuple.get("changetime");
		stuff.modifiedDate = dtFormat.format(new Date(stuff.modifiedTimestamp));
		stuff.description = dbTuple.get("summary").toString();
		stuff.context = "ticket";
		stuff.tags = (dbTuple.get("keywords") != null ? dbTuple.get("keywords").toString() : "");
		
		return stuff;
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

}

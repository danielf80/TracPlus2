package com.redxiii.tracplus.ejb.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Daniel Filgueiras
 * @since 19/08/2011
 */
@Entity
@Table(name="wiki")
public class Wiki {
	
	@EmbeddedId
	private WikiPk 	wikiPk = new WikiPk();
	
	@Basic
	@Column(nullable=false)
	private Integer		time;
	
	@Basic
	@Column(nullable=false)
	private String		author;
	
	private String		ipnr;
	private String		text;
	private String		comment;
	private Integer		readonly;
		
	public int compareTo(Wiki other) {
		return this.wikiPk.compareTo(other.wikiPk);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((wikiPk == null) ? 0 : wikiPk.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Wiki other = (Wiki) obj;
		
		return this.wikiPk.equals(other.wikiPk);
	}
	
	public WikiPk getWikiPk() {
		return wikiPk;
	}
	public void setWikiPk(WikiPk wikiPk) {
		this.wikiPk = wikiPk;
	}

	public String getName() {
		return wikiPk.getName();
	}
	public void setName(String name) {
		this.wikiPk.setName(name);
	}
	
	public Integer getVersion() {
		return wikiPk.getVersion();
	}
	public void setVersion(Integer version) {
		this.wikiPk.setVersion(version);
	}
	
	public Integer getTime() {
		return time;
	}
	public void setTime(Integer time) {
		this.time = time;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getIpnr() {
		return ipnr;
	}
	public void setIpnr(String ipnr) {
		this.ipnr = ipnr;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Integer getReadonly() {
		return readonly;
	}
	public void setReadonly(Integer readonly) {
		this.readonly = readonly;
	}
}

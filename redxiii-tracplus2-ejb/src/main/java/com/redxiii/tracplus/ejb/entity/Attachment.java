package com.redxiii.tracplus.ejb.entity;

import java.io.Serializable;

public class Attachment implements Serializable, Comparable<Attachment> {

	private static final long serialVersionUID = 1L;
	
	private String type;	// ticket or wiki
	private String id;		// ticket Id or wiki Id
	private String filename;
	private Integer	size;
	private Integer time;
	private String description;
	private String author;
	private String ipnr;
	
	public String getFullDescription() {
		return (description == null || description.length() == 0) ? filename : (filename + " " + description);
	}
	
	@Override
	public int compareTo(Attachment other) {
		if (!this.getType().equals(other.getType()))
			return this.getType().compareTo(other.getType());
		
		if (!this.getId().equals(other.getId()))
			return this.getId().compareTo(other.getId());
		
		return this.getFilename().compareTo(other.getFilename());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Attachment other = (Attachment) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public Integer getTime() {
		return time;
	}
	public void setTime(Integer time) {
		this.time = time;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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

	@Override
	public String toString() {
		return "Attachment [type=" + type + ", id=" + id + ", filename="
				+ filename + "]";
	}
}

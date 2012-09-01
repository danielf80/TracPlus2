package com.redxiii.tracplus.ejb.entity;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Embeddable;

/**
 * @author Daniel Filgueiras
 * @since 19/08/2011
 */
@Embeddable
public class WikiPk implements Serializable, Comparable<WikiPk> {

	private static final long serialVersionUID = 1L;

	@Basic
	private String 		name;
	
	@Basic
	private Integer		version;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public int compareTo(WikiPk other) {
		if (this.name.equals(other.name))
			return this.version.compareTo(other.version);
		return this.name.compareTo(other.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		WikiPk other = (WikiPk) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
}

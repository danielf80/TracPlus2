package com.redxiii.tracplus.ejb.search;

import java.util.List;

import org.apache.commons.lang.StringUtils;

public interface SearchResult extends Comparable<SearchResult> {

	int getCode();
	int getIndex();
	float getScore();
	void setScore(float score);
	String getId();
	String getUrl();
	String getDescription();
	String getAuthor();
	String getContext();
	String getCc();
	/**
	 * @return YYYYMMDD
	 */
	String getCreatedDate();

	/**
	 * @return YYYYMMDD
	 */
	String getModifiedDate();
	
	TicketStatus getStatus();

	List<String> getFragments();

	public enum TicketStatus {
		none,
		open,	// same as new
		assigned,
		reopened,
		closed,
		;
		static TicketStatus getStatus(String value) {
			if (StringUtils.isEmpty(value))
				return none;
			if (value.equals("new"))
				return open;
			for (TicketStatus status : TicketStatus.values())
				if (status.name().equals(value))
					return status;
			return none;
		}
		
		public boolean isClosed() {
			return this.equals(closed);
		}
		
		public boolean isNa() {
			return this.equals(none);
		}
	}
}

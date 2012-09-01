package com.redxiii.tracplus.ejb.search;

import java.util.List;


public interface SearchResult {

	float getScore();
	
	String getId();
	String getUrl();
	String getDescription();
	String getAuthor();
	String getContext();
	
	/**
	 * @return YYYYMMDD
	 */
	String getCreatedDate();
	/**
	 * @return YYYYMMDD
	 */
	String getModifiedDate();
	
	
	List<String> getFragments();
}

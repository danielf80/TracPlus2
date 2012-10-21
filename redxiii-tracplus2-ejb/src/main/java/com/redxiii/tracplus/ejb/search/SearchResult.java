package com.redxiii.tracplus.ejb.search;

import java.util.List;


public interface SearchResult extends Comparable<SearchResult> {

	float getScore();
        void setScore(float score);
	
	String getId();
	String getUrl();
	String getDescription();
	String getAuthor();
	String getContext();
        String getCc();
        
        boolean hasCc(String user);
	
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

package com.redxiii.tracplus.ejb.search.query;

import java.util.Set;

import com.redxiii.tracplus.ejb.search.TracStuffField;



public interface SimpleQuerySpec {

	public String getLuceneQuery();
	
	public int getMaxHits();
	
	public boolean isRecentFilterEnable();
	
	public Integer getRecentFilterDays();
	
	public Set<TracStuffField> getQueryFields();
	
	public Set<String> getValuesRestriction(TracStuffField field);
	
	public boolean isStrongRestriction(TracStuffField field, String value);
	
	public boolean isLikeRestriction(TracStuffField field, String value);
	
	public float getLikeRestrictionWeight(TracStuffField field, String value);
	
}

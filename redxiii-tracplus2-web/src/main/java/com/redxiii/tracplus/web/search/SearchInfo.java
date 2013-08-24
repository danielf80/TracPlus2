package com.redxiii.tracplus.web.search;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

/**
 * @author dfilgueiras
 * 
 */
@RequestScoped
@Named
public class SearchInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String searchText;
	private String interpretedQuery;
	private String elapsedTime;

	public SearchInfo() {
	}

	public SearchInfo(String text) {
		this.searchText = text;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public String getInterpretedQuery() {
		return interpretedQuery;
	}

	public void setInterpretedQuery(String interpretedQuery) {
		this.interpretedQuery = interpretedQuery;
	}

	public String getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(String elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	
	public boolean hasMultipleWords() {
		return StringUtils.contains(StringUtils.trim(searchText), ' ');
	}

	@Override
	public String toString() {
		return "SearchInfo [searchText=" + searchText + "]";
	}
}

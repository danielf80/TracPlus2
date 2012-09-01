package com.redxiii.tracplus.web.search;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

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
	
	@Override
	public String toString() {
		return "SearchInfo [searchText=" + searchText + "]";
	}
	
	public String getElapsedTime() {
		return elapsedTime;
	}
	public void setElapsedTime(String elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
}

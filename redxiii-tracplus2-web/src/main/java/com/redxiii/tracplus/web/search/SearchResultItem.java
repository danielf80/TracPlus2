package com.redxiii.tracplus.web.search;

import java.util.List;

import com.redxiii.tracplus.ejb.search.SearchResult;

public class SearchResultItem implements SearchResult {

	private SearchResult result;
	
	public SearchResultItem(SearchResult result) {
		this.result = result;
	}

	@Override
	public float getScore() {
		return result.getScore();
	}

	@Override
	public String getId() {
		return result.getId();
	}

	@Override
	public String getUrl() {
		return result.getUrl();
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCreatedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getFragments() {
		// TODO Auto-generated method stub
		return null;
	}

}

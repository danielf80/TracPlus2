package com.redxiii.tracplus.web.search;

import java.util.Set;

import org.apache.lucene.search.Query;

import com.redxiii.tracplus.ejb.search.SearchManager;
import com.redxiii.tracplus.ejb.search.SearchResult;

public abstract class AbstractSearchHelper {

	protected Query query;
	protected FilterPeriodSelection periodSelection;
	protected FilterTypeSelection typeSelection;
	protected SearchManager manager;

	public AbstractSearchHelper() {
		super();
	}

	public void setPeriodSelection(FilterPeriodSelection periodSelection) {
		this.periodSelection = periodSelection;
	}

	public void setTypeSelection(FilterTypeSelection typeSelection) {
		this.typeSelection = typeSelection;
	}

	public abstract Set<SearchResult> doSearch(SearchInfo searchInfo);

	public Query getQuery() {
		return query;
	}

}
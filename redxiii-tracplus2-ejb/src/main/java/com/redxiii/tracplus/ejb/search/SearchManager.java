package com.redxiii.tracplus.ejb.search;

import java.util.Set;

import org.apache.lucene.search.Query;

import com.redxiii.tracplus.ejb.search.query.SimpleQuerySpec;

public interface SearchManager {

	public abstract Query buildQuery(SimpleQuerySpec spec);

	public abstract Set<SearchResult> doSearch(Query query);

}

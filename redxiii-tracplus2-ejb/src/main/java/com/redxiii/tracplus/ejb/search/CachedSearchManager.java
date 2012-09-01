package com.redxiii.tracplus.ejb.search;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.search.Query;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.redxiii.tracplus.ejb.search.query.SimpleQuerySpec;

public class CachedSearchManager implements SearchManager {

	private SearchManager delegate;
	private Cache<Object, Object> cachedResults;
	
	public CachedSearchManager(SearchManager delegate) {
		this.delegate = delegate;
		this.cachedResults = CacheBuilder.newBuilder()
				.maximumSize(50)
				.expireAfterAccess(6, TimeUnit.HOURS)
				.build();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<SearchResult> doSearch(Query query) {
		
		Object obj = cachedResults.getIfPresent(query);
		if (obj != null && obj instanceof Set)
			return (Set<SearchResult>) obj;
		
		Set<SearchResult> results = delegate.doSearch(query);
		cachedResults.put(query, results);
		
		return results;
	}
	
	@Override
	public Query buildQuery(SimpleQuerySpec spec) {
		return delegate.buildQuery(spec);
	}

}

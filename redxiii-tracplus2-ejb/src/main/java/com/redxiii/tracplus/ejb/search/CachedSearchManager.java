package com.redxiii.tracplus.ejb.search;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.search.Query;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.redxiii.tracplus.ejb.search.query.SimpleQuerySpec;
import com.redxiii.tracplus.ejb.util.AppConfiguration;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedSearchManager implements SearchManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private SearchManager delegate;
    private Cache<Object, Object> cachedResults;


    public CachedSearchManager(SearchManager delegate) {
        this.delegate = delegate;
        
        Configuration configuration = AppConfiguration.getInstance();
        int size = configuration.getInt("cache-manager.maximum-size", 50);
        int accessExpired = configuration.getInt("cache-manager.expire-policy.access", 60);
        int writeExpired = configuration.getInt("cache-manager.expire-policy.write", 180);
        
        this.cachedResults = CacheBuilder.newBuilder()
                .maximumSize(size)
                .expireAfterAccess(accessExpired, TimeUnit.MINUTES)
                .expireAfterWrite(writeExpired, TimeUnit.MINUTES)
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<SearchResult> doSearch(int code, Query query) {

        logger.debug("Getting results from cache: {}", query.toString());
        Object obj = cachedResults.getIfPresent(query.toString());
        if (obj != null) {
            logger.debug("Returning cached results for query: {}", query.toString());
            return (Set<SearchResult>) obj;
        }

        logger.debug("Cached results not found");
        Set<SearchResult> results = delegate.doSearch(code, query);
        
        if (results.size() > 0)
            cachedResults.put(query.toString(), results);

        return results;
    }

    @Override
    public Query buildQuery(SimpleQuerySpec spec) {
        return delegate.buildQuery(spec);
    }
}

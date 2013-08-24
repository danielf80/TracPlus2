package com.redxiii.tracplus.web.search;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.search.SearchManager;
import com.redxiii.tracplus.ejb.search.SearchResult;
import com.redxiii.tracplus.ejb.search.TracStuffField;
import com.redxiii.tracplus.ejb.search.query.QueryBuilder;
import com.redxiii.tracplus.ejb.search.query.SimpleQuerySpec;

public class LuceneSearch extends AbstractSearchHelper {

	final Logger logger = LoggerFactory.getLogger(getClass());
	
	public LuceneSearch(SearchManager manager) {
		this.manager = manager;
	}
	
	public Set<SearchResult> doSearch(SearchInfo searchInfo) {
		
		logger.debug("Creating query...");
	    QueryBuilder<SimpleQuerySpec> baseBuilder = QueryBuilder.buildSimpleQuery();
	    
	    switch (typeSelection) {
	        case ticket:
	        case wiki:
	        case attachment:
	            baseBuilder.addStrongRestriction(typeSelection.name(), TracStuffField.CONTEXT);
	            break;
	            
	        case none:
			default:
				break;
	    }
	
	    if (!periodSelection.equals(FilterPeriodSelection.all_entries)) {
	        baseBuilder.enableRecentFilter(periodSelection.days);
	    }
	    
	    baseBuilder.addLuceneRestriction(searchInfo.getSearchText());
	
	    query = manager.buildQuery(baseBuilder.createQuerySpec());
		
		return manager.doSearch(0, query);
	}

	
}

package com.redxiii.tracplus.web.search;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.search.SearchManager;
import com.redxiii.tracplus.ejb.search.SearchResult;
import com.redxiii.tracplus.ejb.search.TracStuffField;
import com.redxiii.tracplus.ejb.search.query.QueryBuilder;
import com.redxiii.tracplus.ejb.search.query.SimpleQuerySpec;
import com.redxiii.tracplus.ejb.util.AppConfiguration;

public class SmartSearch extends AbstractSearchHelper {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private int stepTwoSmartSearchResults;
	private int priority = 0;
	
	
	public SmartSearch(SearchManager manager) {
		this.manager = manager;
		this.stepTwoSmartSearchResults = AppConfiguration.getInstance().getInt("web.search-manager.smart-search.two-step-minimal-results", 0);
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
	
	    Set<SearchResult> accResults = new LinkedHashSet<SearchResult>();
	    
	    accResults.addAll(doSearch(baseBuilder.clone(), searchInfo.getSearchText(), TracStuffField.DESCRIPTION, true));
	    logger.debug("Got (acc) {} results so far", accResults.size()); 
	    if (searchInfo.hasMultipleWords()) {
	    	accResults.addAll(doSearch(baseBuilder.clone(), searchInfo.getSearchText(), TracStuffField.DESCRIPTION, false));
	    	logger.debug("Got (acc) {} results so far", accResults.size());
	    }
	    
	    accResults.addAll(doSearch(baseBuilder.clone(), searchInfo.getSearchText(), TracStuffField.CONTENT, true));
	    logger.debug("Got (acc) {} results so far", accResults.size());
	    if (searchInfo.hasMultipleWords()) {
	    	accResults.addAll(doSearch(baseBuilder.clone(), searchInfo.getSearchText(), TracStuffField.CONTENT, false));
	    	logger.debug("Got (acc) {} results so far", accResults.size());
	    }
	    
	    if (accResults.size() < stepTwoSmartSearchResults) {
	    	baseBuilder.addLikeRestriction(searchInfo.getSearchText(), TracStuffField.CONTENT);
	    	logger.debug("Executing approximate search");
	    	accResults.addAll( manager.doSearch(priority++, manager.buildQuery(baseBuilder.createQuerySpec())) );
	    	logger.debug("Got (acc) {} results so far", accResults.size());
	    }
	    
	    return accResults; 
	}

	private Set<SearchResult> doSearch(QueryBuilder<SimpleQuerySpec> queryBuilder, String queryText, TracStuffField field, boolean allWords) {
		
		logger.debug("Executing partial search at {} with {} restriction", field, allWords ? "Strong" : "Weak");
		if (allWords) {
			queryBuilder.addStrongRestriction(queryText, field);
		} else {
			queryBuilder.addWeakRestriction(queryText, field);
		}
        
		query = manager.buildQuery(queryBuilder.createQuerySpec());
		
		return manager.doSearch(priority++, query);
	}
}

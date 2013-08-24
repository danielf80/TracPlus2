package com.redxiii.tracplus.web.search;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.search.SearchManager;
import com.redxiii.tracplus.ejb.search.SearchManagerFactory;
import com.redxiii.tracplus.ejb.search.SearchResult;
import com.redxiii.tracplus.ejb.util.AppConfiguration;
import com.redxiii.tracplus.ejb.util.UsageAnalysis;
import com.redxiii.tracplus.web.context.AppSessionContext;
import com.redxiii.tracplus.web.util.SearchResultInvocationHandler;

@Named("searchView")
@SessionScoped
public class SearchView implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DecimalFormat decFormat = new DecimalFormat("#.###");
	private static final AtomicInteger SEARCH_ID = new AtomicInteger(1);
	
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<FilterTypeSelection> filterTypeSelections;
    private final List<FilterPeriodSelection> filterPeriodSelections;
    private final List<SortSelection> sortSelections;
    
    private static final Comparator<SearchResult> SORT_SEARCHS_BY_DATE = new Comparator<SearchResult>() {
        @Override
        public int compare(SearchResult o1, SearchResult o2) {
            if (o1.getModifiedDate() == null || o2.getModifiedDate() == null) {
                return 1;
            }
            if (o1.getModifiedDate().equals(o2.getModifiedDate())) {
                return Float.compare(o2.getScore(), o1.getScore());
            }
            return o2.getModifiedDate().compareTo(o1.getModifiedDate());
        }
    };
    private static final Comparator<SearchResult> SORT_SEARCHS_BY_MAGIC = new Comparator<SearchResult>() {
        @Override
        public int compare(SearchResult o1, SearchResult o2) {

        	int result = Float.compare(o1.getCode(), o2.getCode());
        	if (result != 0) {
                return result;
            }
        	
            if (Math.abs(o1.getScore() - o2.getScore()) >= 1) {
                return Float.compare(o2.getScore(), o1.getScore());
            }

            if (o1.getModifiedDate() == null || o2.getModifiedDate() == null) {
                return 1;
            }

            if (o1.getModifiedDate().equals(o2.getModifiedDate())) {
                return Float.compare(o2.getScore(), o1.getScore());
            }

            return o2.getModifiedDate().compareTo(o1.getModifiedDate());
        }
    };
    private static final Comparator<SearchResult> SORT_SEARCHS_BY_SCORE = new Comparator<SearchResult>() {
        @Override
        public int compare(SearchResult o1, SearchResult o2) {

        	int result = Float.compare(o1.getCode(), o2.getCode());
        	if (result != 0) {
                return result;
            }
        	
            result = Float.compare(o2.getScore(), o1.getScore());
            if (result != 0) {
                return result;
            }
            
            result = o2.getModifiedDate().compareTo(o1.getModifiedDate());
            if (result != 0) {
                return result;
            }
            
            return o2.getId().compareTo(o1.getId());
        }
    };

    private static enum SearchMethod {
        approximate,
        precise,
        advanced,
        smart,
    }

    @Inject
    private SearchManagerFactory searchManagerFactory;
    
    @Inject
    private SearchInfo searchInfo;
    
    @Inject
    private AppSessionContext ctx;
    
    @Inject
    private UsageAnalysis usageAnalysis;
    
    
    @RequestScoped
    private final List<SearchResult> results = new ArrayList<SearchResult>();
    
    private FilterTypeSelection selectedFilterType;
    private FilterPeriodSelection selectedFilterPeriod;
    private SortSelection selectedSort;
    private SearchMethod searchMethod;
    private String reportErrorLink;

    
    public SearchView() {
        filterTypeSelections = new ArrayList<FilterTypeSelection>();
        filterTypeSelections.add(FilterTypeSelection.none);
        
        if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update.wiki", false))
        	filterTypeSelections.add(FilterTypeSelection.wiki);
        if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update.ticket", false))
        	filterTypeSelections.add(FilterTypeSelection.ticket);
        if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update.attachments", false))
        	filterTypeSelections.add(FilterTypeSelection.attachment);
        
        filterPeriodSelections = Collections.unmodifiableList(Arrays.asList(FilterPeriodSelection.values()));
        sortSelections = Collections.unmodifiableList(Arrays.asList(SortSelection.values()));

        selectedFilterType = FilterTypeSelection.none;
        selectedFilterPeriod = FilterPeriodSelection.all_entries;
        selectedSort = SortSelection.relevance;
        searchMethod = SearchMethod.approximate;
    }

    @PostConstruct
    public void init() {
        restoreParameters();
    }
    
    private void restoreParameters() {
        
        if (ctx != null) {
            String text = ctx.getParameter("searchForm:searchText");
            if (text != null) {
                logger.info("Restoring search options. Text: {}", text);
                searchInfo = new SearchInfo(text);

                doSearch(SearchMethod.smart.name());
            }

            ctx.resetParameters();
        } else {
            logger.warn("Application context is null");
        }
    }
    
    public void doSearch(String method) {

    	final int searchId = SEARCH_ID.getAndIncrement();
        results.clear();

        if (ctx != null && ctx.getUser() != null) {
        	logger.info("Searching for {} by [{}]", searchInfo, ctx.getUser().getName());
        } else {
            logger.warn("Session context of User not set");
            return;
        }

        if (searchInfo.getSearchText() == null || searchInfo.getSearchText().length() == 0) {
            return;
        }

        long initSearch = System.currentTimeMillis();
        searchMethod = SearchMethod.valueOf(method);

        String interpreted = makeSearch(searchId);

        long elapsedTime = System.currentTimeMillis() - initSearch;

        searchInfo.setInterpretedQuery(interpreted);
        searchInfo.setElapsedTime(decFormat.format((double) elapsedTime / 1000D));

        usageAnalysis.logSearch(ctx.getUser().getName(), interpreted, elapsedTime, searchId, results);
        
        logger.info("Search done in {} ms. Quantity # {}", elapsedTime, results.size());
    }
    
    private String makeSearch(int searchId) {
        logger.info("Creating SearchManager instance");
        
        
        SearchManager manager = searchManagerFactory.getManager();
        AbstractSearchHelper searchHelper = null;
        
        switch(searchMethod) {
        	case smart:
        		searchHelper = new SmartSearch(manager);
        		break;
        	case advanced:
        		searchHelper = new LuceneSearch(manager);
        		break;
        		
        	default:
        		return "?";
        }
        searchHelper.setPeriodSelection(selectedFilterPeriod);
        searchHelper.setTypeSelection(selectedFilterType);
        
        Set<SearchResult> resultSet = searchHelper.doSearch(searchInfo);
        Query query = searchHelper.getQuery();      
        
        logger.debug("Searching and sorting...");
        results.addAll(getTransformedResults(searchId, getSortedResults(resultSet)));
        
        if (results.isEmpty()) {
            prepareEmptyResultLink(query.toString());
        } else {
            reportErrorLink = null;
        }
        
        return query.toString();
    }
    
    private void prepareEmptyResultLink(String query) {
        
        String host = AppConfiguration.getInstance().getString("trac.web.url","localhost");
        String user = AppConfiguration.getInstance().getString("trac.web.user","anonymous");
        
        try {
            reportErrorLink = new StringBuilder("http://")
                .append(host)
                .append("/trac/newticket?summary=")
                .append(URLDecoder.decode("[TracPlus2] - Pesquisa sem resultados", "UTF-8"))
                .append("&description=")
                .append(URLDecoder.decode("A pesquisa:'" + query + "' não retornou nenhum resultado","UTF-8"))
                .append("&milestone=").append(URLDecoder.decode("Produto.Trac","UTF-8"))
                .append("&owner=").append(user)
                .toString();
        } catch(Exception e) {
            logger.error("Unable to create link to Trac");
        }
    }

    private Set<SearchResult> getSortedResults(Set<SearchResult> results) {

        Set<SearchResult> sorted;

        switch (selectedSort) {
            case date:
                sorted = new TreeSet<SearchResult>(SORT_SEARCHS_BY_DATE);
                break;

            case relevance:
                sorted = new TreeSet<SearchResult>(SORT_SEARCHS_BY_SCORE);
                break;

            case magic:
                sorted = new TreeSet<SearchResult>(SORT_SEARCHS_BY_MAGIC);
                break;

            default:
                sorted = new HashSet<SearchResult>();
                break;
        }

        sorted.addAll(results);

        return sorted;
    }
    
	private Set<SearchResult> getTransformedResults(int searchId, Set<SearchResult> results) {
    	
    	Set<SearchResult> transformed = Collections.emptySet();
    	try {
    		transformed = new LinkedHashSet<SearchResult>();
    		
			for (SearchResult result : results) {
				SearchResult proxy = (SearchResult) Proxy
						.newProxyInstance(
								this.getClass().getClassLoader(), 
								new Class[]{SearchResult.class}, 
								new SearchResultInvocationHandler(searchId, result));
				
				transformed.add(proxy);
			}
		} catch (IllegalArgumentException e) {
			logger.error("Error creating proxyInstance", e);
		} 
    	
    	return transformed;
    }

    public String getResultImage(SearchResult result) {
        if (result.getContext().equals("wiki")) {
            return "wiki_icon";
        } else if (result.getContext().equals("ticket")) {
            return "ticket_icon";
        }
        return "attachment_icon";
    }

    @Produces
    public List<SearchResult> getResults() {
        return results;
    }

    public List<FilterTypeSelection> getFilterTypeSelections() {
        return filterTypeSelections;
    }

    public List<FilterPeriodSelection> getFilterPeriodSelections() {
        return filterPeriodSelections;
    }

    public List<SortSelection> getSortSelections() {
        return sortSelections;
    }

    public FilterTypeSelection getSelectedFilterType() {
        return selectedFilterType;
    }

    public void setSelectedFilterType(FilterTypeSelection selectedFilterType) {
        this.selectedFilterType = selectedFilterType;
    }

    public FilterPeriodSelection getSelectedFilterPeriod() {
        return selectedFilterPeriod;
    }

    public void setSelectedFilterPeriod(
            FilterPeriodSelection selectedFilterPeriod) {
        this.selectedFilterPeriod = selectedFilterPeriod;
    }

    public SortSelection getSelectedSort() {
        return selectedSort;
    }

    public void setSelectedSort(SortSelection selectedSort) {
        this.selectedSort = selectedSort;
    }

    public String getReportErrorLink() {
        return reportErrorLink;
    }
}

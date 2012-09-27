package com.redxiii.tracplus.web.search;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import com.redxiii.tracplus.ejb.search.TracStuffField;
import com.redxiii.tracplus.ejb.search.query.QueryBuilder;
import com.redxiii.tracplus.ejb.search.query.SimpleQuerySpec;
import com.redxiii.tracplus.ejb.util.UsageStatistics;
import com.redxiii.tracplus.web.AppSessionContext;
import java.io.Serializable;
import org.apache.commons.lang.SerializationUtils;

@Named("searchView")
@SessionScoped
public class SearchView implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DecimalFormat decFormat = new DecimalFormat("#.###");
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<SearchResult> results = new ArrayList<SearchResult>();
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

            if (o1.getScore() == o2.getScore()) {
                return o1.getId().compareTo((o2.getId()));
            }
            return Float.compare(o2.getScore(), o1.getScore());
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
    private UsageStatistics usageStatistics;
    private FilterTypeSelection selectedFilterType;
    private FilterPeriodSelection selectedFilterPeriod;
    private SortSelection selectedSort;
    private SearchMethod searchMethod;

    public SearchView() {
        filterTypeSelections = Collections.unmodifiableList(Arrays.asList(FilterTypeSelection.values()));
        filterPeriodSelections = Collections.unmodifiableList(Arrays.asList(FilterPeriodSelection.values()));
        sortSelections = Collections.unmodifiableList(Arrays.asList(SortSelection.values()));

        selectedFilterType = FilterTypeSelection.none;
        selectedFilterPeriod = FilterPeriodSelection.all_entries;
        selectedSort = SortSelection.relevance;
        searchMethod = SearchMethod.approximate;
    }

    public void doSearch(String method) {

        results.clear();

        logger.info("Searching for {}", searchInfo);

        if (ctx != null && ctx.getUser() != null) {
            logger.info("User: {}", ctx.getUser());
        } else {
            logger.warn("Session context od User not set");
        }

        if (searchInfo.getSearchText() == null || searchInfo.getSearchText().length() == 0) {
            return;
        }

        long initSearch = System.currentTimeMillis();
        searchMethod = SearchMethod.valueOf(method);

        String userid = ctx.getUser().getName();

        String interpreted = makeSearch();

        long elapsedTime = System.currentTimeMillis() - initSearch;

        searchInfo.setInterpretedQuery(interpreted);
        searchInfo.setElapsedTime(decFormat.format((double) elapsedTime / 1000D));

        usageStatistics.logSearch(userid, interpreted, elapsedTime);
        logger.info("Search done in {} ms. Quantity # {}", elapsedTime, results.size());
    }
    
    private String makeSearch() {
        logger.info("Creating SearchManager instance");
        SearchManager manager = searchManagerFactory.getManager();

        logger.debug("Creating query...");
        QueryBuilder<SimpleQuerySpec> baseBuilder = QueryBuilder.buildSimpleQuery();
        
        switch (selectedFilterType) {
            case ticket:
            case wiki:
                baseBuilder.addStrongRestriction(selectedFilterType.name(), TracStuffField.CONTEXT);
        }

        if (!selectedFilterPeriod.equals(FilterPeriodSelection.all_entries)) {
            baseBuilder.enableRecentFilter(selectedFilterPeriod.days);
        }
        
        QueryBuilder<SimpleQuerySpec> queryBuilder = (QueryBuilder<SimpleQuerySpec>) SerializationUtils.clone(baseBuilder);
        
        switch (searchMethod) {
            case advanced:
                queryBuilder.addLuceneRestriction(searchInfo.getSearchText());
                break;

            default:
            case smart:
                queryBuilder.addStrongRestriction(searchInfo.getSearchText(), TracStuffField.CONTENT);
                break;
        }
        
        Query query = manager.buildQuery(queryBuilder.createQuerySpec());
        
        Set<SearchResult> resultSet = manager.doSearch(query);
        if (searchMethod == SearchMethod.smart && resultSet.isEmpty()) {
            logger.info("'Smart Search' return empty set. Trying 'Approximate + Precise Search' ...");
            
            baseBuilder.addWeakRestriction(searchInfo.getSearchText(), TracStuffField.CONTENT);
            baseBuilder.addLikeRestriction(searchInfo.getSearchText(), TracStuffField.CONTENT);
            query = manager.buildQuery(baseBuilder.createQuerySpec());
            resultSet = manager.doSearch(query);
        }
        
        logger.debug("Searching and sorting...");
        results.addAll(getSortedResults(resultSet));
        
        return query.toString();
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
}

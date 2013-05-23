package com.redxiii.tracplus.ejb.search.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.redxiii.tracplus.ejb.search.TracStuffField;
import com.redxiii.tracplus.ejb.util.AppConfiguration;

public class QueryBuilder<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private BasicQuery query;

    private final int MINIMAL_SIZE_TERM;
    private final int REGULAR_SIZE_TERM;
    private final float REGULAR_FUZZY_WEIGHT;
    private final float OTHER_FUZZY_WEIGHT;
    
    private QueryBuilder() {
        query = new BasicQuery();
        Configuration config = AppConfiguration.getInstance();
        MINIMAL_SIZE_TERM = config.getInt("lucene.search-manager.term.minimal-size", 3);
        REGULAR_SIZE_TERM = config.getInt("lucene.search-manager.term.regular-size", 8);
        REGULAR_FUZZY_WEIGHT = config.getFloat("lucene.search-manager.fuzzy.regular-weigh", 0.65f);
        OTHER_FUZZY_WEIGHT = config.getFloat("lucene.search-manager.fuzzy.other-weigh", 0.75f);
    }

    public static QueryBuilder<SimpleQuerySpec> buildSimpleQuery() {
        return new QueryBuilder<SimpleQuerySpec>();
    }
    
    public QueryBuilder<SimpleQuerySpec> clone() {
    	QueryBuilder<SimpleQuerySpec> clone = QueryBuilder.buildSimpleQuery();
    	
    	clone.query = this.query.clone();
    	
    	return clone;
    }

    public QueryBuilder<T> setCustomQueryText(String text) {
        query.luceneQuery = text;
        return this;
    }

    public QueryBuilder<T> enableRecentFilter(int days) {
        query.filterRecentDays = days;
        return this;
    }

    private String[] parseSearchInfo(String searchInfo) {
        
        if (searchInfo.startsWith("\"") && searchInfo.endsWith("\"") && searchInfo.contains(" ")) {
            return new String[]{searchInfo.substring(1, searchInfo.length() - 1)};
        }
        
        if (searchInfo.contains(" ")) {
            return searchInfo.split(" ");
        }

        return new String[]{searchInfo};
    }

    public QueryBuilder<T> addWeakRestriction(String value, TracStuffField... fields) {
        for (String part : parseSearchInfo(value)) {
            if (part.length() >= MINIMAL_SIZE_TERM) {
                query.restrictions.add(new Restriction(part.toLowerCase(), new HashSet<TracStuffField>(Arrays.asList(fields)), false, false));
            }
        }
        return this;
    }

    public QueryBuilder<T> addStrongRestriction(String value, TracStuffField... fields) {
        for (String part : parseSearchInfo(value)) {
            if (part.length() >= MINIMAL_SIZE_TERM) {
                query.restrictions.add(new Restriction(part.toLowerCase(), new HashSet<TracStuffField>(Arrays.asList(fields)), false, true));        
            }
        }
        return this;
    }

    public QueryBuilder<T> addLikeRestriction(String value, TracStuffField... fields) {
        for (String part : parseSearchInfo(value)) {
            if (part.length() >= MINIMAL_SIZE_TERM) {

                float weight;
                if (part.length() <= REGULAR_SIZE_TERM) {
                    weight = REGULAR_FUZZY_WEIGHT;
                } else {
                    weight = OTHER_FUZZY_WEIGHT;
                }

                query.restrictions.add(new Restriction(part.toLowerCase(), new HashSet<TracStuffField>(Arrays.asList(fields)), true, true, weight));
            }
        }

        return this;
    }

    public QueryBuilder<T> addLuceneRestriction(String lucene) {
        query.luceneQuery = lucene;
        return this;
    }

    @SuppressWarnings("unchecked")
    public T createQuerySpec() {
        return (T) query;
    }
}

class BasicQuery implements SimpleQuerySpec, Serializable {

	private static final long serialVersionUID = 1L;
	
	protected int maxHits = 100;
    protected String luceneQuery;
    protected Integer filterRecentDays;
    protected Collection<Restriction> restrictions = new ArrayList<Restriction>();

    public String getLuceneQuery() {
        return luceneQuery;
    }
    
    public BasicQuery clone() {
    	BasicQuery clone = new BasicQuery();
    	
    	clone.maxHits = this.maxHits;
    	clone.luceneQuery = this.luceneQuery;
    	clone.filterRecentDays = this.filterRecentDays;
    	clone.restrictions.addAll(this.restrictions);
    	
    	return clone;
    }

    public int getMaxHits() {
        return maxHits;
    }

    @Override
    public boolean isRecentFilterEnable() {
        return filterRecentDays != null;
    }

    @Override
    public Integer getRecentFilterDays() {
        return filterRecentDays;
    }

    @Override
    public Set<String> getValuesRestriction(TracStuffField field) {
        Set<String> set = new HashSet<String>();
        for (Restriction restriction : restrictions) {
            if (restriction.fields.contains(field)) {
                set.add(restriction.value);
            }
        }
        return set;
    }

    @Override
    public Set<TracStuffField> getQueryFields() {
        Set<TracStuffField> set = new HashSet<TracStuffField>();
        for (Restriction restriction : restrictions) {
            set.addAll(restriction.fields);
        }
        return set;
    }

    @Override
    public boolean isLikeRestriction(TracStuffField field, String value) {
        for (Restriction restriction : restrictions) {
            if (restriction.fields.contains(field) && restriction.value.equals(value)) {
                return restriction.like;
            }
        }
        return false;
    }

    @Override
    public boolean isStrongRestriction(TracStuffField field, String value) {
        for (Restriction restriction : restrictions) {
            if (restriction.fields.contains(field) && restriction.value.equals(value)) {
                return restriction.mustHave;
            }
        }
        return false;
    }

    @Override
    public float getLikeRestrictionWeight(TracStuffField field, String value) {
        for (Restriction restriction : restrictions) {
            if (restriction.fields.contains(field) && restriction.value.equals(value)) {
                return restriction.weight;
            }
        }
        return 1;
    }
}

class Restriction implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public final String value;
    public final Set<TracStuffField> fields;
    public final boolean like;
    public final boolean mustHave;
    public final float weight;

    Restriction(String value, Set<TracStuffField> fields, boolean like, boolean mustHave, float weight) {
        this.value = value;
        this.fields = fields;
        this.like = like;
        this.weight = weight;
        this.mustHave = mustHave;
    }

    Restriction(String value, HashSet<TracStuffField> fields, boolean like, boolean mustHave) {
        this(value, fields, like, mustHave, 1);
    }
}
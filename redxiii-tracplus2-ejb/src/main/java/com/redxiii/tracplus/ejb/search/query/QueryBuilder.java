package com.redxiii.tracplus.ejb.search.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.redxiii.tracplus.ejb.search.TracStuffField;

public class QueryBuilder<T> {

	private BasicQuery query;
	
	private QueryBuilder() {
		query = new BasicQuery();
	}
	
	public static QueryBuilder<SimpleQuerySpec> buildSimpleQuery() {
		return new QueryBuilder<SimpleQuerySpec>();
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
		if (searchInfo.contains(" ") && !searchInfo.startsWith("\""))
			return searchInfo.split(" ");
		
		return new String[]{searchInfo};
	}
	
	public QueryBuilder<T> addWeakRestriction(String value, TracStuffField... fields) {
		for (String part : parseSearchInfo(value)) {
			if (part.length() >= 3)
				query.restrictions.add(new Restriction(part.toLowerCase(), new HashSet<TracStuffField>(Arrays.asList(fields)), false, false));
		}
		return this;
	}

	public QueryBuilder<T> addStrongRestriction(String value, TracStuffField... fields) {
		query.restrictions.add(new Restriction(value, new HashSet<TracStuffField>(Arrays.asList(fields)), false, true));
		return this;
	}

	public QueryBuilder<T> addLikeRestriction(String value, TracStuffField... fields) {
		addLikeRestriction(value.toLowerCase(), Restriction.DEFAULT_LIKE_WEIGHT, fields);
		return this;
	}
	
	public QueryBuilder<T> addLikeRestriction(String value, float weight, TracStuffField... fields) {
		for (String part : parseSearchInfo(value)) {
			if (part.length() >= 3)
				query.restrictions.add(new Restriction(part, new HashSet<TracStuffField>(Arrays.asList(fields)), true, false, weight));
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

class BasicQuery implements SimpleQuerySpec {

	protected int maxHits = 100;
	protected String luceneQuery;
	protected Integer filterRecentDays;
	protected Collection<Restriction> restrictions = new ArrayList<Restriction>();
	
	public String getLuceneQuery() {
		return luceneQuery;
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
			if (restriction.fields.contains(field))
				set.add(restriction.value);
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
			if (restriction.fields.contains(field) && restriction.value.equals(value))
				return restriction.like;
		}
		return false;
	}
	
	@Override
	public boolean isStrongRestriction(TracStuffField field, String value) {
		for (Restriction restriction : restrictions) {
			if (restriction.fields.contains(field) && restriction.value.equals(value))
				return restriction.mustHave;
		}
		return false;
	}
	
	@Override
	public float getLikeRestrictionWeight(TracStuffField field, String value) {
		for (Restriction restriction : restrictions) {
			if (restriction.fields.contains(field) && restriction.value.equals(value))
				return restriction.weight;
		}
		return Restriction.DEFAULT_LIKE_WEIGHT;
	}
	
}

class Restriction {
	public final String value;
	public final Set<TracStuffField> fields;
	public final boolean like;
	public final boolean mustHave;
	public final float weight;
	public static final float DEFAULT_LIKE_WEIGHT = 0.70F;
	
	public Restriction(String value, Set<TracStuffField> fields, boolean like, boolean mustHave, float weight) {
		this.value = value;
		this.fields = fields;
		this.like = like;
		this.weight = weight; 
		this.mustHave = mustHave;
	}
	
	public Restriction(String value, Set<TracStuffField> fields, boolean like, boolean mustHave) {
		this(value, fields, like, mustHave, DEFAULT_LIKE_WEIGHT);
	}
}
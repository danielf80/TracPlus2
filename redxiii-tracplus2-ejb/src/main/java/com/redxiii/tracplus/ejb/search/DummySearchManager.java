package com.redxiii.tracplus.ejb.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.search.Query;

import com.redxiii.tracplus.ejb.search.query.SimpleQuerySpec;

public class DummySearchManager implements SearchManager {

	@SuppressWarnings("serial")
	@Override
	public Query buildQuery(SimpleQuerySpec spec) {
		return new Query() {
			@Override
			public String toString(String arg) {
				return arg;
			}
		};
	}

	@Override
	public Set<SearchResult> doSearch(Query query) {
		SearchResult result = new SearchResult() {
			@Override
			public String getUrl() {
				return "url";
			}
			
			@Override
			public float getScore() {
				return 1;
			}
			
			@Override
			public String getModifiedDate() {
				return "2012-04-01";
			}
			
			@Override
			public String getId() {
				return "#1234";
			}
			
			@Override
			public List<String> getFragments() {
				return new ArrayList<String>();
			}
			
			@Override
			public String getDescription() {
				return "description";
			}
			
			@Override
			public String getCreatedDate() {
				return "2012-04-01";
			}
			
			@Override
			public String getContext() {
				return "wiki";
			}
			
			@Override
			public String getAuthor() {
				return "dfilgueiras";
			}
		};
		
		Set<SearchResult> list = new HashSet<SearchResult>();
		list.add(result);
		return list;
	}

	public static SearchManager getInstance() {
		return new DummySearchManager();
	}

}

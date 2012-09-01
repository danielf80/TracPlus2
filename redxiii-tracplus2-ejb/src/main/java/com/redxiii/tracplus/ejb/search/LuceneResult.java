package com.redxiii.tracplus.ejb.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;

public class LuceneResult implements SearchResult {

	private Document document;
	private float score;
	private List<String> fragments = new ArrayList<String>();
	
	public LuceneResult(Document document, float score) {
		this.document = document;
		this.score = score;
	}
	
	public void addFragment(String fragment) {
		this.fragments.add(fragment);
	}
	public List<String> getFragments() {
		return fragments;
	}
	
	public float getScore() {
		return score;
	}

	public String getUrl() {
		return document.get(TracStuffField.URL.toString());
	}

	public String getDescription() {
		return document.get(TracStuffField.DESCRIPTION.toString());
	}

	public String getAuthor() {
		return document.get(TracStuffField.AUTHOR.toString());
	}

	public String getCreatedDate() {
		return document.get(TracStuffField.CREATED_DATE.toString());
	}

	public String getModifiedDate() {
		return document.get(TracStuffField.MODIFIED_DATE.toString());
	}
	public String getContext() {
		return document.get(TracStuffField.CONTEXT.toString());
	}
	public String getId() {
		return document.get(TracStuffField.ID.toString());
	}
	
	@Override
	public String toString() {
		return getId();
	}
}

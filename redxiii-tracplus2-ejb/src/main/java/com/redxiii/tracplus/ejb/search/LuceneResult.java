package com.redxiii.tracplus.ejb.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;

public class LuceneResult implements SearchResult {

    private String id;
    private String modifiedDate;
    private String context;
    private Document document;
    private float score;
    private List<String> fragments = new ArrayList<String>();

    public LuceneResult(Document document, float score) {
        this.document = document;
        this.score = score;
        this.id = document.get(TracStuffField.ID.toString());
        this.modifiedDate = document.get(TracStuffField.MODIFIED_DATE.toString());
        this.context = document.get(TracStuffField.CONTEXT.toString());
    }

    @Override
    public int compareTo(SearchResult other) {
        return this.id.compareTo(other.getId());
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        SearchResult obj = (SearchResult) other;
        return this.id.equals(obj.getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
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

    public void setScore(float score) {
        this.score = score;
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
        return modifiedDate;
    }

    public String getContext() {
        return context;
    }

    public String getId() {
        return id;
    }

    public String getCc() {
        return document.get(TracStuffField.CC.toString());
    }

    @Override
    public boolean hasCc(String user) {
        
        if (context.equals("wiki"))
            return true;
        
        String cc = getCc();
        if (cc == null) {
            return false;
        }
        return cc.contains(user);
    }

    @Override
    public String toString() {
        return getId();
    }
}

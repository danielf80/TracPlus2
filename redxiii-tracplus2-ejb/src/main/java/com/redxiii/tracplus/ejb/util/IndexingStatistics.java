package com.redxiii.tracplus.ejb.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ApplicationScoped
public class IndexingStatistics {

	private long indexingStart;
	private long indexingEnd;
	private long indexedBytes;
	private long indexedDocs;

	@Inject
	private Formatter formatter;
	
	public void resetIndexedStartTime() {
		indexingStart = System.currentTimeMillis();
		indexingEnd = indexingStart + 1; 
	}
	public void updateIndexedEndTime() {
		indexingEnd = System.currentTimeMillis();
	}
	
	public void newIndexedDoc(long bytes) {
		indexedBytes += bytes;
		indexedDocs++;
	}
	public void resetIndexedDocs() {
		indexedBytes = 0;
		indexedDocs = 0;
	}
	public long getIndexedBytes() {
		return indexedBytes;
	}
	public long getIndexedDocs() {
		return indexedDocs;
	}
	
	public String getFormattedIndexedBytes() {
		return formatter.formatBytes(indexedBytes);
	}
	
	public String getFormattedIndexedTime() {
		return formatter.formatTimeInMillisToHour(indexingEnd - indexingStart);
	}
}

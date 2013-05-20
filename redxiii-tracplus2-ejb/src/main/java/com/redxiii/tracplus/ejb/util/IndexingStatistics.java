package com.redxiii.tracplus.ejb.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

	private final Lock writeLock = new ReentrantLock();
	
	@Inject
	private Formatter formatter;
	
	public void resetIndexedStartTime() {
		writeLock.lock();
		try {
			indexingStart = System.currentTimeMillis();
			indexingEnd = indexingStart + 1;
		} finally {
			writeLock.unlock();
		}
	}
	public void updateIndexedEndTime() {
		writeLock.lock();
		try {
			indexingEnd = System.currentTimeMillis();
		} finally {
			writeLock.unlock();
		}
	}
	
	public void newIndexedDoc(long bytes) {
		writeLock.lock();
		try {
			indexedBytes += bytes;
			indexedDocs++;
		} finally {
			writeLock.unlock();
		}
	}
	public void resetIndexedDocs() {
		writeLock.lock();
		try {
			indexedBytes = 0;
			indexedDocs = 0;
		} finally {
			writeLock.unlock();
		}
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

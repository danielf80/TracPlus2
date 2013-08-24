package com.redxiii.tracplus.ejb.util;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Named;

/**
 * @author Daniel Filgueiras
 * @since 17/08/2012
 */
@Named
public class Statistics {

	private int searchCount;
	private int todaySearchCount;
	private Map<String,Integer> searchsPerUser = new TreeMap<String, Integer>();
	private Map<String,Integer> searchsPerPeriod = new TreeMap<String, Integer>();
	
	private long totalElapsedTime;	//TODAY
	
	private int searchWithResultsCount;
	private int searchWithoutResultsCount;
	private int searchWithManyClick;
	private int searchWithOneClick;
	private int searchWithZeroClick;
	
	private Set<String> todayUsers = new TreeSet<String>();

	public void resetTodayStats() {
		todayUsers.clear();
		totalElapsedTime = 0;
		todaySearchCount = 0;
	}
	
	/**
	 * @return average search speed in milliseconds
	 */
	public long getAverageSearchSpeed() {
		if (todayUsers.size() == 0)
			return 0;
		
		return Math.max(totalElapsedTime / todaySearchCount, 1);
	}
	
	public void logSearch(String user, String period, String term, long time, int resultsQtd) {
		
		searchCount++;
		todaySearchCount++;
		
		todayUsers.add(user);
		totalElapsedTime += time;
		
		if (searchsPerUser.containsKey(user)) {
			searchsPerUser.put(user, searchsPerUser.get(user) + 1);
		} else {
			searchsPerUser.put(user, 0);
		}
		
		if (searchsPerPeriod.containsKey(period)) {
			searchsPerPeriod.put(period, searchsPerPeriod.get(period) + 1);
		} else {
			searchsPerPeriod.put(period, 1);
		}
		
		if (resultsQtd > 0)
			searchWithResultsCount++;
		else
			searchWithoutResultsCount++;
		searchWithZeroClick++;
	}
	
	public void logClick(int clicks) {
		switch (clicks) {
			case 1:
				searchWithZeroClick--;
				searchWithOneClick++;
				break;
			case 2:
				searchWithOneClick--;
				searchWithManyClick++;
			break;
		}
	}
	
	public int getTodaySearchCount() {
		return todaySearchCount;
	}
	
	public long getTotalElapsedTime() {
		return totalElapsedTime;
	}

	public void setSearchTime(long searchTime) {
		this.totalElapsedTime = searchTime;
	}

	public Set<String> getTodayUsers() {
		return todayUsers;
	}

	public Map<String, Integer> getSearchsPerPeriod() {
		return searchsPerPeriod;
	}
	public Map<String, Integer> getSearchsPerUser() {
		return searchsPerUser;
	}
	public int getSearchCount() {
		return searchCount;
	}
	public void setSearchCount(int totalSearchs) {
		this.searchCount = totalSearchs;
	}

	public int getSearchWithResultsCount() {
		return searchWithResultsCount;
	}

	public void setSearchWithResultsCount(int searchWithResultsCount) {
		this.searchWithResultsCount = searchWithResultsCount;
	}

	public int getSearchWithoutResultsCount() {
		return searchWithoutResultsCount;
	}

	public void setSearchWithoutResultsCount(int searchWithoutResultsCount) {
		this.searchWithoutResultsCount = searchWithoutResultsCount;
	}

	public int getSearchWithManyClick() {
		return searchWithManyClick;
	}

	public void setSearchWithManyClick(int searchWithMulipleClick) {
		this.searchWithManyClick = searchWithMulipleClick;
	}

	public int getSearchWithOneClick() {
		return searchWithOneClick;
	}

	public void setSearchWithOneClick(int searchWithOneClick) {
		this.searchWithOneClick = searchWithOneClick;
	}

	public int getSearchWithZeroClick() {
		return searchWithZeroClick;
	}

	public void setSearchWithZeroClick(int searchWithZeroClick) {
		this.searchWithZeroClick = searchWithZeroClick;
	}
	
	
}

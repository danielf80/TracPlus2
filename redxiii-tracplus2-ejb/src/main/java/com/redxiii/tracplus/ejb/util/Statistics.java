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
	
	private Set<String> todayUsers = new TreeSet<String>();

	/**
	 * @return average search speed in milliseconds
	 */
	public long getAverageSearchSpeed() {
		if (todayUsers.size() == 0)
			return 0;
		
		return Math.max(totalElapsedTime / todaySearchCount, 1);
	}
	
	public void logSearch(String user, String period, String term, long time) {
		
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
}

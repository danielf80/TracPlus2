package com.redxiii.tracplus.web.info;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.util.Statistics;
import com.redxiii.tracplus.ejb.util.UsageStatistics;

@Named
@RequestScoped
public class InfoView implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final DecimalFormat decFormat = new DecimalFormat("#.###"); 
	
	private final Logger logger = LoggerFactory.getLogger(getClass()); 
	
	@Inject
	private UsageStatistics usageStatistics;
	
	private Statistics statistics;

	@PostConstruct
	public void init() {
		logger.info("Fetching stats...");
		statistics = usageStatistics.getStatistics();
	}
	
	public String getSearchCount() {
		return Integer.toString(statistics.getSearchCount());
	}
	public int getTodaySearchCount() {
		return statistics.getTodaySearchCount();
	}
	public String getAverageSearchTime() {
		return decFormat.format(statistics.getAverageSearchSpeed() / 1000D);
	}
	
	public List<SearchCount> getUsersStats() {
		
		List<SearchCount> counts = new ArrayList<SearchCount>();
		
		for (Entry<String, Integer> entry : statistics.getSearchsPerUser().entrySet()) {
			counts.add(new SearchCount(entry.getKey(), Integer.toString(entry.getValue())));
		}
		return counts;
	}

	public List<SearchCount> getPeriodStats() {
		
		List<SearchCount> counts = new ArrayList<SearchCount>();
		
		for (Entry<String, Integer> entry : statistics.getSearchsPerPeriod().entrySet()) {
			counts.add(new SearchCount(entry.getKey(), Integer.toString(entry.getValue())));
		}
		return counts;
	}
	
	public List<String> getWhoPerformedSearch() {
		return new ArrayList<String>(statistics.getTodayUsers());
	}
}

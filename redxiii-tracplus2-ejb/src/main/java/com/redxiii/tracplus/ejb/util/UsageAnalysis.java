package com.redxiii.tracplus.ejb.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.redxiii.tracplus.ejb.search.SearchResult;

@Named("usageAnalysis")
@ApplicationScoped
public class UsageAnalysis implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Logger logger = LoggerFactory.getLogger(getClass()); 
	
	private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
	
	private long lastIndexUpdate = System.currentTimeMillis();  //TODO: BUG Detected
	
	private LoadingCache<Integer, SearchMetric> resultsCache;
	private Statistics statistics;
	
	private FileConfiguration configuration;

	private class SearchMetric {
		int qtd;
		int qtdBestResults;
		int clickedResults;
	}
	
	@PostConstruct
	public void init() {
		resultsCache = CacheBuilder.newBuilder()
				.maximumSize(500)
				.expireAfterWrite(10, TimeUnit.DAYS)
				.build(new CacheLoader<Integer, SearchMetric>(){
					@Override
					public SearchMetric load(Integer id) throws Exception {
						return null;
					}
				});
		
		lastIndexUpdate = AppConfiguration.getInstance().getLong("lucene.index-builder.last-update", 0);
		File statsFile = new File(AppConfiguration.getServerConfigFolder() + "tracplus2-stats.properties");
		try {
			if (statsFile.exists() || statsFile.createNewFile()) {
				try {
					configuration = new PropertiesConfiguration(statsFile);
					configuration.setAutoSave(true);
					configuration.setReloadingStrategy(new FileChangedReloadingStrategy());	// 5 seconds
					
					logger.debug("Configuration file '{}' loaded", statsFile);
				} catch (ConfigurationException e) {
					logger.error("Unable to load configuration file {}", e, statsFile);
					configuration = new PropertiesConfiguration();
				}
			}
		} catch (IOException e) {
			logger.error("Unable to load configuration file {}", e, statsFile);
			return;
		}
		
		loadSearchsStats();
	}
	
	@Schedule(hour = "8", minute = "50", second = "0")
	public void resetUsage() {
		statistics.resetTodayStats();
	}
	
	@SuppressWarnings("unchecked")
	private void loadSearchsStats() {
		
		logger.info("Loading statistics from file...");
		
		statistics = new Statistics();
		statistics.setSearchCount(configuration.getInt("stats.query-count.all", 0));
		
		final String tagUser = "stats.query-count.user";
		Iterator<String> iterator = configuration.getKeys(tagUser);
		while (iterator.hasNext()) {
			String key = iterator.next();
			String user = key.substring(tagUser.length() + 1);
			statistics.getSearchsPerUser().put(user, configuration.getInteger(key, 0));
		}
		
		final String tagMonth = "stats.query-count.period";
		iterator = configuration.getKeys(tagMonth);
		while (iterator.hasNext()) {
			String key = iterator.next();
			String month = key.substring(tagMonth.length() + 1);
			statistics.getSearchsPerPeriod().put(month, configuration.getInteger(key, 0));
		}
		
		statistics.setSearchWithResultsCount(configuration.getInt("stats.query-count.with-results", 0));
		statistics.setSearchWithoutResultsCount(configuration.getInt("stats.query-count.without-results", 0));
		statistics.setSearchWithZeroClick(configuration.getInt("stats.query-count.with-zero-click", 0));
		statistics.setSearchWithOneClick(configuration.getInt("stats.query-count.with-one-click", 0));
		statistics.setSearchWithManyClick(configuration.getInt("stats.query-count.with-many-click", 0));
	}
	
	@Lock(LockType.WRITE)
	public void logSearch(String userid, String term, long time, int id, List<SearchResult> results) {
		
		logger.debug("Adding SearchMetric to ResultsCache");
		
		SearchMetric metric = new SearchMetric();
		metric.qtd = results.size();
		metric.qtdBestResults = 0;
		for (SearchResult result : results) {
			if (result.getCode() == 0)
				metric.qtdBestResults++;	
		}
		resultsCache.put(id, metric);
		
		String period = monthFormat.format(new Date());
		
		logger.debug("Updating statistics object");
		statistics.logSearch(userid, period, term, time, results.size());
		
		logger.debug("Updating statistics file");
		configuration.setProperty("stats.query-count.all", configuration.getLong("stats.query-count.all", 0) + 1L);
		configuration.setProperty("stats.query-count.user." + userid, configuration.getLong("stats.query-count.user." + userid, 0) + 1L);
		configuration.setProperty("stats.query-count.period." + period, configuration.getLong("stats.query-count.period." + period, 0) + 1L);
		
		configuration.setProperty("stats.query-count.with-results", statistics.getSearchWithResultsCount());
		configuration.setProperty("stats.query-count.without-results", statistics.getSearchWithoutResultsCount());
		configuration.setProperty("stats.query-count.zero-click", statistics.getSearchWithZeroClick());
		
		logger.debug("Updating statistics file done");
	}
	
	@Lock(LockType.WRITE)
	public void logClick(Integer searchId) {
		logger.debug("Updating statistics");
		try {
			SearchMetric metric = resultsCache.get(searchId);
			if (metric != null) {
				metric.clickedResults++;
				statistics.logClick(metric.clickedResults);
				
				configuration.setProperty("stats.query-count.one-click", statistics.getSearchWithOneClick());
				configuration.setProperty("stats.query-count.many-click", statistics.getSearchWithManyClick());
				configuration.setProperty("stats.query-count.zero-click", statistics.getSearchWithZeroClick());
			} else {
				logger.warn("Unable to fetch search id {}", searchId);
			}
		} catch (ExecutionException e) {
			logger.error("Error loading metric from cache", e);
		}
		logger.debug("Updating statistics done");
	}
	
	public int getCachedSearchQtd() {
		Map<Integer, SearchMetric> metrics = resultsCache.asMap();
		return metrics.size();
	}
	
	public int getCachedSearchQtdWithoutResults() {
		Map<Integer, SearchMetric> metrics = resultsCache.asMap();
		int qtd = 0;
		for (SearchMetric metric : metrics.values()) {
			if (metric.qtd == 0) {
				qtd++;
			}
		}
		
		return qtd;
	}
	
	
	/**
	 * 
	 * @param clicks (Zero or One/Single)
	 * @return
	 */
	public int getCachedClickedSearchQtd(int clicks) {
		Map<Integer, SearchMetric> metrics = resultsCache.asMap();
		int qtd = 0;
		for (SearchMetric metric : metrics.values()) {
			if (metric.qtd > 0 && metric.clickedResults == clicks) {
				qtd++;
			}
		}
		
		return qtd;
	}
	
	public int getCachedBestResultsQtd() {
		Map<Integer, SearchMetric> metrics = resultsCache.asMap();
		int qtd = 0;
		for (SearchMetric metric : metrics.values()) {
			if (metric.qtdBestResults > 0) {
				qtd++;
			}
		}
		
		return qtd;
	}
	
	public int getResultsCacheQtd() {
		return resultsCache.asMap().size();
	}
	
	public long getLastIndexUpdate() {
		return lastIndexUpdate;
	}
	
	@Lock(LockType.WRITE)
	public void setLastIndexUpdate(long lastIndexUpdate) {
		AppConfiguration.getInstance().setProperty("lucene.index-builder.last-update", lastIndexUpdate);
		this.lastIndexUpdate = lastIndexUpdate;
	}
	
	public Statistics getStatistics() {
		return statistics;
	}
}

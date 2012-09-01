package com.redxiii.tracplus.ejb.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author Daniel Filgueiras
 * @since 17/08/2012
 */
@Named
@ApplicationScoped
public class UsageStatistics {

	private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
	
	private final Logger logger = LoggerFactory.getLogger(getClass()); 
	
	private FileConfiguration configuration;
	
	private long lastIndexUpdate;
	
	private LoadingCache<LocalDate, Statistics> statisticsCache;
	
	@PostConstruct
	public void init() {
	
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
		}
		
		statisticsCache = CacheBuilder.newBuilder()
				.maximumSize(1)
				.expireAfterWrite(5, TimeUnit.MINUTES)
				.build(new CacheLoader<LocalDate, Statistics>() {
					@Override
					public Statistics load(LocalDate date) throws Exception {
						
						logger.info("Loading statistics from file...");
						
						Statistics statistics = new Statistics();
						statistics.setSearchCount(configuration.getInt("stats.query-count.all"));
						
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
						
						return statistics;
					}
				});
		
	}
	
	public Statistics getStatistics() {
		
		try {
			return statisticsCache.get(new LocalDate());
		} catch (ExecutionException e) {
			logger.error("Error loading statistics from cache", e);
			return new Statistics();
		}
	}
	
	public void logSearch(String userid, String term, long time) {
		
		Statistics statistics = getStatistics();
		String period = monthFormat.format(new Date());
		
		synchronized (configuration) {
			configuration.setProperty("stats.query-count.all", configuration.getLong("stats.query-count.all", 0) + 1L);
			configuration.setProperty("stats.query-count.user." + userid, configuration.getLong("stats.query-count.user." + userid, 0) + 1L);
			configuration.setProperty("stats.query-count.period." + period, configuration.getLong("stats.query-count.period." + period, 0) + 1L);
		}
		
		statistics.logSearch(userid, period, term, time);
	}
	
	public long getLastIndexUpdate() {
		return lastIndexUpdate;
	}
	
	public void setLastIndexUpdate(long lastIndexUpdate) {
		this.lastIndexUpdate = lastIndexUpdate;
	}
}

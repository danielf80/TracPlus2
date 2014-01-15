package com.redxiii.tracplus.ejb.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Filgueiras
 * @since 23/08/2012
 * 
 * TODO: http://czetsuya-tech.blogspot.com.br/2012/07/how-to-load-property-file-from.html
 */
public class AppConfiguration extends ServerConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);
	private static final AtomicInteger INSTANCE_ID = new AtomicInteger(1);
	
	private static AppConfiguration instance;
	private PropertiesConfiguration configuration;
	
	private AppConfiguration() {
		
		String url = getServerConfigFolder() + "tracplus2.properties";
		logger.debug("Loading Configuration file ({}): '{}'", INSTANCE_ID.getAndIncrement(), url);
		try {
			configuration = new PropertiesConfiguration(url);
			configuration.setAutoSave(true);
			configuration.setReloadingStrategy(new FileChangedReloadingStrategy());	// 5 seconds
			
			logger.debug("Configuration file '{}' loaded", url);
		} catch (ConfigurationException e) {
			logger.error("Unable to load configuration file {}", e, url);
			configuration = new PropertiesConfiguration();
		} 
	}
		
	public synchronized static Configuration getInstance() {
		if (instance == null)
			instance = new AppConfiguration();
		
		return instance.configuration;
	}
	
	public Iterator<String> getKeys() {
		return configuration.getKeys();
	}
	
	public Long getQueryCount() {
		synchronized (configuration) {
			return configuration.getLong("stats.query-count.all", 0);
		}
	}
	
	public Long getQueryCountMonth() {
		synchronized (configuration) {
			return configuration.getLong("stats.query-count." + monthFormat.format(new Date()), 0);
		}
	}
	
	public Long getQueryCountUser(String userid) {
		synchronized (configuration) {
			return configuration.getLong("stats.query-count.user." + userid, 0);
		}
	}
	
	private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
	
}

package com.redxiii.tracplus.ejb.util;

import java.io.File;
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
public class AppConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);
	private static final AtomicInteger INSTANCE_ID = new AtomicInteger(1);
	
	private static final String JBOSS_BASE_DIR_KEY = "jboss.server.base.dir";
	private static final String GLASSFISH_BASE_DIR_KEY = "com.sun.aas.instanceRoot";
	
	private static final String JBOSS_CFG_DIR_KEY = File.separator + "configuration" + File.separator;
	private static final String GLASSFISH_CFG_DIR_KEY = File.separator + "config" + File.separator;
	
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
	
	public static String getRootConfigFolder() {
		String baseDir = System.getProperty(JBOSS_BASE_DIR_KEY);
		if (baseDir != null && new File(baseDir).exists()) {
			return baseDir + File.separator;
		}
		
		baseDir = System.getProperty(GLASSFISH_BASE_DIR_KEY);
		if (baseDir != null && new File(baseDir).exists()) {
			return baseDir + File.separator;
		}
		
		return System.getProperty("user.dir") + File.separator;
	}
	
	public static String getServerConfigFolder() {
		String baseDir = System.getProperty(JBOSS_BASE_DIR_KEY);
		if (baseDir != null && new File(baseDir).exists()) {
			return baseDir + JBOSS_CFG_DIR_KEY;
		}
		
		baseDir = System.getProperty(GLASSFISH_BASE_DIR_KEY);
		if (baseDir != null && new File(baseDir).exists()) {
			return baseDir + GLASSFISH_CFG_DIR_KEY;
		}
		
		return System.getProperty("user.dir") + File.separator;
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

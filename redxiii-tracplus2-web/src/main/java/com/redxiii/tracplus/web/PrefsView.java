package com.redxiii.tracplus.web;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.util.AppConfiguration;

/**
 * @author Daniel Filgueiras
 * @since 28/08/2012
 */
@Named
@RequestScoped
public class PrefsView implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(getClass()); 
	
	public static class Preferences {
		boolean autoUpdate;
		boolean autoUpdateWiki;
		boolean autoUpdateTicket;
		boolean autoUpdateAttachments;
		
		boolean googleAuth;
		
		String tracAttachPaths;
		String tracWebUrl;
		
		boolean cachedSearch;

		public boolean isAutoUpdate() {
			return autoUpdate;
		}

		public void setAutoUpdate(boolean autoUpdate) {
			this.autoUpdate = autoUpdate;
		}

		public boolean isAutoUpdateWiki() {
			return autoUpdateWiki;
		}

		public void setAutoUpdateWiki(boolean autoUpdateWiki) {
			this.autoUpdateWiki = autoUpdateWiki;
		}

		public boolean isAutoUpdateTicket() {
			return autoUpdateTicket;
		}

		public void setAutoUpdateTicket(boolean autoUpdateTicket) {
			this.autoUpdateTicket = autoUpdateTicket;
		}

		public boolean isAutoUpdateAttachments() {
			return autoUpdateAttachments;
		}

		public void setAutoUpdateAttachments(boolean autoUpdateAttachments) {
			this.autoUpdateAttachments = autoUpdateAttachments;
		}

		public boolean isGoogleAuth() {
			return googleAuth;
		}

		public void setGoogleAuth(boolean googleAuth) {
			this.googleAuth = googleAuth;
		}

		public String getTracAttachPaths() {
			return tracAttachPaths;
		}

		public void setTracAttachPaths(String tracAttachPaths) {
			this.tracAttachPaths = tracAttachPaths;
		}

		public String getTracWebUrl() {
			return tracWebUrl;
		}

		public void setTracWebUrl(String tracWebUrl) {
			this.tracWebUrl = tracWebUrl;
		}

		public boolean isCachedSearch() {
			return cachedSearch;
		}

		public void setCachedSearch(boolean cachedSearch) {
			this.cachedSearch = cachedSearch;
		}
	}
	
	private Preferences preferences;
	
	@PostConstruct
	public void init() {
		Configuration configuration = AppConfiguration.getInstance();
		
		logger.debug("Loading preferences...");
		
		preferences = new Preferences();

		// Domain restriction
		preferences.googleAuth = configuration.getBoolean("web.security.authentication.google", true);
		
		// Dummy Search Manager
		preferences.cachedSearch = configuration.getBoolean("web.search-manager.cached", false);
		
		preferences.autoUpdate = configuration.getBoolean("lucene.index-builder.update", true);
		preferences.autoUpdateWiki = configuration.getBoolean("lucene.index-builder.update.wiki", true);
		preferences.autoUpdateTicket = configuration.getBoolean("lucene.index-builder.update.ticket", true);
		preferences.autoUpdateAttachments = configuration.getBoolean("lucene.index-builder.update.attachments", false);
		
		preferences.tracWebUrl = configuration.getString("trac.web.url","localhost");
		preferences.tracAttachPaths = configuration.getString("trac.home-dir.attachments","/tmp/");
		// Postgresql connection properties
	}
	
	public void save() {
		Configuration configuration = AppConfiguration.getInstance();
		
		logger.debug("Saving preferences...");
		
		configuration.setProperty("lucene.index-builder.update", preferences.autoUpdate);
		configuration.setProperty("lucene.index-builder.update.wiki", preferences.autoUpdateWiki);
		configuration.setProperty("lucene.index-builder.update.ticket", preferences.autoUpdateTicket);
		configuration.setProperty("lucene.index-builder.update.attachments", preferences.autoUpdateAttachments);
		
		configuration.setProperty("trac.home-dir.attachments", preferences.tracAttachPaths);
		configuration.setProperty("web.trac.host-url", preferences.tracWebUrl);
		
		configuration.setProperty("web.search-manager.cached", preferences.cachedSearch);
		configuration.setProperty("web.security.authentication.google", preferences.googleAuth);
	}
	
	public Preferences getPreferences() {
		return preferences;
	}
	
	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}
}

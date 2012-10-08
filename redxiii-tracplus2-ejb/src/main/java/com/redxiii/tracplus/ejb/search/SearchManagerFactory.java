package com.redxiii.tracplus.ejb.search;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.configuration.Configuration;

import com.redxiii.tracplus.ejb.search.updater.LuceneIndexManager;
import com.redxiii.tracplus.ejb.util.AppConfiguration;

/**
 * @author Daniel Filgueiras
 * @since 01/04/2012
 */
@Named
public class SearchManagerFactory implements Serializable {


	private static final long serialVersionUID = 1L;
	
	@Inject
	private LuceneIndexManager luceneIndexManager;
	
	public SearchManager getManager() {
		
		SearchManager manager = null;
		Configuration configuration = AppConfiguration.getInstance();
		
		boolean dummy = configuration.getBoolean("web.search-manager.dummy", false);
		boolean cached = configuration.getBoolean("web.search-manager.cached", false);

		if (dummy)
			manager = DummySearchManager.getInstance();
		else
			manager = luceneIndexManager;
		
		if (cached)
			manager = new CachedSearchManager(manager);
		
		return manager;
	}
}

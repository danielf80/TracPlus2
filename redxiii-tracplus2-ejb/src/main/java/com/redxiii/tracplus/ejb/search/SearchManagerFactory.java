package com.redxiii.tracplus.ejb.search;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.configuration.Configuration;

import com.redxiii.tracplus.ejb.search.updater.LuceneIndexManager;
import com.redxiii.tracplus.ejb.util.AppConfiguration;
import javax.ejb.Singleton;

/**
 * @author Daniel Filgueiras
 * @since 01/04/2012
 */
@Named
@Singleton
public class SearchManagerFactory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    private LuceneIndexManager luceneIndexManager;
    private SearchManager manager = null;

    public SearchManager getManager() {

        if (manager != null) {
            return manager;
        }

        Configuration configuration = AppConfiguration.getInstance();

        if (configuration.getBoolean("web.search-manager.dummy", false)) {
            manager = null;
        } else {
            manager = luceneIndexManager;
        }

        if (configuration.getBoolean("web.search-manager.cached", false)) {
            manager = new CachedSearchManager(manager);
        }

        return manager;
    }
}

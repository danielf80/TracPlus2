package com.redxiii.tracplus.ejb.search.sched;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.search.updater.TracIndexer;
import com.redxiii.tracplus.ejb.util.AppConfiguration;
import com.redxiii.tracplus.ejb.util.IndexingStatistics;

@Singleton
@Startup
public class IndexUpdaterScheduler {

    private Logger logger;
    
    @EJB
    private TracIndexer tracIndexer;
    
    @Inject
	private IndexingStatistics indexingStatistics;

    @Schedule(hour = "6", minute = "0", second = "0")
    public void completeUpdate() {

        if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update.daily", false)) {
            logger.info("Complete index update - Started");
            completeIndexUpdate();
        } else {
            logger.warn("Complete index update disabled");
        }
    }

    @Schedule(hour = "7-21", minute = "*/15", second = "0")
    public void incrementalUpdate() {

        if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update.incremental", false)) {
            logger.info("Incremental index update started");
            tracIndexer.incrementalIndexUpdate();
        } else {
            logger.warn("Incremental index update disabled");
        }
    }

    @PostConstruct
    public void init() {

        BasicConfigurator.configure();
        logger = LoggerFactory.getLogger(getClass());
        logger.info("App Started");

        if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update.at_startup", false)) {
            logger.info("Started index update at system startup");
            completeIndexUpdate();
        } else {
            logger.info("Index update at system startup disabled");
        }
    }

    public void completeIndexUpdate() {
        tracIndexer.completeIndexUpdate();
        indexingStatistics.resetIndexedDocs();
        indexingStatistics.resetIndexedStartTime();
    }
}

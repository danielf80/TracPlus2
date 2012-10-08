package com.redxiii.tracplus.ejb.search.sched;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.search.updater.TracIndexer;
import com.redxiii.tracplus.ejb.util.AppConfiguration;

@Singleton
@Startup
public class IndexUpdaterScheduler {

	private Logger logger;
	
	@EJB
	private TracIndexer tracIndexer;
	
	@Schedule(hour = "22", minute = "0", second = "0")
	public void completeUpdate() {
		logger.info("Schedule complete update stated");
		completeIndexUpdate();
	}
	
	@Schedule(minute = "0/15", second = "0")
	public void incrementalUpdate() {
		logger.info("Schedule incremental update stated");
		if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update", false))
			tracIndexer.incrementalIndexUpdate();
	}
	
	@PostConstruct
	public void init() {
		
		BasicConfigurator.configure();
		logger = LoggerFactory.getLogger(getClass());
		logger.info("App Started");
		completeIndexUpdate();
	}

	public void completeIndexUpdate() {
		
		if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update", false))
			tracIndexer.completeIndexUpdate();
		
	}
}

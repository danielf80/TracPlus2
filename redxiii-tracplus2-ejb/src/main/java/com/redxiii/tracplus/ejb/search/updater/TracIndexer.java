package com.redxiii.tracplus.ejb.search.updater;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.datasources.Datasource;
import com.redxiii.tracplus.ejb.datasources.RecentWiki;
import com.redxiii.tracplus.ejb.util.AppConfiguration;
import com.redxiii.tracplus.ejb.util.UsageAnalysis;

@Stateless
public class TracIndexer {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm:ss");
	
	@Inject
	private Datasource datasource;
	
	@Inject 
	private UsageAnalysis usageAnaysis;
	
	@Inject
	private LuceneIndexManager luceneIndexManager;
	
	@Resource(mappedName="java:/ConnectionFactory")
	private static ConnectionFactory connectionFactory;
	
	@Resource(mappedName="java:/queue.com.redxiii.tracplus2")
	private static Queue queue;
	
	@Asynchronous
	public void completeIndexUpdate() {
		
		luceneIndexManager.purgeAndRecreate();
		
		try {
			Connection connection = null;
			Session session = null;
			MessageProducer producer = null;
			try {
				connection = connectionFactory.createConnection();
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				producer = session.createProducer(queue);
				producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				
				if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update.wiki", false))
					requestWikiIndexing(session, producer);
				
				if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update.ticket", false))
					requestTicketIndexing(session, producer);
				
				if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update.attachments", false))
					requestAttachmentIndexing(session, producer);
				
				usageAnaysis.setLastIndexUpdate(System.currentTimeMillis());
				
			} catch (JMSException e) {
				logger.error("JMS error on message creation", e);
			} catch (Throwable e) {
				logger.error("Error on message creation", e);
			} finally {
				try {	if (producer != null) producer.close();	} catch (JMSException e) {}
				try {	if (session != null) session.close();	} catch (JMSException e) {}
				try {	if (connection != null) connection.close();	} catch (JMSException e) {}
			}
			
		} catch (Exception e) {
			logger.error("Error updating index",e);
		}
		logger.info("Thread {} done!", Thread.currentThread().getName());
	}
	
	@Asynchronous
	public void incrementalIndexUpdate() {
		
		try {
			Connection connection = null;
			Session session = null;
			MessageProducer producer = null;
			try {
				connection = connectionFactory.createConnection();
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				producer = session.createProducer(queue);
				producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				
				if (AppConfiguration.getInstance().getBoolean("lucene.index-builder.update.ticket", false)) {
					requestChangedTicketIndexing(session, producer);
				}
				usageAnaysis.setLastIndexUpdate(System.currentTimeMillis());
                                
			} catch (JMSException e) {
				logger.error("JMS error on message creation", e);
			} catch (Throwable e) {
				logger.error("Error on message creation", e);
			} finally {
				try {	if (producer != null) producer.close();	} catch (JMSException e) {}
				try {	if (session != null) session.close();	} catch (JMSException e) {}
				try {	if (connection != null) connection.close();	} catch (JMSException e) {}
			}
			
		} catch (Exception e) {
			logger.error("Error updating index",e);
		}
	}
	
	
	private void requestTicketIndexing(Session session, MessageProducer producer) throws JMSException {
		
		logger.info("Getting first and last ticket id...");
		
		Number firstTicketId = datasource.getFirstTicketId();
		logger.info("First ticket: {}", firstTicketId);
		
		final Number lastTicketId = datasource.getLastTicketId();
		logger.info("Last ticket: {}", lastTicketId);
		
//		firstTicketId = lastTicketId.intValue() - 1000;
		
		int ticketId = firstTicketId.intValue();
                int maxFetch = AppConfiguration.getInstance().getInt("lucene.index-builder.batch-size.ticket", 25);
		logger.info("Queuing requests for {} ticket's", lastTicketId.intValue() - firstTicketId.intValue());
		
		while (ticketId <= lastTicketId.intValue()) {
			MapMessage message = session.createMapMessage();
			message.setString("type", "ticket");
			message.setInt("range-start", ticketId);
			message.setInt("range-end", ticketId + maxFetch);
			
			producer.send(message);
					
			ticketId += maxFetch;
		}
	}
	
	private void requestChangedTicketIndexing(Session session, MessageProducer producer) throws JMSException {
		
		long lastUpdate = usageAnaysis.getLastIndexUpdate();
		logger.info("Getting changed tickets after: '{}' ...", formatter.print(lastUpdate));
		
		List<Integer> ticketIds = datasource.getChangeTicketsIds(lastUpdate);
		int maxFetch = AppConfiguration.getInstance().getInt("lucene.index-builder.batch-size.ticket", 25);
                
		Iterator<Integer> iterator = ticketIds.iterator();
		while (iterator.hasNext()) {
			MapMessage message = session.createMapMessage();
			message.setString("type", "ticket-upd");
			for (int c = 0; c < maxFetch && iterator.hasNext(); c++) {
				Integer ticketid = iterator.next();
				message.setInt("size", c+1);
				message.setInt("id-" + c, ticketid);
			}
			producer.send(message);
		}
	}
	
	private void requestWikiIndexing(Session session, MessageProducer producer) throws JMSException {
		
		int maxFetch = AppConfiguration.getInstance().getInt("lucene.index-builder.batch-size.wiki", 25);
                    
		logger.info("Getting last wiki's update...");
		List<RecentWiki> recent = datasource.getLastWikiUpdate();
		
		logger.info("Queuing requests for {} wiki's", recent.size());
		Iterator<RecentWiki> iterator = recent.iterator();
		while (iterator.hasNext()) {
			MapMessage message = session.createMapMessage();
			message.setString("type", "wiki");
			for (int c = 0; c < maxFetch && iterator.hasNext(); c++) {
				RecentWiki wiki = iterator.next();
				message.setInt("size", c+1);
				message.setString("name-" + c, wiki.getName());
				message.setInt("version-" + c, wiki.getVersion().intValue());
			}
			producer.send(message);
		}
	}
	
	private void requestAttachmentIndexing(Session session, MessageProducer producer) throws JMSException {

		logger.info("Getting first and last attachment time...");
		
		final Number firstTime = datasource.getFirstAttachTime();
		logger.info("First attachment added at: {}", formatter.print(firstTime.intValue()));
		
		final Number lastTime = datasource.getLastAttachTime();
		logger.info("Last attachment added at: {}", formatter.print(lastTime.intValue()));
		
		long batchsize = 60L * 60L * AppConfiguration.getInstance().getLong("lucene.index-builder.batch-size.attachments", 720);	// 1 Month
		long startTime = firstTime.longValue();
		logger.info("Queuing requests for attachments");
		
		while(startTime <= lastTime.longValue()) {
			
			MapMessage message = session.createMapMessage();
			message.setString("type", "attachment");
			message.setLong("range-start", startTime);
			message.setLong("range-end", startTime + batchsize);
			
			producer.send(message);
			startTime += batchsize;
		}
	}
}

package com.redxiii.tracplus.ejb.search.updater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redxiii.tracplus.ejb.datasources.Datasource;
import com.redxiii.tracplus.ejb.datasources.TicketQueryResult;
import com.redxiii.tracplus.ejb.entity.Attachment;
import com.redxiii.tracplus.ejb.entity.Wiki;
import com.redxiii.tracplus.ejb.search.TracStuff;
import com.redxiii.tracplus.ejb.search.filters.IndexFilter;
import com.redxiii.tracplus.ejb.util.AppConfiguration;
import com.redxiii.tracplus.ejb.util.IndexingStatistics;

/**
 * @author Daniel Filgueiras
 * @since 08/08/2012
 */
@MessageDriven(mappedName = "TracplusQueue", activationConfig = {
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue.com.redxiii.tracplus2"),
		@ActivationConfigProperty(propertyName = "maxSession", propertyValue = "10") })
public class TracIndexerQueue implements MessageListener {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm:ss");
	
	@Inject
	private Datasource datasource;
	
	@Inject
	private LuceneIndexManager luceneIndexManager;
	
	@Inject
	private IndexingStatistics indexingStatistics;
	
	private IndexFilter indexFilter;
	
	public TracIndexerQueue() {
		if (AppConfiguration.getInstance().containsKey("lucene.index-builder.filter")) {
			String filterClass = AppConfiguration.getInstance().getString("lucene.index-builder.filter");
			try {
				@SuppressWarnings("unchecked")
				Class<IndexFilter> clazz = (Class<IndexFilter>) Thread.currentThread().getContextClassLoader().loadClass(filterClass);
				indexFilter = clazz.newInstance();
				logger.debug("Index filter initialized: '{}'", filterClass);
			} catch (Exception e) {
				logger.error("Fail to load index filter '{}'", filterClass, e);
			} 
		}
		
		if (indexFilter == null) {
			indexFilter = new IndexFilter.AcceptAllFilter();
		}
	}

	public void onMessage(Message inMessage) {
		
		try {
			if (inMessage instanceof MapMessage) {
				MapMessage mapMessage = (MapMessage) inMessage;
				
				String type = mapMessage.getString("type");
				logger.info("New message. Type: {}",mapMessage.getString("type"));
				
				if (type.equals("ticket")) {
					handleTicketMessage(mapMessage);
					indexingStatistics.updateIndexedEndTime();
				} else if (type.equals("wiki")) {
					handleWikiMessage(mapMessage);
					indexingStatistics.updateIndexedEndTime();
				} else if (type.equals("ticket-upd")) {
					handleTicketUpdMessage(mapMessage);
				} else if (type.equals("attachment")) {
					handleAttachment(mapMessage);
					indexingStatistics.updateIndexedEndTime();
				}
				logger.info("Message handling done");
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	private void logIndexedWikis(Collection<TracStuff> stuffs) {
		for (TracStuff stuff : stuffs) {
			indexingStatistics.newIndexedDoc(stuff.getContent().length());
		}
	}

	private void handleWikiMessage(MapMessage mapMessage) throws JMSException {
		int size = mapMessage.getInt("size");
		List<TracStuff> stuffs = new ArrayList<TracStuff>();
		
		for (int c = 0; c < size; c++) {
			String name = mapMessage.getString("name-" + c);
			int version = mapMessage.getInt("version-" + c);
			
			Wiki wiki = datasource.getWiki(name, version);
			
			if (indexFilter.isAllowed(wiki)) {
				stuffs.add(new TracStuff(wiki));
			}
		}

		logger.info("Updating index with {} wiki stuffs", stuffs.size());
		luceneIndexManager.updateIndex(stuffs);
		logger.info("Updating index done");
		
		logIndexedWikis(stuffs);
	}
	
	private Collection<TracStuff> getTickets(List<TicketQueryResult> tickets) {
		Map<Integer, TracStuff> stuffs = new HashMap<Integer, TracStuff>();
		
		for (TicketQueryResult result : tickets) {
			
			if (indexFilter.isAllowed(result)) {
				TracStuff stuff = stuffs.get(result.getId());
				if (stuff == null) {
					stuff = new TracStuff(result);
					stuffs.put(result.getId(), stuff);
				} else {
					stuff.addContent(result.getNewvalue(), result.getModified());
				}
			}
		}
		
		return stuffs.values();
	}

	private void handleTicketMessage(MapMessage mapMessage) throws JMSException {
		int rangeStart = mapMessage.getInt("range-start");
		int rangeEnd = mapMessage.getInt("range-end");
		
		logger.info("Loading tickets from '{}' to '{}'", rangeStart, rangeEnd);
		List<TicketQueryResult> results = datasource.getTicketInfo(rangeStart, rangeEnd);
		Collection<TracStuff> stuffs = getTickets(results);
		
		
		logger.info("Updating index with {} ticket stuffs", stuffs.size());
		luceneIndexManager.updateIndex(stuffs);
		logIndexedWikis(stuffs);
	}
	
	private void handleTicketUpdMessage(MapMessage mapMessage) throws JMSException {
		int size = mapMessage.getInt("size");
		
		logger.info("Loading '{}' tickets...", size);
		List<TracStuff> stuffs = new ArrayList<TracStuff>();
		
		for (int c = 0; c < size; c++) {
			int id = mapMessage.getInt("id-" + c);
			
			List<TicketQueryResult> results = datasource.getTicketInfo(id);

			stuffs.addAll(getTickets(results));
		}
		
		logger.info("Updating index with {} ticket stuffs", stuffs.size());
		luceneIndexManager.updateIndex(stuffs);
	}
	
	private void handleAttachment(MapMessage mapMessage) throws JMSException {
		
		long rangeStart = mapMessage.getLong("range-start");
		long rangeEnd = mapMessage.getLong("range-end");
		List<TracStuff> stuffs = new ArrayList<TracStuff>();
//		String path = AppConfiguration.getInstance().getString("trac.home-dir.attachments");
		
		logger.info("Loading attachments details between '{}' and '{}'", formatter.print(rangeStart), formatter.print(rangeEnd));
		List<Attachment> attachments = datasource.getTicketAttachments(rangeStart, rangeEnd);
		
		logger.info("Handling '{}' attachments", attachments.size());
		for (Attachment attachment : attachments) {

			if (!indexFilter.isAllowed(attachment))
				continue;
			
			logger.trace("Reading '{}'", attachment.getFilename());

			String id = attachment.getType() + "/" + attachment.getId() + "/"
					+ attachment.getFilename();

			try {
				if (attachment.getFilename().endsWith(".pdf")) {
					// String attachmentText = PDFExtraction.extract(path + "/"
					// + attachment.getType() + "/" + attachment.getId(),
					// attachment.getFilename());
				}
				String attachmentText = attachment.getFullDescription();

				if (attachmentText != null && attachmentText.length() > 0) {

					stuffs.add(new TracStuff(id, "attachment/" + id, attachment
							.getAuthor(), attachmentText, new Date(attachment
							.getTime() * 1000L), attachment.getTime() * 1000L,
							attachment.getFullDescription(), "attachment"));
				}
			} catch (Exception e) {
				logger.error("Error reading file: '{}'",
						attachment.getFilename(), e);
			}
		}
		
		if (stuffs.size() > 0) {
			logger.info("Updating index with {} attachments stuffs", stuffs.size());
			luceneIndexManager.updateIndex(stuffs);
		}
	}
}


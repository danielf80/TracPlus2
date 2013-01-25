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

/**
 * @author Daniel Filgueiras
 * @since 08/08/2012
 */
@MessageDriven(mappedName = "TracplusQueue", activationConfig = {
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue.com.redxiii.tracplus2"),
		@ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1") })
public class TracIndexerQueue implements MessageListener {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd_HH:mm:ss");
	
	@Inject
	private Datasource datasource;
	
	@Inject
	private LuceneIndexManager luceneIndexManager;

	public void onMessage(Message inMessage) {
		
		try {
			if (inMessage instanceof MapMessage) {
				MapMessage mapMessage = (MapMessage) inMessage;
				
				String type = mapMessage.getString("type");
				logger.info("New message. Type: {}",mapMessage.getString("type"));
				
				if (type.equals("ticket")) {
					handleTicketMessage(mapMessage);
				} else if (type.equals("wiki")) {
					handleWikiMessage(mapMessage);
				} else if (type.equals("ticket-upd")) {
					handleTicketUpdMessage(mapMessage);
				} else if (type.equals("attachment")) {
					handleAttachment(mapMessage);
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private void handleWikiMessage(MapMessage mapMessage) throws JMSException {
		int size = mapMessage.getInt("size");
		List<TracStuff> stuffs = new ArrayList<TracStuff>();
		
		for (int c = 0; c < size; c++) {
			String name = mapMessage.getString("name-" + c);
			int version = mapMessage.getInt("version-" + c);
			
			logger.info("Loading wiki page: '{}' version '{}'", name, version);
			Wiki wiki = datasource.getWiki(name, version);
			stuffs.add(new TracStuff(wiki));
		}

		logger.info("Updating index with {} wiki stuffs", stuffs.size());
		luceneIndexManager.updateIndex(stuffs);		
	}
	
	private Collection<TracStuff> getWikis(List<TicketQueryResult> tickets) {
		Map<Integer, TracStuff> stuffs = new HashMap<Integer, TracStuff>();
		
		for (TicketQueryResult result : tickets) {
			TracStuff stuff = stuffs.get(result.getId());
			if (stuff == null) {
				stuff = new TracStuff(result);
				stuffs.put(result.getId(), stuff);
			} else {
				stuff.addContent(result.getNewvalue(), result.getModified());
			}
		}
		
		return stuffs.values();
	}

	private void handleTicketMessage(MapMessage mapMessage) throws JMSException {
		int rangeStart = mapMessage.getInt("range-start");
		int rangeEnd = mapMessage.getInt("range-end");
		
		logger.info("Loading tickets from '{}' to '{}'", rangeStart, rangeEnd);
		List<TicketQueryResult> results = datasource.getTicketInfo(rangeStart, rangeEnd);
		Collection<TracStuff> stuffs = getWikis(results);
		
		
		logger.info("Updating index with {} ticket stuffs", stuffs.size());
		luceneIndexManager.updateIndex(stuffs);
	}
	
	private void handleTicketUpdMessage(MapMessage mapMessage) throws JMSException {
		int size = mapMessage.getInt("size");
		
		logger.info("Loading '{}' tickets...", size);
		List<TracStuff> stuffs = new ArrayList<TracStuff>();
		
		for (int c = 0; c < size; c++) {
			int id = mapMessage.getInt("id-" + c);
			
			List<TicketQueryResult> results = datasource.getTicketInfo(id);

			stuffs.addAll(getWikis(results));
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


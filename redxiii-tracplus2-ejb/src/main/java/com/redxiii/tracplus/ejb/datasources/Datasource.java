package com.redxiii.tracplus.ejb.datasources;

import java.util.List;

import com.redxiii.tracplus.ejb.entity.Attachment;
import com.redxiii.tracplus.ejb.entity.Wiki;

public interface Datasource {

	Number getLastTicketId();

	Number getFirstTicketId();

	Number getFirstAttachTime();

	Number getLastAttachTime();

	List<TicketQueryResult> getTicketInfo(int min, int max);

	List<TicketQueryResult> getTicketInfo(Integer id);

	List<Integer> getChangeTicketsIds(long changetime);

	List<Integer> getChangeTicketsIds(String user, long changetime);
	
	List<RecentWiki> getLastWikiUpdate();

	Wiki getWiki(String name, Number version);

	List<Attachment> getTicketAttachments(long start, long end);

	List<Attachment> getWikiAttachments(long start, long end);

}
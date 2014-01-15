package com.redxiii.tracplus.ejb.datasources;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import com.redxiii.tracplus.ejb.entity.Attachment;
import com.redxiii.tracplus.ejb.entity.Wiki;

@Mock
@Named
public class TracDSMock implements Datasource {

	private SortedMap<Integer,TicketQueryResult> results;
	private List<RecentWiki> wikis;
	
	@PostConstruct
	public void init() {
		results = new TreeMap<Integer, TicketQueryResult>();
		{
			TicketQueryResult result = new TicketQueryResult();
			result.setId(1);
			result.setChangetime(1346036400);
			result.setDescription("Homer Simpsons");
			result.setModified(1346036400);
			result.setOwner("dfilgueiras");
			result.setReporter("dfilgueiras");
			result.setSummary("Homer Simpsons");
			result.setTime(1346036400);
			result.setType("ticket");
			results.put(1,result);
		}
		{
			TicketQueryResult result = new TicketQueryResult();
			result.setId(2);
			result.setChangetime(1346036400);
			result.setDescription("Bart Simpsons");
			result.setModified(1346036400);
			result.setOwner("dfilgueiras");
			result.setReporter("dfilgueiras");
			result.setSummary("Bart Simpsons");
			result.setTime(1346036400);
			result.setType("ticket");
			results.put(2,result);
		}
		{
			TicketQueryResult result = new TicketQueryResult();
			result.setId(3);
			result.setChangetime(1346036400);
			result.setDescription("Maggie Simpsons");
			result.setModified(1346036400);
			result.setOwner("dfilgueiras");
			result.setReporter("dfilgueiras");
			result.setSummary("Maggie Simpsons");
			result.setTime(1346036400);
			result.setType("ticket");
			results.put(3,result);
		}
		wikis = new ArrayList<RecentWiki>();
		{
			wikis.add(new RecentWiki("Lisa/Simpson", 1));
			wikis.add(new RecentWiki("Marge/Simpson", 2));
			wikis.add(new RecentWiki("Vovo/Simpson", 1));
		}
	}
	
	@Override
	public Number getLastTicketId() {
		return results.lastKey();
	}

	@Override
	public Number getFirstTicketId() {
		return results.firstKey();
	}

	@Override
	public Number getFirstAttachTime() {
		return 1346036400;
	}

	@Override
	public Number getLastAttachTime() {
		return 1346036400;
	}

	@Override
	public List<TicketQueryResult> getTicketInfo(int min, int max) {
		return new ArrayList<TicketQueryResult>( results.values() );
	}

	@Override
	public List<TicketQueryResult> getTicketInfo(Integer id) {
		List<TicketQueryResult> list = new ArrayList<TicketQueryResult>();
		list.add(results.get(id));
		return list;
	}

	@Override
	public List<Integer> getChangeTicketsIds(long changetime) {
		return new ArrayList<Integer>();
	}
	
	@Override
	public List<Integer> getChangeTicketsIds(String user, long changetime) {
		return new ArrayList<Integer>();
	}

	@Override
	public List<RecentWiki> getLastWikiUpdate() {
		return wikis;
	}

	@Override
	public Wiki getWiki(String name, Number version) {
		Wiki wiki = new Wiki();
		wiki.setAuthor("dfilgueiras");
		wiki.setName(name);
		wiki.setText(name);
		wiki.setTime(1346036400);
		wiki.setVersion(version.intValue());
		return wiki;
	}

	@Override
	public List<Attachment> getTicketAttachments(long start, long end) {
		List<Attachment> attachments = new ArrayList<Attachment>();
		{
			Attachment attachment = new Attachment();
			attachment.setAuthor("dfilgueiras");
			attachment.setDescription("Abbie Simpson");
			attachment.setFilename("Oracle-JavaEE6-Tutorial-July-2012.pdf");
			attachment.setSize(1000);
			attachment.setTime(1346036400);
			attachment.setId("1");
			attachment.setType("ticket");
			
			attachments.add(attachment);
		}
		return attachments;
	}
	
	@Override
	public List<Attachment> getWikiAttachments(long start, long end) {
		List<Attachment> attachments = new ArrayList<Attachment>();
		{
			Attachment attachment = new Attachment();
			attachment.setAuthor("dfilgueiras");
			attachment.setDescription("Abbie Simpson");
			attachment.setFilename("Oracle-JavaEE6-Tutorial-July-2012.pdf");
			attachment.setSize(1000);
			attachment.setTime(1346036400);
			attachment.setId("1");
			attachment.setType("ticket");
			
			attachments.add(attachment);
		}
		return attachments;
	}

}

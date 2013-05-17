package com.redxiii.tracplus.ejb.search.filters;

import java.util.HashSet;
import java.util.Set;

import com.redxiii.tracplus.ejb.entity.Attachment;
import com.redxiii.tracplus.ejb.entity.Ticket;

public class TicketAttachmentIndexFilter extends TicketIndexFilter {

	private final Set<String> notAllowedTicketIds = new HashSet<String>();
	
	@Override
	public boolean isAllowed(Attachment attachment) {
		if (notAllowedTicketIds.contains(attachment.getId()))
			return false;
		return true;
	}
	
	@Override
	public boolean isAllowed(Ticket ticket) {
		if (super.isAllowed(ticket)) {
			return true;
		}
		
		notAllowedTicketIds.add(ticket.getId().toString());
		return true;
	}
	
	
}

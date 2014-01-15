package com.redxiii.tracplus.ejb.search.filters;

import com.redxiii.tracplus.ejb.entity.Attachment;
import com.redxiii.tracplus.ejb.entity.Ticket;
import com.redxiii.tracplus.ejb.entity.Wiki;

/**
 * @author dfilgueiras
 *
 */
public class TicketIndexFilter extends AbstractFieldIndexFilter {

	public TicketIndexFilter() {
		super(Ticket.class);
	}
	
	@Override
	public boolean isAllowed(Attachment attachment) {
		return true;
	}

	@Override
	public boolean isAllowed(Ticket ticket) {
		return super.isObjAllowed(ticket);
	}

	@Override
	public boolean isAllowed(Wiki wiki) {
		return true;
	}

}
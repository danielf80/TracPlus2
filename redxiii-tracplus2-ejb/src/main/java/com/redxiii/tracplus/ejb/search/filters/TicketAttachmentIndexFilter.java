package com.redxiii.tracplus.ejb.search.filters;

import com.redxiii.tracplus.ejb.entity.Attachment;
import com.redxiii.tracplus.ejb.entity.Ticket;
import com.redxiii.tracplus.ejb.entity.TicketAttachment;
import com.redxiii.tracplus.ejb.entity.Wiki;

public class TicketAttachmentIndexFilter extends AbstractFieldIndexFilter {

	public TicketAttachmentIndexFilter() {
		super(TicketAttachment.class);
	}

	@Override
	public boolean isAllowed(Attachment attachment) {
		if (attachment instanceof TicketAttachment)
			return super.isObjAllowed(attachment);
		return true;
	}

	@Override
	public boolean isAllowed(Ticket ticket) {
		return true;
	}

	@Override
	public boolean isAllowed(Wiki wiki) {
		return true;
	}
}

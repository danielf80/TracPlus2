/**
 * 
 */
package com.redxiii.tracplus.ejb.search.filters;

import com.redxiii.tracplus.ejb.entity.Attachment;
import com.redxiii.tracplus.ejb.entity.Ticket;
import com.redxiii.tracplus.ejb.entity.Wiki;

/**
 * @author Daniel
 *
 */
public interface IndexFilter {

	boolean isAllowed(Attachment attachment);
	
	boolean isAllowed(Ticket ticket);
	
	boolean isAllowed(Wiki wiki);
	
	public static class AcceptAllFilter implements IndexFilter {

		@Override
		public boolean isAllowed(Attachment attachment) {
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
}

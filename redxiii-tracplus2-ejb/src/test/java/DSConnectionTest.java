import java.util.List;

import com.redxiii.tracplus.ejb.datasources.TicketQueryResult;
import com.redxiii.tracplus.ejb.datasources.TracDS;



public class DSConnectionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TracDS ds = new TracDS();
		ds.init();
		
		List<Integer> ticketIds = ds.getChangeTicketsIds(1345237472000L);
		
		for (Integer id : ticketIds) {
			List<TicketQueryResult> tickets = ds.getTicketInfo(id);
			
			for (TicketQueryResult result : tickets) {
				System.out.println(result.getId());
			}
		}
	}

}

package blake.bot.eager;

import ddejonge.bandana.negoProtocol.DMZ;
import ddejonge.bandana.negoProtocol.OrderCommitment;
import es.csic.iiia.fabregues.dip.board.Phase;

public class DatedObject {

	private final Phase phase;
	private final int year;

	public DatedObject(OrderCommitment order) {
		this.phase = order.getPhase();
		this.year = order.getYear();
	}

	public DatedObject(DMZ dmz) {
		this.phase = dmz.getPhase();
		this.year = dmz.getYear();
	}

	public Phase getPhase() {
		return this.phase;
	}

	public int getYear() {
		return this.year;
	}

}

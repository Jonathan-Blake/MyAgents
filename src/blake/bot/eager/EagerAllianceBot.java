//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blake.bot.eager;

import ddejonge.bandana.anac.ANACNegotiator;
import ddejonge.bandana.negoProtocol.BasicDeal;
import ddejonge.bandana.negoProtocol.DMZ;
import ddejonge.bandana.negoProtocol.DiplomacyProposal;
import ddejonge.bandana.tools.Utilities;
import ddejonge.negoServer.Message;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.orders.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class EagerAllianceBot extends ANACNegotiator {
	private final Vector<Power> mCoalition = new Vector<>();
	boolean mIsFirstTurn = true;

	public EagerAllianceBot(String[] args) {
		super(args);
	}

	public static void main(String[] args) {
		EagerAllianceBot myPlayer = new EagerAllianceBot(args);
		myPlayer.run();
	}

	public void addToCoallition(Power power) {
		boolean isAllyFounded = false;

		for (Power ally : this.mCoalition) {
			if (ally == power) {
				isAllyFounded = true;
				break;
			}
		}

		if (!isAllyFounded) {
			this.mCoalition.add(power);
		}

	}

	public void start() {
		boolean printToConsole = true;
	}

	public void negotiate(long negotiationDeadline) {
		boolean startOfThisNegotiation = true;

		while (System.currentTimeMillis() < negotiationDeadline) {
			if (!this.hasMessage()) {
				if (startOfThisNegotiation) {
					List<BasicDeal> dealsToOffer = this.getDealsToOffer();
					for (BasicDeal deal : dealsToOffer) {
						this.proposeDeal(deal);
					}
				}

				startOfThisNegotiation = false;

				try {
					Thread.sleep(250L);
				} catch (InterruptedException ignored) {
					Thread.currentThread().interrupt();
				}
			} else {
				Message receivedMessage = this.removeMessageFromQueue();
				this.getLogger().logln("got message " + receivedMessage.getContent(), true);
				switch (receivedMessage.getPerformative()) {
					case "CONFIRM":
						handleConfirmationMessage(receivedMessage);
						break;
					case "REJECT":
						handleRejectedMessage(receivedMessage);
						break;
					case "PROPOSE":
						handleProposedMessage(receivedMessage);
						break;
					case "ACCEPT":
						handleAcceptedMessage(receivedMessage);
						break;
					default:
						this.getLogger().logln("CoalitionBot.negotiate() could not recognise Performative of: " + receivedMessage.getPerformative());
				}
			}
		}

		this.mIsFirstTurn = false;
	}

	private void handleProposedMessage(Message receivedMessage) {
		DiplomacyProposal receivedProposal;
		receivedProposal = (DiplomacyProposal) receivedMessage.getContent();
		BasicDeal deal = (BasicDeal) receivedProposal.getProposedDeal();
		boolean outDated = false;

		for (DMZ dmz : deal.getDemilitarizedZones()) {
			if (this.isHistory(dmz.getPhase(), dmz.getYear())) {
				outDated = true;
				break;
			}
		}

		String consistencyReport = null;
		if (!outDated) {
			List<BasicDeal> commitments = new ArrayList<>(this.getConfirmedDeals());
			commitments.add(deal);
			consistencyReport = Utilities.testConsistency(this.game, commitments);
		}

		if (!outDated && consistencyReport == null) {
			this.acceptProposal(receivedProposal.getId());
		}
	}

	private void handleRejectedMessage(Message receivedMessage) {
		DiplomacyProposal receivedProposal;
		receivedProposal = (DiplomacyProposal) receivedMessage.getContent();
		this.getLogger().logln("CoalitionBot.negotiate() Received rejection from " + receivedMessage.getSender() + ": " + receivedProposal, true);
	}

	private void handleConfirmationMessage(Message receivedMessage) {
		DiplomacyProposal receivedProposal;
		receivedProposal = (DiplomacyProposal) receivedMessage.getContent();
		this.getLogger().logln("CoalitionBot.negotiate() Received confirmed from " + receivedMessage.getSender() + ": " + receivedProposal, true);
		if (this.mIsFirstTurn) {
			List<String> participants = receivedProposal.getParticipants();

			for (String powerName : participants) {
				this.addToCoallition(this.game.getPower(powerName));
			}
		}
	}

	private void handleAcceptedMessage(Message receivedMessage) {
		DiplomacyProposal receivedProposal;
		receivedProposal = (DiplomacyProposal) receivedMessage.getContent();
		this.getLogger().logln("CoallitionBot.negotiate() Received acceptance from " + receivedMessage.getSender() + ": " + receivedProposal, true);
	}

	private ArrayList<Power> getAliveAllies() {
		ArrayList<Power> aliveAllies = new ArrayList<>();

		for (Power ally : this.mCoalition) {
			if (this.getNegotiatingPowers().contains(ally) && !ally.equals(this.me)) {
				aliveAllies.add(ally);
			}
		}

		return aliveAllies;
	}

	private List<BasicDeal> getDealsToOffer() {
		List<BasicDeal> dealsToOffer;
		if (this.mIsFirstTurn) {
			dealsToOffer = getFirstTurnOffers();
		} else {
			dealsToOffer = getCoalitionPeaceOffers();
		}
		return dealsToOffer;
	}

	private List<BasicDeal> getCoalitionPeaceOffers() {
		BasicDeal deal;
		ArrayList<Power> relevantPowers;
		ArrayList<DMZ> demilitarizedZones;
		ArrayList<Power> aliveAllies = this.getAliveAllies();

		List<BasicDeal> dealsToOffer = new ArrayList<>();
		for (int aliveAllyIndex = 0; aliveAllyIndex < aliveAllies.size(); ++aliveAllyIndex) {
			relevantPowers = new ArrayList<>();
			relevantPowers.add(this.me);

			for (int i = 0; i < aliveAllies.size(); ++i) {
				if (i != aliveAllyIndex) {
					relevantPowers.add(aliveAllies.get(i));
				}
			}

			demilitarizedZones = new ArrayList<>();
			demilitarizedZones.add(new DMZ(this.game.getYear(), this.game.getPhase(), relevantPowers, (aliveAllies.get(aliveAllyIndex)).getOwnedSCs()));
			deal = new BasicDeal(Collections.emptyList(), demilitarizedZones);
			dealsToOffer.add(deal);
		}
		return dealsToOffer;
	}

	private List<BasicDeal> getFirstTurnOffers() {
		BasicDeal deal;
		List<Power> relevantPowers;
		List<DMZ> demilitarizedZones;
		List<BasicDeal> dealsToOffer = new ArrayList<>();
		for (Power power : this.game.getPowers()) {
			if (power != this.me) {
				relevantPowers = Collections.singletonList(this.me);
				List<Power> oneAllyVector = Collections.singletonList(power);
				demilitarizedZones = new ArrayList<>();
				demilitarizedZones.add(new DMZ(this.game.getYear(), this.game.getPhase(), oneAllyVector, this.me.getOwnedSCs()));

				demilitarizedZones.add(new DMZ(this.game.getYear(), this.game.getPhase(), relevantPowers, power.getOwnedSCs()));
				deal = new BasicDeal(Collections.emptyList(), demilitarizedZones);
				dealsToOffer.add(deal);
			}
		}
		return dealsToOffer;
	}

	public void receivedOrder(Order arg0) {
		/*
			Required by inheritance.
		 */
	}
}

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blake.bot.eager;

import ddejonge.bandana.anac.ANACNegotiator;
import ddejonge.bandana.dbraneTactics.DBraneTactics;
import ddejonge.bandana.dbraneTactics.Plan;
import ddejonge.bandana.negoProtocol.BasicDeal;
import ddejonge.bandana.negoProtocol.DMZ;
import ddejonge.bandana.negoProtocol.DiplomacyProposal;
import ddejonge.bandana.negoProtocol.OrderCommitment;
import ddejonge.bandana.tools.Utilities;
import ddejonge.negoServer.Message;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.orders.HLDOrder;
import es.csic.iiia.fabregues.dip.orders.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EagerAllianceBot extends ANACNegotiator {
	private final DBraneTactics dBraneTactics = this.getTacticalModule();
	private final List<Power> mCoalition = new ArrayList<>();
	private boolean mIsFirstTurn = true;
	private Plan myPlan;
	private List<OrderCommitment> preferredOrders;

	public static void main(String[] args) {
		EagerAllianceBot myPlayer = new EagerAllianceBot(args);
		myPlayer.run();
	}

	public EagerAllianceBot(String[] args) {
		super(args);
	}

	public void start() {
		boolean printToConsole = true;
	}

	public void negotiate(long negotiationDeadline) {
		boolean startOfThisNegotiation = true;
		int loopsSinceMessage = 0;
		this.evaluatePlan();

		while (System.currentTimeMillis() < negotiationDeadline) {
			if (this.hasMessage()) {
				loopsSinceMessage = 0;
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
			} else {
				if (startOfThisNegotiation) {
					this.getDealsToOffer().forEach(this::proposeDeal);
					startOfThisNegotiation = false;
				}
				if (10 < loopsSinceMessage++) {
					break;
				} else {
					sleep();
				}
			}
		}

		this.mIsFirstTurn = false;
	}

	private void sleep() {
		try {
			Thread.sleep(100L);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}

	public void receivedOrder(Order arg0) {
		/*
			Required by inheritance.
		 */
	}

	private void evaluatePlan() {
		this.myPlan = dBraneTactics.determineBestPlan(this.game, this.me, this.getConfirmedDeals(), this.mCoalition);
		this.preferredOrders = null;
	}

	private List<OrderCommitment> getPreferredOrders() {
		if (this.preferredOrders == null) {
			if (this.myPlan == null) {
				this.preferredOrders = Collections.emptyList();
			} else {
				this.preferredOrders = this.myPlan
						.getMyOrders().stream().filter(order -> !(order instanceof HLDOrder))
						.map(order -> new OrderCommitment(this.game.getYear(), this.game.getPhase(), order))
						.collect(Collectors.toList());
			}
		}
		return this.preferredOrders;
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
				this.addToCoalition(this.game.getPower(powerName));
			}
		}
	}

	private void handleAcceptedMessage(Message receivedMessage) {
		DiplomacyProposal receivedProposal;
		receivedProposal = (DiplomacyProposal) receivedMessage.getContent();
		this.getLogger().logln("CoallitionBot.negotiate() Received acceptance from " + receivedMessage.getSender() + ": " + receivedProposal, true);
	}

	private List<Power> getAliveAllies() {
		this.mCoalition.retainAll(game.getNonDeadPowers());
		this.mCoalition.remove(this.me);

		return this.mCoalition;
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

	private List<BasicDeal> getFirstTurnOffers() {
		List<BasicDeal> dealsToOffer = new ArrayList<>();
		for (Power power : this.game.getPowers()) {
			if (power != this.me) {
				dealsToOffer.add(generatePeaceDeal(power));
			}
		}
		return dealsToOffer;
	}

	private List<BasicDeal> getCoalitionPeaceOffers() {
		List<DMZ> demilitarizedZones;
		List<Power> aliveAllies = this.getAliveAllies();

		List<BasicDeal> dealsToOffer = new ArrayList<>();
		for (Power ally : aliveAllies) {
			demilitarizedZones = new ArrayList<>();
			demilitarizedZones.add(new DMZ(
					this.game.getYear(),
					this.game.getPhase(),
					Utility.Lists.append(
							Utility.Lists.createFilteredList(aliveAllies, ally),
							this.me
					),
					ally.getOwnedSCs()));
			dealsToOffer.add(new BasicDeal(getPreferredOrders(), demilitarizedZones));
		}
		return dealsToOffer;
	}

	private BasicDeal generatePeaceDeal(Power power) {
		List<DMZ> demilitarizedZones = new ArrayList<>();
		demilitarizedZones.add(new DMZ(this.game.getYear(), this.game.getPhase(), Collections.singletonList(power), this.me.getOwnedSCs()));

		demilitarizedZones.add(new DMZ(this.game.getYear(), this.game.getPhase(), Collections.singletonList(this.me), power.getOwnedSCs()));
		return new BasicDeal(getPreferredOrders(), demilitarizedZones);
	}

	public void addToCoalition(Power power) {
		if (!this.mCoalition.contains(power)) {
			this.mCoalition.add(power);
		}
	}
}

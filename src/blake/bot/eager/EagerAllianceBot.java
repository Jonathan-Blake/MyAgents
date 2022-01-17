package blake.bot.eager;

import ddejonge.bandana.anac.ANACNegotiator;
import ddejonge.bandana.dbraneTactics.DBraneTactics;
import ddejonge.bandana.negoProtocol.BasicDeal;
import ddejonge.bandana.negoProtocol.DMZ;
import ddejonge.bandana.negoProtocol.DiplomacyProposal;
import ddejonge.bandana.negoProtocol.OrderCommitment;
import ddejonge.bandana.tools.Utilities;
import ddejonge.negoServer.Message;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.orders.Order;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EagerAllianceBot extends ANACNegotiator {

	boolean m_isFirstTurn = true;
	DBraneTactics dBraneTactics = this.getTacticalModule();
	private List<Power> m_coallition = new LinkedList<>();

	public static void main(String[] args) {
		EagerAllianceBot myPlayer = new EagerAllianceBot(args);
		myPlayer.run();
	}

	public void addToCoalition(Power power) {
		boolean isAllyFounded = false;

		for (Power ally : this.m_coallition) {
			if (ally == power) {
				isAllyFounded = true;
				break;
			}
		}
		if (!isAllyFounded) {
			this.m_coallition.add(power);
		}

	}

	public EagerAllianceBot(String[] args) {
		super(args);
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
				} catch (InterruptedException var11) {
				}
			} else {
				Message receivedMessage = this.removeMessageFromQueue();
				this.getLogger().logln("got meesage " + receivedMessage.getContent(), true);
				DiplomacyProposal receivedProposal;
				if (receivedMessage.getPerformative().equals("ACCEPT")) {
					receivedProposal = (DiplomacyProposal) receivedMessage.getContent();
					this.getLogger().logln("CoallitionBot.negotiate() Received acceptance from " + receivedMessage.getSender() + ": " + receivedProposal, true);
				} else if (!receivedMessage.getPerformative().equals("PROPOSE")) {
					if (receivedMessage.getPerformative().equals("CONFIRM")) {
						receivedProposal = (DiplomacyProposal) receivedMessage.getContent();
						this.getLogger().logln("CoallitionBot.negotiate() Received confirmed from " + receivedMessage.getSender() + ": " + receivedProposal, true);
						if (this.m_isFirstTurn) {
							List<String> participitants = receivedProposal.getParticipants();

							for (String powerName : participitants) {
								this.addToCoalition(this.game.getPower(powerName));
							}
						}
					} else if (receivedMessage.getPerformative().equals("REJECT")) {
					}
				} else {
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
						List<BasicDeal> commitments = new ArrayList(this.getConfirmedDeals());
						commitments.add(deal);
						consistencyReport = Utilities.testConsistency(this.game, commitments);
					}

					if (!outDated && consistencyReport == null) {
						this.acceptProposal(receivedProposal.getId());
					}
				}
			}
		}

		this.m_isFirstTurn = false;
	}

	private ArrayList<Power> getAliveAllies() {
		ArrayList<Power> aliveAllies = new ArrayList<>();

		for (Power ally : this.m_coallition) {
			if (this.getNegotiatingPowers().contains(ally) && !ally.equals(this.me)) {
				aliveAllies.add(ally);
			}
		}

		return aliveAllies;
	}

	private ArrayList<BasicDeal> getDealsToOffer() {
		ArrayList<BasicDeal> dealsToOffer = new ArrayList<>();
		ArrayList<Power> relevant_powers;
		ArrayList<DMZ> demilitarizedZones = new ArrayList<>();
		ArrayList<OrderCommitment> randomOrderCommitments;
		BasicDeal deal;
		if (this.m_isFirstTurn) {

			for (Power power : this.game.getPowers()) {
				if (power != this.me) {
					ArrayList<Power> oneAllyVector = new ArrayList<>();
					oneAllyVector.add(power);
					demilitarizedZones.add(new DMZ(this.game.getYear(), this.game.getPhase(), oneAllyVector, this.me.getOwnedSCs()));
					relevant_powers = new ArrayList<>();
					relevant_powers.add(this.me);
					demilitarizedZones.add(new DMZ(this.game.getYear(), this.game.getPhase(), relevant_powers, power.getOwnedSCs()));
					randomOrderCommitments = new ArrayList<>();
					deal = new BasicDeal(randomOrderCommitments, demilitarizedZones);
					dealsToOffer.add(deal);
				}
			}
		} else {
			ArrayList<Power> aliveAllies = this.getAliveAllies();

			for (int aliveAllyIndex = 0; aliveAllyIndex < aliveAllies.size(); ++aliveAllyIndex) {
				relevant_powers = new ArrayList<>();
				relevant_powers.add(this.me);


				for (int i = 0; i < aliveAllies.size(); ++i) {
					if (i != aliveAllyIndex) {
						relevant_powers.add(aliveAllies.get(i));
					}
				}

				demilitarizedZones = new ArrayList<>();
				demilitarizedZones.add(new DMZ(this.game.getYear(), this.game.getPhase(), relevant_powers, (aliveAllies.get(aliveAllyIndex)).getOwnedSCs()));
				randomOrderCommitments = new ArrayList<>();
				deal = new BasicDeal(randomOrderCommitments, demilitarizedZones);
				dealsToOffer.add(deal);
			}
		}

		return dealsToOffer;
	}

	public void receivedOrder(Order arg0) {
	}
}

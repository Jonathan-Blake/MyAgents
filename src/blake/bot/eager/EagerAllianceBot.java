//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package blake.bot.eager;

import ddejonge.bandana.anac.ANACNegotiator;
import ddejonge.bandana.dbraneTactics.DBraneTactics;
import ddejonge.bandana.negoProtocol.BasicDeal;
import ddejonge.bandana.negoProtocol.DMZ;
import ddejonge.bandana.negoProtocol.DiplomacyProposal;
import ddejonge.bandana.tools.Utilities;
import ddejonge.negoServer.Message;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.orders.Order;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class EagerAllianceBot extends ANACNegotiator {
	private Vector<Power> m_coallition = new Vector();
	boolean m_isFirstTurn = true;
	DBraneTactics dBraneTactics = this.getTacticalModule();

	public static void main(String[] args) throws IOException {
		EagerAllianceBot myPlayer = new EagerAllianceBot(args);
		myPlayer.run();
	}

	public EagerAllianceBot(String[] args) throws IOException {
		super(args);
	}

	public void addToCoallition(Power power) {
		boolean isAllyFounded = false;
		Iterator var4 = this.m_coallition.iterator();

		while (var4.hasNext()) {
			Power ally = (Power) var4.next();
			if (ally == power) {
				isAllyFounded = true;
			}
		}

		if (!isAllyFounded) {
			this.m_coallition.add(power);
		}

	}

	public void start() {
		boolean printToConsole = true;
	}

	public void negotiate(long negotiationDeadline) {
		boolean startOfThisNegotiation = true;
		ArrayList var4 = this.getAlliveAllies();

		while (System.currentTimeMillis() < negotiationDeadline) {
			if (!this.hasMessage()) {
				if (startOfThisNegotiation) {
					List<BasicDeal> dealsToOffer = this.getDealsToOffer();
					Iterator var15 = dealsToOffer.iterator();

					while (var15.hasNext()) {
						BasicDeal deal = (BasicDeal) var15.next();
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
							Iterator var18 = participitants.iterator();

							while (var18.hasNext()) {
								String powerName = (String) var18.next();
								this.addToCoallition(this.game.getPower(powerName));
							}
						}
					} else if (receivedMessage.getPerformative().equals("REJECT")) {
						receivedProposal = (DiplomacyProposal) receivedMessage.getContent();
					}
				} else {
					receivedProposal = (DiplomacyProposal) receivedMessage.getContent();
					BasicDeal deal = (BasicDeal) receivedProposal.getProposedDeal();
					boolean outDated = false;
					Iterator var10 = deal.getDemilitarizedZones().iterator();

					while (var10.hasNext()) {
						DMZ dmz = (DMZ) var10.next();
						if (this.isHistory(dmz.getPhase(), dmz.getYear())) {
							outDated = true;
							break;
						}
					}

					String consistencyReport = null;
					if (!outDated) {
						List<BasicDeal> commitments = new ArrayList();
						commitments.addAll(this.getConfirmedDeals());
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

	private ArrayList<Power> getAlliveAllies() {
		ArrayList<Power> alliveAllies = new ArrayList();
		Iterator var3 = this.m_coallition.iterator();

		while (var3.hasNext()) {
			Power ally = (Power) var3.next();
			if (this.getNegotiatingPowers().contains(ally) && !ally.equals(this.me)) {
				alliveAllies.add(ally);
			}
		}

		return alliveAllies;
	}

	private ArrayList<BasicDeal> getDealsToOffer() {
		ArrayList<BasicDeal> dealsToOffer = new ArrayList();
		ArrayList relevant_powers;
		ArrayList demilitarizedZones;
		ArrayList randomOrderCommitments;
		BasicDeal deal;
		if (this.m_isFirstTurn) {
			Iterator var3 = this.game.getPowers().iterator();

			while (var3.hasNext()) {
				Power power = (Power) var3.next();
				if (power != this.me) {
					relevant_powers = new ArrayList();
					ArrayList<Power> oneAllyVector = new ArrayList();
					oneAllyVector.add(power);
					relevant_powers.add(new DMZ(this.game.getYear(), this.game.getPhase(), oneAllyVector, this.me.getOwnedSCs()));
					demilitarizedZones = new ArrayList();
					demilitarizedZones.add(this.me);
					relevant_powers.add(new DMZ(this.game.getYear(), this.game.getPhase(), demilitarizedZones, power.getOwnedSCs()));
					randomOrderCommitments = new ArrayList();
					deal = new BasicDeal(randomOrderCommitments, relevant_powers);
					dealsToOffer.add(deal);
				}
			}
		} else {
			ArrayList<Power> alliveAllies = this.getAlliveAllies();

			for (int alliveAllyIndex = 0; alliveAllyIndex < alliveAllies.size(); ++alliveAllyIndex) {
				relevant_powers = new ArrayList();
				relevant_powers.add(this.me);
				Vector<Province> allProvinces = this.game.getProvinces();

				for (int i = 0; i < alliveAllies.size(); ++i) {
					if (i != alliveAllyIndex) {
						relevant_powers.add((Power) alliveAllies.get(i));
					}
				}

				demilitarizedZones = new ArrayList();
				demilitarizedZones.add(new DMZ(this.game.getYear(), this.game.getPhase(), relevant_powers, ((Power) alliveAllies.get(alliveAllyIndex)).getOwnedSCs()));
				randomOrderCommitments = new ArrayList();
				deal = new BasicDeal(randomOrderCommitments, demilitarizedZones);
				dealsToOffer.add(deal);
			}
		}

		return dealsToOffer;
	}

	public void receivedOrder(Order arg0) {
	}
}

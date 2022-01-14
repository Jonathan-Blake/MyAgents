package blake.bot.eager;

import ddejonge.bandana.anac.ANACNegotiator;
import ddejonge.bandana.dbraneTactics.Plan;
import ddejonge.bandana.negoProtocol.BasicDeal;
import ddejonge.bandana.negoProtocol.DMZ;
import ddejonge.bandana.negoProtocol.DiplomacyProposal;
import ddejonge.bandana.negoProtocol.OrderCommitment;
import ddejonge.bandana.tools.Utilities;
import ddejonge.negoServer.Message;
import es.csic.iiia.fabregues.dip.board.Phase;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.board.Region;
import es.csic.iiia.fabregues.dip.orders.HLDOrder;
import es.csic.iiia.fabregues.dip.orders.MTOOrder;
import es.csic.iiia.fabregues.dip.orders.Order;
import es.csic.iiia.fabregues.dip.orders.SUPMTOOrder;

import java.util.*;
import java.util.stream.Collectors;

public class EagerAllianceBot extends ANACNegotiator {

	private BasicDeal nonAggressionProposal;
	private Set<Power> alliance = new HashSet<>();
	private Plan bestPlan;
	private BasicDeal myPlan;

	public EagerAllianceBot(String[] args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Main method to start the agent.
	 * <p>
	 * This player can be started with the following arguments:
	 * -name  	[the name of your agent]
	 * -log		[the path to the folder where you want the log files to be stored]
	 * -fy 		[the year after which your agent will propose a draw]
	 * -gamePort  [the port of the game server]
	 * -negoPort  [the port of the negotiation server]
	 * <p>
	 * e.g. java -jar ANACExampleNegotiator.jar -name alice -log C:\\documents\log -fy 1920 -gamePort 16713 -negoPort 16714
	 * <p>
	 * All of these arguments are optional.
	 * <p>
	 * Note however that during the competition the values of these arguments will be chosen by the organizers
	 * of the competition, so you can only control them during the development of your negotiator.
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		EagerAllianceBot myPlayer = new EagerAllianceBot(args);
		myPlayer.run();

	}

	@Override
	public void negotiate(long negotiationDeadline) {
		bestPlan = this.getTacticalModule().determineBestPlan(this.game, this.me, this.getConfirmedDeals(), new LinkedList<>(this.alliance));

		this.proposeDraw();
		if (this.alliance.size() > 1) {
			initPlan();
			this.proposeDeal(myPlan);

			this.getLogger().logln("Current Alliance " + Arrays.toString(this.alliance.toArray()));
		}

		while (System.currentTimeMillis() < negotiationDeadline) {
			if (this.hasMessage()) {
//            	this.getLogger().log("Handling messages");
				handleMessage();
			} else {
//            	this.getLogger().log("Attempting to find new proposals");
//            	considerProposals();
			}
		}
	}

	private void initPlan() {

		List<OrderCommitment> orders = new LinkedList<>();
		bestPlan.getMyOrders().forEach(order -> {
			if (!(order instanceof HLDOrder)) {
				orders.add(new OrderCommitment(
						this.game.getYear(),
						this.game.getPhase(),
						order));
			}
		});
		Set<Province> dmzRegions = new HashSet<>();
		this.alliance.forEach(ally -> {
			dmzRegions.addAll(ally.getOwnedSCs());
			dmzRegions.addAll(ally.getHomes());
		});
//		dmzRegions.addAll(this.me.getHomes());
//		dmzRegions.addAll(this.me.getOwnedSCs());
		List<DMZ> dmzs = new LinkedList<>();
		this.alliance.forEach(ally -> dmzs.add(new DMZ(
				this.game.getYear(),
				this.game.getPhase(),
				createFilteredList(this.alliance, Collections.singletonList(ally)),
				createFilteredList(dmzRegions, ally.getOwnedSCs(), ally.getHomes()))));
//		if(dmzs.size() == 0) {
//			dmzs.add(new DMZ(this.game.getYear(), this.game.getPhase(), Collections.emptyList(), Collections.emptyList()));
//		}
//		
//		DMZ nonAggressionPact = new DMZ(this.game.getYear(), this.game.getPhase(), new ArrayList<>(this.alliance), dmzs);
		myPlan = new BasicDeal(orders, dmzs);
	}

	private void considerProposals() {
		List<BasicDeal> confirmedDeals = getConfirmedDeals();
		List<Order> commitedAllies = new LinkedList<>();
		this.alliance.forEach(ally -> ally.getControlledRegions().forEach(
				region -> confirmedDeals.forEach(deal -> deal.getOrderCommitments().stream()
						.filter(this::dateIsThisTurn).forEach(
								order -> commitedAllies.add(order.getOrder())))));
		List<Region> alliedRegions = new LinkedList<>();
		this.alliance.forEach(ally -> alliedRegions.addAll(ally.getControlledRegions()));
		List<Region> uncommitedAllies = new LinkedList<>(alliedRegions);
		uncommitedAllies.removeAll(commitedAllies.stream().map(Order::getLocation).collect(Collectors.toList()));

		if (bestPlan == null) {
			initPlan();
		}

		if (bestPlan != null) {
			List<HLDOrder> holdOrders = new LinkedList<>();
			List<MTOOrder> moveOrders = new LinkedList<>();
			bestPlan.getMyOrders().forEach(order -> {
				if (order instanceof HLDOrder) {
					holdOrders.add((HLDOrder) order);
				} else if (order instanceof MTOOrder) {
					moveOrders.add((MTOOrder) order);
				}
			});
			commitedAllies.forEach(order -> {
				if (order instanceof MTOOrder) {
					MTOOrder moveOrder = (MTOOrder) order;
					holdOrders.forEach(ourUnit -> {
						if (ourUnit.getLocation().getAdjacentRegions().contains(moveOrder.getDestination())) {
							System.out.println("Proposing holding unit supports");
							this.proposeDeal(new BasicDeal(
									Collections.singletonList(new OrderCommitment(
											this.game.getYear(),
											this.game.getPhase(),
											new SUPMTOOrder(this.me, ourUnit.getLocation(), moveOrder))
									),
									Collections.emptyList()));
						}
					});
				}
			});
		}
	}

	private void handleMessage() {
		Message receivedMessage = this.removeMessageFromQueue();

		switch (receivedMessage.getPerformative()) {
			case ("ACCEPT"): {
				this.getLogger().logln(String.format("EagerAllianceBot.handleMessage() Received acceptance from %s", receivedMessage.getSender()), true);
				if (receivedMessage.getContent() instanceof DiplomacyProposal) {
					DiplomacyProposal acceptance = (DiplomacyProposal) receivedMessage.getContent();
					Power powerSender = this.game.getPower(receivedMessage.getSender());

					if (this.alliance.contains(powerSender)) {
					}
				}
				break;
			}
			case ("PROPOSE"): {
				this.getLogger().logln(String.format("EagerAllianceBot.handleMessage() Received Proposal from %s", receivedMessage.getSender()), true);
				if (receivedMessage.getContent() instanceof DiplomacyProposal) {
					DiplomacyProposal proposal = (DiplomacyProposal) receivedMessage.getContent();
					Power powerSender = this.game.getPower(receivedMessage.getSender());
					boolean[] peace = {false};
					((BasicDeal) proposal.getProposedDeal()).getDemilitarizedZones().forEach(
							dmz -> {
								boolean dmzGuarenteesHomes = dmz.getProvinces().containsAll(me.getHomes()) && dmz.getPowers().contains(powerSender);
								peace[0] = peace[0] || dmzGuarenteesHomes;
							});
					if (peace[0] && !this.alliance.contains(powerSender)) {
						this.getLogger().logln(String.format("Adding %s to allies list", powerSender.getName()));

						this.alliance.add(powerSender);
//						this.myPlan.getDemilitarizedZones().add(new DMZ(this.game.getYear(),this.game.getPhase(), new ArrayList<>(this.alliance), powerSender.getHomes()));
//						this.myPlan.getDemilitarizedZones().add(new DMZ(this.game.getYear(),this.game.getPhase(), new ArrayList<>(this.alliance), powerSender.getOwnedSCs()));
						initPlan();
						this.proposeDeal(myPlan);
						this.getLogger().logln(String.format("EagerAllianceBot.handleMessage() Added %s to alliance", receivedMessage.getSender()), true);
					}
					if (this.alliance.contains(powerSender)) {
						boolean rejected = rejectIfInconsistent(proposal);
						if (!rejected) {
							this.getLogger().logln(String.format("Accepting Proposal from %s", powerSender.getName()));
							this.acceptProposal(proposal.getId());
						} else {
							this.getLogger().logln(String.format("Proposal from %s ", powerSender.getName()));
						}
					}

				}
				this.getLogger().logln(String.format("EagerAllianceBot.handleMessage() Processed Proposal from %s", receivedMessage.getSender()), true);
				break;
			}
			case ("CONFIRM"): {
				this.getLogger().logln(String.format("EagerAllianceBot.handleMessage() Received Confirmation %s", receivedMessage.getMessageId()), true);
				break;
			}
			case ("REJECT"): {
				this.getLogger().logln(String.format("EagerAllianceBot.handleMessage() Received rejection from %s", receivedMessage.getSender()), true);

				break;
			}
			default:
				this.getLogger().logln(String.format("EagerAllianceBot Recieved Message of unknown Type: %s", receivedMessage.getPerformative()), true);
		}
	}

	private boolean rejectIfInconsistent(DiplomacyProposal acceptance) {
		ArrayList<BasicDeal> deals = new ArrayList<>(this.getConfirmedDeals().size() + 1);
		deals.addAll(this.getConfirmedDeals());
		deals.add((BasicDeal) acceptance.getProposedDeal());
		if (Utilities.testConsistency(this.game, deals) != null) {
			this.rejectProposal(acceptance.getId());
			return false;
		} else {
			return true;
		}
	}


	private boolean dateIsThisTurn(OrderCommitment order) {
		return this.game.getPhase() == order.getPhase() && this.game.getYear() == order.getYear();
	}


	@Override
	public void receivedOrder(Order arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		List<Power> negotiatingPowers = this.getNegotiatingPowers();
		List<Province> dmzs = new LinkedList<>();
		for (Power each : negotiatingPowers) {
			dmzs.addAll(each.getOwnedSCs());
			dmzs.addAll(each.getHomes());
		}
		this.alliance.add(this.me);

		DMZ nonAggressionPact = new DMZ(0, Phase.SPR, negotiatingPowers, dmzs);
		nonAggressionProposal = new BasicDeal(Collections.emptyList(), Collections.singletonList(nonAggressionPact));
		this.proposeDeal(nonAggressionProposal);
	}


	@SafeVarargs
	private final <T> List<T> createFilteredList(Collection<T> collection, Collection<T>... itemsToRemove) {
		Collection<T> concatList = new LinkedList<>();
		for (Collection<T> each : itemsToRemove) {
			concatList.addAll(each);
		}
		return createFilteredList(collection, concatList);
	}


	private final <T> List<T> createFilteredList(Collection<T> collection, Collection<T> itemsToRemove) {
		LinkedList<T> ret = new LinkedList<>(collection);
		ret.removeAll(itemsToRemove);
		return ret;
	}
}

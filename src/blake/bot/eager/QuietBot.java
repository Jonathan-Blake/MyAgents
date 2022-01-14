package blake.bot.eager;

import ddejonge.bandana.anac.ANACNegotiator;
import es.csic.iiia.fabregues.dip.orders.Order;

public class QuietBot extends ANACNegotiator {

	public QuietBot(String[] args) {
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

		ANACNegotiator myPlayer = new QuietBot(args);
		myPlayer.run();

	}

	@Override
	public void negotiate(long negotiationDeadline) {
		// TODO Auto-generated method stub
		while (System.currentTimeMillis() < negotiationDeadline) {
		}
	}

	@Override
	public void receivedOrder(Order arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

}

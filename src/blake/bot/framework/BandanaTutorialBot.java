package blake.bot.framework;

import ddejonge.bandana.exampleAgents.RandomBot;
import ddejonge.bandana.tools.Logger;
import es.csic.iiia.fabregues.utilities.Interface;

public class BandanaTutorialBot extends RandomBot {

    private Logger logger = null;

    BandanaTutorialBot(String name, int finalYear, int gameServerPort) {
        super(name, finalYear, gameServerPort);
        this.log = new Interface(name);
        this.log.enableDebug();
        this.log.printMessage("Running Custom Random Bot");
    }

    /**
     * Main method to start the agent.
     *
     * @param args
     */
    public static void main(String[] args) {


        String name = "Random Negotiatior";
        int finalYear = 1905;

        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("-name") && args.length > i + 1) {
                name = args[i + 1];
            }

            //set the final year
            if (args[i].equals("-fy") && args.length > i + 1) {
                try {
                    finalYear = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("main() The final year argument is not a valid integer: " + args[i + 1]);
                    return;
                }
            }
        }


        RandomBot randomBot = new BandanaTutorialBot(name, finalYear, DEFAULT_GAME_SERVER_PORT);

        try {

            //start the agent.
            randomBot.start(randomBot.getComm());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

package ddejonge.bandana.tournament;

import ddejonge.bandana.tools.ProcessRunner;

import java.io.File;
import java.io.IOException;

public class DAIDEServerRunner {


    private static final String DAIDE_SERVER_PATH = "C:\\Program Files (x86)\\daide\\aiserver\\AiServer.exe";
    private static Process parlanceProcess;

    /**
     * Starts the game server and let it play a given number of games.
     * Note that the players and observers have to reconnect to the server each game.
     *
     * @param numGames         The number of games to play.
     * @param moveTimeLimit    Deadline in seconds for move phases.
     * @param retreatTimeLimit Deadline in seconds for retreat phases.
     * @param buildTimeLimit   Deadline in seconds for build phases.
     * @throws IOException
     */
    public static void runDAIDEServer(int numGames, int moveTimeLimit, int retreatTimeLimit, int buildTimeLimit) throws IOException {

        //Create the configuration file in order to set the deadlines.
//        createConfigFile(moveTimeLimit, retreatTimeLimit, buildTimeLimit);

        //Check if the parlance path exists.
        File parlanceFile = new File(DAIDE_SERVER_PATH);
        if (!parlanceFile.exists()) {
            System.out.println("Error! the given path to DAIDESERVER does not exist: " + DAIDE_SERVER_PATH);
            System.out.println("Please adapt the class " + DAIDEServerRunner.class.getName() + " with the correct path.");
            return;
        }

        //Run parlance-server
        String[] cmd = {DAIDE_SERVER_PATH, "-g" + numGames, "standard"};
        parlanceProcess = ProcessRunner.exec(cmd, "parlance");


        //Note: an exception is thrown if parlance is started CORRECTLY.
        try {
            if (parlanceProcess == null) {
                System.out.println("Parlance failed to start.");
            } else {
                System.out.println("ParlanceServer.runParlanceServer() parlance exit value: " + parlanceProcess.exitValue());
            }
        } catch (IllegalThreadStateException e) {
            System.out.println("ParlanceServer.runParlanceServer() PARLANCE SERVER STARTED");
        }


    }


    public static void stop() {

//        File configFile = new File(CONFIG_FOLDER, CONFIG_FILE_NAME);
//        configFile.delete();

        parlanceProcess.destroy();

    }
}

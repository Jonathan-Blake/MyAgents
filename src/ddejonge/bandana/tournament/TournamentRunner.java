package ddejonge.bandana.tournament;

import ddejonge.bandana.tools.Logger;
import ddejonge.bandana.tools.ProcessRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TournamentRunner {
	//Main folder where all the logs are stored. For each tournament a new folder will be created inside this folder
	// where the results of the tournament will be logged.
	static final String LOG_FOLDER = "log";
	private static final String FINAL_YEAR = "1905";
	//Command lines to start the various agents provided with the Bandana framework.
	// Add your own line here to run your own bot.
	static final String[] randomNegotiatorCommand = {"java", "-jar", "agents/RandomNegotiator.jar", "-log", LOG_FOLDER, "-name", "RandomNegotiator", "-fy", FINAL_YEAR};
	static final String[] randomCommand = {"java", "-jar", "agents/RandomBot-0.2.jar", "-log", LOG_FOLDER, "-name", "Random", "-fy", FINAL_YEAR};
	static final String[] dumbBot_1_4_Command = {"java", "-jar", "agents/DumbBot-1.4.jar", "-log", LOG_FOLDER, "-name", "DumbBot", "-fy", FINAL_YEAR};
	static final String[] dbrane_1_1_Command = {"java", "-jar", "agents/D-Brane-1.1.jar", "-log", LOG_FOLDER, "-name", "D-Brane", "-fy", FINAL_YEAR};
	static final String[] dbraneExampleBotCommand = {"java", "-jar", "agents/D-BraneExampleBot.jar", "-log", LOG_FOLDER, "-name", "DBraneExampleBot", "-fy", FINAL_YEAR};

	static final String[] anacExampleBotCommand = {"java", "-jar", "agents/AnacExampleNegotiator.jar", "-log", LOG_FOLDER, "-name", "AnacExampleNegotiator", "-fy", FINAL_YEAR};

	static final String[] BandanaTutorialBotCommand = {"java", "-jar", "agents/BandanaTutorialBot-0.1.jar", "-log", LOG_FOLDER, "-name", "BandanaTutorialBot", "-fy", FINAL_YEAR};

	static final String[] EagerAllianceBotCommand = {"java", "-jar", "agents/EagerAllianceBot-0.5.a.jar", "-log", LOG_FOLDER, "-name", "EagerAllianceBot", "-fy", FINAL_YEAR};
	static final String[] QuietBotCommand = {"java", "-jar", "agents/QuietBot-0.1.2.jar", "-log", LOG_FOLDER, "-name", "QuietBot", "-fy", FINAL_YEAR};
	static List<Process> players = new ArrayList<>();

	public static void main(String[] args) throws IOException {

		int numberOfGames = 50;                //The number of games this tournament consists of.

		int deadlineForMovePhases = 60;    //60 seconds for each SPR and FAL phases
		int deadlineForRetreatPhases = 30;  //30 seconds for each SUM and AUT phases
		int deadlineForBuildPhases = 30;    //30 seconds for each WIN phase

		int finalYear = 1910;    //The year after which the agents in each game are supposed to propose a draw to each other.
		// (It depends on the implementation of the players whether this will indeed happen or not, so this may not always work.)

		run(numberOfGames, deadlineForMovePhases, deadlineForRetreatPhases, deadlineForBuildPhases, finalYear);


		Runtime.getRuntime().addShutdownHook(new Thread() {

			//NOTE: unfortunately, Shutdownhooks don't work on windows if the program was started in eclipse and
			// you stop it by clicking the red button (on MAC it seems to work fine).

			@Override
			public void run() {
				NegoServerRunner.stop();
				ParlanceRunner.stop();
			}
		});
	}

	public static void run(int numberOfGames, int moveTimeLimit, int retreatTimeLimit, int buildTimeLimit, int finalYear) throws IOException {

		//Create a folder to store all the results of the tournament. 
		// This folder will be placed inside the LOG_FOLDER and will have the current date and time as its name.
		// You can change this line if you prefer it differently.
		String tournamentLogFolderPath = LOG_FOLDER + File.separator + Logger.getDateString();
		File logFile = new File(tournamentLogFolderPath);
		logFile.mkdirs();

		//1. Run the Parlance game server.
		ParlanceRunner.runParlanceServer(numberOfGames, moveTimeLimit, retreatTimeLimit, buildTimeLimit);

		//Create a list of ScoreCalculators to determine how the players should be ranked in the tournament.
		ArrayList<ScoreCalculator> scoreCalculators = new ArrayList<>();
		scoreCalculators.add(new SoloVictoryCalculator());
		scoreCalculators.add(new SupplyCenterCalculator());
		scoreCalculators.add(new PointsCalculator());
		scoreCalculators.add(new RankCalculator());

		//2. Create a TournamentObserver to monitor the games and accumulate the results.
		TournamentObserver tournamentObserver = new TournamentObserver(tournamentLogFolderPath, scoreCalculators, numberOfGames, 7);

		//3. Run the Negotiation Server.
		NegoServerRunner.run(tournamentObserver, tournamentLogFolderPath, numberOfGames);

		for (int gameNumber = 1; gameNumber <= numberOfGames; gameNumber++) {

			System.out.println();
			System.out.println("GAME " + gameNumber);

			NegoServerRunner.notifyNewGame(gameNumber);

			//4. Start the players:

//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Random 1", randomCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Random 2", randomCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Random 3", randomCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Tutorial 1", BandanaTutorialBotCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Tutorial 2", BandanaTutorialBotCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Tutorial 3", BandanaTutorialBotCommand);
			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "D-Brane 1", dbrane_1_1_Command);
			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "D-Brane 2", dbrane_1_1_Command);
			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "D-Brane 3", dbrane_1_1_Command);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "D-Brane 4", dbrane_1_1_Command);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "D-BraneExampleBot 1", dbraneExampleBotCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "D-BraneExampleBot 2", dbraneExampleBotCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "D-BraneExampleBot 3", dbraneExampleBotCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "D-BraneExampleBot 4", dbraneExampleBotCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "RandomNegotiator 5", randomNegotiatorCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "RandomNegotiator 6", randomNegotiatorCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "RandomNegotiator 7", randomNegotiatorCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "RandomNegotiator 8", randomNegotiatorCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "DumbBot 7", dumbBot_1_4_Command);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "DumbBot 8", dumbBot_1_4_Command);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "DumbBot 5", dumbBot_1_4_Command);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "DumbBot 6", dumbBot_1_4_Command);
			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Eager Alliance Bot 1", EagerAllianceBotCommand);
			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Eager Alliance Bot 2", EagerAllianceBotCommand);
			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Eager Alliance Bot 3", EagerAllianceBotCommand);
			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Eager Alliance Bot 4", EagerAllianceBotCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Quiet Bot 1", QuietBotCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Quiet Bot 2", QuietBotCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Quiet Bot 3", QuietBotCommand);
//			createPlayer(finalYear, tournamentLogFolderPath, gameNumber, "Quiet Bot 4", QuietBotCommand);

			//5. Let the tournament observer (re-)connect to the game server.
			tournamentObserver.connectToServer();

			//NOW WAIT TILL THE GAME IS FINISHED
			while (tournamentObserver.getGameStatus() == TournamentObserver.GAME_ACTIVE || tournamentObserver.getGameStatus() == TournamentObserver.CONNECTED_WAITING_TO_START) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}

				if (tournamentObserver.playerFailed()) {
					// One or more players did not send its orders in in time.
					System.out.println("Players Failed to respond : " + tournamentObserver.getCCDedPlayers());
				}
			}

			//Kill the player processes.
			// (if everything is implemented okay this isn't necessary because the players should kill themselves. 
			// But just to be sure..)
			for (Process playerProces : players) {
				playerProces.destroy();
			}
		}

		System.out.println("TOURNAMENT FINISHED");

		//Get the results of all the games played in this tournament.
		// Each GameResult object contains the results of one game.
		// The tournamentObserver already automatically prints these results to a text file,
		//  as well as the processed overall results of the tournament.
		// However, you may want to do your own processing of the results, for which
		// you can use this list.
		List<GameResult> results = tournamentObserver.getGameResults();

		tournamentObserver.exit();
		ParlanceRunner.stop();
		NegoServerRunner.stop();
	}

	private static void createPlayer(int finalYear, String tournamentLogFolderPath, int gameNumber, String name, String[] command) {
		//set the log folder for this agent to be a subfolder of the tournament log folder.
		command[4] = tournamentLogFolderPath + File.separator + name + File.separator + "Game " + gameNumber + File.separator;

		//set the name of the agent.
		command[6] = name;

		//set the year after which the agent will propose a draw to the other agents.
		command[8] = "" + finalYear;

		//start the process
		String processName = name;
		System.out.println(Arrays.toString(command) + " name:::" + processName);
		Process playerProcess = ProcessRunner.exec(command, processName);
		// We give  a name to the process so that we can see in the console where its output comes from.
		// This name does not have to be the same as the name given to the agent, but it would be confusing
		// to do otherwise.

		//store the Process object in a list.
		players.add(playerProcess);
	}
}

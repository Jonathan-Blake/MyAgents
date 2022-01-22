package ddejonge.bandana.tournament;

public class CommandArgs {
    private final String jarFile;
    private final String logFolder;
    private final String name;
    private final String finalYear;

    public CommandArgs(String jarFile, String logFolder, String name, String finalYear) {

        this.jarFile = jarFile;
        this.logFolder = logFolder;
        this.name = name;
        this.finalYear = finalYear;
    }

    public String[] toStringArray() {
        return new String[]{"java", "-jar", this.jarFile, "-log", this.logFolder, "-name", this.name, "-fy", this.finalYear};
    }
}

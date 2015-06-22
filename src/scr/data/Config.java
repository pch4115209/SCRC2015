package scr.data;

import scr.Controller.Stage;

/**
 * Stores the configuration parameters read in from a configuration file.
 */
public class Config {
    private int port = 3001;
    private String host = "localhost";
    private String clientID = "SCR";
    private boolean verbose = false;
    private int maxEpisodes = 1;
    private int maxSteps = 0;
    private Stage stage = Stage.UNKNOWN;
    private String trackName = "unknown";

    public Config() {
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int getMaxEpisodes() {
        return maxEpisodes;
    }

    public void setMaxEpisodes(int maxEpisodes) {
        this.maxEpisodes = maxEpisodes;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
}

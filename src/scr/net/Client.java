package scr.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import scr.Controller.Stage;
import scr.Driver;
import scr.MessageBasedSensorModel;
import scr.TrackSegment;
import scr.data.Action;
import scr.data.Config;
import scr.data.Individual;
import scr.data.Population;

/**
 * @author Daniele Loiacono
 * 
 */
public class Client {

    private static int UDP_TIMEOUT = 10000;
    private Config config;

    public Client(Config config) {
        this.config = config;
    }

    /**
     * This method is used for driving during the 3 stages of competition.
     * 
     * @param settings
     *            individual settings
     * @param stage
     *            stage of competition
     */
    public void drive(Population drivers, Stage stage) {
        int driverIndex = 0;
        SocketHandler socket = new SocketHandler(config.getHost(), config.getPort(), config.isVerbose());
        Driver driver = new Driver();
        driver.setStage(config.getStage());
        driver.setTrackName(config.getTrackName());
        driver.setSettings(drivers.get(driverIndex));

        // Apply track segments to driver if qualifying or racing
        if (driver.getUseSegmentation() && (stage == Stage.QUALIFYING || stage == Stage.RACE)) {
            driver.setFirstLap(false);
            driver.setSegList(loadTrackSegments());
        }

        double currentLapTime = -1000d;
        String inMsg;
        long curEpisode = 0;
        boolean shutdownOccurred = false;
        double distance = 0;
        int currStep = 0;
        double damage = 0;
        do {
            /*
             * Client identification
             */
            do {
                socket.send(createInitString(driver));
                inMsg = socket.receive(UDP_TIMEOUT);
            }
            while (inMsg == null || inMsg.indexOf("identified") < 0);

            /*
             * Start to drive
             */
            while (true) {
                /*
                 * Receives from TORCS the game state
                 */
                inMsg = socket.receive(UDP_TIMEOUT);
                if (inMsg != null) {
                    /*
                     * Check if race is ended (shutdown)
                     */
                    if (inMsg.indexOf("shutdown") >= 0) {
                        shutdownOccurred = true;
                        break;
                    }

                    /*
                     * Check if race is restarted
                     */
                    if (inMsg.indexOf("restart") >= 0) {
                        driver.reset();
                        break;
                    }

                    Action action = new Action();
                    if (currStep <= config.getMaxSteps() || config.getMaxSteps() == 0) {
                        MessageBasedSensorModel inputSensor = new MessageBasedSensorModel(inMsg);
                        action = driver.control(inputSensor);
                        distance = inputSensor.getDistanceRaced();

                        // Change drivers
                        if (stage == Stage.WARMUP && inputSensor.getCurrentLapTime() < currentLapTime) {
                            if (driverIndex < drivers.size()) {
                                drivers.get(driverIndex).setFitness(inputSensor.getLastLapTime());
                                if ((inputSensor.getDamage() - damage) > 3000)
                                {
                                    drivers.get(driverIndex).setFitness(Double.MAX_VALUE);
                                }
                                
                                driverIndex++;
                                damage = inputSensor.getDamage();
                                if (driverIndex < drivers.size()) {
                                    driver.setSettings(drivers.get(driverIndex));
                                }
                            }
                        }
                        currentLapTime = inputSensor.getCurrentLapTime();
                    }
                    else {
                        action.restartRace = true;
                    }

                    currStep++;
                    socket.send(action.toString());
                }
                else {
                    System.out.println("Server did not respond within the timeout");
                }
            }
        }
        while (++curEpisode < config.getMaxEpisodes() && !shutdownOccurred);

        if (stage == Stage.WARMUP) {
            try {
                PrintStream output = new PrintStream(new FileOutputStream("group_3_" + config.getTrackName() + "_driver.txt"));
                output.print(drivers.getBestIndividual().toString());
                output.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else if (stage == Stage.QUALIFYING) {
            System.out.println("Distance Travelled: " + distance + "m");
        }

        /*
         * Shutdown the controller
         */
        driver.shutdown();
        socket.close();
    }

    /**
     * This method is used during offline learning before the competition for generating a pool of pre-trained drivers.
     * 
     * @param settings
     *            individual settings
     * @return fitness (lap time)
     */
    public double evaluateFitness(Individual settings) {
        runServer(config.getTrackName());
        SocketHandler socket = new SocketHandler(config.getHost(), config.getPort(), config.isVerbose());
        Driver driver = new Driver();
        driver.setStage(config.getStage());
        driver.setSettings(settings);

        String inMsg;
        boolean shutdownOccurred = false;
        List<Double> lapTimes = new ArrayList<Double>();
        double currentLapTime = -1000d;
        do {
            /*
             * Client identification
             */
            do {
                socket.send(createInitString(driver));
                inMsg = socket.receive(1000);
            }
            while (inMsg == null || inMsg.indexOf("identified") < 0);

            /*
             * Start to drive
             */
            while (true) {
                /*
                 * Receives from TORCS the game state
                 */
                inMsg = socket.receive(1000);
                if (inMsg != null) {
                    /*
                     * Check if race is ended (shutdown)
                     */
                    if (inMsg.indexOf("shutdown") >= 0) {
                        shutdownOccurred = true;
                        break;
                    }

                    /*
                     * Check if race is restarted
                     */
                    if (inMsg.indexOf("restart") >= 0) {
                        driver.reset();
                        break;
                    }

                    MessageBasedSensorModel inputSensor = new MessageBasedSensorModel(inMsg);
                    Action action = driver.control(inputSensor);

                    // Store lap times
                    if (inputSensor.getCurrentLapTime() < currentLapTime) {
                        lapTimes.add(inputSensor.getLastLapTime());
                    }
                    currentLapTime = inputSensor.getCurrentLapTime();

                    socket.send(action.toString());
                }
                else {
                    System.out.println("Server did not respond within the timeout");
                }
            }
        }
        while (!shutdownOccurred);

        /*
         * Shutdown the controller
         */
        driver.shutdown();
        socket.close();

        if (lapTimes.size() < 3) {
            return Double.MAX_VALUE;
        }

        double totalTrackTime = 0;
        for (Double lapTime : lapTimes) {
            totalTrackTime += lapTime;
        }
        return (totalTrackTime / lapTimes.size());
    }

    /**
     * Runs the server software.
     */
    public void runServer(String track) {
        if (System.getProperty("os.name").startsWith("Windows")) {
            try {
                File file = new File("torcs_directory.txt");
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String directory = reader.readLine();
                Runtime.getRuntime().exec("cmd /c cd \"" + directory + "\" & wtorcs.exe -r config\\raceman\\" + track + ".xml");
                reader.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            // If you're on ubuntu/linux put equivalent of the above code here
        }
    }

    /**
     * Create the init string.
     */
    private String createInitString(Driver driver) {
        float[] angles = driver.initAngles();
        String initStr = config.getClientID() + "(init";
        for (int i = 0; i < angles.length; i++) {
            initStr = initStr + " " + angles[i];
        }
        initStr = initStr + ")";
        return initStr;
    }

    /**
     * Loads the track segments for use
     * 
     * @param driverFile
     *            file containing driver parameters
     */
    private ArrayList<TrackSegment> loadTrackSegments() {
        ArrayList<TrackSegment> segList = new ArrayList<TrackSegment>();
        try {
            File file = new File("group_3_" + config.getTrackName() + "_segments.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            TrackSegment seg = new TrackSegment();
            segList.add(seg);
            while ((line = reader.readLine()) != null) {

                // System.out.println(line);
                if (line.isEmpty()) {
                    seg = new TrackSegment();
                    segList.add(seg);
                    continue;
                }

                String[] setting = line.split("=");
                if (setting.length == 2) {
                    String settingName = setting[0];
                    String settingValue = setting[1];

                    // System.out.println(settingValue);

                    if (settingName.equalsIgnoreCase("start")) {
                        seg.setStart(Double.parseDouble(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("distance")) {
                        seg.setDistance(Double.parseDouble(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("type")) {
                        seg.setType(Integer.parseInt(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("sharpness")) {
                        seg.setSharpness(Integer.parseInt(settingValue));
                    }
                }
            }
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return segList;
    }
}

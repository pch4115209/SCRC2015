package scr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import scr.Controller.Stage;
import scr.data.Config;
import scr.data.Individual;
import scr.data.Population;
import scr.ea.EvoAlgorithm;
import scr.net.Client;

public class Main {
    public static void main(String[] args) throws Exception {
        Config config = parseArguments(args);
        new Main(config);
    }

    /**
     * Runs the appropriate part of the software based on the stage in the given configuration.
     * 
     * @param config
     *            the configuration to use
     * @throws Exception
     */
    public Main(Config config) throws Exception {
        if (config.getStage() == Stage.OFFLINE) {
            EvoAlgorithm algorithm = new EvoAlgorithm(config);
            algorithm.run();
        }
        else if (config.getStage() == Stage.WARMUP) {
            Population drivers = loadDrivers(config);
            Client client = new Client(config);
            client.drive(drivers, config.getStage());
        }
        else if (config.getStage() == Stage.QUALIFYING || config.getStage() == Stage.RACE) {
            File driverFile = new File("group_3_" + config.getTrackName() + "_driver.txt");
            Individual driver = loadDriver(new FileReader(driverFile));
            Population drivers = new Population();
            drivers.add(driver);
            Client client = new Client(config);
            client.drive(drivers, config.getStage());
        }
    }

    /**
     * Parses the given command line arguments into a configuration data structure.
     * 
     * @param args
     *            command line arguments
     * @return configuration
     */
    private static Config parseArguments(String[] args) {
        Config config = new Config();
        for (int i = 0; i < args.length; i++) {
            String[] argumentValue = args[i].split(":");
            String argument = argumentValue[0];
            String value = argumentValue[1];
            if (argument.equals("port")) {
                config.setPort(Integer.parseInt(value));
            }
            else if (argument.equals("host")) {
                config.setHost(value);
            }
            else if (argument.equals("id")) {
                config.setClientID(value);
            }
            else if (argument.equals("verbose")) {
                config.setVerbose(Boolean.parseBoolean(value));
            }
            else if (argument.equals("stage")) {
                config.setStage(Stage.fromInt(Integer.parseInt(value)));
            }
            else if (argument.equals("track")) {
                config.setTrackName(value);
            }
            else if (argument.equals("maxEpisodes")) {
                config.setMaxEpisodes(Integer.parseInt(value));
            }
            else if (argument.equals("maxSteps")) {
                config.setMaxSteps(Integer.parseInt(value));
            }
        }
        return config;
    }

    /**
     * Loads the parameters of any pre-learned drivers that can be found on the file system.
     * 
     * @param config
     *            the configuration to use
     * @return a population of drivers
     * @throws Exception
     */
    private Population loadDrivers(Config config) throws Exception {
        Population drivers = new Population();
        JarFile jarFile = new JarFile("group_3_driver.jar");
        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            if (entry.getName().contains(".")) {
                JarEntry fileEntry = jarFile.getJarEntry(entry.getName());
                if (fileEntry.getName().toLowerCase().endsWith(".txt")) {
                    String name = fileEntry.getName().toLowerCase();
                    InputStreamReader inputStream = new InputStreamReader(jarFile.getInputStream(fileEntry));
                    Individual driver = loadDriver(inputStream);
                    driver.setName(name);
                    drivers.add(driver);
                }
            }
        }
        jarFile.close();

        if (drivers.isEmpty()) {
            System.err.println("No drivers found");
            drivers.add(new Individual());
        }

        if (config.getTrackName() != null) {
            String currentDirectory = new File(".").getAbsolutePath().toLowerCase();
            Map<String, Population> driverMap = new HashMap<String, Population>();
            for (Individual driver : drivers) {
                Population typePop = driverMap.get(driver.getType());
                if (typePop == null) {
                    typePop = new Population();
                    driverMap.put(driver.getType(), typePop);
                }
                typePop.add(driver);
            }

            Population allDrivers = driverMap.get("all");
            Population ovalDrivers = driverMap.get("oval");
            Population roadDrivers = driverMap.get("road");
            Population dirtDrivers = driverMap.get("dirt");
            if (allDrivers != null && ovalDrivers != null && roadDrivers != null && dirtDrivers != null) {
                drivers.clear();
                drivers.addAll(allDrivers);
                if (config.getTrackName().contains("oval") || currentDirectory.contains("oval")) {
                    drivers.addAll(ovalDrivers);
                    drivers.addAll(roadDrivers);
                    drivers.addAll(dirtDrivers);
                }
                else if (config.getTrackName().contains("dirt") || currentDirectory.contains("dirt")) {
                    drivers.addAll(dirtDrivers);
                    drivers.addAll(roadDrivers);
                    drivers.addAll(ovalDrivers);
                }
                else if (config.getTrackName().contains("road") || currentDirectory.contains("road")) {
                    drivers.addAll(roadDrivers);
                    for (int i = 0; i < 10; i++) {
                        if (i < driverMap.get("dirt").size()) {
                            drivers.add(dirtDrivers.get(i));
                        }
                        if (i < driverMap.get("oval").size()) {
                            drivers.add(ovalDrivers.get(i));
                        }
                    }
                }
                else {
                    for (int i = 0; i < 10; i++) {
                        if (i < driverMap.get("road").size()) {
                            drivers.add(roadDrivers.get(i));
                        }
                        if (i < driverMap.get("dirt").size()) {
                            drivers.add(dirtDrivers.get(i));
                        }
                        if (i < driverMap.get("oval").size()) {
                            drivers.add(ovalDrivers.get(i));
                        }
                    }
                }
            }
        }
        return drivers;
    }

    /**
     * Loads the parameters of a driver from a given input stream into an individual.
     * 
     * @param inputStream
     *            input stream containing driver parameters
     * @return individual with driver parameters
     */
    private static Individual loadDriver(InputStreamReader inputStream) {
        Individual individual = new Individual();
        try {
            BufferedReader reader = new BufferedReader(inputStream);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }

                String[] setting = line.split("=");
                if (setting.length == 2) {
                    String settingName = setting[0];
                    String settingValue = setting[1];

                    if (settingName.equalsIgnoreCase("gearUp")) {
                        setting = settingValue.split(",");
                        individual.setGearUp(new int[] { Integer.parseInt(setting[0]), Integer.parseInt(setting[1]), Integer.parseInt(setting[2]), Integer.parseInt(setting[3]),
                                Integer.parseInt(setting[4]), Integer.parseInt(setting[5]) });
                    }
                    else if (settingName.equalsIgnoreCase("gearDown")) {
                        setting = settingValue.split(",");
                        individual.setGearDown(new int[] { Integer.parseInt(setting[0]), Integer.parseInt(setting[1]), Integer.parseInt(setting[2]), Integer.parseInt(setting[3]),
                                Integer.parseInt(setting[4]), Integer.parseInt(setting[5]) });
                    }
                    else if (settingName.equalsIgnoreCase("wheelRadius")) {
                        setting = settingValue.split(",");
                        individual.setWheelRadius(new float[] { Float.parseFloat(setting[0]), Float.parseFloat(setting[1]), Float.parseFloat(setting[2]), Float.parseFloat(setting[3]) });
                    }
                    else if (settingName.equalsIgnoreCase("stuckTime")) {
                        individual.setStuckTime(Integer.parseInt(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("stuckAngle")) {
                        individual.setStuckAngle(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("maxSpeedDist")) {
                        individual.setMaxSpeedDist(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("maxSpeed")) {
                        individual.setMaxSpeed(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("sin5")) {
                        individual.setSin5(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("cos5")) {
                        individual.setCos5(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("steerLock")) {
                        individual.setSteerLock(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("steerSensitivityOffset")) {
                        individual.setSteerSensitivityOffset(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("wheelSensitivityCoeff")) {
                        individual.setWheelSensitivityCoeff(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("absSlip")) {
                        individual.setAbsSlip(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("absRange")) {
                        individual.setAbsRange(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("absMinSpeed")) {
                        individual.setAbsMinSpeed(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("clutchMax")) {
                        individual.setClutchMax(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("clutchDelta")) {
                        individual.setClutchDelta(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("clutchRange")) {
                        individual.setClutchRange(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("clutchDeltaTime")) {
                        individual.setClutchDeltaTime(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("clutchDeltaRaced")) {
                        individual.setClutchDeltaRaced(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("clutchDec")) {
                        individual.setClutchDec(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("clutchMaxModifier")) {
                        individual.setClutchMaxModifier(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("clutchMaxTime")) {
                        individual.setClutchMaxTime(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("approachPosition")) {
                        individual.setApproachPosition(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("approachSensitivity")) {
                        individual.setApproachSensitivity(Float.parseFloat(settingValue));
                    }
                    else if (settingName.equalsIgnoreCase("approachCorrection")) {
                        individual.setApproachCorrection(Float.parseFloat(settingValue));
                    }
                }
            }
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return individual;
    }
}
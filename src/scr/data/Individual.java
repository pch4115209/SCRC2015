package scr.data;

public class Individual implements Comparable<Individual> {

    /* Sigma constants */
    /* Usage: 0.1 represents 10% sigma value of the norm dist */
    private double oneSigmaRate = 0.1;
    private double[] nSigmaRate = { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 };

    /* Threshold - the lowest number sigma can get */
    private double oneSigmaRateThreshold = 0.05;
    private double nSigmaRateThreshold = 0.05;

    /* Gear Changing Constants */
    private int[] gearUp = { 5000, 6000, 6000, 6500, 7000, 0 };
    private int[] gearDown = { 0, 2500, 3000, 3000, 3500, 3500 };

    /* Stuck constants */
    private int stuckTime = 25;
    private float stuckAngle = (float) 0.523598775; // PI/6

    /* Accel and Brake Constants */
    private float maxSpeedDist = 60f;
    private float maxSpeed = 200f;
    private float sin5 = (float) 0.08716;
    private float cos5 = (float) 0.99619;

    /* Steering constants */
    private float steerLock = (float) 0.366519;
    private float steerSensitivityOffset = (float) 80.0;
    private float wheelSensitivityCoeff = 1;

    /* ABS Filter Constants */
    private float wheelRadius[] = { (float) 0.3306, (float) 0.3306, (float) 0.3276, (float) 0.3276 };
    private float absSlip = (float) 2.0;
    private float absRange = (float) 3.0;
    private float absMinSpeed = (float) 3.0;

    /* Clutching Constants */
    private float clutchMax = (float) 0.5;
    private float clutchDelta = (float) 0.05;
    private float clutchRange = (float) 0.82;
    private float clutchDeltaTime = (float) 0.02;
    private float clutchDeltaRaced = 10;
    private float clutchDec = (float) 0.01;
    private float clutchMaxModifier = (float) 1.3;
    private float clutchMaxTime = (float) 1.5;

    private int stuck = 0;

    // current clutch
    private float clutch = 0;

    /* Calculated Fitness */
    private double fitness = Double.MAX_VALUE;

    /* New Approach Parameters */

    // determines where the driver positions itself before and during a corner, where higher values mean positioning
    // closer to the apex
    float approachPosition = 1f;

    // determines how aggressively the driver tries to achieve the desired approach
    float approachSensitivity = 1f;

    // determines how a driver recovers when it encounters an "out of bounds" area on
    float approachCorrection = 0.5f;

    // driver name/type
    private String name = "all";

    public Individual() {
    }

    /*
     * Copy constructor.
     */
    public Individual(Individual individual) {
        this.gearUp = individual.gearUp.clone();
        this.gearDown = individual.gearDown.clone();
        this.stuckTime = individual.stuckTime;
        this.stuckAngle = individual.stuckAngle;
        this.maxSpeedDist = individual.maxSpeedDist;
        this.maxSpeed = individual.maxSpeed;
        this.sin5 = individual.sin5;
        this.cos5 = individual.cos5;
        this.steerLock = individual.steerLock;
        this.steerSensitivityOffset = individual.steerSensitivityOffset;
        this.wheelSensitivityCoeff = individual.wheelSensitivityCoeff;
        this.wheelRadius = individual.wheelRadius.clone();
        this.absSlip = individual.absSlip;
        this.absRange = individual.absRange;
        this.absMinSpeed = individual.absMinSpeed;
        this.clutchMax = individual.clutchMax;
        this.clutchDelta = individual.clutchDelta;
        this.clutchRange = individual.clutchRange;
        this.clutchDeltaTime = individual.clutchDeltaTime;
        this.clutchDeltaRaced = individual.clutchDeltaRaced;
        this.clutchDec = individual.clutchDec;
        this.clutchMaxModifier = individual.clutchMaxModifier;
        this.clutchMaxTime = individual.clutchMaxTime;
        this.stuck = individual.stuck;
        this.clutch = individual.clutch;
        this.approachPosition = individual.approachPosition;
        this.approachSensitivity = individual.approachSensitivity;
        this.approachCorrection = individual.approachCorrection;
    }

    /*
     * gets all the parameters ready for tune-ing for 1-sig
     * 
     * @return: para[0-11] rpm for gear up and down para[12-13] max speed para[14-16] approach para[17] one sigma rate
     */
    public double[] getOneSigmaArray() {
        double[] parameters = new double[18];
        for (int i = 0; i < gearUp.length; i++) {
            parameters[i] = gearUp[i];
            parameters[i + gearUp.length] = gearDown[i];
        }
        parameters[12] = maxSpeedDist;
        parameters[13] = maxSpeed;
        parameters[14] = approachPosition;// absSlip;
        parameters[15] = approachSensitivity;// absRange;
        parameters[16] = approachCorrection;// absMinSpeed;
        parameters[17] = oneSigmaRate;

        return parameters;
    }

    // once they are tuned, set them as new parameters
    public void setOneSigmaArray(double[] a) {
        for (int i = 0; i < gearUp.length; i++) {
            gearUp[i] = (int) a[i];
            gearDown[i] = (int) a[i + gearUp.length];
        }
        maxSpeedDist = (float) a[12];
        maxSpeed = (float) a[13];
        /* absSlip */approachPosition = (float) a[14];
        /* absRange */approachSensitivity = (float) a[15];
        /* absMinSpeed */approachCorrection = (float) a[16];
        oneSigmaRate = (float) a[17];
    }

    /*
     * gets all the parameters ready for tune-ing for n-sig
     * 
     * @return: para[0-11] rpm for gear up and down para[12-13] max speed para[14-16] approach para[17-34] individual
     * sigma rate for each parameters
     */
    public double[] getNSigmaArray() {
        double[] parameters = new double[34];
        for (int i = 0; i < gearUp.length; i++) {
            parameters[i] = gearUp[i];
            parameters[i + gearUp.length] = gearDown[i];
        }
        parameters[12] = maxSpeedDist;
        parameters[13] = maxSpeed;
        parameters[14] = approachPosition;// absSlip;
        parameters[15] = approachSensitivity;// absRange;
        parameters[16] = approachCorrection;// absMinSpeed;

        for (int i = 17; i < 34; i++) {
            parameters[i] = nSigmaRate[i - 17];
        }
        return parameters;
    }

    // once they are tuned, set them as new parameters
    public void setNSigmaArray(double[] a) {
        for (int i = 0; i < gearUp.length; i++) {
            gearUp[i] = (int) a[i];
            gearDown[i] = (int) a[i + gearUp.length];
        }
        maxSpeedDist = (float) a[12];
        maxSpeed = (float) a[13];
        /* absSlip */approachPosition = (float) a[14];
        /* absRange */approachSensitivity = (float) a[15];
        /* absMinSpeed */approachCorrection = (float) a[16];

        for (int i = 17; i < a.length; i++) {
            nSigmaRate[i - 17] = a[i];
        }
    }

    public double getOneSigmaRateThreshold() {
        return oneSigmaRateThreshold;
    }

    public double getNSigmaRateThreshold() {
        return nSigmaRateThreshold;
    }

    public int[] getGearUp() {
        return gearUp;
    }

    public void setGearUp(int[] gearUp) {
        this.gearUp = gearUp;
    }

    public int[] getGearDown() {
        return gearDown;
    }

    public void setGearDown(int[] gearDown) {
        this.gearDown = gearDown;
    }

    public int getStuckTime() {
        return stuckTime;
    }

    public void setStuckTime(int stuckTime) {
        this.stuckTime = stuckTime;
    }

    public float getStuckAngle() {
        return stuckAngle;
    }

    public void setStuckAngle(float stuckAngle) {
        this.stuckAngle = stuckAngle;
    }

    public float getMaxSpeedDist() {
        return maxSpeedDist;
    }

    public void setMaxSpeedDist(float maxSpeedDist) {
        this.maxSpeedDist = maxSpeedDist;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getSin5() {
        return sin5;
    }

    public void setSin5(float sin5) {
        this.sin5 = sin5;
    }

    public float getCos5() {
        return cos5;
    }

    public void setCos5(float cos5) {
        this.cos5 = cos5;
    }

    public float getSteerLock() {
        return steerLock;
    }

    public void setSteerLock(float steerLock) {
        this.steerLock = steerLock;
    }

    public float getSteerSensitivityOffset() {
        return steerSensitivityOffset;
    }

    public void setSteerSensitivityOffset(float steerSensitivityOffset) {
        this.steerSensitivityOffset = steerSensitivityOffset;
    }

    public float getWheelSensitivityCoeff() {
        return wheelSensitivityCoeff;
    }

    public void setWheelSensitivityCoeff(float wheelSensitivityCoeff) {
        this.wheelSensitivityCoeff = wheelSensitivityCoeff;
    }

    public float[] getWheelRadius() {
        return wheelRadius;
    }

    public void setWheelRadius(float[] wheelRadius) {
        this.wheelRadius = wheelRadius;
    }

    public float getAbsSlip() {
        return absSlip;
    }

    public void setAbsSlip(float absSlip) {
        this.absSlip = absSlip;
    }

    public float getAbsRange() {
        return absRange;
    }

    public void setAbsRange(float absRange) {
        this.absRange = absRange;
    }

    public float getAbsMinSpeed() {
        return absMinSpeed;
    }

    public void setAbsMinSpeed(float absMinSpeed) {
        this.absMinSpeed = absMinSpeed;
    }

    public float getClutchMax() {
        return clutchMax;
    }

    public void setClutchMax(float clutchMax) {
        this.clutchMax = clutchMax;
    }

    public float getClutchDelta() {
        return clutchDelta;
    }

    public void setClutchDelta(float clutchDelta) {
        this.clutchDelta = clutchDelta;
    }

    public float getClutchRange() {
        return clutchRange;
    }

    public void setClutchRange(float clutchRange) {
        this.clutchRange = clutchRange;
    }

    public float getClutchDeltaTime() {
        return clutchDeltaTime;
    }

    public void setClutchDeltaTime(float clutchDeltaTime) {
        this.clutchDeltaTime = clutchDeltaTime;
    }

    public float getClutchDeltaRaced() {
        return clutchDeltaRaced;
    }

    public void setClutchDeltaRaced(float clutchDeltaRaced) {
        this.clutchDeltaRaced = clutchDeltaRaced;
    }

    public float getClutchDec() {
        return clutchDec;
    }

    public void setClutchDec(float clutchDec) {
        this.clutchDec = clutchDec;
    }

    public float getClutchMaxModifier() {
        return clutchMaxModifier;
    }

    public void setClutchMaxModifier(float clutchMaxModifier) {
        this.clutchMaxModifier = clutchMaxModifier;
    }

    public float getClutchMaxTime() {
        return clutchMaxTime;
    }

    public void setClutchMaxTime(float clutchMaxTime) {
        this.clutchMaxTime = clutchMaxTime;
    }

    public int getStuck() {
        return stuck;
    }

    public void setStuck(int stuck) {
        this.stuck = stuck;
    }

    public float getClutch() {
        return clutch;
    }

    public void setClutch(float clutch) {
        this.clutch = clutch;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public float getApproachPosition() {
        return approachPosition;
    }

    public void setApproachPosition(float approachPosition) {
        this.approachPosition = approachPosition;
    }

    public float getApproachSensitivity() {
        return approachSensitivity;
    }

    public void setApproachSensitivity(float approachSensitivity) {
        this.approachSensitivity = approachSensitivity;
    }

    public float getApproachCorrection() {
        return approachCorrection;
    }

    public void setApproachCorrection(float approachCorrection) {
        this.approachCorrection = approachCorrection;
    }

    public String getType() {
        if (name.contains("oval")) {
            return "oval";
        }
        else if (name.contains("dirt")) {
            return "dirt";
        }
        else if (name.contains("road")) {
            return "road";
        }
        else {
            return "all";
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Individual other) {
        if (fitness == 0) {
            return 1;
        }
        else if (other.fitness == 0) {
            return -1;
        }
        return new Double(fitness).compareTo(other.getFitness());
    }

    @Override
    public String toString() {
        String string = "gearUp=" + gearUp[0] + "," + gearUp[1] + "," + gearUp[2] + "," + gearUp[3] + "," + gearUp[4] + "," + gearUp[5] + System.getProperty("line.separator");
        string += "gearDown=" + gearDown[0] + "," + gearDown[1] + "," + gearDown[2] + "," + gearDown[3] + "," + gearDown[4] + "," + gearDown[5] + System.getProperty("line.separator");
        string += "stuckTime=" + stuckTime + System.getProperty("line.separator");
        string += "stuckAngle=" + stuckAngle + System.getProperty("line.separator");
        string += "maxSpeedDist=" + maxSpeedDist + System.getProperty("line.separator");
        string += "maxSpeed=" + maxSpeed + System.getProperty("line.separator");
        string += "sin5=" + sin5 + System.getProperty("line.separator");
        string += "cos5=" + cos5 + System.getProperty("line.separator");
        string += "steerLock=" + steerLock + System.getProperty("line.separator");
        string += "steerSensitivityOffset=" + steerSensitivityOffset + System.getProperty("line.separator");
        string += "wheelSensitivityCoeff=" + wheelSensitivityCoeff + System.getProperty("line.separator");
        string += "wheelRadius=" + wheelRadius[0] + "," + wheelRadius[1] + "," + wheelRadius[2] + "," + wheelRadius[3] + System.getProperty("line.separator");
        string += "absSlip=" + absSlip + System.getProperty("line.separator");
        string += "absRange=" + absRange + System.getProperty("line.separator");
        string += "absMinSpeed=" + absMinSpeed + System.getProperty("line.separator");
        string += "clutchMax=" + clutchMax + System.getProperty("line.separator");
        string += "clutchDelta=" + clutchDelta + System.getProperty("line.separator");
        string += "clutchRange=" + clutchRange + System.getProperty("line.separator");
        string += "clutchDeltaTime=" + clutchDeltaTime + System.getProperty("line.separator");
        string += "clutchDeltaRaced=" + clutchDeltaRaced + System.getProperty("line.separator");
        string += "clutchDec=" + clutchDec + System.getProperty("line.separator");
        string += "clutchMaxModifier=" + clutchMaxModifier + System.getProperty("line.separator");
        string += "clutchMaxTime=" + clutchMaxTime + System.getProperty("line.separator");
        string += "approachPosition=" + approachPosition + System.getProperty("line.separator");
        string += "approachSensitivity=" + approachSensitivity + System.getProperty("line.separator");
        string += "approachCorrection=" + approachCorrection + System.getProperty("line.separator");
        return string;
    }
}

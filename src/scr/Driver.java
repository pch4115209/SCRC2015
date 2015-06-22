package scr;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import scr.data.Action;
import scr.data.Individual;

public class Driver extends Controller {

    /* Gear Changing Constants */
    int[] gearUp;
    int[] gearDown;

    /* Stuck constants */
    int stuckTime;
    float stuckAngle; // PI/6

    /* Accel and Brake Constants */
    float maxSpeedDist;
    float maxSpeed;
    float originalMaxSpeed;
    float sin5;
    float cos5;

    /* Steering constants */
    float steerLock;
    float steerSensitivityOffset;
    float wheelSensitivityCoeff;

    /* ABS Filter Constants */
    float wheelRadius[];
    float absSlip;
    float absRange;
    float absMinSpeed;

    /* Clutching Constants */
    float clutchMax;
    float clutchDelta;
    float clutchRange;
    float clutchDeltaTime;
    float clutchDeltaRaced;
    float clutchDec;
    float clutchMaxModifier;
    float clutchMaxTime;

    private int stuck = 0;

    // current clutch
    private float clutch = 0;

    /* New Approach Parameters */
    float approachPosition; // determines where the driver positions itself before and during a corner, where higher
                            // values mean positioning closer to the apex
    float approachSensitivity; // determines how aggressively the driver tries to achieve the desired approach
    float approachCorrection; // determines how a driver recovers when it encounters an "out of bounds" area on the
                              // track

    /* Segmentation Variables */
    private boolean useSegmentation = false;
    private boolean turning = false;
    private boolean firstLap = true;
    private double dist1 = 0;
    private double dist2 = 0;
    private int ticks = 0;
    private int type = 0;
    private ArrayList<TrackSegment> segmentList = new ArrayList<TrackSegment>();
    private int currentSeg = 0; // Calculates what the max speed should be based on tracksegments
    private double currentLapTime;

    /* Collision Avoidance Variables */
    private boolean useCollisionAvoidance = false;

    /*
     * Configure this driver to use the settings of the given individual.
     */
    public void setSettings(Individual settings) {

        gearUp = settings.getGearUp();
        gearDown = settings.getGearDown();
        stuckTime = settings.getStuckTime();
        stuckAngle = settings.getStuckAngle();
        maxSpeedDist = settings.getMaxSpeedDist();
        maxSpeed = settings.getMaxSpeed();
        originalMaxSpeed = settings.getMaxSpeed();
        sin5 = settings.getSin5();
        cos5 = settings.getCos5();
        steerLock = settings.getSteerLock();
        steerSensitivityOffset = settings.getSteerSensitivityOffset();
        wheelSensitivityCoeff = settings.getWheelSensitivityCoeff();
        wheelRadius = settings.getWheelRadius();
        absSlip = settings.getAbsSlip();
        absRange = settings.getAbsRange();
        absMinSpeed = settings.getAbsMinSpeed();
        clutchMax = settings.getClutchMax();
        clutchDelta = settings.getClutchDelta();
        clutchRange = settings.getClutchRange();
        clutchDeltaTime = settings.getClutchDeltaTime();
        clutchDeltaRaced = settings.getClutchDeltaRaced();
        clutchDec = settings.getClutchDec();
        clutchMaxModifier = settings.getClutchMaxModifier();
        clutchMaxTime = settings.getClutchMaxTime();
        approachPosition = settings.getApproachPosition();
        approachSensitivity = settings.getApproachSensitivity();
        approachCorrection = settings.getApproachCorrection();
    }

    public void reset() {
    }

    public void shutdown() {
    }

    private int getGear(SensorModel sensors) {
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();

        // if gear is 0 (N) or -1 (R) just return 1
        if (gear < 1)
            return 1;
        // check if the RPM value of car is greater than the one suggested
        // to shift up the gear from the current one
        if (gear < 6 && rpm >= gearUp[gear - 1])
            return gear + 1;
        else
        // check if the RPM value of car is lower than the one suggested
        // to shift down the gear from the current one
        if (gear > 1 && rpm <= gearDown[gear - 1])
            return gear - 1;
        else
            // otherwhise keep current gear
            return gear;
    }

    private float getSteer(SensorModel sensors) {

        float leftOpp = (float) sensors.getOpponentSensors()[17];
        float rightOpp = (float) sensors.getOpponentSensors()[19];
        float frontOpp = (float) sensors.getOpponentSensors()[18];

        // steering angle is compute by correcting the actual car angle w.r.t. to track
        // axis [sensors.getAngle()] and to adjust car position w.r.t to approach
        float approach = getApproach(sensors);
        float targetAngle = (float) (sensors.getAngleToTrackAxis() - approach);

        // Collision avoidance
        if (useCollisionAvoidance) {
            if (leftOpp > 0 && leftOpp <= 200) { // there is an opponent found at the left
                targetAngle -= (float) 0.1; // turn 10 degree towards right
            }
            else if (rightOpp > 0 && rightOpp <= 200) { // there is an opponent found at the right
                targetAngle += (float) 0.1; // turn 10 degree towards left
            }
            /* Pit Maneuver */
            /* Phase 1 - Detecting opponent in front */
            if (0 < frontOpp && frontOpp <= 200) {
                // Phase 2 - Speed up to the left of this opponent

                targetAngle += (float) 0.1; // turn 10 degrees to the left
                if (leftOpp > 0 && leftOpp <= 200) { // once it reaches the left of this car
                    // Phase 3 - Hard turn on the right when we reach the bottom left corner of this opponent
                    targetAngle -= (float) 0.45; // hard steer to right
                }
            }
        }

        // at high speed reduce the steering command to avoid loosing the control
        if (sensors.getSpeed() > steerSensitivityOffset)
            return (float) (targetAngle / (steerLock * (sensors.getSpeed() - steerSensitivityOffset) * wheelSensitivityCoeff));
        else
            return (targetAngle) / steerLock;
    }

    /*
     * Used to turn off track segment generation.
     */
    public void setUseSegmentation(boolean useSegmentation) {
        this.useSegmentation = useSegmentation;
    }

    public boolean getUseSegmentation() {
        return useSegmentation;
    }

    /*
     * Used to turn off track segmentation for the first lap.
     */
    public void setFirstLap(boolean firstLap) {
        this.firstLap = firstLap;
    }

    public void setSegList(ArrayList<TrackSegment> segList) {
        this.segmentList = segList;
        // for (TrackSegment tS : this.segmentList) {
        // System.out.println(tS.toString());
        // }
    }

    /*
     * Calculate the approach that the driver should take based on the current approach parameters.
     */
    private float getApproach(SensorModel sensors) {
        float approach = 0f;

        // reading of sensor at +5 degree w.r.t. car axis
        float rxSensor = (float) sensors.getTrackEdgeSensors()[10];
        // reading of sensor parallel to car axis
        float sensorsensor = (float) sensors.getTrackEdgeSensors()[9];
        // reading of sensor at -5 degree w.r.t. car axis
        float sxSensor = (float) sensors.getTrackEdgeSensors()[8];

        // stuck on wall or is outside of the track
        if (rxSensor == -1 && sxSensor == -1 && sensorsensor == -1) {
            approach = (float) (sensors.getTrackPosition() * approachCorrection);
        }
        // approaching a turn on right
        else if (rxSensor > sxSensor && rxSensor > sensorsensor) {
            approach = (float) ((sensors.getTrackPosition() + approachPosition) * approachSensitivity);

            // Create segment for right turn
            if (useSegmentation) {
                if (firstLap) {
                    if (sensors.getLastLapTime() != 0.0) {
                        firstLap = false;
                        saveSegmentList(segmentList);
                    }

                    if (turning) {
                        ticks = 0;
                    }

                    // Starting a new turn
                    // Begin ticking and record initial dist
                    if (!turning && ticks == 0) {
                        dist2 = sensors.getDistanceFromStartLine();
                        ticks = 1;
                        type = 1;
                    }
                    else if (!turning && ticks < 60) {
                        ticks++;
                    }
                    else if (!turning && ticks >= 60) {
                        turning = true;
                        ticks = 0;
                        segmentList.add(new TrackSegment(dist2 - dist1, dist1, type));

                        dist1 = dist2;
                    }
                }
            }
        }
        // approaching a turn on left
        else if (sxSensor > rxSensor && sxSensor > sensorsensor) {
            approach = (float) ((sensors.getTrackPosition() - approachPosition) * approachSensitivity);

            // Create segment for left turn
            if (useSegmentation) {
                if (firstLap) {
                    if (sensors.getLastLapTime() != 0.0) {
                        firstLap = false;
                        saveSegmentList(segmentList);

                    }

                    if (turning) {
                        ticks = 0;
                    }

                    // Starting a new turn
                    // Begin ticking and record initial dist
                    if (!turning && ticks == 0) {
                        dist2 = sensors.getDistanceFromStartLine();
                        ticks = 1;
                        type = 2;
                    }
                    else if (!turning && ticks < 60) {
                        ticks++;
                    }
                    else if (!turning && ticks >= 60) {
                        turning = true;
                        ticks = 0;
                        segmentList.add(new TrackSegment(dist2 - dist1, dist1, type));
                        dist1 = dist2;
                    }
                }
            }
        }
        // probably going straight
        else {
            if (useSegmentation) {
                if (firstLap) {
                    if (sensors.getLastLapTime() != 0.0) {
                        firstLap = false;
                        saveSegmentList(segmentList);
                    }

                    if (!turning) {
                        ticks = 0;
                    }

                    if (turning && ticks == 0) {
                        dist2 = sensors.getDistanceFromStartLine();
                        ticks = 1;
                        type = 0;
                    }
                    else if (turning && ticks < 60) {
                        ticks++;
                    }
                    else if (turning && ticks >= 60) {
                        turning = false;
                        ticks = 0;
                        segmentList.add(new TrackSegment(dist2 - dist1, dist1, type));
                        dist1 = dist2;
                    }
                }
            }
        }

        return approach;
    }

    /*
     * Calculates the maximum speed that the driver should adhere to based on the current segment of the track.
     */
    private float getMaxSpeed(SensorModel sensors) {
        TrackSegment seg = segmentList.get(currentSeg);
        // Check if end of segment reached
        if (sensors.getDistanceFromStartLine() > (seg.getStart() + seg.getDistance() * .9)) {
            if (currentSeg + 1 < segmentList.size()) {
                // System.out.println("moving from seg " + currentSeg + " to " + (currentSeg + 1));
                currentSeg++;
            }
            seg = segmentList.get(currentSeg);
        }

        // Check for new lap
        if (sensors.getCurrentLapTime() < currentLapTime) {
            currentSeg = 0;
            // System.out.println("New Lap");
        }
        currentLapTime = sensors.getCurrentLapTime();

        if (currentSeg != 0) {
            if (segmentList.get(currentSeg - 1).getType() == 0) {
                return 400;
            }
            else {
                return originalMaxSpeed;
            }
        }
        else {
            return 400;
        }
    }

    /*
     * Saves the given segment list to a file.
     */
    private void saveSegmentList(ArrayList<TrackSegment> aL) {
        try {
            PrintStream output = new PrintStream(new FileOutputStream("group_3_" + getTrackName() + "_segments.txt"));
            for (TrackSegment tS : aL) {
                output.println(tS.toString());
            }
            output.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private float getAccel(SensorModel sensors) {
        // checks if car is out of track
        if (sensors.getTrackPosition() < 1 && sensors.getTrackPosition() > -1) {
            /* get the opponent in front, -10degree to 10degree */
            float leftOpp = (float) sensors.getOpponentSensors()[17];
            float frontOpp = (float) sensors.getOpponentSensors()[18];
            float rightOpp = (float) sensors.getOpponentSensors()[19];
            // reading of sensor at +5 degree w.r.t. car axis
            float rxSensor = (float) sensors.getTrackEdgeSensors()[10];
            // reading of sensor parallel to car axis
            float sensorsensor = (float) sensors.getTrackEdgeSensors()[9];
            // reading of sensor at -5 degree w.r.t. car axis
            float sxSensor = (float) sensors.getTrackEdgeSensors()[8];
            float targetSpeed;

            // Collision avoidance
            if (useCollisionAvoidance) {
                if (frontOpp > 0 && frontOpp <= 200) { // if there car in front is detected,slow down
                    // current speed is cut half, and wait for road clean up
                    targetSpeed = (float) sensors.getSpeed() / 2;
                }
                else if (leftOpp > 0 && leftOpp < 200) { // current speed increases by 5, and ready to overtake
                    targetSpeed = (float) sensors.getSpeed() + 5;
                }
                else if (rightOpp > 0 && rightOpp < 200) { // current speed increases 5 and ready to overtake
                    targetSpeed = (float) sensors.getSpeed() + 5;
                }
            }

            // If using segmentation, then limit the maximum speed
            if (useSegmentation && !firstLap) {
                maxSpeed = getMaxSpeed(sensors);
            }

            // track is straight and enough far from a turn so goes to max speed
            if (sensorsensor > maxSpeedDist || (sensorsensor >= rxSensor && sensorsensor >= sxSensor))
                targetSpeed = maxSpeed;
            else {
                // approaching a turn on right
                if (rxSensor > sxSensor) {
                    // computing approximately the "angle" of turn
                    float h = sensorsensor * sin5;
                    float b = rxSensor - sensorsensor * cos5;
                    float sinAngle = b * b / (h * h + b * b);
                    // estimate the target speed depending on turn and on how close it is
                    targetSpeed = maxSpeed * (sensorsensor * sinAngle / maxSpeedDist);
                }
                // approaching a turn on left
                else {
                    // computing approximately the "angle" of turn
                    float h = sensorsensor * sin5;
                    float b = sxSensor - sensorsensor * cos5;
                    float sinAngle = b * b / (h * h + b * b);
                    // estimate the target speed depending on turn and on how close it is
                    targetSpeed = maxSpeed * (sensorsensor * sinAngle / maxSpeedDist);
                }

            }

            // }
            // accel/brake command is exponentially scaled w.r.t. the difference between target speed and current one
            return (float) (2 / (1 + Math.exp(sensors.getSpeed() - targetSpeed)) - 1);
        }
        else
            return (float) 0.3; // when out of track returns a moderate acceleration command

    }

    public Action control(SensorModel sensors) {
        // check if car is currently stuck
        if (Math.abs(sensors.getAngleToTrackAxis()) > stuckAngle) {
            // update stuck counter
            stuck++;
        }
        else {
            // if not stuck reset stuck counter
            stuck = 0;
        }

        // after car is stuck for a while apply recovering policy
        if (stuck > stuckTime) {
            /*
             * set gear and sterring command assuming car is pointing in a direction out of track
             */

            // to bring car parallel to track axis
            float steer = (float) (-sensors.getAngleToTrackAxis() / steerLock);
            int gear = -1; // gear R

            // if car is pointing in the correct direction revert gear and steer
            if (sensors.getAngleToTrackAxis() * sensors.getTrackPosition() > 0) {
                gear = 1;
                steer = -steer;
            }
            clutch = clutching(sensors, clutch);
            // build a CarControl variable and return it
            Action action = new Action();
            action.gear = gear;
            action.steering = steer;
            action.accelerate = 1.0;
            action.brake = 0;
            action.clutch = clutch;
            return action;
        }

        else // car is not stuck
        {
            // compute accel/brake command
            float accel_and_brake = getAccel(sensors);
            // compute gear
            int gear = getGear(sensors);
            // compute steering
            float steer = getSteer(sensors);

            // normalize steering
            if (steer < -1)
                steer = -1;
            if (steer > 1)
                steer = 1;

            // set accel and brake from the joint accel/brake command
            float accel, brake;
            if (accel_and_brake > 0) {
                accel = accel_and_brake;
                brake = 0;
            }
            else {
                accel = 0;
                // apply ABS to brake
                brake = filterABS(sensors, -accel_and_brake);
            }

            clutch = clutching(sensors, clutch);

            // build a CarControl variable and return it
            Action action = new Action();
            action.gear = gear;
            action.steering = steer;
            action.accelerate = accel;
            action.brake = brake;
            action.clutch = clutch;
            return action;
        }
    }

    private float filterABS(SensorModel sensors, float brake) {
        // convert speed to m/s
        float speed = (float) (sensors.getSpeed() / 3.6);
        // when spedd lower than min speed for abs do nothing
        if (speed < absMinSpeed)
            return brake;

        // compute the speed of wheels in m/s
        float slip = 0.0f;
        for (int i = 0; i < 4; i++) {
            slip += sensors.getWheelSpinVelocity()[i] * wheelRadius[i];
        }
        // slip is the difference between actual speed of car and average speed of wheels
        slip = speed - slip / 4.0f;
        // when slip too high applu ABS
        if (slip > absSlip) {
            brake = brake - (slip - absSlip) / absRange;
        }

        // check brake is not negative, otherwise set it to zero
        if (brake < 0)
            return 0;
        else
            return brake;
    }

    float clutching(SensorModel sensors, float clutch) {

        float maxClutch = clutchMax;

        // Check if the current situation is the race start
        if (sensors.getCurrentLapTime() < clutchDeltaTime && getStage() == Stage.RACE && sensors.getDistanceRaced() < clutchDeltaRaced)
            clutch = maxClutch;

        // Adjust the current value of the clutch
        if (clutch > 0) {
            double delta = clutchDelta;
            if (sensors.getGear() < 2) {
                // Apply a stronger clutch output when the gear is one and the race is just started
                delta /= 2;
                maxClutch *= clutchMaxModifier;
                if (sensors.getCurrentLapTime() < clutchMaxTime)
                    clutch = maxClutch;
            }

            // check clutch is not bigger than maximum values
            clutch = Math.min(maxClutch, clutch);

            // if clutch is not at max value decrease it quite quickly
            if (clutch != maxClutch) {
                clutch -= delta;
                clutch = Math.max((float) 0.0, clutch);
            }
            // if clutch is at max value decrease it very slowly
            else
                clutch -= clutchDec;
        }
        return clutch;
    }

    public float[] initAngles() {

        float[] angles = new float[19];

        /* set angles as {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90} */
        for (int i = 0; i < 5; i++) {
            angles[i] = -90 + i * 15;
            angles[18 - i] = 90 - i * 15;
        }

        for (int i = 5; i < 9; i++) {
            angles[i] = -20 + (i - 5) * 5;
            angles[18 - i] = 20 - (i - 5) * 5;
        }
        angles[9] = 0;
        return angles;
    }
}

package szum.mthesis.indorpositiontracker.utils;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import szum.mthesis.indorpositiontracker.OrientationTracker;
import szum.mthesis.indorpositiontracker.orm.BleSample;
import szum.mthesis.indorpositiontracker.orm.GpsLocation;
import szum.mthesis.indorpositiontracker.orm.Step;

/**
 * Created by blazej on 27/04/16.
 */
public class TrackingData {

    private boolean initialized = false;

    private List<GpsLocation> gpsLocations;
    private Location lastLocation;
    private float currentAccuracy = 0;
    private double walkDistance = 0; // [m]
    private long startWalkingTime = 0; // [ns]
    private long currWalkTime = 0; // [ns]

    private int stepCount = 0;
    private List<Step> steps;

    private ArrayList<BleSample> bleSamples;

    private OrientationTracker orientTrckr = new OrientationTracker();

    public void initialize() {
        getOrientTrckr().init();
        initialized = true;
    }

    public void addBleSample(BleSample bleSample) {
        bleSamples.add(bleSample);
    }


    public Step addStep(long timestamp) {
        Step step = new Step(timestamp / 1000000, orientTrckr.getCurrentOrientation());
        steps.add(step);
        return step;
    }

    public void incrementStepCounter() {
        stepCount++;
    }

    public int getStepCount() {
        return stepCount;
    }

    public OrientationTracker getOrientTrckr() {
        return orientTrckr;
    }

    public void reset() {
        initialized = false;
        bleSamples = new ArrayList<>();
        gpsLocations = new ArrayList<>();
        steps = new ArrayList<>();
        stepCount = 0;
        startWalkingTime = 0; // [ns]
        currWalkTime = 0; // [ns]
        walkDistance = 0;
        currentAccuracy = 0;
        lastLocation = null;
        orientTrckr.reset();
    }

    public String getInfoText(){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Walk distance: %.1f m \n",walkDistance));
        builder.append(String.format("Step count: %d  \n",stepCount));
        builder.append(String.format("Walk time: %d s \n",getWalkTime()));
        builder.append(String.format("Estimated orientation correction: %.1f* \n", orientTrckr.getOrientationCorrection()));
        builder.append(String.format("GPS Accuracy: %.3f \n", currentAccuracy));
        return builder.toString();
    }

    public List<Step> getSteps() {
        return steps;
    }

    public List<GpsLocation> getGpsLocations() {
        return gpsLocations;
    }

    public ArrayList<BleSample> getBleSamples() {
        return bleSamples;
    }

    public long getWalkTime() {
        return (currWalkTime - startWalkingTime)/1000000000; // from ns to s
    }

    public double getWalkDistance() {
        return walkDistance;
    }

    public boolean isNotWalking(){
        return startWalkingTime <= 0;
    }

    public void setStartWalkingTime(long startWalkingTime) {
        this.startWalkingTime = startWalkingTime;
    }

    public void setCurrWalkTime(long currWalkTime) {
        this.currWalkTime = currWalkTime;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void processNewLocation(Location location) {
        currentAccuracy = location.getAccuracy();
        addToWalkDistance(location);
        addGpsPoint(location);
        orientTrckr.processLocation(location);
    }

    private void addToWalkDistance(Location location) {
        if(lastLocation != null){
            walkDistance = walkDistance + lastLocation.distanceTo(location);
        }
        lastLocation = location;
    }

    private  void addGpsPoint(Location location) {
        gpsLocations.add(new GpsLocation(location, orientTrckr.getRealBearing(), orientTrckr.getRelativeBearing()));
    }
}

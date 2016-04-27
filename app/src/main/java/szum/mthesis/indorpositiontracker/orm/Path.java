package szum.mthesis.indorpositiontracker.orm;

import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blazej on 25/04/16.
 */
public class Path extends SugarRecord {

    long time;
    double startLongitude;
    double startLatitude;
    long stepCount;
    long orientationCorrection;
    long walkTime; // [ms]
    double walkDistance; //[m]
    double avgAccuracy;

    public Path() {}

    public Path(long time, double startLongitude, double startLatitude, long stepCount, long orientationCorrection, long walkTime, double walkDistance, double avgAccuracy) {
        this.time = time;
        this.startLongitude = startLongitude;
        this.startLatitude = startLatitude;
        this.stepCount = stepCount;
        this.orientationCorrection = orientationCorrection;
        this.walkTime = walkTime;
        this.walkDistance = walkDistance;
        this.avgAccuracy = avgAccuracy;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public long getStepCount() {
        return stepCount;
    }

    public void setStepCount(long stepCount) {
        this.stepCount = stepCount;
    }

    public long getOrientationCorrection() {
        return orientationCorrection;
    }

    public void setOrientationCorrection(long orientationCorrection) {
        this.orientationCorrection = orientationCorrection;
    }

    public long getWalkTime() {
        return walkTime;
    }

    public void setWalkTime(int walkTime) {
        this.walkTime = walkTime;
    }

    public double getWalkDistance() {
        return walkDistance;
    }

    public void setWalkDistance(double walkDistance) {
        this.walkDistance = walkDistance;
    }

    public double getAvgAccuracy() {
        return avgAccuracy;
    }

    public void setAvgAccuracy(double avgAccuracy) {
        this.avgAccuracy = avgAccuracy;
    }

    public void deletePathAndSubsequentData() {
        Step.deleteAll(Step.class, "path_id = ?", getId().toString());
        GpsLocation.deleteAll(GpsLocation.class, "path_id = ?", getId().toString());
        BleSample.deleteAll(BleSample.class, "path_id = ?", getId().toString());
        delete();
    }

    public List<Step> getSteps() {
        return find(Step.class, "path_id = ?", getId().toString());
    }

    public List<GpsLocation> getGpsPoints() {
        return find(GpsLocation.class, "path_id = ?", getId().toString());
    }

    public List<LatLng> getGpsPointsLatLng() {
        List<LatLng> poly = new ArrayList<>();
        List<GpsLocation> gpss = getGpsPoints();
        for(GpsLocation gps : gpss){
            poly.add(new LatLng(gps.getLatitude(), gps.getLongitude()));
        }

        return poly;
    }

    public List<BleSample> getBleSamples() {
        return find(BleSample.class, "path_id = ?", getId().toString());
    }
}

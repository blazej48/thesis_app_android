package szum.mthesis.indorpositiontracker.entities;

import android.location.Location;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

/**
 * Created by blazej on 25/04/16.
 */
public class GpsLocation extends SugarRecord{

    long pathId;
    long time;
    double latitude;
    double longitude;
    double accuracy;
    double realBearing;
    double relativeBearing;

    @Ignore Location mLocation;

    public GpsLocation(){}

    public GpsLocation(Location location, double realBearing, double relativeBearing){
        this.realBearing = realBearing;
        this.relativeBearing = relativeBearing;
        setmLocation(location);
    }

    public void setmLocation(Location mLocation) {
        this.mLocation = mLocation;
        latitude = mLocation.getLatitude();
        longitude = mLocation.getLongitude();
        accuracy = mLocation.getAccuracy();
        time = getElapsedRealtimeMilis();
    }

    public long getPathId() {
        return pathId;
    }

    public void setPathId(long pathId) {
        this.pathId = pathId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getRealBearing() {
        return realBearing;
    }

    public void setRealBearing(double realBearing) {
        this.realBearing = realBearing;
    }

    public double getRelativeBearing() {
        return relativeBearing;
    }

    public void setRelativeBearing(double relativeBearing) {
        this.relativeBearing = relativeBearing;
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Location getmLocation() {
        return mLocation;
    }

    public long getElapsedRealtimeMilis() {
        return mLocation.getElapsedRealtimeNanos()/1000000;
    }

}

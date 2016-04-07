package szum.mthesis.indorpositiontracker;

import android.location.Location;

/**
 * Created by blazej on 21/03/16.
 */
public class MyLocation {

    private Location mLocation;
    private float realBearing;
    private float relativeBearing;

    public MyLocation(Location location) {
        mLocation = location;
    }

    public MyLocation(Location location, float realBearing, float relativeBearing) {
        this.mLocation = location;
        this.realBearing = realBearing;
    }

    public double getLongitude() {
        return mLocation.getLongitude();
    }

    public double getLatitude() {
        return mLocation.getLatitude();
    }

    public double getAccuracy() {
        return mLocation.getAccuracy();
    }

    public float getRealBearing() {
        return realBearing;
    }

    public void setRealBearing(float realBearing) {
        this.realBearing = realBearing;
    }

    public float getRelativeBearing() {
        return relativeBearing;
    }

    public void setRelativeBearing(float relativeBearing) {
        this.relativeBearing = relativeBearing;
    }

    public double getElapsedRealtimeMilis() {
        return mLocation.getElapsedRealtimeNanos()/1000000;
    }
}

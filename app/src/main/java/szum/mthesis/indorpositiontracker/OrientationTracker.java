package szum.mthesis.indorpositiontracker;

import android.location.Location;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import szum.mthesis.indorpositiontracker.utils.Utils;

/**
 * Created by blazej on 20/03/16.
 */
public class OrientationTracker {

    private static final String TAG = OrientationTracker.class.getSimpleName();

    private Vector3D initGravityVector;
    private Vector3D initMagneticVector;

    private Vector3D currentGravityVector;
    private Vector3D currentMagneticVector;

    private Location prevLocation;
    private int samplesCount = 0;
    private float Sdelta = 0;

    private float realBearing = 0;
    private float relativeBearing = 0;

    public void init() {
        this.initGravityVector = currentGravityVector;
        this.initMagneticVector = currentMagneticVector;
    }

    public void setCurrentGravityVector(Vector3D currentGravityVector) {
        this.currentGravityVector = currentGravityVector;
    }

    public void setCurrentMagneticVector(Vector3D currentMagneticVector) {
        this.currentMagneticVector = currentMagneticVector;
    }

    public void processLocation(Location newLocation){
        if(prevLocation != null){

            realBearing = prevLocation.bearingTo(newLocation);
            relativeBearing = (float)getCurrentOrientation();
            float delta = relativeBearing - realBearing;
            Sdelta = Sdelta + delta;
            samplesCount++;
            Logger.d(TAG, String.format("Computed map bearing: %.1f, computed relative bearing: %.1f, delta: %.1f, curr orientation correction: %.1f",
                    realBearing, relativeBearing, delta, getOrientationCorrection()));
        }

        prevLocation = newLocation;
    }

    public float getOrientationCorrection(){
        if(samplesCount == 0){return 0;}
        return Sdelta/samplesCount;
    }

    public int getCurrentOrientation() {
        if(initGravityVector == null ||
                initMagneticVector == null ||
                currentGravityVector == null ||
                currentMagneticVector == null ){
            Logger.w(TAG,"unable to determine current orientation");
            return 0;
        }
        return (int) Utils.getOrientation(currentMagneticVector, currentGravityVector, initMagneticVector, initGravityVector);
    }

    public void reset() {
        initGravityVector = null;
        initMagneticVector = null;
        currentGravityVector = null;
        currentMagneticVector = null;

        prevLocation = null;
        samplesCount = 0;
        Sdelta = 0;

        realBearing = 0;
        relativeBearing = 0;
    }

    public float getRealBearing() {
        return realBearing;
    }

    public float getRelativeBearing() {
        return relativeBearing;
    }
}

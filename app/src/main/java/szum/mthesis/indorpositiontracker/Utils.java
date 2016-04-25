package szum.mthesis.indorpositiontracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import szum.mthesis.indorpositiontracker.entities.GpsLocation;
import szum.mthesis.indorpositiontracker.entities.Step;

/**
 * Created by blazej on 2/26/2016.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static final String USE_STEPS_ESTIMATION = "use_steps_estimation";
    public static final String CALIBRATION_RATE = "calibration_rate";

    public static final int AVG_STEP_DURATION_FACTOR = 9;

    public static final double TO_RAD = Math.PI / 180;
    public static final double TO_DEG = 180 / Math.PI;
    public static final double R = 6378137;

    public static double getCalibrationRate(Context context){
        double rate = getPref(context, CALIBRATION_RATE, 1);
        if(rate <=0 && rate > 1){
            return 1;
        }
        return rate;
    }

    public static double getPref(Context context, String key, double defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPref.getString(key, Double.toString(defaultValue));
        return Double.parseDouble(value);
    }

    public static boolean getPref(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(key, defaultValue);
    }


    public static void estimateStepLengths(List<Step> Steps, double avgStepLength) {
        filterStepsDurations(Steps);
        double avgTime = Steps.get(Steps.size()-1).getStepTime() - Steps.get(0).getStepTime();
        avgTime = avgTime/(Steps.size() -1 );

        // y = -2.9051x + 2.241;
        double a = -0.0026;
        double b = avgStepLength - avgTime*a;

        // compute step_lengths based on step_duration
        double stepLength;
        double stepDuration;
        for(Step step : Steps){
            stepDuration = (double)step.getmStepDuration();
                if(stepDuration < 650 && stepDuration > 400){
                    stepLength = a * stepDuration + b;
//                      stepLength = -0.0000827*stepDuration*stepDuration + 0.082272*stepDuration - 19.581;
//                    stepLength = -0.0026*stepDuration + 2.1129;
                }else{
                    stepLength = avgStepLength;
                }
            step.setmStepLength(stepLength);
        }
    }

    private static void filterStepsDurations(List<Step> Steps) {
        long avgStepDuration = Steps.get(Steps.size() - 1 ).getStepTime() - Steps.get(0).getStepTime();
        avgStepDuration = avgStepDuration/(Steps.size() -1 );

        Step prev = null;
        long duration;
        Steps.get(0).setmStepDuration(avgStepDuration);
        for(Step Step : Steps){ // calculate step duration of every step
            if(prev != null){
                duration = Step.getStepTime() - prev.getStepTime();
                if(duration > 1000){ // get rid of false steps
                    Step.setmStepDuration(avgStepDuration);
                }else{
                    Step.setmStepDuration(duration);
                }
            }
            prev = Step;
        }

        if(Steps.size() < AVG_STEP_DURATION_FACTOR){
            Logger.e(TAG, "filterStepsDurations - not enought");
        }

        long sum = 0;
        int first = 0, last = 0;
        for(; first < AVG_STEP_DURATION_FACTOR; first++){
            sum = sum + Steps.get(first).getmStepDuration();
        }

        int offset = (AVG_STEP_DURATION_FACTOR-1) / 2;
        int current = offset;

        for(;first < (Steps.size()-1);){
            Steps.get(current).setmStepDuration( sum / AVG_STEP_DURATION_FACTOR );
            sum = sum - Steps.get(last).getmStepDuration();
            sum = sum + Steps.get(first).getmStepDuration();

            last++;
            first++;
            current++;
        }
    }

    public static LatLng calcNextStep(LatLng prevStep, int degree, double stepLenght) {

        double rads = ((double) degree) * TO_RAD;

        //offsets in meters
        double dn = stepLenght * Math.cos(rads);
        double de = stepLenght * Math.sin(rads);

        //Coordinate offsets in radians
        double dLat = dn / R;
        double dLon = de / (R * Math.cos(Math.PI * prevStep.latitude / 180));

        //OffsetPosition, decimal degrees
        double latO = prevStep.latitude + dLat * 180 / Math.PI;
        double lonO = prevStep.longitude + dLon * 180 / Math.PI;
        return new LatLng(latO, lonO);
    }

    public static String VtoS(Vector3D vector){
        return String.format("[%.3f,%.3f,%.3f]", vector.getX(), vector.getY(), vector.getZ());
    }

    public static double getOrientation(Vector3D currMag, Vector3D currGravity, Vector3D initMag, Vector3D initGravity){

        Vector3D mg = currMag.normalize().crossProduct(currGravity.normalize());
        Vector3D currNorthVector = currGravity.normalize().crossProduct(mg.normalize());

        Vector3D mginit = initMag.normalize().crossProduct(initGravity.normalize());
        Vector3D initNorthVector = initGravity.normalize().crossProduct(mginit.normalize());

        Vector3D ng1 = initNorthVector.normalize().crossProduct(initGravity.normalize());
        Vector3D ng2 = currNorthVector.normalize().crossProduct(initGravity.normalize());
        double angbnn = Math.acos(ng1.normalize().dotProduct(ng2.normalize()))*TO_DEG;

        Vector3D g2 = initNorthVector.normalize().crossProduct(currNorthVector.normalize());
        if(0 == g2.getNorm()){
            return 0;
        }
        double gangle =  Math.acos(initGravity.normalize().dotProduct(g2.normalize()))*TO_DEG;

        if(gangle>90){
            angbnn = angbnn*(-1);
        }

        return angbnn;
    }


    public static void estimateBearingAndStepLenght( List<GpsLocation> myRealRoute, List<Step> mRealSteps, Context context, double[] results, boolean useStepModel ){

        if(myRealRoute != null && myRealRoute.size() < 2){
            Logger.e(TAG, "real route is too short for estimating bearing and step lengths");
            results[0] = 0;
            results[1] = 1;
            return;
        }
        double rate = getCalibrationRate(context);
        List<GpsLocation> routeForCalibration = myRealRoute.subList(0, (int) Math.floor(myRealRoute.size() * rate));

        long myLastTime = routeForCalibration.get(routeForCalibration.size() - 1).getTime();
        int stepid = 0;
        for(; stepid < mRealSteps.size();stepid++){
            if(mRealSteps.get(stepid).getStepTime() > myLastTime){
                break;
            }
        }
        List<Step> mStepsListForCalibration;
        if(stepid == 0){
            mStepsListForCalibration = mRealSteps;
        }else{
            mStepsListForCalibration = mRealSteps.subList(0,stepid);
        }

        if(mStepsListForCalibration.size() < 2){
            Logger.e(TAG, "the size of the list is not enougth" );
        }

        double relativeResults[] = new double[2];
        estimateRelativeBearingAndStepCount(mStepsListForCalibration, relativeResults);
        if(useStepModel){
            results[0] = estimateRealBearing(routeForCalibration) - estimateRelativeBearing(mStepsListForCalibration); // bearing
            results[1] = estimateRealDistance(routeForCalibration) / relativeResults[1];
        }else{
            results[0] = estimateRealBearing(routeForCalibration) - relativeResults[0];
            results[1] = estimateRealDistance(routeForCalibration) / relativeResults[1]; // step length
        }
    }

    public static double estimateRealDistance(List<GpsLocation> realRoute){
        if(realRoute == null || realRoute.size() < 2){
            Logger.e(TAG, "too short route while computing real distance");
            return 1;
        }
        Location begining = new Location("no provider");
        begining.setLatitude(realRoute.get(0).getLatitude());
        begining.setLongitude(realRoute.get(0).getLongitude());

        Location end = new Location("no provider");
        end.setLatitude(realRoute.get(realRoute.size() - 1).getLatitude());
        end.setLongitude(realRoute.get(realRoute.size() - 1).getLongitude());

        double distance = (double)begining.distanceTo(end);
        Logger.d(TAG, "Real distance: " + distance);
        return distance;
    }


    public static double estimateRealBearing(List<GpsLocation> realRoute){
        if(realRoute == null || realRoute.size() < 2){
            Logger.e(TAG, "too short route while computing real bearing");
            return 0;
        }
        Location begining = new Location("no provider");
        begining.setLatitude(realRoute.get(0).getLatitude());
        begining.setLongitude(realRoute.get(0).getLongitude());

        Location end = new Location("no provider");
        end.setLatitude(realRoute.get(realRoute.size() - 1 ).getLatitude());
        end.setLongitude(realRoute.get(realRoute.size() - 1).getLongitude());

        double bearing = (double)begining.bearingTo(end);
        Logger.d(TAG, "Real bearing: " + bearing);
        return bearing;
    }

    public static void estimateRelativeBearingAndStepCount(List<Step> steps, double[] results){
        if(steps == null || steps.size() < 2){
            Logger.e(TAG, "too short steps list while computing relative bearing");
            return;
        }

        double x = 0;
        double y = 0;
        double degree;

        for(Step step : steps){
            degree = step.getOrientation();
            x =  x + 1 * Math.sin(degree * TO_RAD);
            y  = y + 1 * Math.cos(degree * TO_RAD);
        }
        double bearing = Math.atan(x/y) * TO_DEG;
        if(y<0){
            bearing = bearing + 180;
        }
        Logger.d(TAG, "Relative bearing: " + bearing);
        double stepsCount = Math.sqrt(x*x + y*y);
        Logger.d(TAG, "Relative steps count: " + stepsCount);
        results[0] = bearing;
        results[1] = stepsCount;
    }


    public static double estimateRelativeBearing(List<Step> steps){
        if(steps == null || steps.size() < 2){
            Logger.e(TAG, "too short steps list while computing relative bearing");
            return 0;
        }

        double x = 0;
        double y = 0;
        double degree;

        for(Step step : steps){
            degree = step.getOrientation();
            x =  x + step.getmStepLength() * Math.sin(degree * TO_RAD);
            y  = y + step.getmStepLength() * Math.cos(degree * TO_RAD);
        }
        double bearing = Math.atan(x/y) * TO_DEG;
        if(y<0){
            bearing = bearing + 180;
        }
        Logger.d(TAG, "Relative bearing(with step model): " + bearing);
        return bearing;
    }
}

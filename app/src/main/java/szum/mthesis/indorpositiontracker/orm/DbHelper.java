package szum.mthesis.indorpositiontracker.orm;

import com.orm.SugarRecord;

import java.util.Calendar;
import java.util.List;

import szum.mthesis.indorpositiontracker.Logger;
import szum.mthesis.indorpositiontracker.utils.TrackingData;

/**
 * Created by blazej on 25/04/16.
 */
public class DbHelper {

    private static final String TAG = DbHelper.class.getSimpleName();

    public static void saveRoute(TrackingData trackingData){
        List<Step> stepsList = trackingData.getSteps();
        List<BleSample> bleSamples = trackingData.getBleSamples();
        List<GpsLocation> locations = trackingData.getGpsLocations();
        int stepsCount = trackingData.getStepCount();
        long walkTime = trackingData.getWalkTime();
        long walkDistance = (long)trackingData.getWalkDistance();
        long rotatCorr = (long) trackingData.getOrientTrckr().getOrientationCorrection();

        if (stepsList == null || stepsList.size() < 1) {
            Logger.w(TAG, "saving route: stepList is empty");
            return;
        }
        if (locations == null || locations.size() < 1) {
            Logger.w(TAG, "saving route: locations list is empty");
            return;
        }

        Logger.d(TAG, "saving route, steps: " + stepsList.size() + ", route: " + locations.size());

        float avgAccuracy = 0;

        for (GpsLocation location : locations) {
            avgAccuracy = avgAccuracy + (float) location.getAccuracy();
        }
        avgAccuracy = avgAccuracy / locations.size();

        GpsLocation startPoint = locations.get(0);

        Path path = new Path(
                Calendar.getInstance().getTimeInMillis(),
                (float) startPoint.getLatitude(),
                (float) startPoint.getLongitude(),
                stepsCount, rotatCorr, walkTime, walkDistance, avgAccuracy);

        path.save();

        long pathId = path.getId();

        for (Step object : stepsList) {
            object.setPathId(pathId);
            SugarRecord.save(object);
        }

        for (GpsLocation object : locations) {
            object.setPathId(pathId);
            SugarRecord.save(object);
        }

        for (BleSample object : bleSamples) {
            object.setPathId(pathId);
            SugarRecord.save(object);
        }
    }

}

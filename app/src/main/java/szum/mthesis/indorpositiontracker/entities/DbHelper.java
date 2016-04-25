package szum.mthesis.indorpositiontracker.entities;

import java.util.Calendar;
import java.util.List;

import szum.mthesis.indorpositiontracker.Logger;

/**
 * Created by blazej on 25/04/16.
 */
public class DbHelper {

    private static final String TAG = DbHelper.class.getSimpleName();

    public static void saveRoute(List<Step> stepsList, List<GpsLocation> locations, int stepsCount,
                                       long walkTime, long walkDistance, long rotatCorr) {

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
            Step.save(object);
        }

        for (GpsLocation object : locations) {
            object.setPathId(pathId);
            Step.save(object);
        }
    }

}

package szum.mthesis.indorpositiontracker;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import szum.mthesis.indorpositiontracker.entities.GpsLocation;
import szum.mthesis.indorpositiontracker.entities.Step;

/**
 * Created by blazej on 4/1/2016.
 */
public class StepPathUtils {

    public enum PathType{
        SIMPLE_ESTIMATION,
        USING_STEP_MODEL
    }

    public static PolylineOptions computeStepRoute(PathType pathType, List<Step> steps, List<GpsLocation> gps, Context context){

        PolylineOptions stepsRoute = new PolylineOptions();
        stepsRoute.geodesic(true);

        // path color
        switch (pathType){
            case SIMPLE_ESTIMATION:
                stepsRoute.color(Color.GREEN);
                break;
            case USING_STEP_MODEL:
                stepsRoute.color(Color.YELLOW);
                break;
        }

        // stepsLength
        double results[] = new double[2];
        Utils.estimateBearingAndStepLenght(gps, steps, context, results, false);
        int estBear = (int)results[0];
        double estLngth = results[1];
        switch (pathType){
            case SIMPLE_ESTIMATION:
                for(Step step : steps){
                    step.setmStepLength(estLngth);
                }
                break;
            case USING_STEP_MODEL:
                Utils.estimateStepLengths(steps, estLngth);
                Utils.estimateBearingAndStepLenght(gps, steps, context, results, true);
                estBear = (int)results[0];
                break;
        }

        // iterating over steps and creating path on map
        LatLng myStep = new LatLng(gps.get(0).getLatitude(),gps.get(0).getLongitude());
        stepsRoute.add(myStep);
        for (Step step : steps){
            myStep = Utils.calcNextStep(myStep, step.getOrientation() + estBear, step.getmStepLength());
            stepsRoute.add(myStep);
        }
        return stepsRoute;
    }
}

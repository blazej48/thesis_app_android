package szum.mthesis.indorpositiontracker;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Created by blazej on 4/1/2016.
 */
public class StepPathUtils {

    public enum PathType{
        SIMPLE_ESTIMATION,
        USING_STEP_MODEL
    }

    public static PolylineOptions computeStepRoute(PathType pathType, List<StepData> steps, List<MyLatLng> gps, Context context){

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
                for(StepData step : steps){
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
        LatLng myStep = new LatLng(gps.get(0).getLat(),gps.get(0).getLng());
        stepsRoute.add(myStep);
        for (StepData step : steps){
            myStep = Utils.calcNextStep(myStep, step.getmStepOrientation() + estBear, step.getmStepLength());
            stepsRoute.add(myStep);
        }
        return stepsRoute;
    }
}

package szum.mthesis.indorpositiontracker.utils;

import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import szum.mthesis.indorpositiontracker.Logger;
import szum.mthesis.indorpositiontracker.orm.Beacon;
import szum.mthesis.indorpositiontracker.orm.BleSample;
import szum.mthesis.indorpositiontracker.orm.Path;

/**
 * Created by blazej on 2/26/2016.
 */
public class BeaconUtils {

    public static final String TAG = BeaconUtils.class.getSimpleName();


    public static void getAdjustmentPoints(Path path, int treashold) {

        List<BleSample> samples = path.getBleSamples();
        List<Beacon> beacons = Beacon.listAll(Beacon.class);

        Map<String, List<BleSample>> lists = new HashMap<>();
        Map<String, Beacon> beaconLists = new HashMap<>();

        // init map of lists
        for(Beacon beacon : beacons){
            lists.put(beacon.getName(), new ArrayList<BleSample>());
            beaconLists.put(beacon.getName(), beacon);
        }

        // group samples of the same device in the same lists
        // filter samples with too weak signal
        List tempList;
        for(BleSample sample : samples){
            tempList = lists.get(sample.getDeviceName());
            if(tempList != null && sample.getSignalStrength() > treashold){
                tempList.add(sample);
            }
        }

        List<Pair<Beacon,Long>> adjustmentPoints = new ArrayList<>();

        for(List<BleSample> list : lists.values()) {
            if (list.size() > 5) {
                long timeSum = 0;
                long sigSum = 0;
                for (BleSample sample : list) {
                    timeSum = timeSum + sample.getTimestamp() * Math.abs(treashold - sample.getSignalStrength());
                    sigSum = sigSum + Math.abs(treashold - sample.getSignalStrength());
                }
                long timeOfClosestPositionToBeacon = timeSum / sigSum;
                adjustmentPoints.add(new Pair<>(beaconLists.get(list.get(0).getDeviceName()), timeOfClosestPositionToBeacon));
            }
        }

        for(Pair<Beacon,Long> pair : adjustmentPoints){
            Logger.d(TAG, "dev: " +  pair.getFirst() + ", timestamp: " + pair.getSecond());
        }
    }

}
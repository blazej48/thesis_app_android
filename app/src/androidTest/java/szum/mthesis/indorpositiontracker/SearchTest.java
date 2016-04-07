package szum.mthesis.indorpositiontracker;

/**
 * Created by blazej on 05/03/16.
 */

import android.util.Pair;
import java.util.ArrayList;
import junit.framework.Assert;

import org.junit.Test;

public class SearchTest extends junit.framework.TestCase {

    @Test
    public void searchingAlg() {
        TrackingService service  = new TrackingService();

        ArrayList<Pair<Long, Integer>> array = new ArrayList<>();

        Pair<Long,Integer> pair = new Pair<>(30l,5);

        array.add(new Pair<>(10l,5));
        array.add(new Pair<>(20l,5));
        array.add(pair);
        array.add(new Pair<>(40l,5));
        array.add(new Pair<>(50l,5));

        Assert.assertEquals(service.getClosestOrientationSample(array, 32l), pair);
    }
}
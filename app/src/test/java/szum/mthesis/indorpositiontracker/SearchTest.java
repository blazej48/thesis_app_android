package szum.mthesis.indorpositiontracker;

import org.junit.Test;

import android.util.Pair;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;

public class SearchTest {

    @Test
    public void searchingAlg() {
        TrackingService service  = new TrackingService();

        ArrayList<Pair<Long, Integer>> array = new ArrayList<>();

        Pair<Long,Integer> pair = new Pair<>(30l,5);

        array.add(new Pair<>(10l,5));
        array.add(new Pair<>(20l,5));
        array.add(pair);
        array.add(new Pair<>(40l, 5));
        array.add(new Pair<>(50l,5));

        assertEquals(service.getClosestOrientationSample(array, 32l), pair);
    }
}
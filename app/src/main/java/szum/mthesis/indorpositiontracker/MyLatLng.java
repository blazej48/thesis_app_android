package szum.mthesis.indorpositiontracker;

/**
 * Created by blazej on 23/03/16.
 */
public class MyLatLng {

    private double lat;
    private double lng;
    private long timestamp;

    public MyLatLng(double lat, double lng, long timestamp) {
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

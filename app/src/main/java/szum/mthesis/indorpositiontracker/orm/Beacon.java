package szum.mthesis.indorpositiontracker.orm;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blazej on 4/4/2016.
 */
public class Beacon extends SugarRecord {

    double lat;
    double lng;
    String name;

    public Beacon() {}

    public Beacon(String name, double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<MarkerOptions> getMarkersForBeacons(){

        List<MarkerOptions> markers = new ArrayList<>();
        for(Beacon beacon : listAll(Beacon.class)){
            markers.add(new MarkerOptions().position(new LatLng(beacon.getLat(), beacon.getLng())).title(beacon.getName())
            );
        }
        return markers;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }

    @Override
    public String toString() {
        return "Beacon{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", name='" + name + '\'' +
                '}';
    }

    //    generated equals and hashCode for placing Beacon it HashMap
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Beacon beacon = (Beacon) o;

        if (Double.compare(beacon.lat, lat) != 0) return false;
        if (Double.compare(beacon.lng, lng) != 0) return false;
        return !(name != null ? !name.equals(beacon.name) : beacon.name != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lng);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}

package szum.mthesis.indorpositiontracker.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import szum.mthesis.indorpositiontracker.R;
import szum.mthesis.indorpositiontracker.orm.Beacon;


public class BeaconsMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = BeaconsMapFragment.class.getSimpleName();

    private GoogleMap mGoogleMap;
    private MapView mMapView;

    public BeaconsMapFragment(){
        super();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        return inflater.inflate(R.layout.beacons_map_fragment, container, false);
    }

    /** Called when the activity is first created. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMapView = (MapView)getView().findViewById(R.id.mapview2);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

    }

    private void refreshMap() {
        mGoogleMap.clear();
        for(MarkerOptions marker : Beacon.getMarkersForBeacons()) {
            mGoogleMap.addMarker(marker.draggable(true));
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "on start activity");
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mGoogleMap = googleMap;

        refreshMap();

        List<Beacon> beacons = Beacon.listAll(Beacon.class);
        if(beacons.isEmpty() == false) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(beacons.get(0).getLatLng(), 18));
        }

        mGoogleMap.setOnMarkerDragListener(
                new GoogleMap.OnMarkerDragListener() {


            @Override
            public void onMarkerDragStart(Marker markerDragStart) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                String name = marker.getTitle().toString();
                List<Beacon> results = Beacon.find(Beacon.class, "name = ? ", name);
                if(results.isEmpty()){return;}
                Beacon beacon = results.get(0);
                beacon.setLat(marker.getPosition().latitude);
                beacon.setLng(marker.getPosition().longitude);
                beacon.save();
                refreshMap();
            }

            @Override
            public void onMarkerDrag(Marker markerDrag) {
            }

        });
    }
}

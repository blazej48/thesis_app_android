package szum.mthesis.indorpositiontracker;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * This fragment displays information about current route, it adds listener to
 * GpsService to get notified about data changed
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private Handler mHandler;
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Button mRefreshButton;

    private static final String TAG = TrackerFragment.class.getSimpleName();
    private RunInfo mLastRunInfo;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        return inflater.inflate(R.layout.map_fragment, container, false);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();

        mHandler = new Handler(getActivity().getMainLooper());

        mMapView = (MapView) view.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mRefreshButton = (Button) view.findViewById(R.id.refresh);
        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLastRunInfo != null){
                    updateMap(mLastRunInfo);
                }
            }
        });
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

    public void updateMap(RunInfo runInfo) {
        mLastRunInfo = runInfo;

        Database database = new Database();
        final PolylineOptions realRoute = database.getRoute(runInfo.getId());
        realRoute.color(Color.BLUE);
        List<StepData> steps = database.getSteps(runInfo.getId());
        List<MyLatLng> gps = database.getMyRoute(runInfo.getId());
        database.close();

        // security checks
        if(realRoute.getPoints().size() <= 2){
           Logger.e(TAG, "the realroute is too short");
            return;
        }
        if(steps.size() <= 2){
            Logger.e(TAG, "the steps list is too short");
            return;
        }
        if(gps.size() <= 2){
            Logger.e(TAG, "the gps list is too short");
            return;
        }


        final PolylineOptions stepsRoute = StepPathUtils.computeStepRoute(StepPathUtils.PathType.SIMPLE_ESTIMATION, steps, gps, getContext());
        final PolylineOptions stepsRouteFromModel;
        if(Utils.getPref(getContext(), Utils.USE_STEPS_ESTIMATION, false)){
            stepsRouteFromModel = StepPathUtils.computeStepRoute(StepPathUtils.PathType.USING_STEP_MODEL, steps, gps, getContext());
        }else{
            stepsRouteFromModel = null;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mGoogleMap.clear();
                mGoogleMap.addPolyline(realRoute);
                mGoogleMap.addPolyline(stepsRoute);
                if(Utils.getPref(getContext(), Utils.USE_STEPS_ESTIMATION, false)){
                    mGoogleMap.addPolyline(stepsRouteFromModel);
                }
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stepsRoute.getPoints().get(0), 15));
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        mMapView.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mGoogleMap = googleMap;
    }
}

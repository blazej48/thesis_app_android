package szum.mthesis.indorpositiontracker.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import szum.mthesis.indorpositiontracker.DataChangedListener;
import szum.mthesis.indorpositiontracker.Logger;
import szum.mthesis.indorpositiontracker.MainActivity;
import szum.mthesis.indorpositiontracker.R;
import szum.mthesis.indorpositiontracker.TrackingService;

/**
 * This fragment displays information about current route, it adds listener to
 * GpsService to get notified about data changed
 */
public class TrackerFragment extends Fragment implements DataChangedListener, OnMapReadyCallback, Logger.LogListener {

    private Handler mHandler;
    private MapView mMapView;
    private TextView mInfoText;
    private GoogleMap mGoogleMap;
    private ImageView mStartStop;
    private TextView mLogContainer;
    private Button mForceStartButton;
    private TrackingService mService;

    private static final String TAG = TrackerFragment.class.getSimpleName();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        return inflater.inflate(R.layout.tracker_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        mService = ((MainActivity) getActivity()).getTrackingService();
        if (mService == null) {
            Logger.w(TAG, "mService == null");
        }

        mHandler = new Handler(getActivity().getMainLooper());

        mMapView = (MapView) view.findViewById(R.id.mapview);
        mLogContainer = (TextView) view.findViewById(R.id.logContainer);
        mLogContainer.setMovementMethod(new ScrollingMovementMethod());
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mInfoText = (TextView) view.findViewById(R.id.infoText);
        mInfoText.setText(mService.getTrackingData().getInfoText());


        mStartStop = (ImageView) view.findViewById(R.id.start_stop_switch);
        mStartStop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mService.isRunning()) {
                    stopCollectingData();
                    mStartStop.setImageResource(R.drawable.start);
                    mForceStartButton.setEnabled(true);
                } else {
                    if (mService.isProviderOff()) {
                        Toast.makeText(getActivity(), "GPS is off, please turn it on", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startCollectingData();
                    mStartStop.setImageResource(R.drawable.stop);
                }
            }

            private void stopCollectingData() {
                Logger.d(TAG, "before asking");
                mService.stop();
                mService.unregisterListener(TrackerFragment.this);
                askIfToSaveRoute();
                Logger.d(TAG, "after asking");
            }

            private void startCollectingData() {
                mService.start();
                mService.registerListener(TrackerFragment.this);
            }
        });

        mForceStartButton = (Button) view.findViewById(R.id.forceStart);
        mForceStartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mForceStartButton.setEnabled(false);
                mService.forceStart();
                mService.registerListener(TrackerFragment.this);
                mStartStop.setImageResource(R.drawable.stop);
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
        mService.registerListener(this);
        Logger.registerLogListener(this);

        if (mService.isNotRunning()) {
            mStartStop.setImageResource(R.drawable.start);
        } else {
            mStartStop.setImageResource(R.drawable.stop);
        }


        if (mService.isRunning()) {
            refreshLayout();
        }
        super.onResume();
    }

    @Override
    public void onDataChanged() {
        refreshLayout();
        mInfoText.setText(mService.getTrackingData().getInfoText());
    }

    private void refreshLayout() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                // draw map
                if (mGoogleMap != null) {
                    PolylineOptions mOptions = mService.getRoute();
                    if (mOptions != null && mOptions.getPoints().size() > 1) {
                        mGoogleMap.addPolyline(mOptions);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mOptions.getPoints().get(0), 15));
                    } else {
                        Logger.w(TAG, " ! (mOptions != null && mOptions.getPoints().size() > 1)");
                    }
                } else {
                    Logger.w(TAG, "mMapView.getMap() == null");
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        mMapView.onPause();
        mService.unregisterListener(this);
        Logger.unregisterLogListener();
    }

    private void askIfToSaveRoute() {

        new AlertDialog.Builder(getActivity())
                .setMessage("Save to history?")
                .setTitle("history")
                .setCancelable(true)
                .setNeutralButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mService.reset();
                            }
                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mService.saveRoute();
                mService.reset();
            }
        })
                .show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Logger.d(TAG, "onMapReady");
        mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
    }

    @Override
    public void addNewLog(String log) {
        mLogContainer.append("\n");
        mLogContainer.append(log);
    }

    @Override
    public void setLogs(List<String> logs) {
        mLogContainer.setText("");
        for(String log : logs){
            mLogContainer.append("\n");
            mLogContainer.append(log);
        }
    }
}

package szum.mthesis.indorpositiontracker;


import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.orm.SugarContext;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import szum.mthesis.indorpositiontracker.orm.Beacon;
import szum.mthesis.indorpositiontracker.orm.BleSample;
import szum.mthesis.indorpositiontracker.orm.DbHelper;
import szum.mthesis.indorpositiontracker.orm.Step;
import szum.mthesis.indorpositiontracker.utils.TrackingData;


public class TrackingService extends Service implements LocationListener {

    private static final String TAG = TrackingService.class.getSimpleName();

    private TrackingData trackingData = new TrackingData();
    private boolean bRunning = false;
    private PolylineOptions mRoute;

    private LocationManager mLocationManager;
    private PowerManager.WakeLock mWakeLock;
    private BluetoothAdapter mBluetoothAdapter;

    private List<DataChangedListener> mListeners = new LinkedList<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Logger.d(TAG, "onCreate");

        SugarContext.init(this);
        putSomeExampleBeaconToTheDatabase();


        // start service in foreground
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.btn_star);
        builder.setContentTitle("Indoor Position Tracker");
        builder.setContentText("tracking your indoor position");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        startForeground(1, builder.build());

        // keep screen active
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyWakelockTag");
        mWakeLock.acquire();

        registerStepDetector();
        reset();

        // bluetooth
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        startScanning();

        super.onCreate();
    }


    public void startScanning() {

        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothAdapter.isEnabled()) {
            ScanSettings.Builder builder = new ScanSettings.Builder();
            builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            bluetoothLeScanner.startScan(null, builder.build(), mScanCallback);
        } else {
            Toast.makeText(this, "bluetooth is off", Toast.LENGTH_SHORT);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int rssi = result.getRssi();
            String name = result.getScanRecord().getDeviceName();
            if(bRunning){
                trackingData.addBleSample(new BleSample(name, result.getTimestampNanos() / 1000000, rssi));
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Logger.d(TAG, "Scan failed, errorCode: " + errorCode);
        }
    };

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    private void registerStepDetector() {
        SensorManager sensorManager = (SensorManager) getApplication().getSystemService(Activity.SENSOR_SERVICE);

        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        Sensor rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        sensorManager.registerListener(mListener, stepSensor, SensorManager.SENSOR_DELAY_FASTEST, 0);
        sensorManager.registerListener(mListener, rotationVectorSensor, SensorManager.SENSOR_DELAY_FASTEST, 0);
        sensorManager.registerListener(mListener, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST, 0);
        sensorManager.registerListener(mListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST, 0);
    }

    private final SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            switch (event.sensor.getType()) {
//                case Sensor.TYPE_ROTATION_VECTOR:
//                    countYaw(event.timestamp, event.values);
//                    break;
                case Sensor.TYPE_STEP_DETECTOR:
                    if (trackingData.isInitialized()) {
                        Step step = trackingData.addStep(event.timestamp);
                        Logger.d(TAG, "adding: " + step);

                        if(trackingData.isNotWalking()) { // init counting walking time alghoritm
                            trackingData.setStartWalkingTime(event.timestamp);
                        }
                        trackingData.setCurrWalkTime(event.timestamp);
                        trackingData.incrementStepCounter();
                    }else{
                        Logger.w(TAG, "lacking first GPS location, cannot start stepCounting");
                    }

                    Logger.d(TAG, "New step detected by STEP_DETECTOR sensor, stepsCount = " + trackingData.getStepCount() + ", timestamp = " + event.timestamp);
                    break;
                case Sensor.TYPE_GRAVITY:
                    trackingData.getOrientTrckr().setCurrentGravityVector(new Vector3D(event.values[0], event.values[1], event.values[2]).normalize());
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    trackingData.getOrientTrckr().setCurrentMagneticVector(new Vector3D(event.values[0], event.values[1], event.values[2]).normalize());
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public synchronized void onLocationChanged(Location location) {
        Logger.d(TAG, "new location timestamp: " + location.getElapsedRealtimeNanos());
        if (!bRunning) {
            return;
        } // check, it GpsListener is started

        if(trackingData.isInitialized() == false) {
            trackingData.initialize();
        }
        trackingData.processNewLocation(location);
        mRoute.add(new LatLng(location.getLatitude(), location.getLongitude()));

        notifyListeners();
    }

    public Pair<Long,Integer> getClosestOrientationSample(ArrayList<Pair<Long, Integer>> array ,long currTime) {

        if(array.size() == 0){
            Logger.e(TAG, "ArrayList of time and angle pairs is empty");
            return new Pair<>(0l, 0);
        }else if (array.size() == 1){
            return array.get(0);
        }

        int low = 0;
        int high = array.size() - 1;

        while (low < high) {
            int mid = (low + high) / 2;
            assert(mid < high);
            long d1 = Math.abs(array.get(mid).first - currTime);
            long d2 = Math.abs(array.get(mid + 1).first - currTime);
            if (d2 <= d1) {
                low = mid+1;
            } else {
                high = mid;
            }
        }
        return array.get(high);
    }

    private final IBinder mBinder = new LocalBinder();

    public void saveRoute() {
        DbHelper.saveRoute(trackingData);
    }

    public void forceStart() {
        trackingData.initialize();
        mRoute.add(new LatLng(1,1));
        bRunning = true;
    }

    public class LocalBinder extends Binder {

        TrackingService getService() {
            return TrackingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "onDestroy");
        mWakeLock.release();
        SugarContext.terminate();
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        super.onDestroy();
    }

    /** turn Service on, so it starts generating gps data */
    public synchronized void start() {
        bRunning = true;
    }

    /** make Service stop generating gps data */
    public synchronized void stop() {
        bRunning = false;
    }

    /** clean generated gps data */
    public synchronized void reset() {

        trackingData.reset();
        mRoute = new PolylineOptions();
        mRoute.geodesic(true);
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 20, this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (status == LocationProvider.AVAILABLE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Toast.makeText(getApplicationContext(), "on status changed - AVALIABLE", Toast.LENGTH_SHORT);
            Logger.d(TAG, "onStatusChanged - AVALIABLE");
        }
    }

    @Override
    public synchronized void onProviderEnabled(String provider) {
    }

    @Override
    public synchronized void onProviderDisabled(String provider) {
    }

	public PolylineOptions getRoute() {
		return mRoute;
	}

    public TrackingData getTrackingData() {
        return trackingData;
    }

    public boolean isNotRunning(){
		return ! bRunning;
	}

	public boolean isRunning(){
		return bRunning;
	}

	public void registerListener(DataChangedListener listener) {
        Logger.d(TAG, "registering listener: " + listener);
	    if( ! mListeners.contains(listener) ){
	        mListeners.add(listener);
	    }
	}

   public void unregisterListener(DataChangedListener listener){
       Logger.d(TAG, "unregistering listener: " + listener);
       mListeners.remove(listener);
   }

   private void notifyListeners(){
       StringBuilder builder = new StringBuilder();
       for(DataChangedListener listener : mListeners){
           listener.onDataChanged();
           builder.append(listener.toString() + ", ");
       }
       Logger.d(TAG, "notifying listeners, : " + builder);
   }

   public boolean isProviderOff(){
       return ! mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
   }

    private void putSomeExampleBeaconToTheDatabase() {
        String beaconName = "Sample Beacon";
        List<Beacon> result = Beacon.find(Beacon.class, "name = ?", beaconName);
        if(result.isEmpty()){
            new Beacon(beaconName, 49.6267, 6.15937).save();
        }
    }

}
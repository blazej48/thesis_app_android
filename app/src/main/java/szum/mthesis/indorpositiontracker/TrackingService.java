package szum.mthesis.indorpositiontracker;


import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class TrackingService extends Service implements LocationListener {

    private static final String TAG = TrackingService.class.getSimpleName();

    private Location lastLocation;
    private long startWalkingTime = 0; // [ns]
    private double walkDistance = 0; // [m]
    private int stepsCount = 0;
    private long walkTime = 0; // [ns]

    private List<MyLocation> gpsPoints;
    private PolylineOptions mRoute;
    private boolean bRunning = false;
    private List<StepData> mStepsPath;
    private boolean isFirstLocaiton = false;
    private LocationManager mLocationManager;

    private List<DataChangedListener> mListeners = new LinkedList<>();

    private Database mDatabase;
    private PowerManager.WakeLock mWakeLock;

    private OrientationTracker mOrientTrckr = new OrientationTracker();

    private float mCurrentAccuracy = 0;
    private long mSystemStartUpTime = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Logger.d(TAG, "onCreate");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.btn_star);
        builder.setContentTitle("Indoor Position Tracker");
        builder.setContentText("tracking your indoor position");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        startForeground(1, builder.build());

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mDatabase = new Database();
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        mWakeLock.acquire();

        mSystemStartUpTime = System.currentTimeMillis() - SystemClock.uptimeMillis();

        registerStepDetector();
        reset();
        super.onCreate();
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
                    if (isFirstLocaiton) {
                        StepData stepData = new StepData(event.timestamp/1000000, mOrientTrckr.getCurrentOrientation());
                        mStepsPath.add(stepData);
                        Logger.d(TAG, "adding: " + stepData);

                        if(startWalkingTime <= 0) { // init counting walking time alghoritm
                            startWalkingTime = event.timestamp;
                        }
                        walkTime = event.timestamp - startWalkingTime;
                        stepsCount++;

                    }else{
                        Logger.w(TAG, "lacking first GPS location, cannot start stepCounting");
                    }

                    Logger.d(TAG, "New step detected by STEP_DETECTOR sensor, stepsCount = " + stepsCount + ", timestamp = " + event.timestamp);
                    break;
                case Sensor.TYPE_GRAVITY:
                    mOrientTrckr.setCurrentGravityVector(new Vector3D(event.values[0], event.values[1], event.values[2]).normalize());
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mOrientTrckr.setCurrentMagneticVector(new Vector3D(event.values[0], event.values[1], event.values[2]).normalize());
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
        if(isFirstLocaiton == false){
            mOrientTrckr.init();
            isFirstLocaiton = true;
        }
        mCurrentAccuracy = location.getAccuracy();
        mOrientTrckr.processLocation(location);
        mRoute.add(new LatLng(location.getLatitude(), location.getLongitude()));
        gpsPoints.add(new MyLocation(location, mOrientTrckr.getRealBearing(), mOrientTrckr.getRelativeBearing()));
        if(lastLocation != null){
            walkDistance += lastLocation.distanceTo(location);
        }
        lastLocation = location;
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
        mDatabase.saveRoute(mStepsPath, gpsPoints, stepsCount, walkTime / 1000000, walkDistance, mOrientTrckr.getOrientationCorrection());
    }

    public void forceStart() {
        isFirstLocaiton = true;
        mRoute.add(new LatLng(1,1));
        bRunning = true;
    }

    public class LocalBinder extends Binder {
        TrackingService getService() {
            // Return this instance of LocalService so clients can call public methods
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
        mDatabase.close();
        mWakeLock.release();

        super.onDestroy();
    }

    /** turn GpsLinster on, so it starts generating gps data */
    public synchronized void start() {
        bRunning = true;
    }

    /** make GpsLinster stop generating gps data */
    public synchronized void stop() {
        bRunning = false;
    }

    /** clean generated gps data */
    public synchronized void reset() {
        gpsPoints = new ArrayList<>();
        mStepsPath = new ArrayList<>();
        mRoute = new PolylineOptions();
        mRoute.geodesic(true);

        startWalkingTime = 0;
        stepsCount = 0;
        walkTime = 0;
        lastLocation = null;
        isFirstLocaiton = false;
        mCurrentAccuracy = 0;
        mOrientTrckr.reset();
        walkDistance = 0;

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
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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

	public boolean isNotRunning(){
		return ! bRunning;
	}

	public boolean isRunning(){
		return bRunning;
	}

	public void registerListener(DataChangedListener listener){
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

    public String getInfoText(){
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Walk distance: %.1f m \n",walkDistance));
        builder.append(String.format("Step count: %d  \n",stepsCount));
        builder.append(String.format("Walk time: %d s \n",walkTime/1000000000));
        builder.append(String.format("Estimated orientation correction: %.1f* \n", mOrientTrckr.getOrientationCorrection()));
        builder.append(String.format("GPS Accuracy: %.3f \n", mCurrentAccuracy));
        return builder.toString();
    }
}
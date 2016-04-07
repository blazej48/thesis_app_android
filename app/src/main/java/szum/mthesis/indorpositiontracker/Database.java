package szum.mthesis.indorpositiontracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Database {

    // database name
    private static final String database_NAME = "path_comparision.db";

    public static final String MAIN_TABLE = "main_table";
    public static final String TIME = "time";
    public static final String START_LONGITUDE = "start_longitude";
    public static final String START_LATITUDE = "start_latitude";
    public static final String STEPS_COUNT = "steps_count";
    public static final String ORIENTATION_CORRECTION = "rotation_correction";
    public static final String WALK_TIME = "walk_time";
    public static final String WALK_DISTANCE = "walk_distance";
    public static final String AVG_ACCURACY = "avg_accuracy";

    public static final String MAIN_ID = "id";

    public static final String STEPS_TABLE = "steps_table";
    public static final String ORIENTATION = "orientation";
    public static final String ORIENTATION_calc = "orientation_calc";
                            // TIME

    public static final String MAP_TABLE = "map_table";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String ACCURACY = "accuracy";
    public static final String REAL_BEARING = "real_bearing";
    public static final String RELATIVE_BEARING = "relative_bearing";
    public static final String ORDER = "my_order";

    private static final String TAG = Database.class.getSimpleName();

    private SQLiteDatabase db;


    public Database() {
        String path = Environment.getExternalStorageDirectory() + "/" + database_NAME;
        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);

        int version = db.getVersion();
        if (version < 1) {
            onCreate(db);
        }
    }

    public synchronized List<RunInfo> getPaths() {
        Cursor cursor = db.query(MAIN_TABLE, null, null, null, TIME, null, null);

        List<RunInfo> runList = new ArrayList<>();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    runList.add(
                            new RunInfo(
                                    cursor.getInt(cursor.getColumnIndex(MAIN_ID)),
                                    cursor.getLong(cursor.getColumnIndex(TIME)),
                                    cursor.getDouble(cursor.getColumnIndex(START_LONGITUDE)),
                                    cursor.getDouble(cursor.getColumnIndex(START_LATITUDE)),
                                    cursor.getInt(cursor.getColumnIndex(STEPS_COUNT)),
                                    cursor.getLong(cursor.getColumnIndex(WALK_TIME)),
                                    cursor.getDouble(cursor.getColumnIndex(WALK_DISTANCE)),
                                    cursor.getInt(cursor.getColumnIndex(ORIENTATION_CORRECTION)),
                                    cursor.getFloat(cursor.getColumnIndex(AVG_ACCURACY))
                            ));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return runList;
    }

    public void onCreate(SQLiteDatabase database) {
        Logger.d(TAG, "Creating the dcm sqlite database");

        database.execSQL(
                "CREATE TABLE " + MAIN_TABLE + " (" +
                        MAIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        TIME + " INTEGER, " +
                        START_LONGITUDE + " INTEGER," +
                        START_LATITUDE + " INTEGER," +
                        STEPS_COUNT + " INTEGER," +
                        ORIENTATION_CORRECTION + " INTEGER," +
                        WALK_TIME + " INTEGER," +  // [ms]
                        WALK_DISTANCE + " REAL," + // m
                        AVG_ACCURACY + " REAL" + // m
                        ");");

        database.execSQL(
                "CREATE TABLE " + STEPS_TABLE + " (" +
                        MAIN_ID + " INTEGER," +
                        TIME + " INTEGER," +
                        ORIENTATION_calc + " REAL," +
                        ORIENTATION + " REAL" +
                        ");");

        database.execSQL(
                "CREATE TABLE " + MAP_TABLE + " (" +
                        MAIN_ID + " INTEGER," +
                        TIME + " INTEGER," +
                        LONGITUDE + " REAL," +
                        LATITUDE + " REAL," +
                        ACCURACY + " REAL," +
                        REAL_BEARING + " REAL," +
                        RELATIVE_BEARING + " REAL," +
                        ORDER + " INTEGER" +
                        ");");

        database.setVersion(1);
    }


    public synchronized void saveRoute( List<StepData> stepsList, List<MyLocation> locations, int stepsCount,
                                        long walkTime, double walkDistance, double rotatCorr) {

        if (stepsList == null || stepsList.size() < 1) {
            Logger.w(TAG, "saving route: stepList is empty");
            return;
        }
        if (locations == null || locations.size() < 1) {
            Logger.w(TAG, "saving route: locations list is empty");
            return;
        }

        Logger.d(TAG, "saving route, steps: " + stepsList.size() + ", route: " + locations.size());

        float avgAccuracy = 0;

        for(MyLocation location : locations){
            avgAccuracy = avgAccuracy + (float)location.getAccuracy();
        }
        avgAccuracy = avgAccuracy/locations.size();

        MyLocation startPoint = locations.get(0);

        db.beginTransaction();

        // summary about the route
        ContentValues values = new ContentValues();
        values.put(TIME, Calendar.getInstance().getTimeInMillis());
        values.put(START_LATITUDE, (float)startPoint.getLatitude());
        values.put(START_LONGITUDE, (float)startPoint.getLongitude());
        values.put(STEPS_COUNT, stepsCount);
        values.put(WALK_TIME, walkTime);
        values.put(ORIENTATION_CORRECTION, rotatCorr);
        values.put(WALK_DISTANCE, walkDistance);
        values.put(AVG_ACCURACY, avgAccuracy);
        long id = db.insert(MAIN_TABLE, null, values);

        // graph data
        String sql = "insert into " + STEPS_TABLE + " (" + MAIN_ID + ", " + TIME + ", " +
                ORIENTATION + ", " + ORIENTATION_calc + ") values (?, ?, ?, ?);";
        SQLiteStatement stmt = db.compileStatement(sql);
        for (StepData step : stepsList) {
            stmt.bindLong(1, id);
            stmt.bindLong(2, step.getmStepTime());
            stmt.bindLong(3, step.getmStepOrientation());
            stmt.bindLong(4, step.getmStepOrientationCalc());

            stmt.executeInsert();
            stmt.clearBindings();
        }

        // map datak
        sql = "insert into " + MAP_TABLE + " (" + MAIN_ID + ", " + ORDER + ", " + LATITUDE + ", " + LONGITUDE + ", " + ACCURACY +
                 ", " + REAL_BEARING + ", " + RELATIVE_BEARING + ", " + TIME + ") values (?, ?, ?, ?, ?, ? , ?, ?);";
        stmt = db.compileStatement(sql);
        int order = 0;
        for (MyLocation location : locations) {
            stmt.bindLong(1, id);
            stmt.bindLong(2, order++);
            stmt.bindDouble(3, location.getLatitude());
            stmt.bindDouble(4, location.getLongitude());
            stmt.bindDouble(5, location.getAccuracy());
            stmt.bindDouble(6, location.getRealBearing());
            stmt.bindDouble(7, location.getRelativeBearing());
            stmt.bindDouble(8, location.getElapsedRealtimeMilis());

            stmt.executeInsert();
            stmt.clearBindings();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public synchronized void close() {
        db.close();
    }

    public synchronized void deleteData(long time, long id) {
        db.delete(MAIN_TABLE, TIME + " = " + time, null);
        db.delete(STEPS_TABLE, MAIN_ID + " = " + id, null);
        db.delete(MAP_TABLE, MAIN_ID + " = " + id, null);

    }

    public synchronized PolylineOptions getRoute(long mainId) {
        Cursor cursor = db.query(MAP_TABLE,
                new  String[]{LATITUDE, LONGITUDE},
                MAIN_ID + " = " + mainId, null,
                ORDER, null, null);

        PolylineOptions poly = new PolylineOptions();
        poly.geodesic(true);

        if(cursor != null){
            if(cursor.moveToFirst()){
                do{
                    poly.add(new LatLng(
                            cursor.getFloat(cursor.getColumnIndex(LATITUDE)),
                            cursor.getFloat(cursor.getColumnIndex(LONGITUDE))));
                }while(cursor.moveToNext());
            }
            cursor.close();
        }

        Log.d(TAG, "GPS route: " + poly.getPoints());
        return poly;
    }

    public synchronized List<MyLatLng> getMyRoute(long mainId) {
        Cursor cursor = db.query(MAP_TABLE,
                new  String[]{LATITUDE, LONGITUDE, TIME},
                MAIN_ID + " = " + mainId, null,
                ORDER, null, null);

        List<MyLatLng> myRoute = new ArrayList<>();

        if(cursor != null){
            if(cursor.moveToFirst()){
                do{
                    myRoute.add(new MyLatLng(
                            cursor.getFloat(cursor.getColumnIndex(LATITUDE)),
                            cursor.getFloat(cursor.getColumnIndex(LONGITUDE)),
                            cursor.getLong(cursor.getColumnIndex(TIME))));
                }while(cursor.moveToNext());
            }
            cursor.close();
        }
        return myRoute;
    }

    public synchronized List<StepData> getSteps(long mainId) {
        Cursor cursor = db.query(STEPS_TABLE,
                new  String[]{TIME, ORIENTATION},
                MAIN_ID + " = " + mainId, null,
                TIME, null, null);

        List<StepData> steps = new ArrayList<>();

        if(cursor != null){
            if(cursor.moveToFirst()){
                do{
                    steps.add(new StepData(
                            cursor.getLong(cursor.getColumnIndex(TIME)),
                            cursor.getInt(cursor.getColumnIndex(ORIENTATION))));
                }while(cursor.moveToNext());
            }
            cursor.close();
        }

        Log.d(TAG, " steps route: " + steps );
        return steps;
    }
}

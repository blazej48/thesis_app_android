package szum.mthesis.indorpositiontracker;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.IBinder;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.junit.BeforeClass;

import java.util.List;

import szum.mthesis.indorpositiontracker.fragments.BeaconsFragment;
import szum.mthesis.indorpositiontracker.fragments.BeaconsMapFragment;
import szum.mthesis.indorpositiontracker.fragments.HistoryFragment;
import szum.mthesis.indorpositiontracker.fragments.MapFragment;
import szum.mthesis.indorpositiontracker.fragments.PrefsFragment;
import szum.mthesis.indorpositiontracker.fragments.TrackerFragment;
import szum.mthesis.indorpositiontracker.orm.Beacon;
import szum.mthesis.indorpositiontracker.orm.Path;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FragmentManager mFragmentManager;

    private MapFragment mMapFragment;
    TrackingService mService;
    boolean mBound = false;

    private ViewPager mViewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFragmentManager = getSupportFragmentManager();
        mSectionsPagerAdapter = new SectionsPagerAdapter(mFragmentManager);

        mViewPager = (ViewPager) findViewById(R.id.container);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mMapFragment = new MapFragment();

        Intent intent = new Intent(this, TrackingService.class);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, TrackingService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TrackingService.LocalBinder binder = (TrackingService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            mViewPager.setAdapter(mSectionsPagerAdapter);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(mViewPager);

            if ( ! mService.isBluetoothEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.clear_log) {
            clearLog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearLog() {
        Logger.clearLog();
    }

    public void openMap(Path path){
        mMapFragment.updateMap(path);
    }

    public TrackingService getTrackingService() {
        return mService;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "switching to item " + position);
            switch (position) {
                case 0:
                    return mMapFragment;
                case 1:
                    return new HistoryFragment();
                case 2:
                    return new TrackerFragment();
                case 3:
                    return new PrefsFragment();
                case 4:
                    return new BeaconsFragment();
                case 5:
                    return new BeaconsMapFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "MAP";
                case 1:
                    return "HISTORY";
                case 2:
                    return "TRACKER";
                case 3:
                    return "PREFS";
                case 4:
                    return "BEACONS";
                 case 5:
                    return "BEACONS MAP";
            }
            return null;
        }

    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you want this app to run in background?")
                .setTitle("Exit")
                .setCancelable(true)
                .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        terminateService();
                        MainActivity.super.onBackPressed();
                    }
                })
                .setPositiveButton("Run in background", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(MainActivity.this, "App runs in background", Toast.LENGTH_SHORT);
                        MainActivity.super.onBackPressed();
                    }
                })
                .show();
    }

    private void terminateService() {
        Log.d(TAG, "stopping service");
        Intent intent = new Intent(this, TrackingService.class);
        stopService(intent);
    }


}

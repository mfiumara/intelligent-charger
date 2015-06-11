package tum.ei.ics.intelligentcharger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import tum.ei.ics.intelligentcharger.bluetooth.BleService;
import tum.ei.ics.intelligentcharger.bluetooth.Bluetooth;
import tum.ei.ics.intelligentcharger.entity.ConnectionEvent;
import tum.ei.ics.intelligentcharger.entity.Cycle;
import tum.ei.ics.intelligentcharger.fragment.ChargeCurveFragment;
import tum.ei.ics.intelligentcharger.fragment.CycleFragment;
import tum.ei.ics.intelligentcharger.fragment.MainFragment;
import tum.ei.ics.intelligentcharger.predictor.TargetSOCPredictor;
import tum.ei.ics.intelligentcharger.receiver.StartChargeReceiver;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


public class SwipeActivity extends FragmentActivity {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    private BleService m_oBluetoothLeService = null;
    private ServiceConnection m_oServiceConnection = null;
    private BroadcastReceiver m_oGattUpdateReceiver = null;

    public static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_swipe, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch(position) {
                case 0:
                    return MainFragment.newInstance(position + 1);
                case 1:
                    return CycleFragment.newInstance(position + 1);
                case 2:
                    return ChargeCurveFragment.newInstance(position + 1);
                case 3:
                    return Bluetooth.newInstance("","");
            }
            return MainFragment.newInstance(0);
        }
        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    public void debug(View view) {
        TargetSOCPredictor targetSOCPredictor = new TargetSOCPredictor(this, Cycle.listAll(Cycle.class), 10);
        Toast.makeText(this, Integer.toString(targetSOCPredictor.predict()) + "%", Toast.LENGTH_SHORT).show();

        Intent i = new Intent(this, MainFragment.updateView.class);
        sendBroadcast(i);
    }

    public void bluetooth(View view) {
        SharedPreferences prefs = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        startBleService(prefs.getString(Global.AUTOCONNECT_BLE_DEVICEADDRESS, ""), prefs.getString(Global.AUTOCONNECT_BLE_DEVICENAME,""));
    }
    public boolean startBleService(final String address, final String deviceName) {
        m_oServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                Log.d(TAG, "onServiceConnected");
                m_oBluetoothLeService = ((BleService.LocalBinder) service).getService();
                if(!m_oBluetoothLeService.initialize()) {
                    Log.e("BLE", "Unable to initialize Bluetooth");
                }
                Log.d(TAG, "connect to ble service");
                m_oBluetoothLeService.connect(address, deviceName);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                m_oBluetoothLeService = null;
            }
        };

        m_oGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if(BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    startBleService(address,deviceName);
                }
            }
        };

        Intent gattServiceIntent = new Intent(this, BleService.class);
        bindService(gattServiceIntent, m_oServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(m_oGattUpdateReceiver, makeGattUpdateIntentFilter());

        return true;
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }
}

package tum.ei.ics.intelligentcharger.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

import tum.ei.ics.intelligentcharger.receiver.BatteryChangedReceiver;
import tum.ei.ics.intelligentcharger.receiver.PowerConnectedReceiver;
import tum.ei.ics.intelligentcharger.receiver.PowerDisconnectedReceiver;

/**
 * Created by mattia on 29.04.15.
 */
public class BatteryService extends Service {

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private static BatteryChangedReceiver mBatteryChangedReceiver = new BatteryChangedReceiver();
    private static PowerConnectedReceiver mPowerConnectedReceiver = new PowerConnectedReceiver();
    private static PowerDisconnectedReceiver mPowerDisconnectedReceiver = new PowerDisconnectedReceiver();

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            registerReceiver(mBatteryChangedReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Toast.makeText(this, "Service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
//        Toast.makeText(this, "Service done", Toast.LENGTH_SHORT).show();
    }
}
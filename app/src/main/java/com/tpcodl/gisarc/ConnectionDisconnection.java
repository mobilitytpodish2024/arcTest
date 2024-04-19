package com.tpcodl.gisarc;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;


public class ConnectionDisconnection extends MultiDexApplication {
    private static ConnectionDisconnection instance;



    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        IntentFilter internetIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        IntentFilter gpsIntentFilter = new IntentFilter(LocationManager.MODE_CHANGED_ACTION);


    }


    public static ConnectionDisconnection getInstance() {
        return instance;
    }
}

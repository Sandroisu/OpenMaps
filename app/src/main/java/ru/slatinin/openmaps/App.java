package ru.slatinin.openmaps;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

public class App extends Application {
    private ConnectionChecker mConnectionReceiver;
    public final static String NOTIFICATION_CHANEL_ID = "OpenMapNotifChannelID";

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIFICATION_CHANEL_ID,
                    "kakoe_to_imya", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
        mConnectionReceiver = new ConnectionChecker();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionReceiver, filter);
    }

    public ConnectionChecker getConnectionReceiver() {
        return mConnectionReceiver;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mConnectionReceiver);
    }
}

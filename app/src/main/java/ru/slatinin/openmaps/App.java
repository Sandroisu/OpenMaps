package ru.slatinin.openmaps;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class App extends Application {
    private ConnectionChecker mConnectionReceiver;
    public final static String NOTIFICATION_CHANEL_ID = "OpenMapNotifChannelID";
    private DownloadProgressListener mDownloadProgressListener;

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

    public void setDownloadProgressListener(DownloadProgressListener listener) {
        mDownloadProgressListener = listener;
    }

    public void onShowDownloadPercentage(long percentage) {
        if (mDownloadProgressListener != null) {
            mDownloadProgressListener.showDownloadPercentage(percentage);
        }
    }

    public void onShowErrorDownloading(String error, int segmentNumber) {
        if (mDownloadProgressListener != null) {
            mDownloadProgressListener.showErrorDownloading(error);
        }else {
            showNotification(error,this, segmentNumber);
        }
    }

    public void onShowSingleSegmentDone(int segmentNumber) {
        if (mDownloadProgressListener != null) {
            mDownloadProgressListener.showSingleSegmentDone(segmentNumber);
        }
    }

    public void onHandleCriticalError(String message, int segmentNumber){
        if (mDownloadProgressListener != null) {
            mDownloadProgressListener.handleCriticalError(message);
        }else {
            showNotification("Загрузка сегмента карты прервалась",this, segmentNumber);
        }
    }

    public void onShowDownloadingDone(boolean atLeastOneSegmentDone){
        if (mDownloadProgressListener != null) {
            mDownloadProgressListener.showDownloadingDone(atLeastOneSegmentDone);
        }
    }

    public ConnectionChecker getConnectionReceiver() {
        return mConnectionReceiver;
    }
    public int getInt (){
        return 2;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mConnectionReceiver);
    }

    public void showNotification(String message, Context context, int segmentNumber) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.NOTIFICATION_CHANEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(context.getResources().getColor(R.color.purple_500))
                .setContentTitle(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(segmentNumber, builder.build());
    }
}

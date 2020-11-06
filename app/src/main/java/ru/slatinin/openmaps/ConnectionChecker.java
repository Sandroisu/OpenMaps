package ru.slatinin.openmaps;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ConnectionChecker extends BroadcastReceiver {

    private CheckConnection mCheckConnection;

    public void setListener(CheckConnection checkConnection) {
        this.mCheckConnection = checkConnection;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (mCheckConnection != null ) {
            mCheckConnection.onConnectionChange(NetworkInfoUtil.isNetworkAvailable(context));
        }
    }

    public interface CheckConnection {
        void onConnectionChange(boolean isConnected);
    }
}
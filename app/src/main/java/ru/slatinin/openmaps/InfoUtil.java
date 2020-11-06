package ru.slatinin.openmaps;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class InfoUtil {
    private final static String INFO_URL = "http://kes.it-serv.ru/covid/osm/info";

    public static void getMapInfo(Handler handler, InfoDownLoadListener listener) {
        Thread thread = new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(INFO_URL);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(5000);

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                Scanner s = new Scanner(in).useDelimiter("\\A");
                JSONArray array = new JSONArray(s.hasNext() ? s.next() : "");
                handler.post(() -> listener.onInfoDone(array));
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> listener.onInfoError(e.getMessage()));
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
        thread.start();
    }


    public interface InfoDownLoadListener{
        void onInfoError(String message);

        void onInfoDone(JSONArray array);
    }



}

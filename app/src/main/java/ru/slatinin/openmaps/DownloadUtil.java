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

public class DownloadUtil {
    private final static String INFO_URL = "http://kes.it-serv.ru/covid/osm/info";
    private final static String DOWNLOAD_URL = "http://kes.it-serv.ru/covid/osm/";

    public static void getMapInfo(Handler handler, MapDownLoadListener listener) {
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


    public static void downloadMapsInfo(ArrayList<SegmentInfo> segmentInfoArrayList, Context context, Handler handler, MapDownLoadListener listener) {
        Thread thread = new Thread(() -> {
            boolean atLeastOneSegmentDone = false;

            for (int i = 0; i < segmentInfoArrayList.size(); i++) {
                boolean isLostInternetConnection = false;
                SegmentInfo singleSegment = segmentInfoArrayList.get(i);
                File file;
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(DOWNLOAD_URL + singleSegment.segmentNumber + ".zip");

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    if (conn.getResponseCode() != 200) {
                        handler.post(() -> listener.onError("Не удалось утановить соединение с сервером."));
                        continue;
                    }
                    long contentLength = conn.getContentLength();
                    if (contentLength <= 0) {
                        handler.post(() -> listener.onError("Файл поврежден"));
                        continue;
                    }
                    File catalog = new File(FileUtil.getRoot(context, Environment.DIRECTORY_PICTURES), "/maps");
                    if (!catalog.exists()) {
                        catalog.mkdirs();
                    }
                    file = new File(catalog, singleSegment.segmentNumber + ".zip");
                    if (file.exists()){
                        file.delete();
                    }
                    DataInputStream stream = new DataInputStream(url.openStream());
                    DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
                    byte[] buffer = new byte[1024];
                    long bytesRead;
                    long progress = 0;
                    while ((bytesRead = stream.read(buffer)) != -1) {
                        if (NetworkInfoUtil.isNetworkAvailable(context)) {
                            fos.write(buffer, 0,
                                    (int) bytesRead);
                            progress += bytesRead;
                            final long sendProgress = progress * 100 / contentLength;
                            handler.post(() -> listener.onProgress(sendProgress));
                        } else {
                            handler.post(() -> listener.onError("Потеряно соединение, попробуйте загрузить повторно"));
                            isLostInternetConnection = true;
                            break;
                        }
                    }
                    stream.close();
                    fos.flush();
                    fos.close();
                    final int currentSegment = i + 1;
                    if (!isLostInternetConnection) {
                        handler.post(() -> listener.onSingleSegmentDone(currentSegment));
                    }
                    atLeastOneSegmentDone = true;
                } catch (IOException | RuntimeException e) {
                    e.printStackTrace();
                    handler.post(() -> listener.onCriticalError(e.getMessage()));
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
            boolean finalAtLeastOneSegmentDone = atLeastOneSegmentDone;
            handler.post(() -> listener.onDone(finalAtLeastOneSegmentDone));
        });
        thread.start();
    }

    public interface MapDownLoadListener {

        void onProgress(long progress);

        void onSingleSegmentDone(int segmentNumber);

        void onDone(boolean atLeastOneSegmentDone);

        void onError(String message);

        void onCriticalError(String message);

        void onInfoError(String message);

        void onInfoDone(JSONArray array);
    }

}

package ru.slatinin.openmaps;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadWorker extends Worker {
    private final static String DOWNLOAD_URL = "http://kes.it-serv.ru/covid/osm/";
    public final static String SEGMENT_ARRAY_KEY = "segment_array_key";

    private final App app;
    private final int[] segmentToDownload;

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        app = (App) context.getApplicationContext();
        segmentToDownload = workerParams.getInputData().getIntArray(SEGMENT_ARRAY_KEY);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @NonNull
    @Override
    public Result doWork() {
        Handler handler = new Handler(Looper.getMainLooper());
        if (segmentToDownload == null || segmentToDownload.length < 1) {
            return null;
        }
        boolean atLeastOneSegmentDone = false;

        for (int i = 0; i < segmentToDownload.length; i++) {
            boolean isLostInternetConnection = false;
            int singleSegment = segmentToDownload[i];
            File file;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(DOWNLOAD_URL + singleSegment + ".zip");

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                if (conn.getResponseCode() != 200) {
                    handler.post(() -> app.onShowErrorDownloading("Не удалось утановить соединение с сервером.", singleSegment));
                    continue;
                }
                long contentLength = conn.getContentLength();
                if (contentLength <= 0) {
                    handler.post(()->app.onShowErrorDownloading("Файл поврежден, попробуйте выполнить загрузку позже.", singleSegment));
                    continue;
                }
                File catalog = new File(FileUtil.getRoot(app, Environment.DIRECTORY_PICTURES), "/maps");
                if (!catalog.exists()) {
                    catalog.mkdirs();
                }
                file = new File(catalog, singleSegment + ".zip");
                if (file.exists()) {
                    file.delete();
                }
                DataInputStream stream = new DataInputStream(url.openStream());
                DataOutputStream fos = new DataOutputStream(new FileOutputStream(file));
                byte[] buffer = new byte[1024];
                long bytesRead;
                long progress = 0;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    if (NetworkInfoUtil.isNetworkAvailable(app)) {
                        if (!isStopped()) {
                            fos.write(buffer, 0,
                                    (int) bytesRead);
                            progress += bytesRead;
                            final long sendProgress = progress * 100 / contentLength;
                            handler.post(()->app.onShowDownloadPercentage(sendProgress));
                        }
                    } else {
                        handler.post(()->app.onShowErrorDownloading("Потеряно соединение, попробуйте загрузить повторно.", singleSegment));
                        isLostInternetConnection = true;
                        break;
                    }
                }
                stream.close();
                fos.flush();
                fos.close();
                final int currentSegment = i + 1;
                if (!isLostInternetConnection) {
                    if (!isStopped()) {
                        handler.post(()->app.onShowSingleSegmentDone(currentSegment));
                        SharedPreferences mPreferences = app.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
                        mPreferences.edit().putInt(String.valueOf(singleSegment), singleSegment).apply();
                        handler.post(()->app.showNotification("Скачан сегмент карты " + singleSegment, app, singleSegment));
                    }
                }
                atLeastOneSegmentDone = true;
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
                handler.post(()->app.onHandleCriticalError(e.getMessage(),singleSegment));
                return null;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        boolean finalAtLeastOneSegmentDone = atLeastOneSegmentDone;
        handler.post(()->app.onShowDownloadingDone(finalAtLeastOneSegmentDone));

        return null;
    }

    @Override
    public void onStopped() {
        super.onStopped();
        app.onShowErrorDownloading("Скачивание отменено.", 0);
    }


}

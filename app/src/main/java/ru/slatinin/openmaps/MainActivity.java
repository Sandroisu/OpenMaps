package ru.slatinin.openmaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DownloadUtil.MapDownLoadListener, ConnectionChecker.CheckConnection {
    private JSONArray infoArray;
    private ContentLoadingProgressBar clpbWholeProgress;
    private ContentLoadingProgressBar clpbSingleProgress;
    private Button btnDownload;
    private Button btnGoToMap;
    private ProgressBar pbInfoDownload;
    private TextView tvInfoText;
    private TextView tvWholePercentage;
    private TextView tvSingleFilePercentage;
    private TextView tvError;
    private SharedPreferences mPreferences;
    private ImageButtonsMap ibMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ibMap = findViewById(R.id.image_buttons_map);
        btnGoToMap = findViewById(R.id.button_go_to_map);
        btnDownload = findViewById(R.id.button_download);
        pbInfoDownload = findViewById(R.id.info_progress);
        clpbWholeProgress = findViewById(R.id.whole_progress);
        clpbSingleProgress = findViewById(R.id.single_file_progress);
        tvInfoText = findViewById(R.id.info_text);
        tvWholePercentage = findViewById(R.id.whole_percentage);
        tvSingleFilePercentage = findViewById(R.id.single_file_percentage);
        tvError = findViewById(R.id.error_text);

        App app = (App) getApplication();
        app.getConnectionReceiver().setListener(this);

        mPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        ArrayList<String> activeTiles = ibMap.getActiveTiles();
        for (int i = 0; i < activeTiles.size(); i++) {
            int x = mPreferences.getInt(activeTiles.get(i), 0);
            if (x > 0) {
                ibMap.setMarked(x);
                btnGoToMap.setVisibility(View.VISIBLE);
            }
        }


        if (NetworkInfoUtil.isNetworkAvailable(this)) {
            pbInfoDownload.setVisibility(View.VISIBLE);
            DownloadUtil.getMapInfo(new Handler(getMainLooper()), this);
        } else {
            setError("Для скачивания карт необходимо подключение к инетрнету.");
            btnDownload.setEnabled(false);
        }

        btnDownload.setOnClickListener(v -> {
            if (!NetworkInfoUtil.isNetworkAvailable(this)) {
                setError("Нужно подключение к сети интернет.");
                return;
            }
            if (infoArray != null && infoArray.length() > 0) {
                ArrayList<SegmentInfo> selectedTiles = getSegments(ibMap.getSelectedTiles());
                if (selectedTiles.size() > 0) {
                    tvError.setVisibility(View.GONE);
                    clpbWholeProgress.setMax(ibMap.getSelectedTiles().size());
                    tvInfoText.setText("Пожалуйста оставайтесь на экране до окончания загрузки.");
                    Handler handler = new Handler(getMainLooper());
                    DownloadUtil.downloadMapsInfo(selectedTiles, MainActivity.this, handler, MainActivity.this);

                    btnDownload.setEnabled(false);
                    if (selectedTiles.size() > 1) {
                        clpbWholeProgress.setVisibility(View.VISIBLE);
                        tvWholePercentage.setVisibility(View.VISIBLE);
                        String wholeInfo = "Загружается 1 файл из " + selectedTiles.size();
                        tvWholePercentage.setText(wholeInfo);
                    }
                    clpbSingleProgress.setVisibility(View.VISIBLE);
                    tvSingleFilePercentage.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, "Нужно выбрать хотя бы один сегмент", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGoToMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 15);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 15) {
            boolean notGranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    notGranted = true;
                    break;
                }
            }
            if (notGranted) {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setPositiveButton("Хорошо", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        ;
                    }
                });

                AlertDialog alert = adb.create();
                alert.setTitle("РукаЛицо");
                alert.setMessage("Это всего лишь прототип, иди теперь в настройки и давай разрешенеия вручную, умник!");
                alert.setCancelable(false);
                alert.show();
            }
        }
    }

    @Override
    public void onProgress(long progress) {
        clpbSingleProgress.setProgress((int) progress);
        String prgrs = progress + "%";
        tvSingleFilePercentage.setText(prgrs);
    }

    @Override
    public void onSingleSegmentDone(int segmentNumber) {
        int max = clpbWholeProgress.getMax();
        if (max > 1) {
            String prgrs = "Загружен " + segmentNumber + " из " + max;
            tvWholePercentage.setText(prgrs);
            clpbWholeProgress.setProgress(segmentNumber);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, App.NOTIFICATION_CHANEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(getResources().getColor(R.color.purple_500))
                .setContentTitle("Скачан сегмент карты " + segmentNumber)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(segmentNumber, builder.build());
        mPreferences.edit().putInt(String.valueOf(segmentNumber), segmentNumber).apply();
        ibMap.setMarked(segmentNumber);

    }

    @Override
    public void onDone(boolean atLeastOneSegmentDone) {
        clpbWholeProgress.setVisibility(View.GONE);
        clpbSingleProgress.setVisibility(View.GONE);
        tvSingleFilePercentage.setVisibility(View.GONE);
        tvWholePercentage.setVisibility(View.GONE);
        btnDownload.setEnabled(atLeastOneSegmentDone);
        btnGoToMap.setVisibility(View.VISIBLE);
        tvInfoText.setText("Выберите районы необходимые для работы, затем нажмите кнопку 'Загрузить'");
    }

    @Override
    public void onError(String message) {
        setError(message);
        clpbWholeProgress.setVisibility(View.GONE);
        clpbSingleProgress.setVisibility(View.GONE);
        btnDownload.setEnabled(true);
    }

    @Override
    public void onCriticalError(String message) {
        setError(message);
        btnDownload.setEnabled(true);
    }

    @Override
    public void onInfoError(String message) {
        setError(message);
    }

    @Override
    public void onInfoDone(JSONArray array) {
        if (array.length() > 0) {
            infoArray = array;
            pbInfoDownload.setVisibility(View.GONE);
            tvInfoText.setVisibility(View.VISIBLE);
        } else {
            setError("Не удалось получить необходимую информацию. Попробуйте позже.");
            tvInfoText.setVisibility(View.GONE);
            pbInfoDownload.setVisibility(View.GONE);
            btnDownload.setEnabled(false);
        }
    }

    private ArrayList<SegmentInfo> getSegments(ArrayList<Integer> tilesToBeDownloaded) {
        ArrayList<SegmentInfo> segmentInfoArrayList = new ArrayList<>();
        if (infoArray.length() > 0) {
            for (int i = 0; i < infoArray.length(); i++) {
                JSONObject temp = null;

                try {
                    temp = infoArray.getJSONObject(i);
                    int remoteNumber = temp.getInt("number");
                    int remoteSize = temp.getInt("size");
                    for (int valid : tilesToBeDownloaded) {
                        if (valid == remoteNumber) {
                            segmentInfoArrayList.add(new SegmentInfo(remoteNumber, remoteSize));
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        return segmentInfoArrayList;
    }

    @Override
    public void onConnectionChange(boolean isConnected) {
        btnDownload.setEnabled(isConnected);
        if (isConnected) {
            DownloadUtil.getMapInfo(new Handler(getMainLooper()), this);
            pbInfoDownload.setVisibility(View.VISIBLE);
            tvError.setVisibility(View.GONE);
        } else {
            tvInfoText.setVisibility(View.GONE);
            tvInfoText.setText("Выберите районы необходимые для работы, затем нажмите кнопку 'Загрузить'");
            setError("Отсутствует подключение к интернету");
        }
    }

    private void setError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
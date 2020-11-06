package ru.slatinin.openmaps;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.GEMFFileArchive;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.io.IOException;

public class MapActivity extends AppCompatActivity {
    MapView mMapView;
    private FusedLocationProviderClient mLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        mMapView = (MapView) findViewById(R.id.map);
        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(this);
        File catalog = new File(FileUtil.getRoot(this, Environment.DIRECTORY_PICTURES), "/maps");
        if (!catalog.exists()) {
            catalog.mkdirs();
        }
        File[] files = catalog.listFiles();
        if (files != null && files.length != 0) {

            OfflineTileProvider offlineTileProvider = new OfflineTileProvider(simpleReceiver, files);
            FileBasedTileSource fileBasedTileSource = new FileBasedTileSource("IServ OSM", 10, 17,
                    256, ".png", new String[]{"http://127.0.0.1"});
            offlineTileProvider.setTileSource(fileBasedTileSource);
            final TilesOverlay firstTilesOverlay = new TilesOverlay(offlineTileProvider, this.getBaseContext());
            firstTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
            final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getApplicationContext());
            final ITileSource tileSource = new XYTileSource("SomeNamejhhgj", 10, 17,256, ".png",
                    new String[]{"http://cic.it-serv.ru/osm/"});
            tileProvider.setOfflineFirst(true);
            tileProvider.setTileSource(tileSource);
            final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, this.getBaseContext());
            tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
            mMapView.getOverlays().add(tilesOverlay);
            mMapView.getOverlays().add(firstTilesOverlay);


            mMapView.setUseDataConnection(true);
            mMapView.setMultiTouchControls(true);
            mMapView.getController().setCenter(new GeoPoint(56.245688, 47.082170));
            mMapView.getController().setZoom(10d);
            mMapView.invalidate();
           // setLocation(this);
        } else {

            Toast.makeText(this, "Карты не загружены", Toast.LENGTH_SHORT).show();
        }

    }

    public void setLocation(Activity activity) {
        LocationCallback callback = new LocationCallback();
        LocationRequest locationRequest = new LocationRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Void> r = mLocationProviderClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper());
            r.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLocationProviderClient.getLastLocation().addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    mLocationProviderClient.removeLocationUpdates(callback);
                                    mMapView.getController().setZoom(10d);
                                    mMapView.getController().setCenter(new GeoPoint(location));
                                }
                            }
                        });
                    }
                }
            });

        }
    }


}

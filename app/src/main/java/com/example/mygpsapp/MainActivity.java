package com.example.mygpsapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements LocationListener {

    private static final int PERMISSIONS_REQUEST_CODE = 123;
    private MapView map = null;
    private LocationManager locationManager;
    private TextView longitudeText, latitudeText, providerText, statusGpsText, statusInternetText;
    private Marker myPositionMarker;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_main);

        initNavigationRail();

        setNavigationItemSelected(R.id.nav_map);

        longitudeText = findViewById(R.id.Longitude);
        latitudeText = findViewById(R.id.Latitude);
        providerText = findViewById(R.id.Provider);
        statusGpsText = findViewById(R.id.status_gps);
        statusInternetText = findViewById(R.id.status_internet);

        map = findViewById(R.id.mapview);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getController().setZoom(15.0);
        map.setMultiTouchControls(true);

        myPositionMarker = new Marker(map);
        myPositionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        myPositionMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.placeholder));
        map.getOverlays().add(myPositionMarker);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkAndRequestPermissions();
        updateStatus();
    }

    private void updateStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            statusInternetText.setText("Internet: ON");
            statusInternetText.setTextColor(Color.parseColor("#008000"));
        } else {
            statusInternetText.setText("Internet: OFF");
            statusInternetText.setTextColor(Color.RED);
        }

        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGpsEnabled) {
            statusGpsText.setText("GPS: ON");
            statusGpsText.setTextColor(Color.parseColor("#008000"));
        } else {
            statusGpsText.setText("GPS: OFF");
            statusGpsText.setTextColor(Color.RED);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = location;
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        providerText.setText("Najlepszy dostawca: fused");
        longitudeText.setText("Długość geograficzna: " + String.format(Locale.US, "%.6f", longitude));
        latitudeText.setText("Szerokość geograficzna: " + String.format(Locale.US, "%.6f", latitude));

        GeoPoint myPosition = new GeoPoint(latitude, longitude);
        map.getController().animateTo(myPosition);
        myPositionMarker.setPosition(myPosition);
        myPositionMarker.setTitle("Moja pozycja");
        map.invalidate();
        updateStatus();
    }

    @Override
    protected void onSendSmsClicked() {
        sendCoordinatesViaSmsIntent();
    }

    @Override
    protected void onSaveMapClicked() {
        saveMapSnapshot();
    }

    @Override
    protected void onShareClicked() {
        shareCoordinates();
    }

    @Override
    protected void onFabClicked() {
        if (currentLocation != null) {
            GeoPoint myPosition = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.getController().animateTo(myPosition);
        }
    }

    private void sendCoordinatesViaSmsIntent() {
        if (currentLocation == null) return;
        String message = String.format(Locale.US, "%.6f %.6f", currentLocation.getLongitude(), currentLocation.getLatitude());
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:"));
        intent.putExtra("sms_body", message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void saveMapSnapshot() {
        map.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(map.getDrawingCache());
        map.setDrawingCacheEnabled(false);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "map_snapshot_" + timeStamp + ".png";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyGpsApp");
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            } catch (Exception e) {
            }
        }
    }

    private void shareCoordinates() {
        if (currentLocation == null) return;
        String shareText = String.format(Locale.US, "%.6f %.6f", currentLocation.getLongitude(), currentLocation.getLatitude());
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Udostępnij koordynaty przez"));
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
        } catch (SecurityException e) {
        }
        updateStatus();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS};
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        updateStatus();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        updateStatus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean fineLocationGranted = false;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        fineLocationGranted = true;
                    }
                }
            }
            if (fineLocationGranted) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
        updateStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
        locationManager.removeUpdates(this);
    }
}

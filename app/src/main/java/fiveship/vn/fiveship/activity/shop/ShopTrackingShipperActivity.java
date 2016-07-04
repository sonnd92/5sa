package fiveship.vn.fiveship.activity.shop;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.model.LocationItem;
import fiveship.vn.fiveship.service.LocationService;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.GoogleDirection;
import fiveship.vn.fiveship.utility.Utils;

public class ShopTrackingShipperActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available

    private static final int MILLISECONDS_PER_SECOND = 1000;

    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private GoogleDirection gd;
    private LatLng from;
    private LatLng to;
    private Marker markerTracking;
    boolean redirectDone;

    private ShopService shopService;
    private LocationService locationService;
    private Toolbar toolbar;
    private int shipId;
    private int shopId;

    private Timer timerTracking;
    private boolean mIsTimerEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.shop_tracking);
        timerTracking = new Timer();

        redirectDone = false;
        mIsTimerEnable = true;

        shopService = ShopService.get_instance(this);

        locationService = LocationService.get_instance(this);

        shipId = getIntent().getIntExtra("id", 0);
        shopId = getIntent().getIntExtra("shopId", 0);

        setupToolBar();
        setUpMapIfNeeded();
    }

    public void initDirection() {
        try {

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(from, 15));

            gd = new GoogleDirection(this);

            GoogleDirection.OnAnimateListener directionFinish = new GoogleDirection.OnAnimateListener() {
                @Override
                public void onFinish(boolean mIsSuccess) {
                    if (mIsSuccess) {
                        redirectDone = true;
                    } else {
                        Toast.makeText(ShopTrackingShipperActivity.this, "Theo dõi đơn không khả dụng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onStart() {
                    //mMap.addMarker(new MarkerOptions().position(start).icon(BitmapDescriptorFactory.fromResource(R.drawable.male_2)));
                }

                @Override
                public void onProgress(int progress, int total) {

                }
            };
            mapDirection(from, to);
            gd.setOnAnimateListener(directionFinish);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void mapDirection(LatLng _start, LatLng _end) {

        mMap.addMarker(new MarkerOptions().position(from).icon(BitmapDescriptorFactory.fromResource(R.drawable.flag)));

        gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            @Override
            public void onResponse(String status, Document doc, GoogleDirection gd) {
                gd.directDirection(mMap, gd.getDirection(doc)
                        , true
                        , new MarkerOptions().position(to).icon(BitmapDescriptorFactory.fromResource(R.drawable.end_flag))
                        , false
                        , true
                        , new PolylineOptions().width(8).color(ContextCompat.getColor(ShopTrackingShipperActivity.this, R.color.direction_to_color)));
            }
        });

        gd.request(_start, _end, GoogleDirection.MODE_DRIVING, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_direction, menu);
        return true;
    }

    public void setupToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        stopTimer();
        super.onDestroy();

    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            // Check if we were successful in obtaining the map.
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng latLng = new LatLng(Config.DEFAULT_LATITUDE, Config.DEFAULT_LONGITUDE);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                latLng, 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to location user
                .zoom(13)                   // Sets the zoom
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        new GetTrackingShipTask().execute();

        timerTracking.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mIsTimerEnable)
                    new TrackingShipTask().execute();
            }
        }, 0, 3000);
    }

    public void stopTimer() {
        if (mIsTimerEnable && timerTracking != null) {
            timerTracking.cancel();
            timerTracking.purge();
            timerTracking = null;
            mIsTimerEnable = false;
        }
    }

    private class GetTrackingShipTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return shopService.getTrackingShip(String.valueOf(shipId));
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            try {
                if (result != null && !result.getBoolean("error")) {

                    from = new LatLng(result.getDouble("xFrom"), result.getDouble("yFrom"));
                    to = new LatLng(result.getDouble("xTo"), result.getDouble("yTo"));

                    if (!redirectDone)
                        initDirection();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class TrackingShipTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return locationService.trackingShipperOfShip(String.valueOf(shipId), String.valueOf(shopId));
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            try {
                if (result != null && !result.getBoolean("error")) {
                    if (markerTracking == null) {

                        MarkerOptions markerTrackingOption = new MarkerOptions().position(new LatLng(result.getDouble("x"), result.getDouble("y"))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_shipper));

                        markerTracking = mMap.addMarker(markerTrackingOption);

                        markerTracking.setPosition(new LatLng(result.getDouble("x"), result.getDouble("y")));
                    } else {

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(result.getDouble("x"), result.getDouble("y")), 13));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

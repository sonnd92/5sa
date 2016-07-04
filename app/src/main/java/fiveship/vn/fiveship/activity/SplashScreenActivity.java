package fiveship.vn.fiveship.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Calendar;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.shipper.MainActivity;
import fiveship.vn.fiveship.activity.shop.ShopActivity;
import fiveship.vn.fiveship.gcm.RegistrationIntentService;
import fiveship.vn.fiveship.interfaces.LocationEventListener;
import fiveship.vn.fiveship.other.AlarmReceiver;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.LocationStatus;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

public class SplashScreenActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private final int SPLASH_DISPLAY_LENGTH = 1000;
    private Context _context;
    private SessionManager sessionManager;
    private Intent intent;
    private PendingIntent pendingIntent;
    private boolean success;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        _context = this;

        success = false;

        sessionManager = new SessionManager(_context);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        Intent intentGCM = new Intent(this, RegistrationIntentService.class);
        startService(intentGCM);

        if(sessionManager.isLoggedIn()
                && sessionManager.isShipper()) {

            Intent alarmIntent = new Intent(SplashScreenActivity.this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(SplashScreenActivity.this, 0, alarmIntent, 0);
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Calendar c = Calendar.getInstance();
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 30000, pendingIntent);

        }
    }

    public void CheckInternetConnection() {

        findViewById(R.id.require_location_box).setVisibility(View.GONE);

        if (Utils.isConnectingToInternet(_context)) {

            CheckPermission();

        } else {

            findViewById(R.id.require_internet_box).setVisibility(View.VISIBLE);

            findViewById(R.id.reconnect_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isConnectingToInternet(_context)) {

                        CheckPermission();

                    } else {
                        Toast.makeText(getApplication(),
                                getResources().getString(R.string.require_internet_message),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void CheckPermission(){

        findViewById(R.id.require_internet_box).setVisibility(View.GONE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            findViewById(R.id.require_my_location_box).setVisibility(View.VISIBLE);
            findViewById(R.id.submit_my_location_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(SplashScreenActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            Utils.REQUEST_CODE_ACCESS_FINE_LOCATION_PERMISSIONS);
                }
            });
        } else{
            CheckGPSEnable();
        }
    }
    public void CheckGPSEnable() {
        //submit_my_location_button

        new GPSTracker(_context, new LocationEventListener() {
            @Override
            public void OnLocationStateChange(int status) {
                if(!success) {
                    if (status == LocationStatus.ENABLE.getStatusCode()) {

                        success = true;
                        if (sessionManager.isLoggedIn()) {
                            if (sessionManager.isShipper()) {
                                intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                            } else {
                                intent = new Intent(SplashScreenActivity.this, ShopActivity.class);
                            }
                        } else {
                            intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                        }
                        redirect();
                    } else {

                        findViewById(R.id.require_location_box).setVisibility(View.VISIBLE);
                        findViewById(R.id.submit_button).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }

            @Override
            public void OnLocationChange(Location location) {

            }
        });
    }

    public void redirect() {

        /* New Handler to start the MainActivity
         * and close this Splash Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the MainActivity. */
                startActivity(intent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Utils.REQUEST_CODE_ACCESS_FINE_LOCATION_PERMISSIONS: {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                    findViewById(R.id.require_my_location_box).setVisibility(View.GONE);
                    CheckInternetConnection();
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckInternetConnection();
    }
}

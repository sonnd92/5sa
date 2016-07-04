package fiveship.vn.fiveship.utility;

/**
 * Created by BVN on 21/04/2015.
 */

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import fiveship.vn.fiveship.interfaces.LocationEventListener;

public class GPSTracker extends Service implements LocationListener {

    private Context mContext;
    // flag for GPS status
    private boolean canGetLocation = false;

    private Location location;   // location
    private double latitude;     // latitude
    private double longitude;    // longitude
    private LocationEventListener listener;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 500; // 500 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker() {
    }

    public GPSTracker(Context mContext) {
        this.mContext = mContext;
        getLocation();
    }

    public GPSTracker(Context mContext, LocationEventListener listener) {
        this.mContext = mContext;
        this.listener = listener;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                //fire callback when can't get location
                if (listener != null) {
                    listener.OnLocationStateChange(LocationStatus.DISABLE.getStatusCode());
                    if (location == null) {
                        listener.OnLocationStateChange(LocationStatus.NULL.getStatusCode());
                    } else {
                        listener.OnLocationStateChange(LocationStatus.SUCCESS.getStatusCode());
                        listener.OnLocationChange(location);
                    }
                }
            } else {
                this.canGetLocation = true;
                if (listener != null)
                    listener.OnLocationStateChange(LocationStatus.ENABLE.getStatusCode());

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {

                    if ((ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    }

                    if(locationManager != null)
                        location = locationManager
                                   .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }

                if(isNetworkEnabled){
                    if(location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if(locationManager != null)
                            location = locationManager
                                       .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
            }

            //fire callback when get location successful
            if (listener != null) {
                if (location != null) {
                    listener.OnLocationStateChange(LocationStatus.SUCCESS.getStatusCode());
                    listener.OnLocationChange(location);
                } else {
                    listener.OnLocationStateChange(LocationStatus.NULL.getStatusCode());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null)
                listener.OnLocationStateChange(LocationStatus.NULL.getStatusCode());
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(GPSTracker.this);
            }
        }
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;

        if (listener != null) {
            listener.OnLocationChange(location);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }



}
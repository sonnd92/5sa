package fiveship.vn.fiveship.activity.shop;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.interfaces.LocationEventListener;
import fiveship.vn.fiveship.service.ShipperService;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.SessionManager;

public class ShopMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private View mRootView;
    private Context mContext;
    private RotateLoading mRotateLoading;

    private ShipperService mService;

    private SessionManager mSessionMng;

    private boolean mCallbackSuccess = true;

    public TextView mTvShipperNum;

    public static synchronized ShopMapFragment newInstance() {
        ShopMapFragment instance = new ShopMapFragment();
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.shop_map, container, false);

        mRotateLoading = (RotateLoading) mRootView.findViewById(R.id.rotateloading);
        mService = ShipperService.get_instance(mContext);
        mSessionMng = new SessionManager(mContext);
        setUpMapIfNeeded();
        mTvShipperNum = (TextView) mRootView.findViewById(R.id.txt_number_shipper);
        return mRootView;
    }

    public void fetchData(Location location) {
        mTvShipperNum.setText(mContext.getString(R.string.three_dots));
        mCallbackSuccess = false;

        new GetShipperNearShopTask().execute(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
    }


    public void clearAllMarker() {
        mGoogleMap.clear();
    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public void onLocationChanged(Location location) {
        mRotateLoading.start();
        clearAllMarker();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the mGoogleMap to location user
                .zoom(13)                   // Sets the zoom
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.face)));

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

        fetchData(location);
    }

    private void setUpMapIfNeeded() {
        if (mGoogleMap == null) {
            // Try to obtain the mGoogleMap from the SupportMapFragment.
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.shop_map_shipper)).getMapAsync(this);
        }
    }

    public void placeOrderMarker(LatLng position, boolean isConfirm) {
        mGoogleMap.addMarker(
                new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.fromResource(isConfirm ? R.drawable.ic_shipper : R.drawable.ic_shipper_normal)));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Config.DEFAULT_LATITUDE, Config.DEFAULT_LONGITUDE), Config.DEFAULT_MAP_ZOOM));
        if ((ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            mGoogleMap.setMyLocationEnabled(true);
        }
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);


        if (!mSessionMng.getXPoint().equals("null") && mSessionMng.getXPoint().length() > 4
                && !mSessionMng.getYPoint().equals("null") && mSessionMng.getYPoint().length() > 4) {
            mGoogleMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(mSessionMng.getXPoint()), Double.parseDouble(mSessionMng.getXPoint())))
                            .title("Shop của bạn")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag)));
        }

        new GPSTracker(mContext, new LocationEventListener() {
            @Override
            public void OnLocationStateChange(int status) {

            }

            @Override
            public void OnLocationChange(final Location location) {
                //fetch data
                onLocationChanged(location);
                fetchData(location);
                mRootView.findViewById(R.id.btn_list_refresh).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCallbackSuccess) {
                            onLocationChanged(location);
                        }
                    }
                });
            }
        });
    }

    private class GetShipperNearShopTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return mService.getListShipperNearShip(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null && !result.getBoolean("error")) {

                    for (int i = 0; i < result.getJSONArray("data").length(); i++) {

                        placeOrderMarker(
                                new LatLng(result.getJSONArray("data").getJSONObject(i).getDouble("XPoint"), result.getJSONArray("data").getJSONObject(i).getDouble("YPoint"))
                                , result.getJSONArray("data").getJSONObject(i).getBoolean("IsConfirm"));

                    }
                    mTvShipperNum.setText(" " + result.getJSONArray("data").length());
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.has_exception_try_again), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, mContext.getString(R.string.has_exception_try_again), Toast.LENGTH_LONG).show();
            }

            mCallbackSuccess = true;
            mRotateLoading.stop();
        }
    }
}

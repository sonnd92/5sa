package fiveship.vn.fiveship.activity.shipper;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.victor.loading.rotate.RotateLoading;

import org.w3c.dom.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.StepDurationAdapter;
import fiveship.vn.fiveship.interfaces.LocationEventListener;
import fiveship.vn.fiveship.model.StepDirectionItem;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.GoogleDirection;
import fiveship.vn.fiveship.utility.LocationStatus;
import fiveship.vn.fiveship.utility.Utils;

public class DirectionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available

    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    //private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    //private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private GoogleDirection gd;
    private LatLng start;
    private boolean rdDone;
    private boolean rdSimpleDone;

    private LatLngBounds startBounds;
    private LatLngBounds endBounds;

    //private int durationValueFrom;
    private int distanceValueFrom;
    //private int durationValueTo;
    private int distanceValueTo;
    private String startAddress = "";
    private String endAddress = "";
    private Animation fadeIn;
    private Animation fadeOut;
    private ArrayList<LatLng> directions;
    private ArrayList<StepDirectionItem> directionSteps;
    private int index;
    private LatLngBounds.Builder builder;
    private Bundle locations;

    private ListView lv_step;
    private RotateLoading rotateLoading;
    private Marker mMyLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        rdDone = false;
        rdSimpleDone = false;
        setupToolBar();
        setUpMapIfNeeded();
        locations = getIntent().getExtras();
        directionSteps = new ArrayList<>();
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        rotateLoading = ((RotateLoading) findViewById(R.id.rotateloading));
        lv_step = (ListView) findViewById(R.id.lv_steps);
    }

    public void initDirection() {
        try {

            new GPSTracker(this, new LocationEventListener() {
                @Override
                public void OnLocationStateChange(int status) {
                    if (status == LocationStatus.DISABLE.getStatusCode()) {
                        showSettingsAlert();
                    }
                }

                @Override
                public void OnLocationChange(Location currentLocation) {
                    if (mMyLocationMarker != null) {
                        mMyLocationMarker.remove();
                    }

                    mMyLocationMarker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.face_direction)));

                    if (locations != null && locations.containsKey("locations") && !rdDone) {

                        rotateLoading.start();
                        onLocationChanged(currentLocation);
                        directions = (ArrayList<LatLng>) locations.getSerializable("locations");
                        builder = new LatLngBounds.Builder();

                        start = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 15));

                        gd = new GoogleDirection(DirectionActivity.this);

                        if (directions.size() > 2) {
                            directionMultiVer2();
                        } else if (directions.size() == 2) {
                            directingSimple();
                        } else {
                            directing();
                        }

                        rdDone = true;
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void directing() {
        try {

            final LatLng to = new LatLng(directions.get(0).latitude,
                    directions.get(0).longitude);

            //final String addressShop = directions.get(0).getName();

            GoogleDirection.OnAnimateListener directionFinish = new GoogleDirection.OnAnimateListener() {
                @Override
                public void onFinish(boolean mIsSuccess) {
                    if (mIsSuccess) {
                        // calculate total bounds
                        if (calculateTotalBounds() != null) {

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(calculateTotalBounds(), 150));
                                }
                            }, MILLISECONDS_PER_SECOND);
                        }

                        showDirectionSummary();
                    } else {
                        Toast.makeText(DirectionActivity.this, getString(R.string.can_not_direction_message), Toast.LENGTH_SHORT).show();
                    }

                    rotateLoading.stop();
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onProgress(int progress, int total) {

                }
            };

            // received way
            gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
                @Override
                public void onResponse(String status, Document doc, GoogleDirection gd) {
                    gd.directDirection(mMap, gd.getDirection(doc), true, new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.flag)), false, true, new PolylineOptions().width(6).color(ContextCompat.getColor(DirectionActivity.this, R.color.direction_from_color)));

                    //durationValueFrom = gd.getTotalDurationValue(doc);
                    distanceValueFrom = gd.getTotalDistanceValue(doc);
                    startAddress = gd.getStartAddress(doc);
                    endAddress = gd.getEndAddress(doc);
                    directionSteps.addAll(gd.getSection(doc));
                    startBounds = gd.getBound(doc);
                }
            });

            gd.request(start, to, GoogleDirection.MODE_WALKING, null);
            gd.setOnAnimateListener(directionFinish);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void directingSimple() {
        try {

            final LatLng from = new LatLng(directions.get(0).latitude,
                    directions.get(0).longitude);
            final LatLng to = new LatLng(directions.get(1).latitude,
                    directions.get(1).longitude);

            //final String addressShop = directions.get(0).getName();

            //final String addressCustomer = directions.get(1).getName();

            GoogleDirection.OnAnimateListener directionFinish = new GoogleDirection.OnAnimateListener() {
                @Override
                public void onFinish(boolean mIsSuccess) {
                    if (mIsSuccess) {
                        if ((to.latitude != from.latitude
                                || to.longitude != from.longitude) && !rdSimpleDone) {
                            //delivery way
                            gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
                                @Override
                                public void onResponse(String status, Document doc, GoogleDirection gd) {

                                    gd.directDirection(mMap,
                                            gd.getDirection(doc),
                                            true,
                                            new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.end_flag)),
                                            false, true,
                                            new PolylineOptions().width(6).color(ContextCompat.getColor(DirectionActivity.this, R.color.direction_to_color)));

                                    //durationValueTo = gd.getTotalDurationValue(doc);
                                    distanceValueTo = gd.getTotalDistanceValue(doc);
                                    directionSteps.addAll(gd.getSection(doc));
                                    endAddress = gd.getEndAddress(doc);
                                    endBounds = gd.getBound(doc);

                                    if (calculateTotalBounds() != null) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(calculateTotalBounds(), 150));
                                            }
                                        }, MILLISECONDS_PER_SECOND);
                                    }

                                    showDirectionSummary();
                                    rotateLoading.stop();
                                    rdSimpleDone = true;
                                }
                            });
                            gd.request(from, to, GoogleDirection.MODE_WALKING, null);
                        } else {
                            rotateLoading.stop();
                        }
                    } else {
                        Toast.makeText(DirectionActivity.this, getString(R.string.can_not_direction_message), Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(int progress, int total) {
                }
            };

            // received way
            gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
                @Override
                public void onResponse(String status, Document doc, GoogleDirection gd) {
                    gd.directDirection(mMap,
                            gd.getDirection(doc),
                            true,
                            new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.flag)),
                            false,
                            true,
                            new PolylineOptions().width(6).color(ContextCompat.getColor(DirectionActivity.this, R.color.direction_from_color)));

                    //durationValueFrom = gd.getTotalDurationValue(doc);
                    distanceValueFrom = gd.getTotalDistanceValue(doc);
                    startAddress = gd.getStartAddress(doc);
                    directionSteps.addAll(gd.getSection(doc));
                    startBounds = gd.getBound(doc);
                }
            });
            gd.request(start, from, GoogleDirection.MODE_WALKING, null);
            gd.setOnAnimateListener(directionFinish);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void directionMultiVer2() {
        try {

            final LatLng from = new LatLng(directions.get(0).latitude,
                    directions.get(0).longitude);

            final LatLng to = findLongestLocation(directions);

            //final String addressShop = directions.get(0).getName();

            //final String addressCustomer = directions.get(1).getName();

            GoogleDirection.OnAnimateListener directionFinish = new GoogleDirection.OnAnimateListener() {
                @Override
                public void onFinish(boolean mIsSuccess) {
                    if (mIsSuccess) {
                        if ((to.latitude != from.latitude
                                || to.longitude != from.longitude) && !rdSimpleDone) {

                            //delivery way
                            //remove start and end point from way points
                            directions.remove(from);
                            directions.remove(to);

                            gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
                                @Override
                                public void onResponse(String status, Document doc, GoogleDirection gd) {

                                    gd.directDirection(mMap,
                                            gd.getDirection(doc),
                                            true,
                                            new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.end_flag)),
                                            false, true,
                                            new PolylineOptions().width(6).color(ContextCompat.getColor(DirectionActivity.this, R.color.direction_to_color)));

                                    //durationValueTo = gd.getTotalDurationValue(doc);
                                    distanceValueTo = gd.getTotalDistanceValue(doc);
                                    directionSteps.addAll(gd.getSection(doc));
                                    endAddress = gd.getEndAddress(doc);
                                    endBounds = gd.getBound(doc);

                                    if (calculateTotalBounds() != null) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(calculateTotalBounds(), 150));
                                            }
                                        }, MILLISECONDS_PER_SECOND);
                                    }

                                    showDirectionSummary();

                                    rotateLoading.stop();
                                    rdSimpleDone = true;
                                }
                            });

                            gd.request(from, to, GoogleDirection.MODE_WALKING, directions);
                        } else {
                            rotateLoading.stop();
                        }
                    } else {
                        Toast.makeText(DirectionActivity.this, getString(R.string.can_not_direction_message), Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(int progress, int total) {
                }
            };

            // received way
            gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
                @Override
                public void onResponse(String status, Document doc, GoogleDirection gd) {
                    gd.directDirection(mMap,
                            gd.getDirection(doc),
                            true,
                            new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.flag)),
                            false,
                            true,
                            new PolylineOptions().width(6).color(ContextCompat.getColor(DirectionActivity.this, R.color.direction_from_color)));

                    //durationValueFrom = gd.getTotalDurationValue(doc);
                    distanceValueFrom = gd.getTotalDistanceValue(doc);
                    startAddress = gd.getStartAddress(doc);
                    directionSteps.addAll(gd.getSection(doc));
                    startBounds = gd.getBound(doc);
                }
            });
            gd.request(start, from, GoogleDirection.MODE_WALKING, null);
            gd.setOnAnimateListener(directionFinish);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //calculator nearest location
    public LatLng findLongestLocation(ArrayList<LatLng> locations) {
        LatLng longest = null;
        double longestDistance = 0d;
        for (int i = 1; i < locations.size() ; i++) {

            if (Double.compare(gd.Haversine(locations.get(0), locations.get(i)), longestDistance) < 0)
                continue;

            longestDistance = gd.Haversine(locations.get(0), locations.get(i));
            longest = locations.get(i);
        }

        return longest;
    }

    //calculator total bound of many location
    public LatLngBounds calculateTotalBounds() {
        LatLngBounds totalBounds = null;
        try {
            double southwestLat = startBounds.southwest.latitude <= endBounds.southwest.latitude
                    ? startBounds.southwest.latitude
                    : endBounds.southwest.latitude;

            double southwestLgn = startBounds.southwest.longitude <= endBounds.southwest.longitude
                    ? startBounds.southwest.longitude
                    : endBounds.southwest.longitude;

            double northestLat = startBounds.northeast.latitude >= endBounds.northeast.latitude
                    ? startBounds.northeast.latitude
                    : endBounds.northeast.latitude;

            double northestLgn = startBounds.northeast.longitude >= endBounds.northeast.longitude
                    ? startBounds.northeast.longitude
                    : endBounds.northeast.longitude;

            totalBounds = new LatLngBounds(new LatLng(southwestLat, southwestLgn), new LatLng(northestLat, northestLgn));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return totalBounds;
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
            // Check if we were successful in obtaining the map.
        }
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        final Dialog confirmDlg = Utils.setupConfirmDialog(this);
        confirmDlg.show();
        ((TextView) confirmDlg.findViewById(R.id.tv_content)).setText(getString(R.string.location_required_confirm_box));
        confirmDlg.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDlg.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, Utils.REQUEST_TOKEN_GET_GPS);
            }
        });
        confirmDlg.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDlg.dismiss();
            }
        });
    }

    public void showDirectionSummary() {
        findViewById(R.id.direction_info).setVisibility(View.VISIBLE);

//        String totalDuration;
//        if (durationValueFrom + durationValueTo > 60) {
//            int m = (durationValueFrom + durationValueTo) / 60;
//            if (m > 60) {
//                totalDuration = getString(R.string.interval) + " " + Math.round(m / 60) + " " + getString(R.string.hour);
//            } else {
//                totalDuration = Math.round(m) + " " + getString(R.string.minute);
//            }
//        } else {
//            totalDuration = durationValueFrom + durationValueTo + " " + getString(R.string.second);
//        }

        DecimalFormat df = new DecimalFormat("#.#");
        String totalDistance = (distanceValueTo + distanceValueFrom) / MILLISECONDS_PER_SECOND > 1
                ? df.format((distanceValueTo + distanceValueFrom) / MILLISECONDS_PER_SECOND) + " km"
                : distanceValueTo + distanceValueFrom + " m";
        ((TextView) findViewById(R.id.startAddress)).setText(startAddress);
        ((TextView) findViewById(R.id.endAddress)).setText(endAddress);
        ((TextView) findViewById(R.id.duration)).setText(String.valueOf(totalDistance));

        findViewById(R.id.view_more_direction_summary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final StepDurationAdapter stepDurationAdapter = new StepDurationAdapter(DirectionActivity.this, directionSteps);

                lv_step.setAdapter(stepDurationAdapter);

                final LinearLayout detailLayout = (LinearLayout) findViewById(R.id.detail_direction_summary);

                detailLayout.startAnimation(fadeIn);

                detailLayout.setVisibility(View.VISIBLE);

                findViewById(R.id.collapse_summary).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        detailLayout.startAnimation(fadeOut);

                        detailLayout.setVisibility(View.GONE);
                    }
                });

                lv_step.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        CameraUpdate cu = CameraUpdateFactory.newLatLng(directionSteps.get(position).getEndPosition());
                        mMap.animateCamera(cu, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                CameraUpdate zout = CameraUpdateFactory.zoomTo(20);
                                mMap.animateCamera(zout);
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                    }
                });
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Config.DEFAULT_LATITUDE, Config.DEFAULT_LONGITUDE), Config.DEFAULT_MAP_ZOOM));

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });

        if (!rdDone) {
            initDirection();
        }
    }

    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                latLng, 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to location user
                .zoom(13)                   // Sets the zoom
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //MarkerOptions option = new MarkerOptions();
        //option.icon(BitmapDescriptorFactory.fromResource(R.drawable.male_2));
        //option.position(latLng);
        //Marker currentMarker = mMap.addMarker(option);
        //currentMarker.showInfoWindow();
    }

    public void setupToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
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
}

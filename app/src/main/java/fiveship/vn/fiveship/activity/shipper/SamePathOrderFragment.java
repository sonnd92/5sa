package fiveship.vn.fiveship.activity.shipper;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.AssignShipAction;
import fiveship.vn.fiveship.activity.RegisterActivity;
import fiveship.vn.fiveship.adapter.PlacesAutoCompleteAdapter;
import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.DeliveryToItem;
import fiveship.vn.fiveship.model.NearlyMapItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.service.adapter.GetListShipOfListId;
import fiveship.vn.fiveship.service.adapter.GetSamePathOrder;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.GoogleDirection;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;
import fiveship.vn.fiveship.widget.DelayAutocompleteTextView;

public class SamePathOrderFragment extends Fragment implements LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available

    private static final int MILLISECONDS_PER_SECOND = 1000;

    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private final double DEFAULT_LATITUDE = 17.3699734;
    private final double DEFAULT_LONGITUDE = 105.9732017;
    private final float DEFAULT_MAP_ZOOM = 5.3f;
    private View rootView;
    private FragmentActivity myContext;

    private GoogleDirection gd;
    private LatLng start;
    private LatLng from;
    private LatLngBounds directionBounds;
    private ArrayList<LatLng> latLngArrayListDrt;

    private Dialog confirmDlg;
    private View loadingLayout;

    private HashMap<Marker, NearlyMapItem> markersIdentify;

    private DelayAutocompleteTextView actFrom;
    private DelayAutocompleteTextView actTo;
    private GoogleApiClient mGoogleApiClient;
    private PlacesAutoCompleteAdapter mAdapter;
    private GPSTracker gps;
    private NearlyMapItem nearlyItem;
    private ArrayList<ShippingOrderItem> list = new ArrayList();
    private LinearLayout detailInfoLayout;
    private LinearLayout actionLayout;
    private Dialog loadingDlg;
    private Dialog alertDlg;
    private SessionManager sessionManager;
    private String listId = "";

    private RotateLoading rotateLoading;

    public static SamePathOrderFragment newInstance() {
        SamePathOrderFragment instance = new SamePathOrderFragment();
        return instance;
    }

    public SamePathOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_same_path_order, container, false);
        //setupToolBar();

        gps = new GPSTracker(myContext);

        detailInfoLayout = (LinearLayout) rootView.findViewById(R.id.detail_info);
        actionLayout = (LinearLayout) rootView.findViewById(R.id.map_actions);
        loadingLayout = rootView.findViewById(R.id.layout_loading);
        loadingLayout.setVisibility(View.GONE);

        confirmDlg = Utils.setupConfirmDialog(myContext);
        loadingDlg = Utils.setupLoadingDialog(myContext);
        alertDlg = Utils.setupAlertDialog(myContext);

        sessionManager = new SessionManager(myContext);

        markersIdentify = new HashMap<>();

        int idGG = new Random().nextInt(10000);

        rotateLoading = ((RotateLoading) rootView.findViewById(R.id.rotateloading));

        mGoogleApiClient = new GoogleApiClient.Builder(myContext)
                .enableAutoManage(myContext, idGG /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        detailInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detail = new Intent(myContext, DetailOrderActivity.class);
                detail.putExtra("OrderId", nearlyItem.getShipItem().getId());
                myContext.startActivity(detail);
            }
        });

        rootView.findViewById(R.id.filter_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSearchPopup();
            }
        });

        rootView.findViewById(R.id.btn_list_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllMarker();
                initDirection();
            }
        });

        rootView.findViewById(R.id.btn_list_near).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detail = new Intent(myContext, ListOrderOfArrayIdActivity.class);
                detail.putExtra("listId", listId);
                myContext.startActivity(detail);

            }
        });

        rootView.findViewById(R.id.list_order).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent listOrderIntent = new Intent(myContext, ListOrderInPlaceActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("ListOrders", list);
                        listOrderIntent.putExtras(bundle);
                        startActivity(listOrderIntent);
                    }
                });

        rootView.findViewById(R.id.direct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<LatLng> locations = new ArrayList();

                Intent mapIntent = new Intent(myContext, DirectionActivity.class);
                Bundle locationsBnl = new Bundle();

                if (nearlyItem != null) {

                    locations.add(new LatLng(Double.parseDouble(list.get(0).getOrderFrom().getLatitude())
                            ,Double.parseDouble( list.get(0).getOrderFrom().getLongitude())));

                    for (ShippingOrderItem s : list) {
                        if (s.isGroup()) {
                            for (DeliveryToItem d : s.getListShipToOfGroup()) {
                                locations.add(new LatLng(d.getXPoint(), d.getYPoint()));
                            }
                        } else {
                            locations.add(new LatLng(Double.parseDouble(s.getShippingTo().getLatitude()),
                                    Double.parseDouble(s.getShippingTo().getLongitude())));
                        }
                    }
                }

                locationsBnl.putSerializable("locations", locations);
                mapIntent.putExtras(locationsBnl);
                startActivity(mapIntent);
            }
        });

        rootView.findViewById(R.id.action_accept_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssignShipAction assignShipAction = new AssignShipAction(myContext, nearlyItem.getShipItem(), sessionManager.getShipperId(), nearlyItem.getShipItem().getStatusNote(), nearlyItem.getShipItem().isShopReliable());

                assignShipAction.assign();

                assignShipAction.setAssignShipActionCallback(new AssignShipAction.AssignShipActionCallback() {
                    @Override
                    public void callBackSuccess() {

                    }
                });
            }
        });

        rootView.findViewById(R.id.action_recommend_shipping_cost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setupRecommendShippingCostDialog(myContext, nearlyItem.getShipItem()).show();
            }
        });

        rootView.findViewById(R.id.action_direction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<LatLng> locations = new ArrayList();
                Intent mapIntent = new Intent(myContext, DirectionActivity.class);
                Bundle locationsBnl = new Bundle();
                if (nearlyItem != null) {
                    locations.add(new LatLng(
                            Double.parseDouble(nearlyItem.getShipItem().getOrderFrom().getLatitude()),
                            Double.parseDouble(nearlyItem.getShipItem().getOrderFrom().getLongitude())));

                    if (nearlyItem.getShipItem().isGroup()) {
                        for (DeliveryToItem d : nearlyItem.getShipItem().getListShipToOfGroup()) {
                            locations.add(new LatLng(d.getXPoint(),
                                    d.getYPoint()));
                        }
                    } else {
                        locations.add(new LatLng(
                                Double.parseDouble(nearlyItem.getShipItem().getShippingTo().getLatitude()),
                                Double.parseDouble(nearlyItem.getShipItem().getShippingTo().getLongitude())));
                    }
                }
                locationsBnl.putSerializable("locations", locations);
                mapIntent.putExtras(locationsBnl);
                myContext.startActivity(mapIntent);
            }
        });

        initSearchPopup();

        setUpMapIfNeeded();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        myContext = (FragmentActivity) context;
        super.onAttach(context);
    }

    public void clearAllMarker() {
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(gps.getLatitude(), gps.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_shipper))
                .draggable(false));
    }

//region Setup autocomplete address

    public void initAutocompleteAddress(View parentView) {
        // Retrieve the AutoCompleteTextView that will display Place suggestions.
        actFrom = (DelayAutocompleteTextView)
                parentView.findViewById(R.id.et_address);
        actFrom.setLoadingIndicator((RotateLoading) parentView.findViewById(R.id.li_et_address));

        actTo = (DelayAutocompleteTextView)
                parentView.findViewById(R.id.et_received_address);
        actTo.setLoadingIndicator((RotateLoading) parentView.findViewById(R.id.li_et_received_address));

        // Register a listener that receives callbacks when a suggestion has been selected

        mAdapter = new PlacesAutoCompleteAdapter(myContext, mGoogleApiClient, RegisterActivity.BOUNDS_GREATER_VIETNAM,
                null);
        actFrom.setAdapter(mAdapter);
        actFrom.setThreshold(2);
        actTo.setAdapter(mAdapter);
        actTo.setThreshold(2);

        actFrom.setOnItemClickListener(fromAutocompleteClickListener);
        actTo.setOnItemClickListener(toAutocompleteClickListener);
    }

    private AdapterView.OnItemClickListener fromAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdateFromPlaceCallback);
        }
    };

    private AdapterView.OnItemClickListener toAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdateToPlaceCallback);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details rootView on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdateFromPlaceCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                places.release();
                return;
            }
// Get the Place object from the buffer.
            final Place place = places.get(0);
            //Get coordinates of place
            start = place.getLatLng();

            places.release();
        }
    };
    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details rootView on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdateToPlaceCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                places.release();
                return;
            }
// Get the Place object from the buffer.
            final Place place = places.get(0);
            //Get coordinates of place
            from = place.getLatLng();

            places.release();
        }
    };

//endregion

    public void hideOrderInfoLayout() {
        //actionLayout.startAnimation(fadeOut);
        actionLayout.setVisibility(View.GONE);
        //detailInfoLayout.startAnimation(fadeOut);
        detailInfoLayout.setVisibility(View.GONE);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                if (!gps.canGetLocation()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE), DEFAULT_MAP_ZOOM));
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gps.getLatitude(), gps.getLongitude()), 13));
                }

                if (ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }

                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(gps.getLatitude(), gps.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_shipper))
                        .draggable(false));
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        initSheetBottom(marker);

                        return true;
                    }
                });

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        hideOrderInfoLayout();
                    }
                });

                if (!gps.canGetLocation()) {
                    confirmDlg.show();
                    ((TextView) confirmDlg.findViewById(R.id.tv_content)).setText(getString(R.string.location_required_confirm_box));
                    confirmDlg.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, Utils.REQUEST_TOKEN_GET_GPS);
                            confirmDlg.dismiss();
                        }
                    });
                    confirmDlg.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmDlg.dismiss();
                        }
                    });
                }
            }
        }
    }

    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                latLng, 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to location user
                .zoom(13)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //MarkerOptions option = new MarkerOptions();
        //option.icon(BitmapDescriptorFactory.fromResource(R.drawable.male_2));
        //option.position(latLng);
        //Marker currentMarker = mMap.addMarker(option);
        //currentMarker.showInfoWindow();
    }

//endregion

//region Finding same path shipping orders

    public void initSearchPopup() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(myContext);

        View dialogView = myContext.getLayoutInflater().inflate(R.layout.fragment_shiper_search_same_path_order, null);

        builder.setView(dialogView);

        final AlertDialog inputPlaceDlg = builder.create();
        inputPlaceDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        inputPlaceDlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        initAutocompleteAddress(dialogView);

        dialogView.findViewById(R.id.btn_local_me).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (gps.canGetLocation()) {
                    actFrom.setText("Vị trí của bạn");
                    start = new LatLng(gps.getLatitude(), gps.getLongitude());
                } else {
                    showSettingsAlert();
                }
            }
        });

        dialogView.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPlaceDlg.dismiss();
            }
        });

        dialogView.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actFrom.getText() != null && !actFrom.getText().toString().isEmpty() && actTo.getText() != null && !actTo.getText().toString().isEmpty()) {
                    if (from != null && start != null) {
                        inputPlaceDlg.dismiss();
                        clearAllMarker();
                        initDirection();
                    } else {
                        Toast.makeText(myContext, myContext.getString(R.string.suggest_to_use_place_by_google), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(myContext, myContext.getString(R.string.required_from_to_address_message), Toast.LENGTH_SHORT).show();
                }
            }
        });

        inputPlaceDlg.show();

    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        final Dialog confirmDlg = Utils.setupConfirmDialog(myContext);
        confirmDlg.show();
        ((TextView) confirmDlg.findViewById(R.id.tv_content)).setText(getString(R.string.location_required_confirm_box));
        confirmDlg.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, Utils.REQUEST_TOKEN_GET_GPS);
                confirmDlg.dismiss();
            }
        });
        confirmDlg.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDlg.dismiss();
            }
        });
    }

    public void initDirection() {
        try {

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 9));

            gd = new GoogleDirection(myContext);

            mapDirection(start, from);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // handler when get response of google direction api successful
    public void mapDirection(final LatLng _start, final LatLng _end) {

        mMap.addMarker(
                new MarkerOptions()
                        .position(_start)
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(BitmapFactory.decodeResource(myContext.getResources(), R.drawable.face_direction))));

        gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            @Override
            public void onResponse(String status, Document doc, GoogleDirection gd) {

                rotateLoading.start();

                latLngArrayListDrt = gd.getDirection(doc);

                directionBounds = gd.getBound(doc);

                gd.animateDirection(mMap, latLngArrayListDrt, GoogleDirection.SPEED_VERY_VERY_FAST
                        , false, false, false, true, new MarkerOptions().title("Điểm đến").icon(BitmapDescriptorFactory.fromResource(R.drawable.end_flag)), false, true,
                        new PolylineOptions().width(6).color(ContextCompat.getColor(myContext, R.color.direction_from_color)));

                if (directionBounds != null) {

                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(directionBounds, 200));

                }

                new GetSamePathOrder(new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(ArrayList result, int total) {
                        if (result != null && result.size() > 0) {
                            listId = "";
                            for (int i = 0; i < result.size(); i++) {
                                NearlyMapItem item = (NearlyMapItem) result.get(i);
                                listId += (listId == "" ? "" : ",") + item.getListId();
                                LatLng point = new LatLng(Double.parseDouble(item.getXPoint()), Double.parseDouble(item.getYPoint()));
                                placeOrderMarker(point, item);
                            }
                        }
                        rotateLoading.stop();
                    }
                }, ShippingOrderService.get_instance(myContext), sessionManager.getShipperId(), actFrom.getText().toString(), String.valueOf(_start.latitude), String.valueOf(_start.longitude), actTo.getText().toString(), String.valueOf(_end.latitude), String.valueOf(_end.longitude)).execute();
            }
        });

        gd.request(_start, _end, GoogleDirection.MODE_DRIVING, null);
    }

//endregion

    //region utils
    public void initSheetBottom(Marker marker) {

        nearlyItem = markersIdentify.get(marker);

        hideOrderInfoLayout();

        if (nearlyItem != null) {

            loadingLayout.setVisibility(View.VISIBLE);

            if (nearlyItem.getTotal() > 1) {
                list.clear();
                new GetListShipOfListId(myContext, new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(ArrayList result, int total) {
                        list = result;

                        loadingLayout.setVisibility(View.GONE);
                        hideOrderInfoLayout();
                        actionLayout.setVisibility(View.VISIBLE);
                    }
                }).execute(nearlyItem.getListId());
            } else {

                loadingLayout.setVisibility(View.GONE);

                if (detailInfoLayout.getVisibility() == View.GONE) {
                    //detailInfoLayout.startAnimation(fadeIn);
                    detailInfoLayout.setVisibility(View.VISIBLE);
                }

                if (actionLayout.getVisibility() == View.VISIBLE) {
                    //actionLayout.startAnimation(fadeOut);
                    actionLayout.setVisibility(View.GONE);
                }

                if (nearlyItem.getShipItem().isUrgent()) {
                    detailInfoLayout.findViewById(R.id.urgent_banner).setVisibility(View.VISIBLE);
                    detailInfoLayout.findViewById(R.id.divider_line).setVisibility(View.VISIBLE);
                } else {
                    detailInfoLayout.findViewById(R.id.urgent_banner).setVisibility(View.GONE);
                    detailInfoLayout.findViewById(R.id.divider_line).setVisibility(View.GONE);
                }
                if (nearlyItem.getShipItem().isHasPromotionCode()) {
                    detailInfoLayout.findViewById(R.id.iv_promotion_label).setVisibility(View.VISIBLE);
                } else {
                    detailInfoLayout.findViewById(R.id.iv_promotion_label).setVisibility(View.GONE);
                }

                Utils.setImageToImageView(myContext, nearlyItem.getShipItem().getUrlPicture(), (ImageView) rootView.findViewById(R.id.order_image));
                Utils.setImageToImageViewNoDefault(myContext, nearlyItem.getShipItem().getUrlPictureLabel(), (ImageView) rootView.findViewById(R.id.order_image_label));

                ((TextView) rootView.findViewById(R.id.order_time_create)).setText(nearlyItem.getShipItem().getDateCreated());
                ((TextView) rootView.findViewById(R.id.order_start_date)).setText(nearlyItem.getShipItem().getDateEnd());
                ((TextView) rootView.findViewById(R.id.order_shipping_cost)).setText(nearlyItem.getShipItem().getStrCostShip());
                ((TextView) rootView.findViewById(R.id.order_pre_pay)).setText(nearlyItem.getShipItem().getStrTotalValue());
                ((TextView) rootView.findViewById(R.id.shipping_from)).setText(nearlyItem.getShipItem().getOrderFrom().getAddress());

                branching(rootView, nearlyItem.getShipItem());
            }
        }
    }

    public void branching(View rootView, final ShippingOrderItem model) {
        LinearLayout addressBox = (LinearLayout) rootView.findViewById(R.id.list_address_delivery);
        LayoutInflater layoutInflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ((TextView) rootView.findViewById(R.id.shipping_from)).setText(nearlyItem.getShipItem().getOrderFrom().getAddress());

        if (model.isGroup()) {

            try {
                ((View) rootView.findViewById(R.id.order_distance).getParent()).setVisibility(View.GONE);
            } catch (Exception e) {
                // if the view is top most view
            }

            for (int i = 0; i < model.getListShipToOfGroup().size(); i++) {
                final DeliveryToItem item = model.getListShipToOfGroup().get(i);
                //delivery address view
                View deliveryView = layoutInflater.inflate(R.layout.delivery_address_info_layout_item, null);
                ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.directions, 0);
                ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setText(item.getAddress());
                ((TextView) deliveryView.findViewById(R.id.add_index)).setText(String.valueOf(i + 1));
                if (i == model.getListShipToOfGroup().size() - 1) {
                    deliveryView.findViewById(R.id.dead_line).setVisibility(View.INVISIBLE);
                }

                deliveryView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<LatLng> locations = new ArrayList<>();
                        Intent mapIntent = new Intent(myContext, DirectionActivity.class);
                        Bundle locationsBnl = new Bundle();

                        if (model.getStatusId() != OrderStatusEnum.SHIPPING.getStatusCode())
                        {

                            locations.add(new LatLng(Double.parseDouble(model.getOrderFrom().getLatitude()),
                                    Double.parseDouble(model.getOrderFrom().getLongitude())));
                        }

                        locations.add(new LatLng(item.getXPoint(), item.getYPoint()));
                        locationsBnl.putSerializable("locations", locations);
                        mapIntent.putExtras(locationsBnl);
                        myContext.startActivity(mapIntent);
                    }
                });
                addressBox.addView(deliveryView);
            }
        } else {
            ((TextView) rootView.findViewById(R.id.order_distance)).setText(nearlyItem.getShipItem().getDistance());
            View deliveryView = layoutInflater.inflate(R.layout.delivery_address_info_layout_item, null);
            ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setText(model.getShippingTo().getAddress());
            deliveryView.findViewById(R.id.dead_line).setVisibility(View.INVISIBLE);

            ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.directions, 0);
            deliveryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<LatLng> locations = new ArrayList<>();
                    Intent mapIntent = new Intent(myContext, DirectionActivity.class);
                    Bundle locationsBnl = new Bundle();

                    if (model.getStatusId() != OrderStatusEnum.SHIPPING.getStatusCode()) {

                        locations.add(new LatLng(Double.parseDouble(model.getOrderFrom().getLatitude()),
                                Double.parseDouble(model.getOrderFrom().getLongitude())));
                    }

                    locations.add(new LatLng(Double.parseDouble(model.getShippingTo().getLatitude()),
                            Double.parseDouble(model.getShippingTo().getLongitude())));
                    locationsBnl.putSerializable("locations", locations);
                    mapIntent.putExtras(locationsBnl);
                    myContext.startActivity(mapIntent);
                }
            });

            addressBox.addView(deliveryView);
        }
    }

    public void placeOrderMarker(LatLng position, NearlyMapItem item) {
        Bitmap marker;
        if (item.getTotal() > 1) {
            marker = Utils.writeTextOnDrawable(
                    myContext,
                    R.drawable.flag_empty,
                    String.valueOf(item.getTotal()),
                    R.color.colorPrimary);

        } else {
            if (item.getShipItem().isGroup() && item.getShipItem().getListShipToOfGroup().size() > 1) {
                marker = Utils.writeTextOnDrawable(
                        myContext,
                        item.getShipItem().isUrgent() ? R.drawable.ic_group_urgent_order : R.drawable.flag_empty,
                        String.valueOf(item.getShipItem().getListShipToOfGroup().size()),
                        item.getShipItem().isUrgent() ? R.color.order_urgent_text : R.color.colorPrimary);
            } else {
                marker = BitmapFactory.decodeResource(myContext.getResources(), item.getShipItem().isUrgent() ? R.drawable.ic_urgent_order : R.drawable.flag);
            }
        }
        Marker orderMrk = mMap.addMarker(
                new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(marker)));
        markersIdentify.put(orderMrk, item);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    //endregion
}

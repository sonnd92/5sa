package fiveship.vn.fiveship.activity.shipper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.AssignShipAction;
import fiveship.vn.fiveship.interfaces.LocationEventListener;
import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.DeliveryToItem;
import fiveship.vn.fiveship.model.NearlyMapItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.service.adapter.GetListShipOfListId;
import fiveship.vn.fiveship.service.adapter.GetNearlyShippingOrderForMap;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;

public class MapFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available

    private static final int MILLISECONDS_PER_SECOND = 1000;

    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private View rootView;
    private Context myContext;
    private SessionManager sessionManager;

    private View loadingLayout;
    private HashMap<Marker, NearlyMapItem> markersIdentify;
    protected GoogleApiClient mGoogleApiClient;
    private LinearLayout detailInfoLayout;
    private LinearLayout actionLayout;
    private int totalResult;
    private NearlyMapItem nearlyItem;

    private ArrayList<ShippingOrderItem> list = new ArrayList();

    private boolean callbackSuccess = true;
    private RotateLoading rotateLoading;
    //private Dialog loadingDialog;

    public static MapFragment newInstance() {
        MapFragment instance = new MapFragment();
        return instance;
    }

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_map, container, false);
        //setupToolBar();
        //set total
        loadingLayout = rootView.findViewById(R.id.layout_loading);
        loadingLayout.setVisibility(View.GONE);

        rootView.findViewById(R.id.filter_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchActivity = new Intent(myContext, SearchActivity.class);
                startActivity(searchActivity);
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
                ArrayList<LatLng> locations = new ArrayList<>();

                Intent mapIntent = new Intent(myContext, DirectionActivity.class);
                Bundle locationsBnl = new Bundle();

                if (nearlyItem != null) {

                    locations.add(new LatLng(Double.parseDouble(list.get(0).getOrderFrom().getLatitude())
                            , Double.parseDouble(list.get(0).getOrderFrom().getLongitude())));

                    for (ShippingOrderItem s : list) {
                        if (s.isGroup()) {
                            for (DeliveryToItem d : s.getListShipToOfGroup())
                            {
                                locations.add(new LatLng(d.getXPoint(), d.getYPoint()));
                            }
                        } else {
                            locations.add(new LatLng(Double.parseDouble(s.getShippingTo().getLatitude())
                                    , Double.parseDouble(s.getShippingTo().getLongitude())));
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

        rootView.findViewById(R.id.action_direction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<LatLng> locations = new ArrayList<>();
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

        rootView.findViewById(R.id.action_recommend_shipping_cost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setupRecommendShippingCostDialog(myContext, nearlyItem.getShipItem()).show();
            }
        });

        rotateLoading = ((RotateLoading) rootView.findViewById(R.id.rotateloading));

        detailInfoLayout = (LinearLayout) rootView.findViewById(R.id.detail_info);

        detailInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent detail = new Intent(myContext, DetailOrderActivity.class);
                detail.putExtra("OrderId", nearlyItem.getShipItem().getId());
                startActivity(detail);

            }
        });

        actionLayout = (LinearLayout) rootView.findViewById(R.id.map_actions);

        sessionManager = new SessionManager(myContext);

        rootView.findViewById(R.id.btn_list_near).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listNear = new Intent(myContext, ShipperListNearActivity.class);
                startActivity(listNear);
            }
        });

        markersIdentify = new HashMap<>();

        int idGG = new Random().nextInt(10000);

        mGoogleApiClient = new GoogleApiClient.Builder(myContext)
                .enableAutoManage(getActivity(), idGG /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        setUpMapIfNeeded();

        return rootView;
    }

    public void fetchData(Location location) {
        ((TextView) rootView.findViewById(R.id.total_order)).setText(myContext.getString(R.string.three_dots));
        callbackSuccess = false;
        OnTaskCompleted callback = new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList result, int total) {
                callbackSuccess = true;

                rotateLoading.stop();

                totalResult = total;

                ((TextView) rootView.findViewById(R.id.total_order)).setText(" " + totalResult + " ");

                if (result != null && result.size() > 0) {
                    for (int i = 0; i < result.size(); i++) {
                        NearlyMapItem item = (NearlyMapItem) result.get(i);
                        LatLng point = new LatLng(Double.parseDouble(item.getXPoint()), Double.parseDouble(item.getYPoint()));
                        placeOrderMarker(point, item);
                    }
                }

            }
        };
        //new GetNearlyShippingOrder(callback, keyword, 1, top).execute();
        String keyword = "";
        int top = 100;
        new GetNearlyShippingOrderForMap(callback,
                ShippingOrderService.get_instance(myContext),
                sessionManager.getShipperId(),
                keyword,
                1,
                top,
                String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude())).execute();
    }

    @Override
    public void onAttach(Context con) {
        myContext = con;
        super.onAttach(con);
    }

    public void clearAllMarker() {
        mMap.clear();
    }

    public void hideOrderInfoLayout() {
        //actionLayout.startAnimation(fadeOut);
        actionLayout.setVisibility(View.GONE);
        //detailInfoLayout.startAnimation(fadeOut);
        detailInfoLayout.setVisibility(View.GONE);
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
           ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        }
    }

    public void onLocationChanged(Location location) {
        rotateLoading.start();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        clearAllMarker();
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.face))
                .draggable(false));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                latLng, 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to location user
                .zoom(13)                   // Sets the zoom
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        fetchData(location);
    }

    //endregion

    //region utils
    public void initSheetBottom(Marker marker) {

        nearlyItem = markersIdentify.get(marker);

        hideOrderInfoLayout();

        if (nearlyItem != null) {

            loadingLayout.setVisibility(View.VISIBLE);

            if (nearlyItem.getTotal() > 1) {
                new GetListShipOfListId(myContext, new OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted(ArrayList result, int total) {

                        list = result;

                        actionLayout.setVisibility(View.VISIBLE);

                        loadingLayout.setVisibility(View.GONE);
                    }
                }).execute(nearlyItem.getListId());
            } else {
                LinearLayout addressBox = (LinearLayout) rootView.findViewById(R.id.list_address_delivery);
                addressBox.removeAllViews();
                loadingLayout.setVisibility(View.GONE);

                detailInfoLayout.setVisibility(View.VISIBLE);
                if(nearlyItem.getShipItem().isUrgent()) {
                    detailInfoLayout.findViewById(R.id.urgent_banner).setVisibility(View.VISIBLE);
                    detailInfoLayout.findViewById(R.id.divider_line).setVisibility(View.VISIBLE);
                }else{
                    detailInfoLayout.findViewById(R.id.urgent_banner).setVisibility(View.GONE);
                    detailInfoLayout.findViewById(R.id.divider_line).setVisibility(View.GONE);
                }

                if(nearlyItem.getShipItem().isHasPromotionCode()){
                    detailInfoLayout.findViewById(R.id.iv_promotion_label).setVisibility(View.VISIBLE);
                }else{
                    detailInfoLayout.findViewById(R.id.iv_promotion_label).setVisibility(View.GONE);
                }

                /*detailInfoLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return onTouchOrder(v, event);
                    }
                });*/

                Utils.setImageToImageView(myContext, nearlyItem.getShipItem().getUrlPicture(), (ImageView) rootView.findViewById(R.id.order_image));
                Utils.setImageToImageViewNoDefault(myContext, nearlyItem.getShipItem().getUrlPictureLabel(), (ImageView) rootView.findViewById(R.id.order_image_label));

                ((TextView) rootView.findViewById(R.id.order_time_create)).setText(nearlyItem.getShipItem().getDateCreated());
                ((TextView) rootView.findViewById(R.id.order_start_date)).setText(nearlyItem.getShipItem().getDateEnd());
                ((TextView) rootView.findViewById(R.id.order_shipping_cost)).setText(nearlyItem.getShipItem().getStrCostShip());
                ((TextView) rootView.findViewById(R.id.order_pre_pay)).setText(nearlyItem.getShipItem().getStrTotalValue());

                branching(rootView, nearlyItem.getShipItem());
            }
        }
    }

    /*float dY;

    public boolean onTouchOrder(View view, MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                dY = view.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:

                view.animate()
                        .y(event.getRawY() + dY)
                        .setDuration(0)
                        .start();
                break;
            default:
                return false;
        }
        return true;
    }*/

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

                        locations.add(new LatLng(item.getXPoint(),item.getYPoint() ));
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

                    if (model.getStatusId() != OrderStatusEnum.SHIPPING.getStatusCode())
                    {
                        locations.add(new LatLng(Double.parseDouble(model.getOrderFrom().getLatitude())
                                , Double.parseDouble(model.getOrderFrom().getLongitude())));
                    }

                    locations.add(new LatLng(Double.parseDouble(model.getShippingTo().getLatitude())
                            ,Double.parseDouble(model.getShippingTo().getLongitude())));

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
                marker = BitmapFactory.decodeResource(myContext.getResources(),item.getShipItem().isUrgent() ? R.drawable.ic_urgent_order : R.drawable.flag);
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
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Config.DEFAULT_LATITUDE, Config.DEFAULT_LONGITUDE), Config.DEFAULT_MAP_ZOOM));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {

                initSheetBottom(marker);

                return true;
            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                hideOrderInfoLayout();
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideOrderInfoLayout();
            }
        });
        if ((ActivityCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(myContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        }

        new GPSTracker(myContext, new LocationEventListener() {
            @Override
            public void OnLocationStateChange(int status) {
            }

            @Override
            public void OnLocationChange(final Location location) {
                onLocationChanged(location);

                rootView.findViewById(R.id.btn_list_refresh).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (callbackSuccess) {
                            onLocationChanged(location);
                        }
                    }
                });
            }
        });
    }
    //endregion
}

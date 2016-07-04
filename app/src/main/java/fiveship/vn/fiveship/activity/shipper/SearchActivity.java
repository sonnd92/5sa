package fiveship.vn.fiveship.activity.shipper;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Random;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.RegisterActivity;
import fiveship.vn.fiveship.adapter.PlacesAutoCompleteAdapter;
import fiveship.vn.fiveship.adapter.ShippingOrderAdapter;
import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.adapter.SearchShippingOrder;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private View filterBox;
    private View filterButton;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private View vs_noResult;
    private View footerLoading;
    private Animation fadeIn, fadeOut;
    private ArrayList<ShippingOrderItem> data = new ArrayList();
    private ShippingOrderAdapter shippingOrderAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    ActionBar action;

    boolean isFilter;
    int skip;
    int top = 10;
    int preLast;
    int pageSize = 10;
    boolean isLoadMore;
    double xFrom;
    double yFrom;
    double xTo;
    double yTo;

    int range_from;
    int range_to;
    boolean is_big = false;
    boolean is_fragile = false;
    boolean is_weight = false;
    int pre_pay_to;
    int pre_pay_from;
    int shipping_cost_from;
    int shipping_cost_to;
    TextView cb_is_big;
    TextView cb_is_fragile;
    TextView cb_is_weight;

    AutoCompleteTextView startAddress;
    AutoCompleteTextView endAddress;

    RangeBar rgbShippingCost;
    RangeBar rgbPrePay;
    RangeBar rgbDistance;

    LatLng from;
    LatLng to;
    SessionManager sessionManager;


    protected GoogleApiClient mGoogleApiClient;
    private PlacesAutoCompleteAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Setup toolbar
        setupToolBar();
        recyclerView = (RecyclerView) findViewById(R.id.rv_data_binding);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (isLoadMore) {

                    int visibleItemCount = recyclerView.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                    final int lastItem = firstVisibleItem + visibleItemCount;

                    if (lastItem == totalItemCount && lastItem != -0) {
                        if (preLast < lastItem) {
                            skip = ((lastItem - 1) / top) + 1;

                            OnTaskCompleted onTaskCompleted = new OnTaskCompleted() {
                                @Override
                                public void onTaskCompleted(ArrayList result, int total) {

                                    if (result.size() > 0) {
                                        data.addAll(result);

                                        if (skip * pageSize >= total) {

                                            isLoadMore = false;

//                                    recyclerView.removeFooterView(footerLoading);
                                        }

                                        dataSaveChange();

                                    } else {
//                                recyclerView.removeFooterView(footerLoading);
                                    }
                                }
                            };
                            new SearchShippingOrder(
                                    onTaskCompleted, SearchActivity.this, sessionManager.getShipperId(), shipping_cost_from, shipping_cost_to, range_from, range_to, pre_pay_from, pre_pay_to, is_fragile, is_big,
                                    skip,
                                    top,
                                    xFrom,
                                    yFrom,
                                    xTo,
                                    yTo
                            ).execute();
                            preLast = lastItem;
                        }
                    }
                }
            }
        });

        sessionManager = new SessionManager(this);

        //Setup loader
        vs_noResult = findViewById(R.id.vs_no_result);

        LayoutInflater inflater = (LayoutInflater) getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);

        footerLoading = inflater.inflate(R.layout.listview_footer_loadmore, recyclerView, false);

        //Fade anim
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);

        //Init rootView
        filterBox = findViewById(R.id.box_filter);

        filterButton = findViewById(R.id.box_button);

        initView();

        //initFilterPopup();
    }

    public void initView() {

        findViewById(R.id.layout_loading).startAnimation(fadeOut);
        findViewById(R.id.layout_loading).setVisibility(View.GONE);

        findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
                fetchData();
            }
        });

        int idGG = new Random().nextInt(10000);

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, idGG /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        //init view
        cb_is_big = (TextView) findViewById(R.id.cb_is_big);
        cb_is_fragile = (TextView) findViewById(R.id.cb_is_fragile);
        cb_is_weight = (TextView) findViewById(R.id.cb_is_weight);

        //init autocomplete address

        mAdapter = new PlacesAutoCompleteAdapter(getApplicationContext(), mGoogleApiClient, RegisterActivity.BOUNDS_GREATER_VIETNAM,
                null);

        startAddress = (AutoCompleteTextView) findViewById(R.id.start_address);
        endAddress = (AutoCompleteTextView) findViewById(R.id.end_address);

        startAddress.setAdapter(mAdapter);
        startAddress.setThreshold(2);
        endAddress.setAdapter(mAdapter);
        endAddress.setThreshold(2);

        cb_is_fragile.setOnClickListener(this);
        cb_is_weight.setOnClickListener(this);
        cb_is_big.setOnClickListener(this);

        startAddress.setOnItemClickListener(fromAutocompleteClickListener);
        startAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                xFrom = 0;
                yFrom = 0;
            }
        });
        endAddress.setOnItemClickListener(toAutocompleteClickListener);
        endAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                xTo = 0;
                yTo = 0;
            }
        });

        //pre pay
        rgbPrePay = (RangeBar) findViewById(R.id.range_bar_pre_pay);

        rgbPrePay.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                pre_pay_from = Integer.parseInt(leftPinValue) * 1000;
                pre_pay_to = Float.parseFloat(rightPinValue) < rgbPrePay.getTickEnd()
                                ? Integer.parseInt(rightPinValue) * 1000 : 0;

            }
        });

        //shipping cost
        rgbShippingCost = (RangeBar) findViewById(R.id.range_bar_shipping_cost);

        rgbShippingCost.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                shipping_cost_from = Integer.parseInt(leftPinValue)* 1000;
                shipping_cost_to = Float.parseFloat(rightPinValue) < rgbShippingCost.getTickEnd()
                                    ?  Integer.parseInt(rightPinValue)* 1000 : 0;
            }
        });

        //distance
        rgbDistance = (RangeBar) findViewById(R.id.range_bar_distance);

        rgbDistance.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                range_from = Integer.parseInt(leftPinValue);
                range_to = Float.parseFloat(rightPinValue) < rgbDistance.getTickEnd()
                            ? Integer.parseInt(rightPinValue) : 0;
            }
        });

        findViewById(R.id.btn_filter).setOnClickListener(this);

        findViewById(R.id.btn_reset).setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    public void setupToolBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_filter:
                showFilter();
                findViewById(R.id.box_result).setVisibility(View.GONE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void resetInput() {

        is_big = true;
        is_fragile = true;
        is_weight = true;

        changeIsBig(cb_is_big);
        changeIsFragile(cb_is_fragile);
        changeIsWeight(cb_is_weight);

        endAddress.setText("");
        startAddress.setText("");

    }

    public void changeIsBig(View view) {
        is_big = !is_big;
        ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(0, 0, is_big ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);
    }

    public void changeIsFragile(View view) {
        is_fragile = !is_fragile;
        ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(0, 0, is_fragile ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);
    }

    public void changeIsWeight(View view) {
        is_weight = !is_weight;
        ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(0, 0, is_weight ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);
    }

    private AdapterView.OnItemClickListener fromAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdateFromPlaceCallback);
        }
    };

    private AdapterView.OnItemClickListener toAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdateToPlaceCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdateFromPlaceCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }

            final Place place = places.get(0);
            from = place.getLatLng();
            if(from != null) {
                xFrom = from.latitude;
                yFrom = from.longitude;
            }

            places.release();
        }
    };

    private ResultCallback<PlaceBuffer> mUpdateToPlaceCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }
            final Place place = places.get(0);
            to = place.getLatLng();
            if(to != null) {
                xTo = to.latitude;
                yTo = to.longitude;
            }
            places.release();
        }
    };

    public void fetchData() {
        findViewById(R.id.box_result).setVisibility(View.VISIBLE);
        hideFilter();
//                String start_address_val = startAddress.getText().toString();
//                String end_address_val = endAddress.getText().toString();

        isFilter = true;
        preLast = 0;
        skip = 1;
        isLoadMore = true;
        findViewById(R.id.layout_loading).setAnimation(fadeIn);
        findViewById(R.id.layout_loading).setVisibility(View.VISIBLE);
        if (vs_noResult != null && vs_noResult.getVisibility() == View.VISIBLE) {

            vs_noResult.setVisibility(View.GONE);

        }
        if (data != null) {
            data.clear();
        }
//        if (recyclerView.getFooterViewsCount() > 0) {
//            recyclerView.removeFooterView(footerLoading);
//        }
        OnTaskCompleted onTaskCompleted = new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList result, int total) {

                swipeRefreshLayout.setRefreshing(false);

                isFilter = false;

                if (total > 0) {
                    data.addAll(result);

                    if (skip * pageSize < total) {
//                        if (recyclerView.getFooterViewsCount() < 1)
//                            recyclerView.addFooterView(footerLoading, null, false);
                    } else {
                        isLoadMore = true;
                    }
                    dataSaveChange();

                } else {
                    vs_noResult.setVisibility(View.VISIBLE);
                }

//                recyclerView.setSelectionAfterHeaderView();
                findViewById(R.id.layout_loading).setAnimation(fadeOut);
                findViewById(R.id.layout_loading).setVisibility(View.GONE);
            }
        };

        new SearchShippingOrder(
                onTaskCompleted,
                this,
                sessionManager.getShipperId(),
                this.shipping_cost_from,
                this.shipping_cost_to,
                this.range_from,
                this.range_to,
                this.pre_pay_from,
                this.pre_pay_to,
                this.is_fragile,
                this.is_big,
                skip,
                top,
                xFrom,
                yFrom,
                xTo,
                yTo
        ).execute();
    }

    public void hideFilter() {
        filterButton.setVisibility(View.GONE);
        filterBox.setVisibility(View.GONE);
    }

    public void showFilter() {
        filterButton.setVisibility(View.VISIBLE);
        filterBox.setVisibility(View.VISIBLE);
    }

    public void dataSaveChange() {

        if (shippingOrderAdapter != null) {

            shippingOrderAdapter.notifyDataSetChanged();

        } else {

            shippingOrderAdapter = new ShippingOrderAdapter(SearchActivity.this, data);
            recyclerView.setAdapter(shippingOrderAdapter);
            recyclerView.startAnimation(fadeIn);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb_is_big:
                changeIsBig(v);
                break;
            case R.id.cb_is_fragile:
                changeIsFragile(v);
                break;
            case R.id.cb_is_weight:
                changeIsWeight(v);
                break;
            case R.id.btn_filter:
                Utils.hideKeyboard(this, v);
                if (data != null) {
                    data.clear();
                }

                fetchData();

                break;
            case R.id.btn_reset:
                resetInput();
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
        if (data.isEmpty() || filterBox.getVisibility() == View.GONE) {
            super.onBackPressed();
        } else {
            hideFilter();
            findViewById(R.id.box_result).setVisibility(View.VISIBLE);
        }
    }
}

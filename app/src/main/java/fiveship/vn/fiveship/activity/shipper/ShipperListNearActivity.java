package fiveship.vn.fiveship.activity.shipper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.ShippingOrderAdapter;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.service.adapter.GetNearlyShippingOrder;
import fiveship.vn.fiveship.service.adapter.GetNewShippingOrder;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.OnScrollListenerFactory;
import fiveship.vn.fiveship.utility.SessionManager;

public class ShipperListNearActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private InternalOnScrollListener mDataListener;

    private View mViewNoResult;
    private Animation mFadeIn, mFadeOut;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    String mKeyWord = "";

    SessionManager mSessionManager;
    GPSTracker mGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shipper_list_near_activity);

        setupToolBar();

        mGPS = new GPSTracker(this);
        mSessionManager = new SessionManager(this);

        // set total
        findViewById(R.id.filter_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchActivity = new Intent(ShipperListNearActivity.this, SearchActivity.class);
                startActivity(searchActivity);
            }
        });

        findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataListener.refresh();
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                mSwipeRefreshLayout.setEnabled(false);
                mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(ShipperListNearActivity.this, R.color.colorPrimary));
                mDataListener.refresh();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_data_binding);
        // setup loader
        mViewNoResult = findViewById(R.id.vs_no_result);
        // fade anim
        mFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        mFadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);

        mDataListener = new InternalOnScrollListener(this);
        if (!mDataListener.isDataLoaded()) {
            mDataListener.fetchData();
        }
    }

    public void setupToolBar()
    {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //getSupportActionBar().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    private class InternalOnScrollListener extends OnScrollListenerFactory {

        public InternalOnScrollListener(Context mContext) {
            super(mContext);
        }

        @Override
        public void onPreLoad() {

        }

        @Override
        public void onLoaded(boolean mIsSuccess, int mTotal) {

            ((TextView) findViewById(R.id.total_order)).setText(String.valueOf(" " + mTotal + " "));

            findViewById(R.id.layout_loading).setAnimation(mFadeOut);
            findViewById(R.id.layout_loading).setVisibility(View.GONE);
            if(mIsSuccess) {
                mViewNoResult.setVisibility(View.GONE);
                mRecyclerView.setAnimation(mFadeIn);
            }else{
                mViewNoResult.setVisibility(View.VISIBLE);
            }
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onScrolled(boolean mIsSuccess) {

        }

        @Override
        public RecyclerView.Adapter getAdapter(ArrayList data) {
            return new ShippingOrderAdapter(ShipperListNearActivity.this, data);
        }

        @Override
        public AsyncTask<String, Void, JSONObject> getAsyncRequest() {
            return new GetNearlyShippingOrder(getCallbackMethod(),
                    ShippingOrderService.get_instance(ShipperListNearActivity.this),
                    mSessionManager.getShipperId(),
                    mKeyWord,
                    String.valueOf(mGPS.getLatitude()),
                    String.valueOf(mGPS.getLongitude()));
        }

        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }
    }
}

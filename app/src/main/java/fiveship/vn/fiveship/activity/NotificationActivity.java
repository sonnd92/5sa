package fiveship.vn.fiveship.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;
import java.util.ArrayList;
import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.NewsAdapter;
import fiveship.vn.fiveship.adapter.NotificationAdapter;
import fiveship.vn.fiveship.adapter.ShippingOrderAdapter;
import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.NotificationItem;
import fiveship.vn.fiveship.service.NewsService;
import fiveship.vn.fiveship.service.NotificationService;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.service.adapter.GetNearlyShippingOrder;
import fiveship.vn.fiveship.utility.InfiniteScrollListener;
import fiveship.vn.fiveship.utility.Notification;
import fiveship.vn.fiveship.utility.OnScrollListenerFactory;
import fiveship.vn.fiveship.utility.SessionManager;
import it.sephiroth.android.library.tooltip.Tooltip;

public class NotificationActivity extends AppCompatActivity {

    private SessionManager mSessionManager;

    private NotificationService mService;
    private Animation mFadeIn, mFadeOut;
    private SwitchCompat mSwitchCompat;

    private RecyclerView mRecyclerView;
    private View mViewNoResult;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private InternalOnScrollListener mDataListener;

    public NotificationActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        setupToolBar();

        mDataListener = new InternalOnScrollListener(this);

        mService = NotificationService.get_instance(this);

        mSessionManager = new SessionManager(getApplication());

        mFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        mFadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        mViewNoResult = findViewById(R.id.vs_no_result);

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_data_binding);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(NotificationActivity.this, R.color.colorPrimary));
                mDataListener.refresh();
            }
        });

        mSwitchCompat = ((SwitchCompat) findViewById(R.id.notification_switch));
        mSwitchCompat.setChecked(mSessionManager.isNotificationOn());

        findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataListener.refresh();
            }
        });

        mSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSessionManager.setIsOnNotification(isChecked);
                if(isChecked){
                    startService(new Intent(NotificationActivity.this, FShipService.class));
                }else{
                    Tooltip.make(NotificationActivity.this,
                            new Tooltip.Builder(101)
                                    .anchor(buttonView, Tooltip.Gravity.BOTTOM)
                                    .closePolicy(new Tooltip.ClosePolicy()
                                            .insidePolicy(true, false)
                                            .outsidePolicy(true, false), 3000)
                                    .text("Bạn sẽ không nhận được thông báo về đơn hàng mới.")
                                    .withArrow(true)
                                    .withStyleId(R.style.TooltipLayoutStlye)
                                    .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                    .maxWidth(getResources(), R.dimen.tool_tip_max_width)
                                    .withOverlay(true)
                                    .build())
                            .show();

                    Intent i = new Intent(NotificationActivity.this, FShipService.class);
                    stopService(i);

                    PendingIntent pi = PendingIntent.getService(NotificationActivity.this, 0, i, 0);
                    AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
                    alarmMgr.cancel(pi);
                }
            }
        });

        mDataListener.fetchData();
    }

    public void setupToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.notification_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
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
            return new NotificationAdapter(NotificationActivity.this, data, mSessionManager.isShipper());
        }

        @Override
        public AsyncTask<String, Void, JSONObject> getAsyncRequest() {
            return new GetListNotification(getCallbackMethod());
        }

        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }
    }

    private class GetListNotification extends AsyncTask<String, Void, JSONObject> {
        private OnTaskCompleted mListener;

        public GetListNotification (OnTaskCompleted listener)     {
            this.mListener = listener;
        }
        @Override
        protected JSONObject doInBackground(String... params) {
            return mService.GetList(mSessionManager.isShipper() ? mSessionManager.getShipperId() : mSessionManager.getShopId()
                    , !mSessionManager.isShipper()
                    , Integer.parseInt(params[0])
                    , Integer.parseInt(params[1]));
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            ArrayList<NotificationItem> mData = new ArrayList<NotificationItem>();
            int total = 0;
            try {

                if (result != null && result.length() > 0 && ((result.has("error") && !result.getBoolean("error")) || !result.has("error"))) {

                    total = result.getInt("total");

                    for (int i = 0; i < result.getJSONArray("data").length(); i++) {

                        NotificationItem noti = new NotificationItem();

                        noti.setId(result.getJSONArray("data").getJSONObject(i).getInt("Id"));
                        noti.setName(result.getJSONArray("data").getJSONObject(i).getString("Name"));
                        noti.setUrlAvatar(result.getJSONArray("data").getJSONObject(i).getString("Avatar"));
                        noti.setTypeId(result.getJSONArray("data").getJSONObject(i).getInt("TypeId"));
                        noti.setShipId(result.getJSONArray("data").getJSONObject(i).getInt("ShipId"));
                        noti.setDateCreated(result.getJSONArray("data").getJSONObject(i).getString("DateCreated"));
                        noti.setShipperId(result.getJSONArray("data").getJSONObject(i).getInt("ShipperId"));
                        noti.setShopId(result.getJSONArray("data").getJSONObject(i).getInt("ShopId"));
                        noti.setIsView(result.getJSONArray("data").getJSONObject(i).getBoolean("IsView"));
                        noti.setData(result.getJSONArray("data").getJSONObject(i).getString("Data"));

                        mData.add(noti);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mListener.onTaskCompleted(mData, total);
        }
    }

}

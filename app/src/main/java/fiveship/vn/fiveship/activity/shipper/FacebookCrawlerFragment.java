package fiveship.vn.fiveship.activity.shipper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.FShipService;
import fiveship.vn.fiveship.adapter.CrawlerFacebookAdapter;
import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.FacebookFeedCrawlerItem;
import fiveship.vn.fiveship.service.OtherService;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.OnScrollListenerFactory;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

public class FacebookCrawlerFragment extends Fragment {

    private FragmentActivity mContext;
    private View mRootView;
    private RecyclerView mRecyclerView;
    private View mViewNoResult;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private InternalScrollListener mDataListener;

    private SessionManager mSessionMng;
    private String mClientMQTTId;
    private MqttAndroidClient client;
    private IMqttToken token;
    private Runnable reconnect;
    private Runnable keepAlive;
    private final int KEEP_ALIVE_TIME_INTERVAL = 30 * 1000;
    private final int RETRY_TIME_INTERVAL = 15 * 1000;
    private final int MAX_DATA_SIZE = 30;
    private boolean mKeepAlive;
    private boolean mRetryConnect;
    private boolean mDataLoaded = false;
    private boolean mActivityStopped;
    private GPSTracker mGpsTracker;
    private final double LATITUDE_OF_BOUNDARY = 17d;
    private boolean mIsBlock;
    private String mBlockMessage;

    public FacebookCrawlerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FacebookCrawlerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FacebookCrawlerFragment newInstance() {
        FacebookCrawlerFragment fragment = new FacebookCrawlerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mSessionMng = new SessionManager(mContext);
        mGpsTracker = new GPSTracker(mContext);
        mDataListener = new InternalScrollListener(mContext);

        mRootView = inflater.inflate(R.layout.fragment_facebook_crawler, container, false);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.rv_data_binding);
        mViewNoResult = mRootView.findViewById(R.id.vs_no_result);

        mFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.abc_fade_in);
        mFadeOut = AnimationUtils.loadAnimation(mContext, R.anim.abc_fade_out);

        mRootView.findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataListener.refresh();
            }
        });

        mRootView.findViewById(R.id.btn_pinned_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent favFeedIntent = new Intent(mContext, PinnedFbCrawlerActivity.class);
                mContext.startActivity(favFeedIntent);
            }
        });

        mDataListener.fetchData();
        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        mContext = (FragmentActivity) context;
        super.onAttach(context);
    }

    //region mqtt service
    /*
     * connect to MQTT service method
     */
    private void connect() {

        mClientMQTTId = Utils.getMQTTClientId(mContext, mSessionMng, "_facebook");

        MqttConnectOptions options;
        options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(FShipService.USERNAME_MQTT);
        options.setPassword(FShipService.PASSWORD_MQTT.toCharArray());

        try {
            options.setWill(FShipService.TOPIC_WILL, mClientMQTTId.getBytes("UTF-8"), 1, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //options.setConnectionTimeout(5000);
        //options.setKeepAliveInterval(1000 * 60 * 90);

        client = new MqttAndroidClient(mContext, FShipService.SERVER_MQTT, mClientMQTTId);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                if (!mRetryConnect && !mActivityStopped) {
                    Toast.makeText(mContext, "Mất kết nối", Toast.LENGTH_SHORT).show();

                    enableRetryConnect();
                    stopKeepAlive();

                    reconnectIfNecessary();
                }
            }

            @Override
            public void messageArrived(String topic, final MqttMessage message) {

                FacebookFeedCrawlerItem notify;

                try {

                    JSONObject jObj = new JSONObject(new String(message.getPayload()));
                    if (mGpsTracker.getLatitude() != 0) {
                        if (jObj.has("IsInHCM") && jObj.getBoolean("IsInHCM") && Double.compare(mGpsTracker.getLatitude(), LATITUDE_OF_BOUNDARY) >= 0) {
                            return;
                        }

                        if ((!jObj.has("IsInHCM") || !jObj.getBoolean("IsInHCM")) && Double.compare(mGpsTracker.getLatitude(), LATITUDE_OF_BOUNDARY) < 0) {
                            return;
                        }
                    }

                    notify = new FacebookFeedCrawlerItem();

                    notify.setId(jObj.has("Id") ? jObj.getInt("Id") : 0);

                    notify.setId(jObj.getInt("Id"));
                    notify.setFeedId(jObj.getString("PostId"));
                    notify.setCreatorName(jObj.getString("Name"));
                    notify.setCreatorId(jObj.getString("UserId"));
                    notify.setContent(jObj.getString("Content"));
                    notify.setTimeCreated(jObj.getString("DateCreated"));
                    notify.setDistance(jObj.getString("Distance"));
                    notify.setPinned(false);
                    notify.setShowPinnedIcon(true);

                    ArrayList<String> listPhoneNumbers = new ArrayList<>();
                    for (int n = 0; n < jObj.getJSONArray("Phone").length(); n++) {
                        listPhoneNumbers.add(jObj.getJSONArray("Phone").get(n).toString());
                    }

                    notify.setPhoneNumbers(listPhoneNumbers);


                    ArrayList<FacebookFeedCrawlerItem> data = mDataListener.getData();
                    data.add(0, notify);

                    if(data.size() > MAX_DATA_SIZE){
                        data.remove(data.size() - 1);
                    }

                    mDataListener.setData(data);
                    mDataListener.notifyItemRemoved(data.size() - 1);
                    mDataListener.notifyItemInserted(0);

                    if (mRecyclerView.computeVerticalScrollOffset() == 0) {
                        mRecyclerView.smoothScrollToPosition(0);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

        });

        try {
            token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    if (!mKeepAlive && !mActivityStopped) {

                        stopRetryConnect();
                        enableKeepAlive();
                        subscribeNotify();
                        publishConnected();
                        startKeepAlive();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    if (!mRetryConnect && !mActivityStopped) {
                        enableRetryConnect();
                        stopKeepAlive();
                        reconnectIfNecessary();
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void publishConnected() {
        try {
            client.publish(FShipService.TOPIC_CONNECTED, mClientMQTTId.getBytes("UTF-8"), 1, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subscribeNotify() {
        try {
            String TOPIC_NOTIFY = "/5ship/shipper/facebook";
            String[] arrTopic = new String[]{TOPIC_NOTIFY};
            int[] arrQos = new int[]{2};
            client.subscribe(arrTopic, arrQos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // reconnect to mqtt server
    private synchronized void reconnect() {
        if (client == null || !client.isConnected()) {
            if (client != null)
                stop();
            connect();
        }
    }

    // keeping connection alive method
    private void startKeepAlive() {

        final Handler handler = new Handler();

        keepAlive = new Runnable() {
            @Override
            public void run() {

                if (mKeepAlive) {
                    reconnect();
                    handler.postDelayed(keepAlive, KEEP_ALIVE_TIME_INTERVAL);
                }
            }
        };

        handler.postDelayed(keepAlive, KEEP_ALIVE_TIME_INTERVAL);

    }

    //try to reconnect to server every specific second
    private void reconnectIfNecessary() {

        final Handler handler = new Handler();

        reconnect = new Runnable() {
            @Override
            public void run() {

                if (mRetryConnect) {
                    Toast.makeText(mContext, "Đang thử kết nối lại...", Toast.LENGTH_SHORT).show();
                    reconnect();
                    handler.postDelayed(reconnect, RETRY_TIME_INTERVAL);
                }
            }
        };

        handler.postDelayed(reconnect, RETRY_TIME_INTERVAL);

    }

    private void stopKeepAlive() {
        mKeepAlive = false;
    }

    private void enableKeepAlive() {
        mKeepAlive = true;
    }

    private void stopRetryConnect() {
        mRetryConnect = false;
    }

    private void enableRetryConnect() {
        mRetryConnect = true;
    }

    //disconnect to mqtt server
    private synchronized void stop() {
        // Disconnect the MQTT connection if there is one
        if (client != null) {
            try {
                client.disconnect();
                client = null;
                token = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //endregion

    public class InternalScrollListener extends OnScrollListenerFactory {

        public InternalScrollListener(final Context mContext) {
            super(mContext);
            setCanLoadMore(false);
            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                // we want to cache these and not allocate anything repeatedly in the onChildDraw method
                Drawable background;
                Drawable xMark;
                boolean initiated;
                int xMarkMargin;

                private void init() {

                    background = new ColorDrawable(ContextCompat.getColor(mContext, R.color.fb_crawler_backgroud));
                    xMark = ContextCompat.getDrawable(mContext, R.drawable.ic_fb_feed_delete);
                    xMarkMargin = (int) mContext.getResources().getDimension(R.dimen.ic_clear_margin);

                    initiated = true;

                }

                // not important, we don't want drag & drop
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    int swipedPosition = viewHolder.getAdapterPosition();
                    ArrayList<FacebookFeedCrawlerItem> data = getData();
                    data.remove(swipedPosition);
                    setData(data);
                    notifyItemRemoved(swipedPosition);
                }

                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    View itemView = viewHolder.itemView;

                    // not sure why, but this method get's called for viewholder that are already swiped away
                    if (viewHolder.getAdapterPosition() == -1) {
                        // not interested in those
                        return;
                    }

                    if (!initiated) {
                        init();
                    }

                    // draw red background
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);

                    // draw x mark
                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    int intrinsicWidth = xMark.getIntrinsicWidth();
                    int intrinsicHeight = xMark.getIntrinsicHeight();

                    int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                    int xMarkRight = itemView.getRight() - xMarkMargin;

                    int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                    int xMarkBottom = xMarkTop + intrinsicHeight;
                    xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                    xMark.draw(c);

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }

            };

            ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            setItemTouchHelper(mItemTouchHelper);
        }

        @Override
        public void onLoaded(boolean mIsSuccess, int mTotal) {

            mRootView.findViewById(R.id.layout_loading).setAnimation(mFadeOut);
            mRootView.findViewById(R.id.layout_loading).setVisibility(View.GONE);
            if (mIsSuccess) {
                mViewNoResult.setVisibility(View.GONE);
                mRecyclerView.setAnimation(mFadeIn);
            } else {
                mViewNoResult.setVisibility(View.VISIBLE);
            }

            //connect to mqtt server
            mDataLoaded = true;

            if (!mIsBlock) {
                reconnect();
            }else {
                Toast.makeText(mContext, mBlockMessage, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onScrolled(boolean mIsSuccess) {

        }

        @Override
        public RecyclerView.Adapter getAdapter(ArrayList data) {
            CrawlerFacebookAdapter mAdapter = new CrawlerFacebookAdapter(mContext, data);
            return mAdapter;
        }

        @Override
        public AsyncTask<String, Void, JSONObject> getAsyncRequest() {
            return new CrawlerFacebookFeeds(OtherService.get_instance(mContext), getCallbackMethod());
        }

        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }


        @Override
        public void onPreLoad() {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivityStopped = false;

        if (mDataLoaded && !mIsBlock)
            reconnect();
    }

    @Override
    public void onDestroy() {
        try {
            // Destroy the MQTT connection if there is one
            if (client != null) {
                client.unregisterResources();
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stop();
        stopKeepAlive();
        stopRetryConnect();
        mActivityStopped = true;
        super.onDestroy();
    }

    public class CrawlerFacebookFeeds extends AsyncTask<String, Void, JSONObject> {

        private OtherService mService;
        private OnTaskCompleted mListener;

        public CrawlerFacebookFeeds(OtherService mService, OnTaskCompleted mListener) {
            this.mService = mService;
            this.mListener = mListener;
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject result = (mGpsTracker.getLatitude() == 0 || Double.compare(mGpsTracker.getLatitude(), LATITUDE_OF_BOUNDARY) >= 0)
                    ? mService.crawlerFacebookFeed(mSessionMng.getShipperId())
                    : mService.crawlerFacebookFeedHcm(mSessionMng.getShipperId());

            return result;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            ArrayList<FacebookFeedCrawlerItem> data = new ArrayList<>();
            int total = 0;

            try {

                mIsBlock = result.has("error") && result.getBoolean("error");
                mBlockMessage = result.has("message") ? result.getString("message") : "";

                if (result != null
                        && result.length() > 0
                        && result.has("list")
                        && !result.getString("list").isEmpty()) {

                    JSONArray lstItem = new JSONArray(result.getString("list"));

                    total = lstItem.length();
                    for (int i = 0; i < lstItem.length(); i++) {
                        JSONObject jObj = lstItem.getJSONObject(i);
                        FacebookFeedCrawlerItem item = new FacebookFeedCrawlerItem();
                        item.setId(jObj.getInt("Id"));
                        item.setFeedId(jObj.getString("PostId"));
                        item.setCreatorName(jObj.getString("Name"));
                        item.setCreatorId(jObj.getString("UserId"));
                        item.setContent(jObj.getString("Content"));
                        item.setTimeCreated(jObj.getString("DateCreated"));
                        item.setDistance(jObj.getString("Distance"));
                        item.setShowPinnedIcon(true);

                        ArrayList<String> listPhoneNumbers = new ArrayList<>();
                        for (int n = 0; n < jObj.getJSONArray("Phone").length(); n++) {
                            listPhoneNumbers.add(jObj.getJSONArray("Phone").get(n).toString());
                        }
                        item.setPhoneNumbers(listPhoneNumbers);

                        data.add(item);
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mListener.onTaskCompleted(data, total);
        }
    }
}

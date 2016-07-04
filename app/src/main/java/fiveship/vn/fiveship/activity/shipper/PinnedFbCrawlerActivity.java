package fiveship.vn.fiveship.activity.shipper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.params.Face;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.CrawlerFacebookAdapter;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.FacebookFeedCrawlerItem;
import fiveship.vn.fiveship.service.OtherService;
import fiveship.vn.fiveship.service.adapter.RemovePinnedFbFeed;
import fiveship.vn.fiveship.utility.OnScrollListenerFactory;
import fiveship.vn.fiveship.utility.SessionManager;

public class PinnedFbCrawlerActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private View mViewNoResult;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private SessionManager mSessionMng;
    private InternalScrollListener mDataListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinned_fb_crawler);
        setupToolBar();
        mSessionMng = new SessionManager(this);
        mDataListener = new InternalScrollListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_data_binding);
        mViewNoResult = findViewById(R.id.vs_no_result);

        mFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        mFadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);

        findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataListener.refresh();
            }
        });

        mDataListener.fetchData();
    }

    public void setupToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Đơn hàng đã ghim");
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
                    int position = viewHolder.getAdapterPosition();
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    final int swipedPosition = viewHolder.getAdapterPosition();
                    FacebookFeedCrawlerItem currentItem = (FacebookFeedCrawlerItem) getData().get(swipedPosition);

                    new RemovePinnedFbFeed(mContext, new OnSendRequestCompleted() {
                        @Override
                        public void onSendRequestCompleted(boolean error, String message) {
                            if (error) {
                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                            } else {
                                ArrayList<FacebookFeedCrawlerItem> data = getData();
                                data.remove(swipedPosition);
                                setData(data);
                                notifyItemRemoved(swipedPosition);
                            }
                        }
                    }).execute(
                            String.valueOf(mSessionMng.getShipperId())
                            , String.valueOf(currentItem.getId())
                            , currentItem.getFeedId());
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

                    int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight)/2;
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

            findViewById(R.id.layout_loading).setAnimation(mFadeOut);
            findViewById(R.id.layout_loading).setVisibility(View.GONE);
            if (mIsSuccess) {
                mViewNoResult.setVisibility(View.GONE);
                mRecyclerView.setAnimation(mFadeIn);
            } else {
                mViewNoResult.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onScrolled(boolean mIsSuccess) {

        }

        @Override
        public RecyclerView.Adapter getAdapter(ArrayList data) {
            CrawlerFacebookAdapter mAdapter = new CrawlerFacebookAdapter(PinnedFbCrawlerActivity.this, data);
            return mAdapter;
        }

        @Override
        public AsyncTask<String, Void, JSONObject> getAsyncRequest() {
            return new CrawlerFavFacebookFeeds(OtherService.get_instance(PinnedFbCrawlerActivity.this), getCallbackMethod());
        }

        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }


        @Override
        public void onPreLoad() {

        }
    }
    public class CrawlerFavFacebookFeeds extends AsyncTask<String, Void, JSONObject> {

        private OtherService mService;
        private OnTaskCompleted mListener;

        public CrawlerFavFacebookFeeds(OtherService mService, OnTaskCompleted mListener) {
            this.mService = mService;
            this.mListener = mListener;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return mService.GetListShipFacebookOfShipper(mSessionMng.getShipperId(), Integer.parseInt(params[0]), Integer.parseInt(params[1]));
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);

            ArrayList<FacebookFeedCrawlerItem> data = new ArrayList<>();
            int total = 0;

            try {
                if (result != null && result.length() > 0 && result.has("list") && !result.getString("list").isEmpty()) {
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
                        item.setShowPinnedIcon(false);

                        ArrayList<String> listPhoneNumbers = new ArrayList<>();
                        for (int n = 0; n < jObj.getJSONArray("Phone").length(); n++) {
                            listPhoneNumbers.add(jObj.getJSONArray("Phone").get(n).toString());
                        }
                        item.setPhoneNumbers(listPhoneNumbers);

                        data.add(item);
                    }
                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            mListener.onTaskCompleted(data, total);
        }
    }
}

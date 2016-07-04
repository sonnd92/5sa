package fiveship.vn.fiveship.activity.shop;

import android.content.Context;
import android.media.Rating;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.RatingAndReviewShipperAdapter;
import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.model.ReviewOfShipperItem;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.utility.OnScrollListenerFactory;
import fiveship.vn.fiveship.utility.Utils;

public class RatingAndReviewShipperActivity extends AppCompatActivity {
    private int mShipperId;

    private RecyclerView mRecyclerView;
    private InternalScrollListener mDataListener;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private View mViewNoResult;
    private View mContainer;
    private TextView mTvTotalRating;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rating_and_review_shipper);
        setupToolbar();

        mDataListener = new InternalScrollListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_data_binding);
        mViewNoResult = findViewById(R.id.vs_no_result);
        mContainer = findViewById(R.id.container);
        mTvTotalRating = (TextView) findViewById(R.id.total_rating_message);

        mFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        mFadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);

        findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataListener.refresh();
            }
        });

        if(getIntent().hasExtra("shipper"))
        {
            bindingData((CustomerItem) getIntent().getSerializableExtra("shipper"));
        }

        mDataListener.fetchData();
    }
    public void bindingData(CustomerItem item){

        mShipperId = item.getShipperId();

        Utils.setImageToImageView(this, item.getAvatar(), (ImageView) findViewById(R.id.cus_image));
        Utils.setImageToImageView(this, item.getAvatarLabel(), (ImageView) findViewById(R.id.cus_image_label));

        ((TextView)findViewById(R.id.shipper_name)).setText(item.getFullName());
        ((TextView)findViewById(R.id.shipper_motor_license)).setText(item.getMotorId());
        ((TextView)findViewById(R.id.shipper_proportion)).setText(item.getShipStatistics());
        ((TextView)findViewById(R.id.shipper_distance)).setText(item.getDistance());
        ((TextView)findViewById(R.id.rating_star_number)).setText("(" + item.getNumberStar() + ")");

        LinearLayout starBox = (LinearLayout) findViewById(R.id.rating_star_box);

        int maxStar = 5;
        int fractional = (int) item.getNumberStar();
        float decimalPart = item.getNumberStar() - fractional;
        for (int i = 0; i < maxStar; i++) {
            ImageView star = new ImageView(this);

            if (i < fractional) {
                star.setImageResource(R.drawable.ic_full_orange_star);
            } else if (i == fractional) {
                if (decimalPart <= 0.25) {
                    star.setImageResource(R.drawable.ic_empty_orange_star);
                } else if (decimalPart > 0.25 && decimalPart <= 0.5) {
                    star.setImageResource(R.drawable.ic_half_orange_star);
                } else {
                    star.setImageResource(R.drawable.ic_full_orange_star);
                }
            } else {
                star.setImageResource(R.drawable.ic_empty_orange_star);
            }
            final float scale = getResources().getDisplayMetrics().density;
            int dpWidthInPx  = (int) (18 * scale);
            int dpHeightInPx = (int) (17 * scale);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dpWidthInPx, dpHeightInPx);
            lp.setMargins(0, 0, 2, 0);

            star.setLayoutParams(lp);

            starBox.addView(star);
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(item.getFullName());
    }

    public void setupToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                finish();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private class InternalScrollListener extends OnScrollListenerFactory{

        public InternalScrollListener(Context mContext) {
            super(mContext);
        }

        @Override
        public void onPreLoad() {

        }

        @Override
        public void onLoaded(boolean mIsSuccess, int mTotal) {
            findViewById(R.id.layout_loading).setAnimation(mFadeOut);
            findViewById(R.id.layout_loading).setVisibility(View.GONE);
            mTvTotalRating.setText(mTotal + " đánh giá về shipper này");
            mContainer.setAnimation(mFadeIn);
            if (mIsSuccess) {
                mViewNoResult.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            } else {
                mRecyclerView.setVisibility(View.INVISIBLE);
                mViewNoResult.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onScrolled(boolean mIsSuccess) {

        }

        @Override
        public RecyclerView.Adapter getAdapter(ArrayList data) {
            return new RatingAndReviewShipperAdapter(RatingAndReviewShipperActivity.this, data);
        }

        @Override
        public AsyncTask<String, Void, JSONObject> getAsyncRequest() {
            return new GetRatingAndReviewOfShipper(getCallbackMethod());
        }

        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }
    }

    private class GetRatingAndReviewOfShipper extends AsyncTask<String, Void, JSONObject> {

        private ShopService mService;
        private OnTaskCompleted mListener;

        public GetRatingAndReviewOfShipper(OnTaskCompleted mListener) {
            this.mListener = mListener;
        }

        @Override
        protected void onPreExecute() {
            mService = ShopService.get_instance(RatingAndReviewShipperActivity.this);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            ArrayList<ReviewOfShipperItem> data = new ArrayList<>();
            int total = 0;
            try {
                if (jsonObject != null
                        && jsonObject.length() > 0
                        && ((jsonObject.has("error") && !jsonObject.getBoolean("error")) || !jsonObject.has("error"))) {

                    JSONArray lstItem = jsonObject.getJSONArray("list");
                    total = jsonObject.getInt("total");

                    for (int i = 0; i < lstItem.length(); i++) {

                        JSONObject oneSurvey = lstItem.getJSONObject(i);
                        ReviewOfShipperItem item = new ReviewOfShipperItem();
                        item.setDateTime(oneSurvey.getString("DateCreated"));
                        item.setNumberRating(oneSurvey.getInt("NumberStart"));
                        item.setReviewer(oneSurvey.getString("Name"));
                        item.setReviewContent(oneSurvey.getString("Content"));
                        item.setReviewerAvatarUrl(oneSurvey.getString("Avatar"));
                        data.add(item);
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mListener.onTaskCompleted(data, total);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return mService.getRatingAndReviewOfShipper(mShipperId, Integer.parseInt(params[0]), Integer.parseInt(params[1]));
        }
    }

}

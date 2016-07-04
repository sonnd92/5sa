package fiveship.vn.fiveship.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.model.NewsItem;
import fiveship.vn.fiveship.service.NewsService;
import fiveship.vn.fiveship.utility.Utils;

public class NewsDetailActivity extends AppCompatActivity implements Html.ImageGetter {

    private int mNewsId;
    private TextView mTVTitle;
    private TextView mTVContent;
    private TextView mTVDateCreated;
    private ImageView mIVReplyBtn;
    private ImageView mCover;
    private LinearLayout mOtherNewsBox;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private View mViewNoResult;
    private ScrollView mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.order_detail_toolbar);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setHomeButtonEnabled(true);
        }


        mNewsId = getIntent().getIntExtra("newsId", 0);

        mContainer = (ScrollView) findViewById(R.id.detail_box);
        mTVTitle = (TextView) findViewById(R.id.news_author);
        mTVContent = (TextView) findViewById(R.id.news_content);
        mTVDateCreated = (TextView) findViewById(R.id.news_date_created);
        mOtherNewsBox = (LinearLayout) findViewById(R.id.other_news_box);
        mCover = (ImageView) findViewById(R.id.news_cover_image);

        mFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        mFadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);

        mViewNoResult = findViewById(R.id.vs_no_result);

        findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetNewsDetail().execute();
            }
        });

        new GetNewsDetail().execute();
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

    public void onBindingDataToView(NewsItem model, ArrayList<NewsItem> others){

        findViewById(R.id.layout_loading).setAnimation(mFadeOut);
        findViewById(R.id.layout_loading).setVisibility(View.GONE);

        if (model == null) {

            mViewNoResult.setVisibility(View.VISIBLE);

        } else {

            mViewNoResult.setVisibility(View.GONE);

            mContainer.startAnimation(mFadeIn);
            mContainer.setVisibility(View.VISIBLE);
            mTVTitle.setText(model.getTitle());
            mTVDateCreated.setText(model.getDateCreated());
            Spanned spanned = Html.fromHtml(model.getContent(), this, null);
            mTVContent.setText(spanned);
            Utils.setImageToImageView(this, model.getUrlImageCover(), mCover);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(model.getTitle());
            }

            for (final NewsItem news : others) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View mLayoutItem = inflater.inflate(R.layout.other_news_item_layout, null);
                ((TextView) mLayoutItem.findViewById(R.id.news_title)).setText(news.getTitle());
                ((TextView) mLayoutItem.findViewById(R.id.news_date_created)).setText(news.getDateCreated());
                Utils.setImageToImageView(this, news.getUrlImageCover(), (ImageView) mLayoutItem.findViewById(R.id.news_cover_image));
                mLayoutItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        Intent selfIntent = getIntent();
                        selfIntent.putExtra("newsId", news.getId());
                        startActivity(selfIntent);
                    }
                });
                mOtherNewsBox.addView(mLayoutItem);
            }
        }
    }

    @Override
    public Drawable getDrawable(String source) {
        LevelListDrawable d = new LevelListDrawable();
        Drawable empty = ContextCompat.getDrawable(this, R.drawable.ic_launcher);
        d.addLevel(0, 0, empty);
        d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());

        new LoadImage().execute(source, d);

        return d;
    }

    class LoadImage extends AsyncTask<Object, Void, Bitmap> {

        private LevelListDrawable mDrawable;

        @Override
        protected Bitmap doInBackground(Object... params) {
            String source = (String) params[0];
            mDrawable = (LevelListDrawable) params[1];
            try {
                InputStream is = new URL(source).openStream();
                return BitmapFactory.decodeStream(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                BitmapDrawable d = new BitmapDrawable(getResources(), bitmap);

                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int width = displaymetrics.widthPixels;

                mDrawable.addLevel(1, 1, d);
                mDrawable.setBounds(0, 0, width, ( width * bitmap.getHeight() / bitmap.getWidth()));
                mDrawable.setLevel(1);
                // i don't know yet a better way to refresh TextView
                // mTv.invalidate() doesn't work as expected
                CharSequence t = mTVContent.getText();
                mTVContent.setText(t);
            }
        }
    }

    public class GetNewsDetail extends AsyncTask<String, Void, JSONObject>{
        private NewsService mService;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mService = NewsService.get_instance(NewsDetailActivity.this);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return mService.GetNewsDetail(mNewsId);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try {
                if(jsonObject != null && !jsonObject.toString().isEmpty() && !jsonObject.getBoolean("error")){

                    NewsItem item = new NewsItem();
                    JSONObject news = jsonObject.getJSONObject("data");
                    item.setId(news.getInt("Id"));
                    item.setTitle(news.getString("Name"));
                    item.setDateCreated(news.getString("DateCreated"));
                    item.setSummary(news.getString("Summary"));
                    item.setContent(news.getString("Detail"));
                    item.setUrlImageCover(news.getString("UrlPicture"));

                    ArrayList<NewsItem> others = new ArrayList<>();
                    for(int i = 0; i < jsonObject.getJSONArray("list").length(); i++){
                        JSONObject _temp = jsonObject.getJSONArray("list").getJSONObject(i);
                        NewsItem temp = new NewsItem();
                        temp.setId(_temp.getInt("Id"));
                        temp.setTitle(_temp.getString("Name"));
                        temp.setDateCreated(_temp.getString("DateCreated"));
                        temp.setSummary(_temp.getString("Summary"));
                        temp.setUrlImageCover(_temp.getString("UrlPicture"));
                        others.add(temp);
                    }

                    onBindingDataToView(item, others);
                }else{
                    Toast.makeText(NewsDetailActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(NewsDetailActivity.this, getString(R.string.exception_message), Toast.LENGTH_SHORT).show();
            }
        }
    }

}

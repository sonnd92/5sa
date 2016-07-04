package fiveship.vn.fiveship.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.NewsAdapter;
import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.NewsItem;
import fiveship.vn.fiveship.service.NewsService;
import fiveship.vn.fiveship.utility.OnScrollListenerFactory;

public class PolicyTab extends Fragment {

    private Context mContext;
    private String mKeyword;
    private View mRootView;

    private EditText mTvSearch;
    private RecyclerView mRecyclerView;
    private View mViewNoResult;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Animation mFadeIn;
    private Animation mFadeOut;

    private InternalScrollListener mDataListener;

    public PolicyTab() {
        // Required empty public constructor
    }

    public static PolicyTab newInstance() {
        PolicyTab fragment = new PolicyTab();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_policy, container, false);

        mDataListener = new InternalScrollListener(mContext);

        mKeyword = "";

        mTvSearch = (EditText) mRootView.findViewById(R.id.txt_ship_search);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.rv_data_binding);
        mViewNoResult = mRootView.findViewById(R.id.vs_no_result);
        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_layout);

        mTvSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mKeyword = mTvSearch.getText().toString();
                } else {
                    mKeyword = "";
                }
            }
        });

        mTvSearch.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            mDataListener.refresh();
                            return true;
                        }
                        return false;
                    }
                });

        mTvSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    mDataListener.refresh();
                    return true;
                }
                return false;
            }
        });

        // fade anim
        mFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_fade_in);
        mFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_fade_out);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                mSwipeRefreshLayout.setEnabled(false);
                mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorPrimary));
                mDataListener.refresh();
            }
        });

        mRootView.findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataListener.refresh();
            }
        });

        mDataListener.fetchData();

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public class InternalScrollListener extends OnScrollListenerFactory {
        private NewsService service;

        public InternalScrollListener(Context mContext) {
            super(mContext);
            service = NewsService.get_instance(mContext);
        }

        @Override
        public void onPreLoad() {

            mRecyclerView.setAnimation(mFadeOut);
            mRecyclerView.setVisibility(View.INVISIBLE);
            mRootView.findViewById(R.id.layout_loading).setVisibility(View.VISIBLE);
            mViewNoResult.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(false);

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

            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);
        }

        @Override
        public void onScrolled(boolean mIsSuccess) {

        }

        @Override
        public RecyclerView.Adapter getAdapter(ArrayList data) {
            NewsAdapter newsAdapter = new NewsAdapter(data, mContext);
            return newsAdapter;
        }

        @Override
        public AsyncTask<String, Void, JSONObject> getAsyncRequest() {
            return new GetPolicies(getCallbackMethod(), service, mKeyword);
        }

        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }
    }

    public class GetPolicies extends AsyncTask<String, Void, JSONObject> {
        private NewsService mService;
        private String keyword;
        private OnTaskCompleted mListener;

        public GetPolicies(OnTaskCompleted mListener, NewsService mService, String keyword) {
            this.mService = mService;
            this.keyword = keyword;
            this.mListener = mListener;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return mService.GetPolicies(keyword, Integer.parseInt(params[0]), Integer.parseInt(params[1]));
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            ArrayList<NewsItem> items = new ArrayList<>();
            try {
                if (result != null && result.length() > 0 && result.has("list") && !result.getString("list").isEmpty()) {

                    JSONArray oneSurvey = result.getJSONArray("list");
                    int total = result.getInt("total");
                    for (int i = 0; i < oneSurvey.length(); i++) {
                        JSONObject jsObject = oneSurvey.getJSONObject(i);
                        NewsItem item = new NewsItem();
                        item.setId(jsObject.getInt("Id"));
                        item.setTitle(jsObject.getString("Name"));
                        item.setUrlImageCover(jsObject.getString("UrlPicture"));
                        item.setDateCreated(jsObject.getString("DateCreated"));
                        items.add(item);
                    }

                    mListener.onTaskCompleted(items, total);
                } else {
                    mListener.onTaskCompleted(null, 0);
                }

            } catch (JSONException e) {
                e.printStackTrace();

                mListener.onTaskCompleted(null, 0);
            }
        }
    }
}

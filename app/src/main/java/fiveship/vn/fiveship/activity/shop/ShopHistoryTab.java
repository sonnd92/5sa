package fiveship.vn.fiveship.activity.shop;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
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

import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.ShopShippingOrderAdapter;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.service.adapter.GetShipHistoryOfShop;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.OnScrollListenerFactory;
import fiveship.vn.fiveship.utility.SessionManager;

public class ShopHistoryTab extends Fragment {

    private RecyclerView mRecyclerView;
    private FragmentActivity mContext;
    private BroadcastReceiver bReceiver;
    private String mKeyword;

    private View mRootView;
    private EditText mTxtSearch;
    private int mOrderStatus;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private InternalScrollListener mDataListener;
    private Animation fadeIn;
    private Animation fadeOut;
    private View mViewNoResult;
    private OnShopTabTitleChanged mTabTitleChangedListener;

    private boolean mIsTab;
    private boolean mIsViewShown;
    private int mPosition;
    private String mCurrentTitle;

    public static ShopHistoryTab newInstance(int orderStatus, int position, boolean isTab) {

        ShopHistoryTab instance = new ShopHistoryTab();
        Bundle args = new Bundle();
        args.putInt("mOrderStatus", orderStatus);
        args.putBoolean("mIsTab", isTab);
        args.putInt("mPosition", position);
        instance.setArguments(args);

        return instance;
    }

    public static ShopHistoryTab newInstance(int orderStatus, int mPosition, boolean isTab, boolean isHiddenSearch) {

        ShopHistoryTab instance = new ShopHistoryTab();
        Bundle args = new Bundle();
        args.putInt("mOrderStatus", orderStatus);
        args.putBoolean("mIsTab", isTab);
        args.putBoolean("mIsHiddenSearch", isHiddenSearch);
        args.putInt("mPosition", mPosition);
        instance.setArguments(args);

        return instance;
    }

    public ShopHistoryTab() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mOrderStatus = getArguments() != null ? getArguments().getInt("mOrderStatus") : 0;
        mIsTab = getArguments() != null && getArguments().getBoolean("mIsTab");
        boolean mIsHiddenSearch = getArguments() != null && getArguments().getBoolean("mIsHiddenSearch");
        mPosition = getArguments() != null ? getArguments().getInt("mPosition") : 0;
        mKeyword = "";

        mRootView = inflater.inflate(R.layout.shop_shipping_order_pager_fragment, container, false);

        mCurrentTitle = mContext.getResources().getStringArray(R.array.arr_shop_order_mng_tab_title)[mPosition];
        mTxtSearch = (EditText) mRootView.findViewById(R.id.txt_ship_search);
        mViewNoResult = mRootView.findViewById(R.id.vs_no_result);

        fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.abc_fade_in);
        fadeOut = AnimationUtils.loadAnimation(mContext, R.anim.abc_fade_out);

        mTxtSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mKeyword = mTxtSearch.getText().toString();
                } else {
                    mKeyword = "";
                }
            }
        });

        mTxtSearch.setOnEditorActionListener(
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

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.rv_data_binding);

        mRootView.findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataListener.refresh();
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_layout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                mSwipeRefreshLayout.setEnabled(false);
                mSwipeRefreshLayout.setColorSchemeColors(ActivityCompat.getColor(mContext, R.color.colorPrimary));
                mDataListener.refresh();
            }
        });

        if (mIsHiddenSearch) {
            mRootView.findViewById(R.id.search_bar).setVisibility(View.GONE);
        }

        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.RECEIVE_SHOP_LIST_SHIP)) {
                    if (mDataListener == null)
                        mDataListener = new InternalScrollListener(mContext);
                    mDataListener.refresh();
                }
            }
        };

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.RECEIVE_SHOP_LIST_SHIP);
        bManager.registerReceiver(bReceiver, intentFilter);

        if (mIsViewShown) {
            if (mDataListener == null)
                mDataListener = new InternalScrollListener(mContext);
            if (!mDataListener.isDataLoaded())
                mDataListener.fetchData();
        }

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        mContext = (FragmentActivity) context;
        if (context instanceof OnShopTabTitleChanged) {
            mTabTitleChangedListener = (OnShopTabTitleChanged) context;
        }
        super.onAttach(context);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mIsTab && getView() != null) {
                if (mDataListener == null)
                    mDataListener = new InternalScrollListener(mContext);
                if (!mDataListener.isDataLoaded()) {
                    mDataListener.fetchData();
                    mIsViewShown = false;
                }
            } else {
                mIsViewShown = true;
            }
        } else {
            mIsViewShown = false;
        }
    }

    public class InternalScrollListener extends OnScrollListenerFactory {

        private SessionManager mSessionManager;

        public InternalScrollListener(Context mContext) {
            super(mContext);
            mSessionManager = new SessionManager(mContext);
        }

        @Override
        public void onPreLoad() {
            mRecyclerView.setAnimation(fadeOut);
            mRecyclerView.setVisibility(View.INVISIBLE);
            mRootView.findViewById(R.id.layout_loading).setVisibility(View.VISIBLE);
            mViewNoResult.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(false);
        }

        @Override
        public AsyncTask<String, Void, JSONObject> getAsyncRequest() {
            return new GetShipHistoryOfShop(getCallbackMethod(), ShopService.get_instance(mContext), mKeyword, mSessionManager.getShopId(), mOrderStatus);
        }

        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }

        @Override
        public RecyclerView.Adapter getAdapter(ArrayList data) {
            return new ShopShippingOrderAdapter(mContext, data);
        }

        @Override
        public void onLoaded(boolean mIsSuccess, int mTotal) {
            mTabTitleChangedListener.onShopTitleChanged(mPosition, mCurrentTitle + "(" + mTotal + ")");
            mRootView.findViewById(R.id.layout_loading).setAnimation(fadeOut);
            mRootView.findViewById(R.id.layout_loading).setVisibility(View.GONE);
            if (mIsSuccess) {
                mViewNoResult.setVisibility(View.GONE);
                mRecyclerView.setAnimation(fadeIn);
            } else {
                mViewNoResult.setVisibility(View.VISIBLE);
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);
        }

        @Override
        public void onScrolled(boolean mIsSuccess) {

        }
    }

    public interface OnShopTabTitleChanged {
        void onShopTitleChanged(int i, String t);
    }
}

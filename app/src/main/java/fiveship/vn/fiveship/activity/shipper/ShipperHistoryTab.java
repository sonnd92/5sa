package fiveship.vn.fiveship.activity.shipper;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
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
import fiveship.vn.fiveship.adapter.ShippingOrderAdapter;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.service.adapter.GetOldShippingOrder;
import fiveship.vn.fiveship.service.adapter.GetPendingShippingOrder;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.OnScrollListenerFactory;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;
import fiveship.vn.fiveship.utility.SessionManager;

public class ShipperHistoryTab extends Fragment {
    private static final String ORDER_STATUS_ARG = "mOrderStatus";
    private static final String TAB_POSITION_ARG = "mTabPosition";
    private View mRootView;
    private EditText mTvSearch;
    private RecyclerView mRecyclerView;
    private View mViewNoResult;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BroadcastReceiver bReceiver;
    private InternalScrollListener mDataListener;
    private boolean mIsViewShown;
    private OnShipTabTitleChanged mTitleChangeListener;

    private String mKeyWords;
    private int mOrderStatus = 0;
    private int mPosition = 0;
    private String mTabTitle;

    private FragmentActivity mContext;

    public static ShipperHistoryTab newInstance(int status, int position) {

        ShipperHistoryTab fragment = new ShipperHistoryTab();
        Bundle args = new Bundle();
        args.putInt(ORDER_STATUS_ARG, status);
        args.putInt(TAB_POSITION_ARG, position);
        fragment.setArguments(args);
        return fragment;
    }

    public ShipperHistoryTab() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mOrderStatus = getArguments() != null ? getArguments().getInt(ORDER_STATUS_ARG) : 0;
        mPosition = getArguments() != null ? getArguments().getInt(ORDER_STATUS_ARG) : 0;
        mKeyWords = "";

        mRootView = inflater.inflate(R.layout.fragment_navigator_history_tab, container, false);
        if(mPosition >= 0)
            mTabTitle = getResources().getStringArray(R.array.arr_shipper_order_mng_tab_title)[mPosition];

        mTvSearch = (EditText) mRootView.findViewById(R.id.txt_ship_search);

        mTvSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mKeyWords = mTvSearch.getText().toString();
                } else {
                    mKeyWords = "";
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

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.rv_data_binding);
        // setup loader
        mViewNoResult = mRootView.findViewById(R.id.vs_no_result);

        // fade anim
        mFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_fade_in);
        mFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_fade_out);

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
                mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorPrimary));
                mDataListener.refresh();
            }
        });

        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.RECEIVE_SHIPPER_LIST_SHIP)) {
                    if(mDataListener == null)
                        mDataListener = new InternalScrollListener(mContext);
                    mDataListener.refresh();
                }
            }
        };

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getContext());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.RECEIVE_SHIPPER_LIST_SHIP);
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
        super.onAttach(context);
        mContext = (FragmentActivity) context;
        if (context instanceof OnShipTabTitleChanged) {
            mTitleChangeListener = (OnShipTabTitleChanged) context;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            if (getView() != null) {
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

        public InternalScrollListener(final Context mContext) {
            super(mContext);
            mSessionManager = new SessionManager(mContext);
        }

        @Override
        public void onLoaded(boolean mIsSuccess, int mTotal) {
            if (mPosition >= 0)
                mTitleChangeListener.onShipTabTitleChanged(mPosition, mTabTitle + "(" + mTotal + ")");

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
            ShippingOrderAdapter mAdapter = new ShippingOrderAdapter(mContext, data);
            return mAdapter;
        }

        @Override
        public AsyncTask<String, Void, JSONObject> getAsyncRequest() {
            if (mOrderStatus == OrderStatusEnum.PENDING.getStatusCode()) {
                return new GetPendingShippingOrder(getCallbackMethod(),ShippingOrderService.get_instance(mContext), mKeyWords, mSessionManager.getShipperId());
            } else {
                return new GetOldShippingOrder(getCallbackMethod(), ShippingOrderService.get_instance(mContext), mKeyWords, mOrderStatus, mSessionManager.getShipperId());
            }
        }

        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }


        @Override
        public void onPreLoad() {

        }
    }

    public interface OnShipTabTitleChanged {
        void onShipTabTitleChanged(int idx, String title);
    }

}

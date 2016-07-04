package fiveship.vn.fiveship.activity.shop;

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
import android.support.v7.widget.RecyclerView;
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
import fiveship.vn.fiveship.adapter.ShopShipperManagerAdapter;
import fiveship.vn.fiveship.model.ShipperItem;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.service.adapter.GetListShipperManager;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.OnScrollListenerFactory;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.enums.ShipperMngDataChangeEnum;


public class ShopShipperManagerTab extends Fragment {

    private static final String ARG_PAGER_TYPE = "pagerType";

    private int mType;
    private RecyclerView mRecyclerView;
    private EditText mTxtSearch;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private InternalScrollListener mDataListener;
    private Animation fadeIn;
    private Animation fadeOut;
    private View mViewNoResult;
    private String mKeyword = "";
    private FragmentActivity mContext;
    private boolean mIsViewShown;
    private OnTitleChanged mTitleChangeListener;

    private View mRootView;

    public ShopShipperManagerTab() {
        // Required empty public constructor
    }

    public static ShopShipperManagerTab newInstance(int pageType) {
        ShopShipperManagerTab fragment = new ShopShipperManagerTab();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGER_TYPE, pageType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(ARG_PAGER_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_shop_shipper_manager_tab, container, false);
        fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.abc_fade_in);
        fadeOut = AnimationUtils.loadAnimation(mContext, R.anim.abc_fade_out);
        mViewNoResult = mRootView.findViewById(R.id.vs_no_result);

        mTxtSearch = (EditText) mRootView.findViewById(R.id.txt_ship_search);
        mTxtSearch.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            mKeyword = v.getText().toString();
                            mDataListener.refresh();
                            Utils.hideKeyboard(mContext);
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
                mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorPrimary));
                mDataListener.refresh();
            }
        });

        if (mIsViewShown) {
            if (mDataListener == null)
                mDataListener = new InternalScrollListener(mContext);
            if (!mDataListener.isDataLoaded())
                mDataListener.fetchData();
        }

        return mRootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mType = getArguments() != null ? getArguments().getInt(ARG_PAGER_TYPE) : 0;
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTitleChanged) {
            mTitleChangeListener = (OnTitleChanged) context;
            mContext = (FragmentActivity) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnTitleChanged interface");
        }
    }

    public interface OnTitleChanged {
        void inTitleChanged(int index, String title);
    }

    private class InternalScrollListener extends OnScrollListenerFactory {
        private ShopService mService;
        private SessionManager mSessionManager;
        private int mTotal;

        public InternalScrollListener(Context mContext) {
            super(mContext);
            mService = ShopService.get_instance(mContext);
            mSessionManager = new SessionManager(mContext);

            BroadcastReceiver bReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(Config.RECEIVE_SHIPPER_MANAGER) && isDataLoaded()) {

                        final ShipperItem tObj = (ShipperItem) intent.getSerializableExtra("shipperItem");
                        final int state = intent.getIntExtra("state", 0);
                        int position = 0;

                        ShipperItem item = null;
                        ArrayList<ShipperItem> data = getData();

                        for (int i = 0; i < data.size(); i++) {
                            if (data.get(i).getId() == tObj.getId()) {
                                item = data.get(i);
                                position = i;
                                break;
                            }
                        }
                        if (state == ShipperMngDataChangeEnum.BLOCK.getStatusCode()) {
                            switch (mType) {
                                case 0:
                                    if (item != null) {
                                        item.setIsBlock(true);
                                        item.setIsFavorite(false);
                                        data.set(position, item);
                                        notifyItemChanged(position, item);
                                        break;
                                    }
                                case 1:
                                    if (item != null) {
                                        data.remove(position);
                                        setData(data);
                                        notifyItemRemoved(position);
                                        mTotal--;
                                        mTitleChangeListener.inTitleChanged(mType, getResources().getStringArray(R.array.arr_shipper_manager_tab_title)[mType] + " (" + mTotal + ")");
                                    }
                                    break;
                                case 2:
                                    data.add(data.size(), tObj);
                                    setData(data);
                                    notifyItemInserted(data.size());
                                    mTotal++;
                                    mTitleChangeListener.inTitleChanged(mType, getResources().getStringArray(R.array.arr_shipper_manager_tab_title)[mType] + " (" + mTotal + ")");
                                    break;
                            }
                        }

                        if (state == ShipperMngDataChangeEnum.REMOVE_BLOCK.getStatusCode()) {
                            switch (mType) {
                                case 0:
                                    if (item != null) {
                                        item.setIsBlock(false);
                                        data.set(position, item);
                                        notifyItemChanged(position, item);
                                    }
                                    break;
                                case 2:
                                    if (item != null) {
                                        data.remove(position);
                                        setData(data);
                                        notifyItemRemoved(position);
                                        mTotal--;
                                        mTitleChangeListener.inTitleChanged(mType, getResources().getStringArray(R.array.arr_shipper_manager_tab_title)[mType] + " (" + mTotal + ")");

                                    }
                                    break;
                            }
                        }
                        if (state == ShipperMngDataChangeEnum.REMOVE_FAVORITE.getStatusCode()) {
                            switch (mType) {
                                case 0:
                                    if (item != null) {
                                        item.setIsFavorite(false);
                                        data.set(position, item);
                                        notifyItemChanged(position, item);
                                    }
                                    break;
                                case 1:
                                    if (item != null) {
                                        data.remove(position);
                                        setData(data);
                                        notifyItemRemoved(position);
                                        mTotal--;
                                        mTitleChangeListener.inTitleChanged(mType, getResources().getStringArray(R.array.arr_shipper_manager_tab_title)[mType] + " (" + mTotal + ")");
                                    }
                                    break;
                            }
                        }

                        if (state == ShipperMngDataChangeEnum.FAVORITE.getStatusCode()) {

                            switch (mType) {
                                case 0:
                                    if (item != null) {
                                        item.setIsFavorite(true);
                                        data.set(position, item);
                                        notifyItemChanged(position, item);
                                    }
                                    break;
                                case 1:
                                    data.add(data.size(), tObj);
                                    setData(data);
                                    notifyItemInserted(data.size());
                                    mTotal++;
                                    mTitleChangeListener.inTitleChanged(mType, getResources().getStringArray(R.array.arr_shipper_manager_tab_title)[mType] + " (" + mTotal + ")");
                                    break;
                            }
                        }
                    }
                }
            };

            LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(getContext());
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Config.RECEIVE_SHIPPER_MANAGER);
            bManager.registerReceiver(bReceiver, intentFilter);

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
        public void onLoaded(boolean mIsSuccess, int mTotal) {
            if(!isDataLoaded()) {
                this.mTotal = mTotal;
                mTitleChangeListener.inTitleChanged(mType, getResources().getStringArray(R.array.arr_shipper_manager_tab_title)[mType] + " ("+ mTotal +")");
            }
            mRootView.findViewById(R.id.layout_loading).setAnimation(fadeOut);
            mRootView.findViewById(R.id.layout_loading).setVisibility(View.GONE);
            if (mIsSuccess) {
                mViewNoResult.setVisibility(View.GONE);
                mRecyclerView.setAnimation(fadeIn);
                mRecyclerView.setVisibility(View.VISIBLE);
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
            return new ShopShipperManagerAdapter(mContext, data, mType);
        }

        @Override
        public AsyncTask<String, Void, JSONObject> getAsyncRequest() {
            switch (mType) {
                case 1:
                    //favorite tab
                    return new GetListShipperManager(getCallbackMethod(), mService, mSessionManager.getShopId(), mKeyword, false, true);
                case 2:
                    //block tab
                    return new GetListShipperManager(getCallbackMethod(), mService, mSessionManager.getShopId(), mKeyword, true, false);
                default:
                    return new GetListShipperManager(getCallbackMethod(), mService, mSessionManager.getShopId(), mKeyword);
            }
        }

        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }
    }

}

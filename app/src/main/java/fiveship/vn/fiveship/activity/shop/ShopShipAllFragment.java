package fiveship.vn.fiveship.activity.shop;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.GetTitleTabAction;
import fiveship.vn.fiveship.adapter.ShopShipPagerAdapter;
import fiveship.vn.fiveship.interfaces.OnTabChanged;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.SlidingTabLayout;

public class ShopShipAllFragment extends Fragment implements ShopActivity.OnShopTabTitleChanged{

    private SlidingTabLayout mTabStrip;
    private FragmentActivity mContext;
    private static ViewPager sViewPage;
    private int mTabIndex = 0;
    private OnTabChanged mListener;

    public static synchronized ShopShipAllFragment newInstance(int tabIndex) {

        ShopShipAllFragment instance = new ShopShipAllFragment();
        instance.mTabIndex = tabIndex;
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.shop_ship_all, container, false);

        String[] mTitles = mContext.getResources().getStringArray(R.array.arr_shop_order_mng_tab_title);

        // Give the PagerSlidingTabStrip the ViewPager
        mTabStrip = (SlidingTabLayout) mRootView.findViewById(R.id.shop_ship_tabs);

        ShopShipPagerAdapter mAdapter = new ShopShipPagerAdapter(mContext.getSupportFragmentManager(), mTitles, false);
        sViewPage = (ViewPager) mRootView.findViewById(R.id.shop_ship_viewpager);
        sViewPage.setAdapter(mAdapter);
        sViewPage.setOffscreenPageLimit(mTitles.length - 1);
        sViewPage.setCurrentItem(mTabIndex);
        sViewPage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mListener.onTabChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabStrip.setDistributeEvenly(true);
        mTabStrip.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        mTabStrip.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(mContext, R.color.fiveship_common_white);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        mTabStrip.setViewPager(sViewPage);

        return mRootView;

    }

    @Override
    public void onAttach(Context context) {
        mContext = (FragmentActivity) context;
        if(context instanceof OnTabChanged){
            mListener = (OnTabChanged) context;
        }else{
            throw new RuntimeException(context.toString() + " must implement OnTabChanged interface");
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onShopTabTitleChanged(int i, String t) {
        mTabStrip.setTitleTab(i, t);
    }

    public static void ChangeCurrentPager(int position){
        sViewPage.setCurrentItem(position, true);
    }
}

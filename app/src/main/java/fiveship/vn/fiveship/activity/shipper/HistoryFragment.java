package fiveship.vn.fiveship.activity.shipper;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.HistoryTabAdapter;
import fiveship.vn.fiveship.interfaces.OnTabChanged;
import fiveship.vn.fiveship.utility.SlidingTabLayout;

public class HistoryFragment extends Fragment implements MainActivity.OnShipTabTitleChanged{

    public static ViewPager sViewPager;
    private HistoryTabAdapter mHistoryTabAdapter;
    private SlidingTabLayout mTabs;
    private String[] mTitles;

    private View mRootView;
    private FragmentActivity mContext;

    private int mTabIndex = 0;
    private OnTabChanged mListener;

    public static HistoryFragment newInstance(int tabIndex) {

        HistoryFragment instance = new HistoryFragment();
        instance.mTabIndex = tabIndex;
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.activity_history, container, false);

        mTabs = (SlidingTabLayout) mRootView.findViewById(R.id.tabs);
        //Assigning ViewPager and setting an adapter
        mTitles = getResources().getStringArray(R.array.arr_shipper_order_mng_tab_title);

        mHistoryTabAdapter = new HistoryTabAdapter(mContext.getSupportFragmentManager(), this.mTitles);
        sViewPager = (ViewPager) mRootView.findViewById(R.id.customer_info_pager);
        sViewPager.setAdapter(mHistoryTabAdapter);
        sViewPager.setOffscreenPageLimit(this.mTitles.length - 1);
        sViewPager.setCurrentItem(mTabIndex);
        sViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        mTabs.setDistributeEvenly(true);
        mTabs.setBackgroundColor(ActivityCompat.getColor(mContext, R.color.colorPrimary));
        mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ActivityCompat.getColor(mContext, R.color.fiveship_common_white);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        mTabs.setViewPager(sViewPager);

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (FragmentActivity) context;
        if(context instanceof OnTabChanged){
            mListener = (OnTabChanged) context;
        }else{
            throw new RuntimeException(context.toString() + " must implement OnTabChanged interface");
        }
    }

    @Override
    public void onShipTitleChanged(int idx, String title) {
        mTabs.setTitleTab(idx, title);
    }

    public static void ChangeTab(int position){
        sViewPager.setCurrentItem(position, true);
    }
}

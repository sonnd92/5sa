package fiveship.vn.fiveship.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.NewsPagerAdapter;
import fiveship.vn.fiveship.adapter.ShippingOrderPagerAdapter;
import fiveship.vn.fiveship.utility.SlidingTabLayout;

public class NewsFragment extends Fragment {
    private ViewPager mViewPager;
    private NewsPagerAdapter mNewsPagerAdapter;
    private SlidingTabLayout mTabs;
    private String mTitles[];
    private View mRootView;
    private FragmentActivity mContext;

    ActionBar action;


    public NewsFragment() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static NewsFragment newInstance() {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView =  inflater.inflate(R.layout.fragment_news, container, false);

        mTitles = getResources().getStringArray(R.array.arr_news_and_policy_tab_title);
        //Assigning Sliding Tab Layout View
        mTabs = (SlidingTabLayout) mRootView.findViewById(R.id.tabs);

        //Assigning ViewPager and setting an adapter
        if(mTitles.length <= 0){

            mTabs.setVisibility(View.GONE);

        }else {
            mNewsPagerAdapter = new NewsPagerAdapter(mContext.getSupportFragmentManager(), mTitles);
            mViewPager = (ViewPager) mRootView.findViewById(R.id.pager);
            mViewPager.setAdapter(mNewsPagerAdapter);
            mViewPager.setOffscreenPageLimit(mTitles.length - 1);
            mTabs.setDistributeEvenly(true);
            mTabs.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            mTabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return ContextCompat.getColor(mContext, R.color.fiveship_common_white);
                }
            });

            // Setting the ViewPager For the SlidingTabsLayout
            mTabs.setViewPager(mViewPager);
        }

        return mRootView;
    }

    @Override
    public void onAttach(Context activity) {
        mContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.menu_main, menu);
    }
}

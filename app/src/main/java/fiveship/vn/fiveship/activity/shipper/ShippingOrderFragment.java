package fiveship.vn.fiveship.activity.shipper;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.ShippingOrderPagerAdapter;
import fiveship.vn.fiveship.utility.SlidingTabLayout;

public class ShippingOrderFragment extends Fragment{
    private ViewPager viewPager;
    private ShippingOrderPagerAdapter shippingOrderPagerAdapter;
    SlidingTabLayout tabs;
    String Titles[];
    private View rootView;
    private FragmentActivity myContext;

    ActionBar action;

    public static ShippingOrderFragment newInstance() {

        ShippingOrderFragment instance = new ShippingOrderFragment();

        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView =  inflater.inflate(R.layout.activity_shipping_order, container, false);

        Titles = getResources().getStringArray(R.array.arr_shipping_order_tab_title);
        //Assigning Sliding Tab Layout View
        tabs = (SlidingTabLayout) rootView.findViewById(R.id.tabs);

        //Assigning ViewPager and setting an adapter
        if(Titles.length <= 0){

            tabs.setVisibility(View.GONE);

        }else {
            shippingOrderPagerAdapter = new ShippingOrderPagerAdapter(myContext.getSupportFragmentManager(), Titles);
            viewPager = (ViewPager) rootView.findViewById(R.id.pager);
            viewPager.setAdapter(shippingOrderPagerAdapter);
            viewPager.setOffscreenPageLimit(Titles.length - 1);
            tabs.setDistributeEvenly(true);
            tabs.setBackgroundColor(ContextCompat.getColor(myContext, R.color.colorPrimary));
            tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return ContextCompat.getColor(myContext, R.color.fiveship_common_white);
                }
            });

            // Setting the ViewPager For the SlidingTabsLayout
            tabs.setViewPager(viewPager);
        }

        return rootView;
    }

    @Override
    public void onAttach(Context activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.menu_main, menu);
    }
}

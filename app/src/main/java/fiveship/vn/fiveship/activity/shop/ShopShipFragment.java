package fiveship.vn.fiveship.activity.shop;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.ShopShipPagerAdapter;
import fiveship.vn.fiveship.utility.SlidingTabLayout;

public class ShopShipFragment extends Fragment{

    public  ViewPager mViewPager;
    private String[] mTabTitles;
    private FragmentActivity mContext;
    private ShopShipPagerAdapter mAdapter;
    private SlidingTabLayout mTabStrip;

    public static synchronized ShopShipFragment newInstance() {

        ShopShipFragment instance = new ShopShipFragment();

        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shop_ship, container, false);
        String mTitleOne = mContext.getResources().getString(R.string.shop_ship_tab_shipper_near) ;
        String mTitleTwo = mContext.getResources().getString(R.string.shop_ship_tab_new);
        mTabTitles = new String[] {mTitleOne,mTitleTwo};

        // Give the PagerSlidingTabStrip the ViewPager
        mTabStrip = (SlidingTabLayout) view.findViewById(R.id.shop_ship_tabs);

        view.findViewById(R.id.btn_shop_create_ship).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent create = new Intent(getActivity(), ShopCreateGroupOrderActivity.class);
                create.putExtra("isGroup", true);
                startActivity(create);

                /*final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.choose_create_order_template_dialog);
                dialog.findViewById(R.id.create_single_order_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent create = new Intent(getActivity(), ShopShipCreateActivity.class);
                        startActivity(create);
                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.create_group_order_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent create = new Intent(getActivity(), ShopCreateGroupOrderActivity.class);
                        create.putExtra("isGroup", true);
                        startActivity(create);
                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.create_multi_order_btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent create = new Intent(getActivity(), ShopCreateGroupOrderActivity.class);
                        create.putExtra("isGroup", false);
                        startActivity(create);
                        dialog.dismiss();
                    }
                });
                dialog.show();*/
            }
        });

        mAdapter = new ShopShipPagerAdapter(mContext.getSupportFragmentManager(), mTabTitles, true);
        mViewPager = (ViewPager) view.findViewById(R.id.shop_ship_viewpager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(mTabTitles.length - 1);
        mTabStrip.setDistributeEvenly(true);
        mTabStrip.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        mTabStrip.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(mContext, R.color.fiveship_common_white);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        mTabStrip.setViewPager(mViewPager);

        return view;
    }

    @Override
    public void onAttach(Context activity) {
        mContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                //MainActivity.openNavigator();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}

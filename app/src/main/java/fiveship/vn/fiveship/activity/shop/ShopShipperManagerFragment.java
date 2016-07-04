package fiveship.vn.fiveship.activity.shop;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.ShopShipperManagerPagerAdapter;
import fiveship.vn.fiveship.utility.SlidingTabLayout;

public class ShopShipperManagerFragment extends Fragment implements ShopActivity.OnTitleChanged{


    private Context mContext;
    private SlidingTabLayout mTabStrips;

    public ShopShipperManagerFragment() {
        // Required empty public constructor
    }

    public static ShopShipperManagerFragment newInstance() {
        ShopShipperManagerFragment fragment = new ShopShipperManagerFragment();
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
        View view = inflater.inflate(R.layout.fragment_shop_shipper_manager, container, false);
        String[] mTabTitles = getActivity().getResources().getStringArray(R.array.arr_shipper_manager_tab_title);

        // Give the PagerSlidingTabStrip the ViewPager
        mTabStrips = (SlidingTabLayout) view.findViewById(R.id.shop_shipper_manager_tabs);

        if(mTabTitles.length <=0){

            mTabStrips.setVisibility(View.GONE);

        }else {

            ShopShipperManagerPagerAdapter shopShipPagerAdapter = new ShopShipperManagerPagerAdapter(((FragmentActivity)mContext).getSupportFragmentManager(), mTabTitles);
            ViewPager viewPager = (ViewPager) view.findViewById(R.id.shop_shipper_manager_viewpager);
            viewPager.setAdapter(shopShipPagerAdapter);
            viewPager.setOffscreenPageLimit(mTabTitles.length);
            mTabStrips.setDistributeEvenly(true);
            mTabStrips.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            mTabStrips.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return ContextCompat.getColor(mContext, R.color.fiveship_common_white);
                }
            });

            // Setting the ViewPager For the SlidingTabsLayout
            mTabStrips.setViewPager(viewPager);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onTitleChanged(int i, String title) {
        mTabStrips.setTitleTab(i, title);
    }
}

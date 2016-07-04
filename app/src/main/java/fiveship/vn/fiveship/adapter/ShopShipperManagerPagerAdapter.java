package fiveship.vn.fiveship.adapter;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import fiveship.vn.fiveship.activity.shop.ShopShipperManagerTab;

/**
 * Created by Unstopable on 4/8/2016.
 */
public class ShopShipperManagerPagerAdapter extends FragmentStatePagerAdapter{

    public String mTitles[];

    public ShopShipperManagerPagerAdapter(FragmentManager fm, String[] mTitles) {
        super(fm);
        this.mTitles = mTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return ShopShipperManagerTab.newInstance(position);
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public String getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}

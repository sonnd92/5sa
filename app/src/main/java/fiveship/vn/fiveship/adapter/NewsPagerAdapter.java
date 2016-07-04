package fiveship.vn.fiveship.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import fiveship.vn.fiveship.activity.NewsTab;
import fiveship.vn.fiveship.activity.PolicyTab;
import fiveship.vn.fiveship.activity.shipper.MapFragment;
import fiveship.vn.fiveship.activity.shipper.ShipperHistoryTab;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;

/**
 * Created by Unstopable on 6/6/2016.
 */
public class NewsPagerAdapter extends FragmentStatePagerAdapter {
    private String[] mTitles;

    public NewsPagerAdapter(FragmentManager fm, String[]mTitles) {
        super(fm);
        this.mTitles = mTitles;
    }

    public NewsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        String typeOrder;
        if (position == 0) {

            return NewsTab.newInstance();

        } else {

            return PolicyTab.newInstance();

        }
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

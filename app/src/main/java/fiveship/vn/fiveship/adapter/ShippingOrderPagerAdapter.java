package fiveship.vn.fiveship.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import fiveship.vn.fiveship.activity.shipper.MapFragment;
import fiveship.vn.fiveship.activity.shipper.ShipperHistoryTab;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;

/**
 * Created by sonnd on 13/10/2015.
 */
public class ShippingOrderPagerAdapter extends FragmentStatePagerAdapter {
    public String mTitle[];

    public ShippingOrderPagerAdapter(FragmentManager fm, String[] mTitle) {
        super(fm);
        this.mTitle = mTitle;
    }

    public ShippingOrderPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        String typeOrder;
        if (position == 0) {
            return MapFragment.newInstance();

        } else {
            return ShipperHistoryTab.newInstance(OrderStatusEnum.PENDING.getStatusCode(), -1);
        }
    }

    @Override
    public int getCount() {
        return mTitle.length;
    }

    @Override
    public String getPageTitle(int position) {
        return mTitle[position];
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

}

package fiveship.vn.fiveship.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;

import fiveship.vn.fiveship.activity.shipper.ShipperHistoryTab;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;

/**
 * Created by sonnd on 14/10/2015.
 */
public class HistoryTabAdapter extends FragmentStatePagerAdapter {
    public String[] mTitle;

    public HistoryTabAdapter(FragmentManager fm, String[] mTitle) {
        super(fm);
        this.mTitle = mTitle;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return ShipperHistoryTab.newInstance(0, position);
            case 1:
                return ShipperHistoryTab.newInstance(OrderStatusEnum.PENDING.getStatusCode(), position);
            case 2:
                return ShipperHistoryTab.newInstance(OrderStatusEnum.RECEIVED.getStatusCode(), position);
            case 3:
                return ShipperHistoryTab.newInstance(OrderStatusEnum.SHIPPING.getStatusCode(), position);
            case 4:
                return ShipperHistoryTab.newInstance(OrderStatusEnum.END.getStatusCode(), position);
            case 5:
                return ShipperHistoryTab.newInstance(OrderStatusEnum.COMPLETED.getStatusCode(), position);
            case 6:
                return ShipperHistoryTab.newInstance(OrderStatusEnum.CANCEL.getStatusCode(), position);
            default:
                return null;
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

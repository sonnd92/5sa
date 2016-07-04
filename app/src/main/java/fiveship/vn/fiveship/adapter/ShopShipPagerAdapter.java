package fiveship.vn.fiveship.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import fiveship.vn.fiveship.activity.shop.ShopMapFragment;
import fiveship.vn.fiveship.activity.shop.ShopHistoryTab;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;
import fiveship.vn.fiveship.utility.enums.ShopTabManagerPositionEnum;

/**
 * Created by BVN on 04/11/2015.
 */
public class ShopShipPagerAdapter extends FragmentStatePagerAdapter {

    private String[] mTabTitles;
    private boolean mIsHome;

    public ShopShipPagerAdapter(FragmentManager fm, String[] tabTitles, boolean isHome) {
        super(fm);
        this.mTabTitles = tabTitles;
        this.mIsHome = isHome;
    }

    @Override
    public int getCount() {
        return mTabTitles.length;
    }

    @Override
    public Fragment getItem(int position) {

        if(mIsHome && position == ShopTabManagerPositionEnum.ALL.getPosition()){
            return ShopMapFragment.newInstance();
        }

        if(mIsHome && position == ShopTabManagerPositionEnum.WAIT.getPosition()){
            return ShopHistoryTab.newInstance(OrderStatusEnum.PENDING.getStatusCode(), ShopTabManagerPositionEnum.WAIT.getPosition(), true, true);
        }

        if(!mIsHome && position == ShopTabManagerPositionEnum.ALL.getPosition()){
            return ShopHistoryTab.newInstance(0, ShopTabManagerPositionEnum.ALL.getPosition(), true);
        }

        if(!mIsHome && position == ShopTabManagerPositionEnum.WAIT.getPosition()){
            return ShopHistoryTab.newInstance(OrderStatusEnum.PENDING.getStatusCode(), ShopTabManagerPositionEnum.WAIT.getPosition(), true);
        }

        if(!mIsHome && position == ShopTabManagerPositionEnum.RECEIVED.getPosition()){
            return ShopHistoryTab.newInstance(OrderStatusEnum.RECEIVED.getStatusCode(),ShopTabManagerPositionEnum.RECEIVED.getPosition(), true);
        }

        if(!mIsHome && position == ShopTabManagerPositionEnum.SHIPPING.getPosition()){
            return ShopHistoryTab.newInstance(OrderStatusEnum.SHIPPING.getStatusCode(),ShopTabManagerPositionEnum.SHIPPING.getPosition(), true);
        }

        if(!mIsHome && position == ShopTabManagerPositionEnum.END.getPosition()){
            return ShopHistoryTab.newInstance(OrderStatusEnum.END.getStatusCode(),ShopTabManagerPositionEnum.END.getPosition(), true);
        }

        if(!mIsHome && position == ShopTabManagerPositionEnum.COMPLETE.getPosition()){
            return ShopHistoryTab.newInstance(OrderStatusEnum.COMPLETED.getStatusCode(), ShopTabManagerPositionEnum.COMPLETE.getPosition(), true);
        }

        if(!mIsHome && position == ShopTabManagerPositionEnum.CANCEL.getPosition()){
            return ShopHistoryTab.newInstance(OrderStatusEnum.CANCEL.getStatusCode(), ShopTabManagerPositionEnum.CANCEL.getPosition(), true);
        }

        return ShopMapFragment.newInstance();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return mTabTitles[position];
    }

}

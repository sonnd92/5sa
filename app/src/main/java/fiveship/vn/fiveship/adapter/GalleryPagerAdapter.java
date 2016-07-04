package fiveship.vn.fiveship.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import fiveship.vn.fiveship.activity.GalleryItemFragment;

/**
 * Created by BVN on 04/11/2015.
 */
public class GalleryPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> src;

    public GalleryPagerAdapter(FragmentManager fm, ArrayList<String> src) {
        super(fm);
        this.src = src;
    }

    @Override
    public int getCount() {
        return src.size();
    }

    @Override
    public Fragment getItem(int position) {
        return GalleryItemFragment.newInstance(src.get(position));
    }
}

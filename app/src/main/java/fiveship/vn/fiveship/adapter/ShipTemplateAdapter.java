package fiveship.vn.fiveship.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.model.ShipTemplateItem;

/**
 * Created by sonnd on 18/10/2015.
 */
public class ShipTemplateAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ShipTemplateItem> items;
    private int lastPosition = 0;
    private int currentPos;

    public ShipTemplateAdapter(Context context, ArrayList<ShipTemplateItem> items) {
        this.context = context;
        this.items = items;
    }

    public int getIdItem(int position){
        return items.get(position).getId();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ShipTemplateItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater mInflate = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView = mInflate.inflate(R.layout.shop_ship_template_item, null);

        ((TextView)convertView.findViewById(R.id.ship_template_item_name)).setText(String.valueOf(position + 1) + ". " + items.get(position).getName());

        if(position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.up_from_bottom);
            convertView.startAnimation(animation);
            lastPosition = position;
        }

        return convertView;
    }

}

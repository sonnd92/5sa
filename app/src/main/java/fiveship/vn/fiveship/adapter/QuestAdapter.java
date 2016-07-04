package fiveship.vn.fiveship.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;

/**
 * Created by sonnd on 18/10/2015.
 */
public class QuestAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<JSONObject> items;
    private int lastPosition = 0;
    private int currentPos;

    public QuestAdapter() {
    }

    public QuestAdapter(Context context, ArrayList<JSONObject> items) {
        this.context = context;
        this.items = items;
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            LayoutInflater mInflate = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflate.inflate(R.layout.quest_item, null);

            try {

                JSONObject data = items.get(position);
                Picasso.with(context).load(data.getString("Avatar")).placeholder(R.drawable.default_avatar).into((ImageView) convertView.findViewById(R.id.message_avatar));
                ((TextView)convertView.findViewById(R.id.quest_name)).setText(data.getString("Name"));
                ((TextView)convertView.findViewById(R.id.quest_gift)).setText(data.getString("Gift"));
                ((TextView)convertView.findViewById(R.id.quest_status)).setText(data.getString("StatusName"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.up_from_bottom);
            convertView.startAnimation(animation);
            lastPosition = position;
        }
        currentPos = position;
        return convertView;
    }
}

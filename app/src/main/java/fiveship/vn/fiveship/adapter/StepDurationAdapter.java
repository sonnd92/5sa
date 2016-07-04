package fiveship.vn.fiveship.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.model.StepDirectionItem;

/**
 * Created by sonnd on 05/12/2015.
 */
public class StepDurationAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<StepDirectionItem> items;
    private int lastPosition;

    public StepDurationAdapter() {
    }

    public StepDurationAdapter(Context context, ArrayList<StepDirectionItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflate = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflate.inflate(R.layout.step_duration_item_layout, null);

        }

        Spanned sp1 = Html.fromHtml(items.get(position).getSummary(), null, null);

        String summary = sp1.toString().replaceAll("\\n+", "\n").replaceAll("\\n$", ".");

        ((TextView) convertView.findViewById(R.id.step_summary)).setText(summary);

        ((TextView) convertView.findViewById(R.id.step_duration))
                .setText(items.get(position).getDistance() + " (" + items.get(position).getDuration() + ")");

        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.up_from_bottom);
            convertView.startAnimation(animation);
            lastPosition = position;
        }
        return convertView;
    }
}

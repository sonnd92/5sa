package fiveship.vn.fiveship.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;

/**
 * Created by sonnd on 18/10/2015.
 */
public class ShipHistoryTradeAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<JSONObject> items;

    public ShipHistoryTradeAdapter() {
    }

    public ShipHistoryTradeAdapter(Context context, ArrayList<JSONObject> items) {
        this.context = context;
        this.items = items;
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public JSONObject getItem(int position) {
        try {
            return items.get(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater mInflate = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView = mInflate.inflate(R.layout.ship_account_trade_item, null);

        LinearLayout listContainer = (LinearLayout)convertView.findViewById(R.id.list_trade);

        try {

            JSONObject data = items.get(position);

            ((TextView) convertView.findViewById(R.id.account_trade_date)).setText(data.getString("Date"));

            JSONArray listTrade = data.getJSONArray("List");

            for (int i = 0; i < listTrade.length(); i++){

                JSONObject dataList = listTrade.getJSONObject(i);

                View history = mInflate.inflate(R.layout.ship_account_history_trade_item, null);

                ((TextView) history.findViewById(R.id.account_trade_name)).setText(dataList.getString("Name"));

                ((TextView) history.findViewById(R.id.account_trade_money)).setText(dataList.getString("Value"));

                listContainer.addView(history);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }
}

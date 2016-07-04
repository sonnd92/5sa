package fiveship.vn.fiveship.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.model.MessageItem;
import fiveship.vn.fiveship.model.ShipTemplateItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShippingOrderService;

import android.widget.Filter;

import org.json.JSONException;
import org.json.JSONObject;

public class SuggestShipAdapter extends ArrayAdapter<String> {

    protected static final String TAG = "SuggestShipAdapter";

    private List<String> suggestions;
    private List<ShipTemplateItem> items;
    protected ShippingOrderService shippingOrderService;
    private int shopId;

    public SuggestShipAdapter(Context context, int shopId) {
        super(context, R.layout.simple_item_list_suggest);
        this.shopId = shopId;
        suggestions = new ArrayList<>();
        items = new ArrayList<>();
        shippingOrderService = ShippingOrderService.get_instance(context);
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public String getItem(int index) {
        return suggestions.get(index);
    }

    public ShipTemplateItem getShipItem(int index) {
        return items.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // A class that queries a web API, parses the data and
                    // returns an ArrayList<GoEuroGetSet>
                    JSONObject sug = shippingOrderService.autocompleteShip(String.valueOf(shopId), constraint.toString());

                    suggestions.clear();

                    items.clear();

                    try {

                        for (int i=0;i< sug.getJSONArray("data").length();i++) {

                            suggestions.add(sug.getJSONArray("data").getJSONObject(i).getString("Name"));

                            ShipTemplateItem item = new ShipTemplateItem();
                            JSONObject obj = sug.getJSONArray("data").getJSONObject(i);

                            item.setName(obj.getString("Name"));
                            item.setPrepayShip(obj.getString("StrTotalValue"));
                            item.setCostShip(obj.getString("StrCostShip"));
                            item.setStrProperty(obj.getString("StrProperty"));
                            item.setDate(obj.getString("DateEnd"));
                            item.setNote(obj.getString("Note"));
                            item.setIsBig(obj.has("IsBig") && obj.getBoolean("IsBig"));
                            item.setIsBreak(obj.has("IsBreak") && obj.getBoolean("IsBreak"));
                            item.setIsLight(obj.has("IsLight") && obj.getBoolean("IsLight"));
                            item.setIsHeavy(obj.has("IsHeavy") && obj.getBoolean("IsHeavy"));
                            item.setIsFood(obj.has("IsFood") && obj.getBoolean("IsFood"));
                            item.setIsFood(obj.has("IsGroup") && obj.getBoolean("IsGroup"));

                            items.add(item);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Now assign the values and count to the FilterResults
                    // object
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint,
                                          FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return myFilter;
    }

}

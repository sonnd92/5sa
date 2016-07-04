package fiveship.vn.fiveship.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.model.ShipTemplateItem;
import fiveship.vn.fiveship.service.ShipperService;
import fiveship.vn.fiveship.service.ShippingOrderService;

public class SuggestShipperAmityAdapter extends ArrayAdapter<String> {

    protected static final String TAG = "SuggestShipAdapter";

    private List<String> suggestions;
    private List<CustomerItem> items;
    protected ShipperService shipperService;
    private int shopId;

    public SuggestShipperAmityAdapter(Context context, int shopId) {
        super(context, android.R.layout.simple_dropdown_item_1line);
        this.shopId = shopId;
        suggestions = new ArrayList<>();
        items = new ArrayList<>();
        shipperService = ShipperService.get_instance(context);
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public String getItem(int index) {
        return suggestions.get(index);
    }

    public CustomerItem getCustomerItem(int index) {
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
                    JSONObject suggests = shipperService.getListShipperAmity(shopId, constraint.toString());

                    suggestions.clear();

                    items.clear();

                    try {

                        for (int i=0;i< suggests.getJSONArray("data").length();i++) {

                            CustomerItem item = new CustomerItem();

                            item.setId(suggests.getJSONArray("data").getJSONObject(i).getInt("Id"));
                            item.setFirstName(suggests.getJSONArray("data").getJSONObject(i).getString("FirstName"));
                            item.setLastName(suggests.getJSONArray("data").getJSONObject(i).getString("LastName"));

                            suggestions.add(item.getFullName());

                            items.add(item);
                        }

                    } catch (Exception e) {
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

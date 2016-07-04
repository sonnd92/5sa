package fiveship.vn.fiveship.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.service.ShopService;

public class SuggestInfoToAdapter extends ArrayAdapter<String> {

    protected static final String TAG = "SuggestShipAdapter";

    private List<String> suggestions;
    private List<CustomerItem> items;
    private ShopService shopService;
    private int shopId;

    public SuggestInfoToAdapter(Context context, String nameFilter, int shopId) {
        super(context, R.layout.simple_item_list_suggest);
        this.shopId = shopId;
        suggestions = new ArrayList<>();
        items = new ArrayList<>();
        shopService = ShopService.get_instance(context);
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
                    JSONObject new_suggestions = shopService.autoCompleteCustomer(String.valueOf(shopId), constraint.toString());

                    suggestions.clear();

                    items.clear();

                    try {

                        for (int i=0;i< new_suggestions.getJSONArray("data").length();i++) {

                            CustomerItem item = new CustomerItem();

                            item.setLastName(new_suggestions.getJSONArray("data").getJSONObject(i).getString("LastName"));

                            item.setFirstName(new_suggestions.getJSONArray("data").getJSONObject(i).getString("FirstName"));

                            item.setPhone(new_suggestions.getJSONArray("data").getJSONObject(i).getString("Phone"));

                            item.setAddressDetail(new_suggestions.getJSONArray("data").getJSONObject(i).getString("AddressDetail"));

                            item.setAddress(new_suggestions.getJSONArray("data").getJSONObject(i).getString("Address"));

                            item.setXPoint(new_suggestions.getJSONArray("data").getJSONObject(i).getString("XPoint"));

                            item.setYPoint(new_suggestions.getJSONArray("data").getJSONObject(i).getString("YPoint"));

                            suggestions.add(item.getPhone() + " - " + item.getFullName());

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

package fiveship.vn.fiveship.service.adapter;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.model.ShipTemplateItem;
import fiveship.vn.fiveship.service.ShippingOrderService;

/**
 * Created by Unstopable on 09/03/2016.
 */
public class AddNewGroupShippingOrder extends AsyncTask<String, Void, JSONObject> {
    private Context context;
    private OnSendRequestCompleted listener;
    private ShippingOrderService service;
    private ShipTemplateItem item;

    public AddNewGroupShippingOrder(Context context, OnSendRequestCompleted listener, ShipTemplateItem item){
        this.service = ShippingOrderService.get_instance(context);
        this.context = context;
        this.listener = listener;
        this.item = item;
    }
    @Override
    protected JSONObject doInBackground(String... params) {
        return service.AddShippingOrderGroup(item);
    }
    @Override
    protected void onPostExecute(JSONObject result) {
        try {

            listener.onSendRequestCompleted(result != null && result.getBoolean("error"), result.getString("message"));

        } catch (Exception e) {
            listener.onSendRequestCompleted(true, context.getString(R.string.exception_message));
            e.printStackTrace();
        }
    }
}

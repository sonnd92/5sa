package fiveship.vn.fiveship.service.adapter;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.service.ShipperService;

/**
 * Created by sonnd on 30/10/2015.
 */
public class ChangeStateOrder extends AsyncTask<String, Void, JSONObject> {

    private OnSendRequestCompleted listener;
    private ShipperService orderServices;
    private int shipperId;
    private int shopId;
    private int shipId;
    private int status;

    private Context context;

    public ChangeStateOrder(Context _context, OnSendRequestCompleted _listener, int shipperId, int shopId, int shipId, int status) {
        context = _context;
        listener = _listener;
        this.shipId = shipId;
        this.shipperId = shipperId;
        this.shopId = shopId;
        this.status = status;
        this.orderServices = ShipperService.get_instance(context);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return orderServices.ChangeOrder(shipperId, shopId, shipId, status);
    }


    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            listener.onSendRequestCompleted(result.has("error") && result.getBoolean("error"), result.getString("message"));

        } catch (JSONException e) {
            e.printStackTrace();
            listener.onSendRequestCompleted(false, context.getString(R.string.received_order_fail));
        }
    }
}

package fiveship.vn.fiveship.service.adapter;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.service.ShipperService;

/**
 * Created by sonnd on 26/10/2015.
 */
public class CancelOrder extends AsyncTask<String, Void, JSONObject> {

    private OnSendRequestCompleted listener;
    private ShipperService orderServices;
    private int cusId;
    private int shipId;

    private Context context;

    public CancelOrder(Context _context, OnSendRequestCompleted _listener, int _cusId, int _shipId) {
        context = _context;
        listener = _listener;
        cusId = _cusId;
        shipId = _shipId;
        orderServices = ShipperService.get_instance(_context);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return orderServices.CancelOrder(cusId, shipId);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {

            listener.onSendRequestCompleted(result.has("error") && result.getBoolean("error"), result.has("message") ? result.getString("message") : "");

        } catch (JSONException e) {
            e.printStackTrace();
            listener.onSendRequestCompleted(false, context.getString(R.string.received_order_fail));
        }
    }
}

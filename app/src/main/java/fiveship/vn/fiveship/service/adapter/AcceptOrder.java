package fiveship.vn.fiveship.service.adapter;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.service.ShipperService;
import fiveship.vn.fiveship.service.ShippingOrderService;

/**
 * Created by sonnd on 22/10/2015.
 */
public class AcceptOrder extends AsyncTask<String, Void, JSONObject>{

    private OnSendRequestCompleted listener;
    private ShipperService orderServices;
    private int cusId;
    private int shipId;
    private String xPoint;
    private String yPoint;

    private Context context;

    public AcceptOrder(Context _context, ShipperService orderServices, OnSendRequestCompleted _listener, int _cusId, int _shipId, String _xPoint, String _yPoint) {
        context = _context;
        listener = _listener;
        cusId = _cusId;
        shipId = _shipId;
        xPoint = _xPoint;
        yPoint = _yPoint;
        this.orderServices = orderServices;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return orderServices.ReceptionOrder(cusId, shipId, xPoint, yPoint);
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

package fiveship.vn.fiveship.service.adapter;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.service.ShopService;

/**
 * Created by Unstopable on 4/13/2016.
 */
public class ChangeShippingCost extends AsyncTask<String, Void, JSONObject> {
    private OnSendRequestCompleted mListener;
    private ShopService mService;
    private Context mContext;
    private int mShopId, mShipId, mShipCost;

    public ChangeShippingCost(OnSendRequestCompleted mListener, ShopService mService, Context mContext, int mShopId, int mShipId, int mShipCost) {
        this.mListener = mListener;
        this.mService = mService;
        this.mContext = mContext;
        this.mShopId = mShopId;
        this.mShipId = mShipId;
        this.mShipCost = mShipCost;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return mService.changeShippingCost(mShopId, mShipId, mShipCost);
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        try {

            mListener.onSendRequestCompleted(jsonObject.has("error") && jsonObject.getBoolean("error"), jsonObject.has("message") ? jsonObject.getString("message") : "");

        } catch (Exception e) {
            e.printStackTrace();
            mListener.onSendRequestCompleted(false, mContext.getString(R.string.exception_message));
        }

    }
}

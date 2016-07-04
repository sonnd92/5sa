package fiveship.vn.fiveship.service.adapter;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.service.ShippingOrderService;

/**
 * Created by Unstoppable on 4/27/2016.
 */
public class VerifyPromotionCode extends AsyncTask<String, Void, JSONObject>{
    Context mContext;
    ShippingOrderService mService;
    String mCode;
    OnSendRequestCompleted mListener;

    public VerifyPromotionCode(Context mContext, ShippingOrderService mService, String mCode, OnSendRequestCompleted mListener) {
        this.mContext = mContext;
        this.mService = mService;
        this.mCode = mCode;
        this.mListener = mListener;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return mService.verifyPromotionCode(mCode);
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        try {
            mListener.onSendRequestCompleted(jsonObject != null, jsonObject != null ? jsonObject.getString("message") : mContext.getString(R.string.exception_message));
        } catch (JSONException e) {
            e.printStackTrace();
            mListener.onSendRequestCompleted(false, mContext.getString(R.string.exception_message));
        }
    }
}

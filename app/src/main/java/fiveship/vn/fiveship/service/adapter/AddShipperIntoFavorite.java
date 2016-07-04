package fiveship.vn.fiveship.service.adapter;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.service.ShopService;

/**
 * Created by Unstoppable on 4/11/2016.
 */
public class AddShipperIntoFavorite extends AsyncTask<String, Void, JSONObject> {
    OnSendRequestCompleted mListener;
    ShopService mService;
    Context mContext;
    int mShopId, mShipperId;
    boolean mIsFav;

    public AddShipperIntoFavorite(OnSendRequestCompleted mListener, ShopService mService, Context mContext, int mShopId, int mShipperId, boolean mIsFav) {
        this.mListener = mListener;
        this.mService = mService;
        this.mContext = mContext;
        this.mShopId = mShopId;
        this.mShipperId = mShipperId;
        this.mIsFav = mIsFav;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return mService.addShipperOfShop(mShopId, mShipperId, mIsFav);
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

package fiveship.vn.fiveship.service.adapter;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.service.OtherService;

/**
 * Created by Unstoppable on 5/13/2016.
 */
public class PinFacebookFeed extends AsyncTask<String, Void, JSONObject> {

    private OnSendRequestCompleted mListener;
    private Context mContext;
    private OtherService mService;

    public PinFacebookFeed(Context mContext,OnSendRequestCompleted mListener) {
        this.mListener = mListener;
        this.mContext = mContext;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return mService.AddFavoriteOfShipper(Integer.parseInt(params[0]), Integer.parseInt(params[1]), params[2]);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mService = OtherService.get_instance(mContext);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);

        try {

            mListener.onSendRequestCompleted(result.has("error") && result.getBoolean("error"), result.getString("message"));

        } catch (JSONException e) {

            e.printStackTrace();
            mListener.onSendRequestCompleted(false, mContext.getString(R.string.received_order_fail));
        }
    }
}

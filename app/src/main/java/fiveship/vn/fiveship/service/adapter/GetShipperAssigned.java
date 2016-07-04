package fiveship.vn.fiveship.service.adapter;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import fiveship.vn.fiveship.model.ShipperItem;
import fiveship.vn.fiveship.service.ShipperService;

/**
 * Created by Unstoppable on 4/22/2016.
 */
public class GetShipperAssigned extends AsyncTask<String, Void, JSONObject> {
    private ShipperService mService;
    private int shopId;
    private int shipId;
    private OnGetShipperCompleted mListener;

    public GetShipperAssigned(ShipperService mService, int shopId, int shipId, OnGetShipperCompleted mListener) {
        this.mService = mService;
        this.shopId = shopId;
        this.shipId = shipId;
        this.mListener = mListener;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return mService.GetShipperAssigned(shipId,shopId);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);
        ShipperItem item = null;
        String message = "";
        try {
            if (result != null && result.length() > 0 && result.has("data") && !result.getString("data").isEmpty()) {

                JSONObject oneSurvey = new JSONObject(result.getString("data"));
                item = new ShipperItem();
                item.setId(oneSurvey.getInt("Id"));
                item.setName(oneSurvey.getString("Name"));
                item.setAvatarUrl(oneSurvey.getString("UrlAvatar"));
                item.setAvatarLabelUrl(oneSurvey.getString("UrlAvatarLabel"));
                item.setMotorId(oneSurvey.getString("MotorId"));
                item.setIsConfirm(oneSurvey.getBoolean("IsConfirm"));
                item.setNumberShip(oneSurvey.getString("NumberShip"));
                item.setNumberShipSuccess(oneSurvey.getString("NumberShipSuccess"));
                item.setShipStatistic(oneSurvey.getString("StatisticsShip"));
                message = oneSurvey.getString("Message");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mListener.onGetShipperCompleted(item, message);
    }
    public interface OnGetShipperCompleted{
        void onGetShipperCompleted(ShipperItem item, String message);
    }
}

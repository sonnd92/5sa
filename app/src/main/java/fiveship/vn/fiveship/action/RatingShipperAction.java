package fiveship.vn.fiveship.action;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import fiveship.vn.fiveship.service.ShipperService;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.SessionManager;

/**
 * Created by sonnd on 13/11/2015.
 */
public class RatingShipperAction {
    Context context;
    ShipperService shipperService;
    RatingActionCallback mListener;

    int shipId, shopId;
    float rating;
    String comment;
    boolean isAmity;

    public RatingShipperAction(Context context, int shipId, int shopId, float rating, String comment, boolean isAmity){
        this.context = context;
        this.shipperService = ShipperService.get_instance(context);
        this.shipId = shipId;
        this.shopId =  shopId;
        this.rating = rating;
        this.comment = comment;
        this.isAmity = isAmity;
    }

    public interface RatingActionCallback {
        void callBackSuccess(String message);
    }

    public void setRatingActionCallback(RatingActionCallback listener) {
        mListener = listener;
    }

    public RatingShipperAction post(){
        new PostRatingShipperActionTask().execute();
        return this;
    }

    private class PostRatingShipperActionTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return shipperService.RatingShipper(shipId, shopId, rating, comment, isAmity);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                mListener.callBackSuccess(result.getString("message"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

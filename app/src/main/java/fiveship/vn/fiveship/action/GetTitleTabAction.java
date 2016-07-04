package fiveship.vn.fiveship.action;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.activity.EditCustomerInfoActivity;
import fiveship.vn.fiveship.service.CustomerService;
import fiveship.vn.fiveship.service.OtherService;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.SessionManager;

/**
 * Created by BVN on 08/11/2015.
 */
public class GetTitleTabAction {

    Context context;
    OtherService otherService;
    int id;
    boolean isShop;
    GetTitleTabActionCallback mListener;
    private ArrayList<String> titles = new ArrayList<>();

    public GetTitleTabAction(Context context, int id, boolean isShop, GetTitleTabActionCallback callback){
        this.context = context;
        this.otherService = OtherService.get_instance(context);
        this.id = id;
        this.isShop = isShop;
        this.mListener = callback;
    }

    public interface GetTitleTabActionCallback {
        void callBackSuccess(ArrayList<String> titles, boolean isSuccess);
    }

    public void get(){
        new GetTitles().execute();
    }

    private class GetTitles extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return otherService.getTitleTab(id, isShop);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                    JSONArray listJO = result.getJSONArray("list");
                    for (int i = 0; i < listJO.length(); i++){
                        titles.add(listJO.getJSONObject(i).getString("Name"));
                    }

                    mListener.callBackSuccess(titles, result.has("error") && !result.getBoolean("error"));
            } catch (Exception e) {
                e.printStackTrace();
                mListener.callBackSuccess(titles, false);
            }
        }
    }

}

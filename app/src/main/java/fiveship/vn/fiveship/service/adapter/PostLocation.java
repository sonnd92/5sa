package fiveship.vn.fiveship.service.adapter;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import fiveship.vn.fiveship.service.LocationService;

/**
 * Created by sonnd on 26/10/2015.
 */
public class PostLocation extends AsyncTask<String, Void, JSONObject> {
    private LocationService locationService;

    public PostLocation(Context mContext) {
        this.locationService = LocationService.get_instance(mContext);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try{
            return locationService.addLocation(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
        }catch (Exception ex){
            ex.printStackTrace();
            return new JSONObject();
        }
    }

    @Override
    protected void onPostExecute(JSONObject result) {
    }
}

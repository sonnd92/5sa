package fiveship.vn.fiveship.service.adapter;

import android.os.AsyncTask;

import org.json.JSONObject;

import fiveship.vn.fiveship.service.NotificationService;

/**
 * Created by Unstoppable on 6/30/2016.
 */
public class UpdateIsViewedNotification extends AsyncTask<String, Void, JSONObject> {

    private NotificationService mService;

    public UpdateIsViewedNotification(NotificationService service){
        this.mService = service;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return mService.updateIsViewed(params[0]);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
    }
}
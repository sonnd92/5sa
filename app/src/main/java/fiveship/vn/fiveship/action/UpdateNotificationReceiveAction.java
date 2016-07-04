package fiveship.vn.fiveship.action;

import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONObject;
import fiveship.vn.fiveship.service.NotificationService;

/**
 * Created by BVN on 08/11/2015.
 */
public class UpdateNotificationReceiveAction {

    private static UpdateNotificationReceiveAction _instance;

    public synchronized static UpdateNotificationReceiveAction get_instance(Context context){
        if(_instance == null){

            _instance = new UpdateNotificationReceiveAction(context);
        }
        return _instance;
    }

    NotificationService notificationService;

    public UpdateNotificationReceiveAction(Context context){
        this.notificationService = NotificationService.get_instance(context);
    }

    public void post(String id){
        new UpdateNotificationReceiveTask().execute(id);
    }

    private class UpdateNotificationReceiveTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                return notificationService.updateIsReceive(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return new JSONObject();
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
        }
    }

}

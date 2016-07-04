package fiveship.vn.fiveship.service;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sonnd on 29/10/2015.
 */
public class NotificationService extends BaseService {

    private static NotificationService _instance;

    public NotificationService(Context context){
        super(context);
    }

    public synchronized static NotificationService get_instance(Context context){
        if(_instance == null){

            _instance = new NotificationService(context);
        }
        return _instance;
    }

    public JSONObject GetList(int id, boolean isShop, int skip, int top){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("id", String.valueOf(id));

            params.put("isShop",  String.valueOf(isShop));

            params.put("skip", String.valueOf(skip * top));

            params.put("top", String.valueOf(top));

            JSONObject data = new JSONObject(readJSONWS("SystemNotification/GetNotification", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject getListWaiting(String id, String isShop){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("id", id);

            params.put("isShop",  isShop);

            JSONObject data = new JSONObject(readJSONWS("SystemNotification/GetNotificationWaiting", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject updateIsViewed(String id){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("id", id);

            JSONObject data = new JSONObject(readJSONWSWithPost("SystemNotification/UpdateIsViewed", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject updateIsReceive(String id){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("id", id);

            params.put("platform", "android");

            JSONObject data = new JSONObject(readJSONWSWithPost("SystemNotification/UpdateIsReceive", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

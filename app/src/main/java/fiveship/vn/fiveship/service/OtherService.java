package fiveship.vn.fiveship.service;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sonnd on 21/10/2015.
 */
public class OtherService extends BaseService {
    private static OtherService _instance;

    public OtherService(Context context){
        super(context);
    }

    public synchronized static OtherService get_instance(Context context){
        if(_instance == null){

            _instance = new OtherService(context);
        }
        return _instance;
    }

    public JSONObject getTitleTab(int id, boolean isShop){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("id", String.valueOf(id));

            params.put("isShop", String.valueOf(isShop));

            JSONObject data = new JSONObject(readJSONWS("Other/GetTitleTab", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject getDistanceAndCostShip(String fromX, String fromY, String toX, String toY){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("fromX", fromX);

            params.put("fromY", fromY);

            params.put("toX", toX);

            params.put("toY", toY);

            JSONObject data = new JSONObject(readJSONWS("Other/GetDistanceAndCostShip", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject addConnection(String connection, int id, boolean isShipper){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("connection", connection);

            params.put("id", String.valueOf(id));

            params.put("isShipper", String.valueOf(isShipper));

            params.put("platform", "android");

            JSONObject data = new JSONObject(readJSONWSWithPost("Connection/AddConnection", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject AddFavoriteOfShipper(int shipperId, int id, String postId){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("id", String.valueOf(id));

            params.put("postId", postId);

            JSONObject data = new JSONObject(readJSONWSWithPost("Facebook/AddFavoriteOfShipper", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject RemoveFavoriteOfShipper(int shipperId, int id, String postId){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("id", String.valueOf(id));

            params.put("postId", postId);

            JSONObject data = new JSONObject(readJSONWSWithPost("Facebook/RemoveFavoriteOfShipper", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject crawlerFacebookFeedHcm(int shipperId){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            JSONObject data = new JSONObject(readJSONWS("Facebook/GetListShipFacebookHcm", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject crawlerFacebookFeed(int shipperId){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            JSONObject data = new JSONObject(readJSONWS("Facebook/GetListShipFacebook", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject GetListShipFacebookOfShipper(int shipperId, int skip, int top){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));
            params.put("skip", String.valueOf(skip - 1));
            params.put("top", String.valueOf(top));

            JSONObject data = new JSONObject(readJSONWS("Facebook/GetListShipFacebookOfShipper", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}

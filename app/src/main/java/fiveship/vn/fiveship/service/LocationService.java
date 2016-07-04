package fiveship.vn.fiveship.service;

import android.content.Context;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sonnd on 29/10/2015.
 */
public class LocationService extends BaseService {

    private static LocationService _instance;

    public LocationService(Context context){
        super(context);
    }

    public synchronized static LocationService get_instance(Context context){
        if(_instance == null){

            _instance = new LocationService(context);
        }
        return _instance;
    }

    public JSONObject addLocation(String shipperId, String shopId, String xPoint, String yPoint, String connId, String isShipper, String uuid){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", shipperId);

            params.put("shopId",  shopId);

            params.put("xPoint", xPoint);

            params.put("yPoint", yPoint);

            params.put("connId", connId);

            params.put("isShipper", isShipper);

            params.put("uuid", uuid);

            JSONObject data = new JSONObject(readJSONWSWithPost("Location/AddLocationCustomer", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject trackingShipperOfShip(String shipId, String shopId){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipId", shipId);

            params.put("shopId",  shopId);

            JSONObject data = new JSONObject(readJSONWSWithPost("Location/TrackingShipperOfShip", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

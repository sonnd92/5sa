package fiveship.vn.fiveship.service;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sonnd on 23/10/2015.
 */
public class ShipperService extends BaseService {

    private static ShipperService _instance;

    public ShipperService(Context context){
        super(context);
    }

    public synchronized static ShipperService get_instance(Context context){
        if(_instance == null){

            _instance = new ShipperService(context);
        }
        return _instance;
    }
    public JSONObject ReceptionOrder(int shipperId, int shipId, String xPoint, String yPoint){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("shipId",  String.valueOf(shipId));

            params.put("XPoint", xPoint);

            params.put("YPoint", yPoint);

            params.put("costShip", String.valueOf(0));

            return new JSONObject(readJSONWSWithPost("Shipper/ShipperReciveShip", params));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject ReceptionOrder(int shipperId, int shipId, String xPoint, String yPoint, int recommendCost){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("shipId",  String.valueOf(shipId));

            params.put("XPoint", xPoint);

            params.put("YPoint", yPoint);

            params.put("costShip", String.valueOf(recommendCost));

            return new JSONObject(readJSONWSWithPost("Shipper/ShipperReciveShip", params));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject cancelAssignShip(int shipperId, int shipId){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("shipId",  String.valueOf(shipId));

            return new JSONObject(readJSONWSWithPost("Shipper/CancelAssignShip", params));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject CancelOrder(int shipperId, int shipId){
        try{
            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("shipId",  String.valueOf(shipId));

            return new JSONObject(readJSONWSWithPost("Ship/DeleteShip", params));
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }
    public JSONObject ChangeOrder(int shipperId, int shopId, int shipId, int statusId){
        try{
            Map<String, String> params = new HashMap<String, String>();

            params.put("shipId", String.valueOf(shipId));

            params.put("statusId",  String.valueOf(statusId));

            params.put("shopId",  String.valueOf(shopId));

            params.put("shipperId",  String.valueOf(shipperId));

            return new JSONObject(readJSONWSWithPost("Ship/UpdateStatus", params));
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject UpdateShipperInfo(int accountId,
                                        int shipperId,
                                        String name,
                                        String sendPhone,
                                        String sendAddress,
                                        String sendX,
                                        String sendY,
                                        String motorId,
                                        String licenseMotor,
                                        String avatar){
        try{
            Map<String, String> params = new HashMap<String, String>();

            params.put("accountId", String.valueOf(accountId));

            params.put("shipperId", String.valueOf(shipperId));

            params.put("name",  name);

            params.put("sendPhone",  sendPhone);

            params.put("sendAddress",  sendAddress);

            params.put("sendX", sendX);

            params.put("sendY", sendY);

            params.put("motorId", motorId);

            params.put("licenseMotor", licenseMotor);

            params.put("avatar", avatar);

            return new JSONObject(readJSONWSWithPost("Shipper/UpdateShipperInfo", params));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject GetListShipperAssignShip(int shipId){
        try{

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipId", String.valueOf(shipId));

            return new JSONObject(readJSONWS("Shipper/GetListShipperAssignShip", params));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getListShipperAmity(int shopId, String keyword){
        try{

            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));

            params.put("keyword", keyword);

            return new JSONObject(readJSONWS("Shipper/GetListShipperAmity", params));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getShipperInfo( String shipperId, String shipId){
        try{

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", shipperId);

            params.put("shipId", shipId);

            return new JSONObject(readJSONWS("Shipper/GetShipperInfo", params));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getListShipperNearShip(String xPoint, String yPoint){
        try{

            Map<String, String> params = new HashMap<String, String>();

            params.put("xPoint", xPoint);

            params.put("yPoint", yPoint);

            return new JSONObject(readJSONWS("Shipper/GetListShipperNearShop", params));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject RatingShipper(int shipId, int shopId, float mark, String note, boolean isAmity){

        try{

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipId", String.valueOf(shipId));

            params.put("shopId", String.valueOf(shopId));

            params.put("mark", String.valueOf(mark));

            params.put("note", String.valueOf(note));

            params.put("isAmity", String.valueOf(isAmity));

            return new JSONObject(readJSONWSWithPost("Shipper/RatingShipper", params));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject GetShipperAssigned(int shipId, int shopId){
        try{

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipId", String.valueOf(shipId));

            params.put("shopId", String.valueOf(shopId));

            return new JSONObject(readJSONWS("Shipper/GetShipperOfShip", params));

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

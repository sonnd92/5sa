package fiveship.vn.fiveship.service;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sonnd on 29/10/2015.
 */
public class ShopService extends BaseService {

    private static ShopService _instance;

    public ShopService(Context context){
        super(context);
    }

    public synchronized static ShopService get_instance(Context context){
        if(_instance == null){

            _instance = new ShopService(context);
        }
        return _instance;
    }

    public JSONObject autoCompleteCustomer(String shopId, String name){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", shopId);

            params.put("name",  name);

            JSONObject data = new JSONObject(readJSONWS("Shop/AutoCompleteCustomer", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public JSONObject getTrackingShip(String shipId){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipId", shipId);

            JSONObject data = new JSONObject(readJSONWS("Shop/GetTrackingShip", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject GetOrdersOfShop(int shopId, String keyword, int page, int pageSize, int statusId){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));

            params.put("key",  keyword);

            params.put("page", String.valueOf(page));

            params.put("pageSize", String.valueOf(pageSize));

            params.put("statusId", String.valueOf(statusId));

            JSONObject data = new JSONObject(readJSONWS("Ship/GetListShipOfShop", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject acceptShipperForShip(int shopId, int shipperId, int shipId){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));

            params.put("shipperId",  String.valueOf(shipperId));

            params.put("shipId", String.valueOf(shipId));

            JSONObject data = new JSONObject(readJSONWSWithPost("Shop/ShopAcceptShipper", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject UpdateShopInfo(int accountId,
                                     int shopId,
                                     String name,
                                     String phone,
                                     String address,
                                     String shopName,
                                     String shopPhone,
                                     String shopAddressDetail,
                                     String shopAddress,
                                     String xPoint,
                                     String yPoint,
                                     String carer,
                                     String avatar){
        try{

            Map<String, String> params = new HashMap<String, String>();

            params.put("accountId", String.valueOf(accountId));

            params.put("shopId", String.valueOf(shopId));

            params.put("avatar",  avatar);

            params.put("name",  name);

            params.put("phone",  phone);

            params.put("address",  address);

            params.put("shopName",  shopName);

            params.put("shopPhone",  shopPhone);

            params.put("shopAddressDetail",  shopAddressDetail);

            params.put("shopAddress",  shopAddress);

            params.put("shopX", xPoint);

            params.put("shopY", yPoint);

            params.put("shopCarrer", carer);

            JSONObject data = new JSONObject(readJSONWSWithPost("Shop/UpdateShopInfo", params));

            return data;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject capturePictureForWeb(int shopId, String image){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));

            params.put("image",  image);

            JSONObject data = new JSONObject(readJSONWSWithPost("Shop/CapturePictureForWeb", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject getShopInfo(String shopId){
        try{

            Map<String, String> params = new HashMap<String, String>();

            params.put("id", shopId);

            JSONObject data = new JSONObject(readJSONWS("Shop/GetShopInfo", params));

            return data;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject prepareCreateShip(String id){
        try{

            Map<String, String> params = new HashMap<String, String>();

            params.put("id", id);

            JSONObject data = new JSONObject(readJSONWS("Shop/PrepareCreateShip", params));

            return data;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getListShipperManager(int shopId, String keyword, int skip, int top, boolean isBlock, boolean isAmity, boolean getAll){
        try{

            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));
            params.put("keyword", keyword);
            params.put("page", String.valueOf(skip));
            params.put("pageSize", String.valueOf(top));
            if(!getAll) {
                params.put("isBlock", String.valueOf(isBlock));
                params.put("isAmity", String.valueOf(isAmity));
            }
            JSONObject data = new JSONObject(readJSONWS("Shipper/GetListShipperOfShop", params));

            return data;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public JSONObject blockShipper(int shopId, int shipperId, boolean isBlock){
        try{
            JSONObject data;
            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));
            params.put("shipperId", String.valueOf(shipperId));
            if(isBlock)
                data = new JSONObject(readJSONWSWithPost("Shipper/BlockShipperOfShop", params));
            else{
                data = new JSONObject(readJSONWSWithPost("Shipper/UnblockShipperOfShop", params));
            }

            return data;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject addShipperOfShop(int shopId, int shipperId, boolean isFav){
        try{
            JSONObject data;
            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));
            params.put("shipperId", String.valueOf(shipperId));
            if(isFav)
                data = new JSONObject(readJSONWSWithPost("Shipper/AddAmityShipperOfShop", params));
            else{
                data = new JSONObject(readJSONWSWithPost("Shipper/RemoveAmityShipperOfShop", params));
            }

            return data;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject changeShippingCost(int shopId, int shipId, int shippingCost){
        try{
            JSONObject data;
            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));
            params.put("shipId", String.valueOf(shipId));
            params.put("costShip", String.valueOf(shippingCost));
            data = new JSONObject(readJSONWSWithPost("Ship/UpdateCostShip", params));

            return data;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getRatingAndReviewOfShipper(int shipperId, int skip, int top ){
        try{
            JSONObject data;
            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));
            params.put("page", String.valueOf(skip));
            params.put("pageSize", String.valueOf(top));
            data = new JSONObject(readJSONWS("Shipper/GetRattingOfShipper", params));

            return data;
        }catch (Exception e){
        e.printStackTrace();
    }
    return null;
    }

    //public JSONObject UpdateShopInfo(int accountId, int shopId, String name, String address, String phone, )
}

package fiveship.vn.fiveship.service;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import fiveship.vn.fiveship.model.DeliveryToItem;
import fiveship.vn.fiveship.model.ShipTemplateItem;

/**
 * Created by sonnd on 21/10/2015.
 */
public class ShippingOrderService extends BaseService {
    private static ShippingOrderService _instance;

    public ShippingOrderService(Context context){
        super(context);
    }

    public synchronized static ShippingOrderService get_instance(Context context){
        if(_instance == null){

            _instance = new ShippingOrderService(context);
        }
        return _instance;
    }

	public JSONObject autocompleteShip(String shopId, String name){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", shopId);

            params.put("name", name);

            JSONObject data = new JSONObject(readJSONWS("Ship/AutoCompleteShip", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject getListShipByListId(String listId, int shipperId){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("listId", listId);

            JSONObject data = new JSONObject(readJSONWS("Ship/GetListShipByListId", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject listShipNearlyShipperGroup(int shipperId, String keyword, String lat, String lng, String skip, String top){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("keyword", keyword);

            params.put("lat", lat);

            params.put("lng", lng);

            params.put("page", skip);

            params.put("pageSize", top);

            JSONObject data = new JSONObject(readJSONWS("Ship/ListShipNearlyShipperGroup", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject GetNearlyShippingOrder(int shipperId, String keyword, String lat, String lng, String skip, String top){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("keyword", keyword);

            params.put("lat", lat);

            params.put("lng", lng);

            params.put("page", skip);

            params.put("pageSize", top);

            JSONObject data = new JSONObject(readJSONWS("Ship/ListShipNearlyShipper", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject GetNewShippingOrder(String keyword, int status, int skip, int top){
        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("status", String.valueOf(status));

            params.put("keyword", keyword);

            params.put("page", String.valueOf(skip));

            params.put("pageSize", String.valueOf(top));

            JSONObject data = new JSONObject(readJSONWS("Ship/GetListShip", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public JSONObject GetShippingOrderDetail(int id, int shipperId){
        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("id", String.valueOf(id));

            params.put("shipperId", String.valueOf(shipperId));

            JSONObject data = new JSONObject(readJSONWS("Ship/GetShipDetail", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public JSONObject GetOldOrderOfShipper(String keyword,int status, int shipperId, int skip) {
        try {
            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("status", String.valueOf(status));

            params.put("keyword", keyword);

            params.put("page", String.valueOf(skip));

            JSONObject data = new JSONObject(readJSONWS("Ship/GetListShipOfShipper", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject CreateShippingOrder(HashMap<String, String> params){
        try {

            JSONObject data = new JSONObject(readJSONWSWithPost("Ship/AddShip", params));

            return data;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject modifyShipTemplate(HashMap<String, String> params){
        try {

            JSONObject data = new JSONObject(readJSONWSWithPost("Ship/ModifyShipTemplate", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject GetPendingShippingOrder(String keyword, int shipperId, int page, int pageSize){
        try {
            Map<String, String> params = new HashMap<String, String>();

            params.put("keyword", keyword);

            params.put("shipper", String.valueOf(shipperId));

            params.put("page", String.valueOf(page));

            params.put("pageSize", String.valueOf(pageSize));

            JSONObject data = new JSONObject(readJSONWS("Ship/GetListShipWaitingOfShipper", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject updateStatusShip(int shipId, int shipperId, int shopId, int statusId){
        try{
            Map<String, String> params = new HashMap<String, String>();

            params.put("shipId", String.valueOf(shipId));

            params.put("shipperId",  String.valueOf(shipperId));

            params.put("shopId",  String.valueOf(shopId));

            params.put("statusId",  String.valueOf(statusId));

            JSONObject data = new JSONObject(readJSONWSWithPost("Ship/UpdateStatus", params));

            return data;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject reportCheat(int shipperId, int shipId, String note){
        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("shipId", String.valueOf(shipId));

            params.put("note", String.valueOf(note));

            JSONObject data = new JSONObject(readJSONWSWithPost("Ship/ReportCheat", params));

            return data;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject cancelShip(int shopId,int shipId, String note){
        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));

            params.put("shipId", String.valueOf(shipId));

            params.put("note", String.valueOf(note));

            JSONObject data = new JSONObject(readJSONWSWithPost("Ship/CancelShip", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject GetSamePathShippingOrder(int shipperId, String from, String fromX, String fromY, String to, String toX, String toY){
        try {
            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("from", from);

            params.put("fromX", fromX);

            params.put("fromY", fromY);

            params.put("to", to);

            params.put("toX", toX);

            params.put("toY", toY);

            JSONObject data = new JSONObject(readJSONWS("Ship/ListShipNearlyDirection", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject SearchShippingOrder(int shipperId, int costFrom, int costTo, int distanceFrom, int distanceTo, int totalFrom, int totalTo, boolean isBreak, boolean isBig, int page, int pageSize, double xFrom, double yFrom, double xTo, double yTo){
        try {
            Map<String, String> params = new HashMap<String, String>();

            params.put("shipperId", String.valueOf(shipperId));

            params.put("costfrom", String.valueOf(costFrom));

            params.put("costto", String.valueOf(costTo));

            params.put("distancefrom", String.valueOf(distanceFrom));

            params.put("distanceto", String.valueOf(distanceTo));

            params.put("isBreak", String.valueOf(isBreak));

            params.put("isBig", String.valueOf(isBig));

            params.put("totalfrom", String.valueOf(totalFrom));

            params.put("totalto", String.valueOf(totalTo));

            params.put("page", String.valueOf(page));

            params.put("pageSize", String.valueOf(pageSize));

            params.put("xFrom", String.valueOf(xFrom));

            params.put("yFrom", String.valueOf(yFrom));

            params.put("xTo", String.valueOf(xTo));

            params.put("yTo", String.valueOf(yTo));

            JSONObject data = new JSONObject(readJSONWS("Ship/ListShipperSearchShip", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getListShipTemplate(int shopId, String name, int skip, int top){
        try {
            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));

            params.put("name", name);

            params.put("skip", String.valueOf(skip));

            params.put("top", String.valueOf(top));

            JSONObject data = new JSONObject(readJSONWS("Ship/GetListShipTemplate", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getShipTemplateById(int shopId, int id){
        try {
            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));

            params.put("id", String.valueOf(id));

            JSONObject data = new JSONObject(readJSONWS("Ship/GetShipTemplateById", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject deleteShipTemplateById(int shopId, int id){
        try {
            Map<String, String> params = new HashMap<String, String>();

            params.put("shopId", String.valueOf(shopId));

            params.put("id", String.valueOf(id));

            JSONObject data = new JSONObject(readJSONWSWithPost("Ship/DeleteShipTemplateById", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject resetShip(int shipId, int shopId, String cost, String hour){
        try {
            Map<String, String> params = new HashMap<String, String>();

            params.put("shipId", String.valueOf(shipId));

            params.put("shopId", String.valueOf(shopId));

            params.put("cost", cost);

            params.put("hour", hour);

            JSONObject data = new JSONObject(readJSONWSWithPost("Ship/ResetShip", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject AddShippingOrderGroup(ShipTemplateItem item){
        try {

            JSONObject params = new JSONObject();
            params.put("ShopId", item.getShopId());
            params.put("Name", item.getName());
            params.put("Details", item.getNote());
            params.put("CostShip", item.getCostShip());
            params.put("TotalValue", item.getPrepayShip());
            params.put("Images", item.getImage());

            JSONObject shipFrom = new JSONObject();
            shipFrom.put("ShopId", item.getShipFrom().getId());
            shipFrom.put("Name", item.getShipFrom().getName());
            shipFrom.put("Phone", item.getShipFrom().getPhone());
            shipFrom.put("AddressDetail", item.getShipFrom().getDetailAddress());
            shipFrom.put("Address", item.getShipFrom().getAddress());
            shipFrom.put("XPoint", item.getShipFrom().getXPoint());
            shipFrom.put("YPoint", item.getShipFrom().getYPoint());

            params.put("ShipFrom", shipFrom);

            JSONArray listShipTo = new JSONArray();

            for(DeliveryToItem dt : item.getShipInfoItem())
            {
                if(!dt.isDeleted()) {
                    JSONObject info = new JSONObject();
                    info.put("Name", dt.getName());
                    info.put("Phone", dt.getPhone());
                    info.put("AddressDetail", dt.getDetailAddress());
                    info.put("Address", dt.getAddress());
                    info.put("XPoint", dt.getXPoint());
                    info.put("YPoint", dt.getYPoint());
                    info.put("ShipCost", dt.getShipCost());
                    info.put("Prepay", dt.getShipCost());
                    info.put("Note", dt.getNote());
                    info.put("DateEnd", dt.getDateEnd());
                    listShipTo.put(info);
                }
            }

            params.put("ListShipTo", listShipTo);
            params.put("IsGroup", item.isGroup());
            params.put("IsNear", item.isNear());
            params.put("IsSafe", item.isSafe());

            JSONObject data = new JSONObject(PostJson("Ship/AddGroupShip", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject verifyPromotionCode(String code){
        try {
            Map<String, String> params = new HashMap<String, String>();

            params.put("code", code);

            JSONObject data = new JSONObject(readJSONWSWithPost("Promotion/CheckPromotionCode", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public JSONObject sendCustomerFeedback(int shipId, int shopId, int shipperId, String report, String feedback){
        try {
            Map<String, String> params = new HashMap<String, String>();

            params.put("shipId", String.valueOf(shipId));
            params.put("shopId", String.valueOf(shopId));
            params.put("shipperId", String.valueOf(shipperId));
            params.put("report", report);
            params.put("feedback", feedback);

            JSONObject data = new JSONObject(readJSONWSWithPost("Ship/FeedbackShip", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

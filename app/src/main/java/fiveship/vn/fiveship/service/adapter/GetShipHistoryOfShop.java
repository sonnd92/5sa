package fiveship.vn.fiveship.service.adapter;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.DeliveryToItem;
import fiveship.vn.fiveship.model.LocationItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by sonnd on 30/10/2015.
 */


public class GetShipHistoryOfShop extends AsyncTask<String, Void, JSONObject> {
    private OnTaskCompleted listener;
    private ShopService service;

    String keyword;
    int shopId;
    int statusId;

    public GetShipHistoryOfShop(OnTaskCompleted _listener, ShopService service, String _keyword, int _shopId, int statusId) {

        this.listener = _listener;
        this.keyword = _keyword;
        this.shopId = _shopId;
        this.statusId = statusId;
        this.service = service;

    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return service.GetOrdersOfShop(shopId, keyword, Integer.parseInt(params[0]), Integer.parseInt(params[1]), statusId);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        ArrayList<ShippingOrderItem> datas = new ArrayList<>();
        JSONArray lstItem = null;
        int total = 0;
        try {
            if (result != null && result.length() > 0 && ((result.has("error") && !result.getBoolean("error")) || !result.has("error"))) {
                if (result.has("data") && !result.getString("data").isEmpty()) {

                    lstItem = new JSONArray(result.getString("data"));

                    total = result.getInt("total");

                }
                for (int i = 0; i < lstItem.length(); i++) {
                    // Take one survey
                    JSONObject oneSurvey;
                    JSONObject from;
                    JSONObject to;
                    try {
                        oneSurvey = lstItem.getJSONObject(i);
                        ShippingOrderItem item = new ShippingOrderItem();
                        item.setId(oneSurvey.getInt("Id"));
                        item.setStatusId(oneSurvey.getInt("StatusId"));
                        item.setUrlPicture(oneSurvey.getString("UrlPicture"));
                        item.setName(oneSurvey.getString("Name"));
                        item.setCostShip(oneSurvey.getInt("CostShip"));
                        item.setPrepay(oneSurvey.has("PrePay") ? oneSurvey.getInt("PrePay") : 0);
                        item.setDateCreated(oneSurvey.getString("DateCreated"));
                        item.setDateEnd(oneSurvey.getString("DateEnd"));
                        item.setSummary(oneSurvey.getString("Summary"));
                        item.setStrCostShip(oneSurvey.has("StrCostShip") ? oneSurvey.getString("StrCostShip") : "");
                        item.setStrTotalValue(oneSurvey.has("StrTotalValue") ? oneSurvey.getString("StrTotalValue") : "");
                        item.setDistance(oneSurvey.has("Distance") ? oneSurvey.getString("Distance") : "");
                        item.setStatusName(oneSurvey.has("StatusName") ? oneSurvey.getString("StatusName") : "");
                        item.setStatusNote(oneSurvey.has("StatusNote") ? oneSurvey.getString("StatusNote") : "");
                        item.setIsExpired(oneSurvey.has("IsExpired") && oneSurvey.getBoolean("IsExpired"));

                        item.setIsGroup(oneSurvey.has("IsGroup") && oneSurvey.getBoolean("IsGroup"));
                        item.setIsUrgent(oneSurvey.has("IsUrgent") && oneSurvey.getBoolean("IsUrgent"));
                        item.setIsShopReliable(oneSurvey.has("IsShopConfirm") && oneSurvey.getBoolean("IsShopConfirm"));
                        //item.setMinRecommendShippingCost(oneSurvey.getString("StrEstimateCostShip"));

                        if (item.isGroup()) {
                            JSONArray deliveryJSList = oneSurvey.getJSONArray("ListShipToOfGroup");
                            ArrayList deliveryList = new ArrayList();
                            for (int j = 0; j < deliveryJSList.length(); j++) {
                                JSONObject o = deliveryJSList.getJSONObject(j);
                                DeliveryToItem obj = new DeliveryToItem();
                                obj.setId(o.getInt("Id"));
                                obj.setName(o.getString("Name"));
                                obj.setPhone(o.getString("Phone"));
                                obj.setXPoint(o.getDouble("XPoint"));
                                obj.setYPoint(o.getDouble("YPoint"));
                                obj.setAddress(o.getString("Address"));
                                obj.setNote(o.getString("Note"));
                                deliveryList.add(obj);
                            }
                            item.setListShipToOfGroup(deliveryList);
                        }

                        from = oneSurvey.getJSONObject("ShipFrom");
                        item.setOrderFrom(new LocationItem(from.getInt("Id"), from.getString("Name"), from.getString("Phone"), from.getString("XPoint"), from.getString("YPoint"), from.getString("Address")));
                        to = oneSurvey.getJSONObject("ShipTo");
                        item.setShippingTo(new LocationItem(to.getInt("Id"), to.getString("Name"), to.getString("Phone"), to.getString("XPoint"), to.getString("YPoint"), to.getString("Address")));
                        item.setTotalShipper(oneSurvey.getInt("TotalShipper"));
                        item.setShipperId(oneSurvey.getJSONObject("Shipper").getInt("Id"));
                        item.setShipperName(oneSurvey.getJSONObject("Shipper").getString("Name"));
                        item.setShipperPhone(oneSurvey.getJSONObject("Shipper").getString("Phone"));

                        datas.add(item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            listener.onTaskCompleted(null, total);
        }
        listener.onTaskCompleted(datas, total);
    }
}

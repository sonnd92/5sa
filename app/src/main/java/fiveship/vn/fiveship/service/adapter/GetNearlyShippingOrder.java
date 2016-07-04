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
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.utility.GPSTracker;

/**
 * Created by sonnd on 23/10/2015.
 */
public class GetNearlyShippingOrder extends AsyncTask<String, Void, JSONObject> {
    private OnTaskCompleted listener;
    private ShippingOrderService service;

    String keyword;
    String lat;
    String lng;
    int shipperId;

    public GetNearlyShippingOrder(OnTaskCompleted _listener, ShippingOrderService service, int _shipperId, String _keyword, String lat, String lng) {
        this.listener = _listener;
        this.shipperId = _shipperId;
        this.keyword = _keyword;
        this.lat = lat;
        this.lng = lng;
        this.service = service;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return service.GetNearlyShippingOrder(shipperId, keyword, lat, lng, String.valueOf(params[0]), String.valueOf(params[1]));
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        ArrayList<ShippingOrderItem> datas = new ArrayList<>();
        JSONArray lstItem;
        int total = 0;
        try {
            if (result != null && result.length() > 0 && result.has("data") && !result.getString("data").isEmpty()) {

                lstItem = new JSONArray(result.getString("data"));

                total = result.getInt("total");
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
                        item.setStatusName(oneSurvey.has("StatusName") ? oneSurvey.getString("StatusName") : "");
                        item.setStatusNote(oneSurvey.has("StatusNote") ? oneSurvey.getString("StatusNote") : "");
                        item.setUrlPicture(oneSurvey.getString("UrlPicture"));
                        item.setUrlPictureLabel(oneSurvey.getString("UrlPictureLabel"));
                        item.setName(oneSurvey.getString("Name"));
                        item.setCostShip(oneSurvey.getInt("CostShip"));
                        item.setPrepay(oneSurvey.has("PrePay") ? oneSurvey.getInt("PrePay") : 0);
                        item.setDateCreated(oneSurvey.getString("DateCreated"));
                        item.setDateEnd(oneSurvey.getString("DateEnd"));
                        item.setSummary(oneSurvey.getString("Summary"));
                        item.setIsAssign(oneSurvey.has("IsAssign") && oneSurvey.getBoolean("IsAssign"));
                        item.setStrCostShip(oneSurvey.has("StrCostShip") ? oneSurvey.getString("StrCostShip") : "");
                        item.setStrTotalValue(oneSurvey.has("StrTotalValue") ? oneSurvey.getString("StrTotalValue") : "");
                        item.setDistance(oneSurvey.has("Distance") ? oneSurvey.getString("Distance") : "");
                        item.setIsGroup(oneSurvey.has("IsGroup") && oneSurvey.getBoolean("IsGroup"));
                        item.setIsUrgent(oneSurvey.has("IsUrgent") && oneSurvey.getBoolean("IsUrgent"));
                        item.setIsShopReliable(oneSurvey.has("IsShopConfirm") && oneSurvey.getBoolean("IsShopConfirm"));
                        item.setMinRecommendShippingCost(oneSurvey.getString("StrEstimateCostShip"));
                        item.setHasPromotionCode(oneSurvey.has("IsPromotionCode") && oneSurvey.getBoolean("IsPromotionCode"));
                        item.setDetails(oneSurvey.getString("Details"));

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
                        item.setShippingTo(new LocationItem(to.getInt("Id"), to.getString("Name"), from.getString("Phone"), to.getString("XPoint"), to.getString("YPoint"), to.getString("Address")));

                        datas.add(item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        listener.onTaskCompleted(datas, total);
    }
}

package fiveship.vn.fiveship.service.adapter;

import android.content.Context;
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
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by sonnd on 12/11/2015.
 */
public class SearchShippingOrder extends AsyncTask<String, Void, JSONObject> {

    private OnTaskCompleted listener;
    private ShippingOrderService orderServices;
    private Context context;
    private int costFrom;
    private int costTo;
    private int distanceFrom;
    private int distanceTo;
    private int totalFrom;
    private int totalTo;
    private boolean isBreak;
    private boolean isBig;
    private int skip;
    private int top;
    private double xFrom;
    private double yFrom;
    private double xTo;
    private double yTo;
    private int shipperId;

    public SearchShippingOrder(OnTaskCompleted listener, Context context, int shipperId, int costFrom, int costTo, int distanceFrom, int distanceTo, int totalFrom, int totalTo, boolean isBreak, boolean isBig, int skip, int top, double xFrom, double yFrom, double xTo, double yTo) {
        this.listener = listener;
        this.context = context;
        this.costFrom = costFrom;
        this.costTo = costTo;
        this.distanceFrom = distanceFrom;
        this.distanceTo = distanceTo;
        this.totalFrom = totalFrom;
        this.totalTo = totalTo;
        this.isBreak = isBreak;
        this.isBig = isBig;
        this.skip = skip;
        this.top = top;
        this.xFrom = xFrom;
        this.yFrom = yFrom;
        this.xTo = xTo;
        this.yTo = yTo;
        this.shipperId = shipperId;
        this.orderServices  = ShippingOrderService.get_instance(context);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return orderServices.SearchShippingOrder(shipperId, costFrom, costTo, distanceFrom, distanceTo, totalFrom, totalTo, isBreak, isBig, skip, top, xFrom, yFrom, xTo, yTo);
    }


    @Override
    protected void onPostExecute(JSONObject result) {
        ArrayList<ShippingOrderItem> datas = new ArrayList<>();
        JSONArray lstItem = null;
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
                        item.setStatusName(oneSurvey.has("StatusName") ? oneSurvey.getString("StatusName") : "");
                        item.setStatusNote(oneSurvey.has("StatusNote") ? oneSurvey.getString("StatusNote") : "");
                        item.setIsGroup(oneSurvey.has("IsGroup") && oneSurvey.getBoolean("IsGroup"));
                        item.setIsUrgent(oneSurvey.has("IsUrgent") && oneSurvey.getBoolean("IsUrgent"));
                        item.setIsShopReliable(oneSurvey.has("IsShopConfirm") && oneSurvey.getBoolean("IsShopConfirm"));
                        item.setHasPromotionCode(oneSurvey.has("IsPromotionCode") && oneSurvey.getBoolean("IsPromotionCode"));
                        item.setDetails(oneSurvey.getString("Details"));

                        if(item.isGroup()) {
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


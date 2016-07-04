package fiveship.vn.fiveship.service.adapter;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.DeliveryToItem;
import fiveship.vn.fiveship.model.LocationItem;
import fiveship.vn.fiveship.model.NearlyMapItem;
import fiveship.vn.fiveship.model.ShippingOrderInMapItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.utility.GPSTracker;

/**
 * Created by sonnd on 05/11/2015.
 */
public class GetNearlyShippingOrderForMap extends AsyncTask<String, Void, JSONObject> {

    private OnTaskCompleted listener;
    private ShippingOrderService service;

    String keyword;
    String lat;
    String lng;
    int shipperId;
    int skip;
    int top;

    public GetNearlyShippingOrderForMap(OnTaskCompleted _listener, ShippingOrderService service, int _shipperId, String _keyword, int _skip, int _top, String lat, String lng) {
        this.listener = _listener;
        this.shipperId = _shipperId;
        this.keyword = _keyword;
        this.lat = lat;
        this.lng = lng;
        this.skip = _skip;
        this.top = _top;
        this.service = service;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return service.listShipNearlyShipperGroup(shipperId, keyword, lat, lng, String.valueOf(skip), String.valueOf(top));
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        ArrayList<NearlyMapItem> data = new ArrayList<>();
        JSONArray lstItem;
        int total = 0;

        try {
            if (result != null && result.length() > 0 && result.has("data") && !result.getString("data").isEmpty()) {
                lstItem = new JSONArray(result.getString("data"));

                total = result.getInt("total");

                for (int i = 0; i < lstItem.length(); i++) {

                    try {

                        NearlyMapItem item = new NearlyMapItem();
                        JSONObject o = lstItem.getJSONObject(i);
                        item.setTotal(o.getInt("Total"));
                        item.setXPoint(o.getString("XPoint"));
                        item.setYPoint(o.getString("YPoint"));
                        item.setListId(o.getString("ListId"));

                        ShippingOrderItem shipItem = new ShippingOrderItem();

                        shipItem.setId(o.getJSONObject("Ship").getInt("Id"));
                        shipItem.setName(o.getJSONObject("Ship").getString("Name"));
                        shipItem.setStatusId(o.getJSONObject("Ship").getInt("StatusId"));
                        shipItem.setStatusName(o.getJSONObject("Ship").has("StatusName") ? o.getJSONObject("Ship").getString("StatusName") : "");
                        shipItem.setStatusNote(o.getJSONObject("Ship").has("StatusNote") ? o.getJSONObject("Ship").getString("StatusNote") : "");
                        shipItem.setUrlPicture(o.getJSONObject("Ship").getString("UrlPicture"));
                        shipItem.setUrlPictureLabel(o.getJSONObject("Ship").getString("UrlPictureLabel"));
                        shipItem.setDateCreated(o.getJSONObject("Ship").getString("DateCreated"));
                        shipItem.setDateEnd(o.getJSONObject("Ship").getString("DateEnd"));
                        shipItem.setDistance(o.getJSONObject("Ship").getString("Distance"));
                        shipItem.setStrTotalValue(o.getJSONObject("Ship").getString("StrTotalValue"));
                        shipItem.setStrCostShip(o.getJSONObject("Ship").getString("StrCostShip"));
                        shipItem.setDetails(o.getJSONObject("Ship").getString("Details"));

                        LocationItem orderForm = new LocationItem();
                        orderForm.setAddress(o.getJSONObject("Ship").getJSONObject("ShipFrom").getString("Address"));
                        orderForm.setLatitude(o.getJSONObject("Ship").getJSONObject("ShipFrom").getString("XPoint"));
                        orderForm.setLongitude(o.getJSONObject("Ship").getJSONObject("ShipFrom").getString("YPoint"));
                        orderForm.setPhone(o.getJSONObject("Ship").getJSONObject("ShipFrom").getString("Phone"));
                        shipItem.setOrderFrom(orderForm);

                        LocationItem shipTo = new LocationItem();
                        shipTo.setAddress(o.getJSONObject("Ship").getJSONObject("ShipTo").getString("Address"));
                        shipTo.setLatitude(o.getJSONObject("Ship").getJSONObject("ShipTo").getString("XPoint"));
                        shipTo.setLongitude(o.getJSONObject("Ship").getJSONObject("ShipTo").getString("YPoint"));
                        shipTo.setPhone(o.getJSONObject("Ship").getJSONObject("ShipTo").getString("Phone"));
                        shipItem.setShippingTo(shipTo);

                        shipItem.setIsGroup(o.getJSONObject("Ship").has("IsGroup") && o.getJSONObject("Ship").getBoolean("IsGroup"));
                        shipItem.setIsUrgent(o.getJSONObject("Ship").has("IsUrgent") && o.getJSONObject("Ship").getBoolean("IsUrgent"));
                        shipItem.setIsShopReliable(o.getJSONObject("Ship").has("IsShopConfirm") && o.getJSONObject("Ship").getBoolean("IsShopConfirm"));
                        shipItem.setHasPromotionCode(o.getJSONObject("Ship").has("IsPromotionCode") && o.getJSONObject("Ship").getBoolean("IsPromotionCode"));

                        if (shipItem.isGroup()) {
                            JSONArray deliveryJSList = o.getJSONObject("Ship").getJSONArray("ListShipToOfGroup");
                            ArrayList deliveryList = new ArrayList();
                            for (int j = 0; j < deliveryJSList.length(); j++) {
                                JSONObject jObj = deliveryJSList.getJSONObject(j);
                                DeliveryToItem obj = new DeliveryToItem();
                                obj.setId(jObj.getInt("Id"));
                                obj.setName(jObj.getString("Name"));
                                obj.setPhone(jObj.getString("Phone"));
                                obj.setXPoint(jObj.getDouble("XPoint"));
                                obj.setYPoint(jObj.getDouble("YPoint"));
                                obj.setAddress(jObj.getString("Address"));
                                obj.setNote(jObj.getString("Note"));
                                deliveryList.add(obj);
                            }
                            shipItem.setListShipToOfGroup(deliveryList);
                        }

                        item.setShipItem(shipItem);

                        data.add(item);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        listener.onTaskCompleted(data, total);
    }
}

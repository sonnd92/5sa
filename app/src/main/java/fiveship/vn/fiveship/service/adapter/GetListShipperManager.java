package fiveship.vn.fiveship.service.adapter;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.DeliveryToItem;
import fiveship.vn.fiveship.model.LocationItem;
import fiveship.vn.fiveship.model.ShipperItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShopService;

/**
 * Created by Unstoppable on 4/8/2016.
 */
public class GetListShipperManager extends AsyncTask<String, Void, JSONObject> {
    private ShopService mShopService;
    private int mShopId;
    private boolean mIsBlock;
    private boolean mIsAmity;
    private String mKeyword;
    private boolean mGetAll;
    private OnTaskCompleted mListener;

    public GetListShipperManager(OnTaskCompleted listener, ShopService shopService, int shopId, String keyword, boolean isBlock, boolean isAmity) {
        this.mShopService = shopService;
        this.mShopId = shopId;
        this.mIsBlock = isBlock;
        this.mKeyword = keyword;
        this.mIsAmity = isAmity;
        mListener = listener;
        mGetAll = false;
    }

    public GetListShipperManager(OnTaskCompleted listener, ShopService shopService, int shopId, String keyword) {
        this.mShopService = shopService;
        this.mShopId = shopId;
        this.mKeyword = keyword;
        mListener = listener;
        mGetAll = true;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return mShopService.getListShipperManager(mShopId, mKeyword, Integer.parseInt(params[0]), Integer.parseInt(params[1]), mIsBlock, mIsAmity, mGetAll);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);
        ArrayList<ShipperItem> data = new ArrayList<>();
        JSONArray lstItem;
        int total = 0;
        try {
            if (result != null && result.length() > 0 && result.has("data") && !result.getString("data").isEmpty()) {

                lstItem = new JSONArray(result.getString("data"));

                total = result.getInt("total");
                for (int i = 0; i < lstItem.length(); i++) {
                    // Take one survey
                    JSONObject oneSurvey;
                    try {

                        oneSurvey = lstItem.getJSONObject(i);
                        ShipperItem item = new ShipperItem();
                        item.setName(oneSurvey.getString("Name"));
                        item.setMotorId(oneSurvey.getString("MotorId"));
                        item.setShipStatistic(oneSurvey.getString("ShipStatistics"));
                        item.setAvatarUrl(oneSurvey.getString("Avatar"));
                        item.setIsFavorite(oneSurvey.has("IsAmity") && oneSurvey.getBoolean("IsAmity"));
                        item.setIsBlock(oneSurvey.has("IsBlock") && oneSurvey.getBoolean("IsBlock"));
                        item.setId(oneSurvey.getInt("Id"));

                        data.add(item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        mListener.onTaskCompleted(data, total);
    }
}

package fiveship.vn.fiveship.service.adapter;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.interfaces.OnRegisterCompleted;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.service.LoginRegisterService;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by sonnd on 08/01/2016.
 */
public class Register extends AsyncTask<String, Void, JSONObject> {
    Context context;
    private OnRegisterCompleted listener;
    private LoginRegisterService service;
    String username;
    String password;
    String email;
    String address;
    String phone;
    String provider;
    String xPoint;
    String yPoint;
    boolean isShipper;
    String home;
    String motorId;
    CustomerItem item;
    String encodeImage;
    String oauthId;
    String referralCode;

    public
    Register(Context context,OnRegisterCompleted lstn, String usn, String pwd, String _email, String _address, String _xPoint, String _yPoint, String _phone, String pvd, boolean _isShipper, String home, String motorId, String encodeImage, String oauthId, String referralCode) {
        listener = lstn;
        username = usn;
        email = _email;
        address = _address;
        phone = _phone;
        password = pwd;
        provider = pvd;
        isShipper = _isShipper;
        xPoint = _xPoint;
        yPoint = _yPoint;
        this.home = home;
        this.motorId = motorId;
        this.context = context;
        this.encodeImage = encodeImage;
        this.oauthId = oauthId;
        this.referralCode = referralCode;
        service = LoginRegisterService.get_instance(context);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return service.Register(email, username, password, phone, address, xPoint, yPoint, provider, isShipper, home, motorId, encodeImage, oauthId, referralCode, Utils.getImei(context), Utils.getUuid(context));
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        item = new CustomerItem();
        try {
            if (result != null && !result.getBoolean("error")) {

                item.setId(result.getJSONObject("data").getInt("Id"));
                item.setAvatar(result.getJSONObject("data").getString("Avatar"));
                item.setAvatarLabel(result.getJSONObject("data").getString("AvatarLabel"));
                item.setFirstName(result.getJSONObject("data").getString("FirstName"));
                item.setLastName(result.getJSONObject("data").getString("LastName"));
                item.setAddress(result.getJSONObject("data").getString("Address"));
                item.setEmail(result.getJSONObject("data").getString("Email"));
                item.setIsShipper(result.getJSONObject("data").getBoolean("IsShipper"));
                item.setIsShop(result.getJSONObject("data").getBoolean("IsShop"));
                item.setPhone(result.getJSONObject("data").getString("Phone"));
                item.setShipperId(result.getJSONObject("data").getInt("ShipperId"));
                item.setShopId(result.getJSONObject("data").getInt("ShopId"));
                item.setXPoint(result.getJSONObject("data").getString("XPoint"));
                item.setYPoint(result.getJSONObject("data").getString("YPoint"));
                item.setReferralCode(result.getJSONObject("data").getString("ReferralCode"));

                listener.onRegisterCompleted(false, item, result.getString("message"));
            }

            if (result != null && result.getBoolean("error")) {
                listener.onRegisterCompleted(true, null, result.getString("message"));
            }

            if (result == null) {
                listener.onRegisterCompleted(true, null, context.getString(R.string.register_failed));
            }
        } catch (Exception e) {
            listener.onRegisterCompleted(true, null, context.getString(R.string.register_failed));
            e.printStackTrace();
        }
    }
}



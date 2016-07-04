package fiveship.vn.fiveship.service;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sonnd on 08/10/2015.
 */
public class LoginRegisterService extends BaseService {
    private static LoginRegisterService _instance;

    public LoginRegisterService(Context context){
        super(context);
    }

    public synchronized static LoginRegisterService get_instance(Context context){
        if(_instance == null){

            _instance = new LoginRegisterService(context);
        }
        return _instance;
    }

    public JSONObject Login(String oauthId, String email, String password, String provider){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("username", email);

            params.put("password", password);

            params.put("provider", provider);

            params.put("oauthId", oauthId);

            JSONObject data = new JSONObject(readJSONWSWithPost("Customer/Signin", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public JSONObject Register(String email, String name, String password, String phone, String address,String xPoint, String yPoint, String provider, boolean isShipper, String home, String motorId, String img, String oauthId, String referralCode, String imei, String uuid){
        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("email", email);

            params.put("home", home);

            params.put("motorId", motorId);

            params.put("password", password);

            params.put("provider", provider);

            params.put("name", name);

            params.put("phone", phone);

            params.put("address", address);

            params.put("isShipper", String.valueOf(isShipper));

            params.put("isShop", String.valueOf(!isShipper));

            params.put("xPoint", xPoint);

            params.put("yPoint", yPoint);

            params.put("img", img);

            params.put("oauthId", oauthId);

            params.put("referralCode", referralCode);

            params.put("imei", imei);

            params.put("uuid", uuid);

            JSONObject data = new JSONObject(readJSONWSWithPost("Customer/Register", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

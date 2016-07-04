package fiveship.vn.fiveship.service;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sonnd on 08/10/2015.
 */
public class CustomerService extends BaseService {

    private static CustomerService _instance;

    public CustomerService(Context context){
        super(context);
    }

    public synchronized static CustomerService get_instance(Context context){
        if(_instance == null){

            _instance = new CustomerService(context);
        }
        return _instance;
    }

    public JSONObject accessApp(int id,
                                boolean isShop,
                                String connection,
                                String xPoint,
                                String yPoint,
                                String imei,
                                String platformVersion,
                                String model,
                                String version,
                                String manufacturer,
                                String uuid,
                                int versionCode){

        try {

            Map<String, String> params = new HashMap<String, String>();

            params.put("id", String.valueOf(id));

            params.put("isShop", String.valueOf(isShop));

            params.put("connection", connection);

            params.put("xPoint", xPoint);

            params.put("yPoint", yPoint);

            params.put("imei", imei);

            params.put("platform", "android");

            params.put("model", model);

            params.put("platformVersion", platformVersion);

            params.put("version", version);

            params.put("manufacturer", manufacturer);

            params.put("uuid", uuid);

            params.put("versionCode", String.valueOf(versionCode));

            JSONObject data = new JSONObject(readJSONWSWithPost("Customer/AccessApp", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject getHistoryTrade(int id, boolean isShop, int page, int pageSize){

        try {

            Map<String, String> params = new HashMap<>();

            params.put("id", String.valueOf(id));

            params.put("isShop", String.valueOf(isShop));

            params.put("page", String.valueOf(page));

            params.put("pageSize", String.valueOf(pageSize));

            JSONObject data = new JSONObject(readJSONWS("Customer/GetHistoryTrade", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject forgotPassword(String email){

        try {

            Map<String, String> params = new HashMap<>();

            params.put("email", email);

            JSONObject data = new JSONObject(readJSONWSWithPost("Customer/ForgetPassword", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject changePassword(int id, String password, String newpassword){

        try {

            Map<String, String> params = new HashMap<>();

            params.put("id", String.valueOf(id));
            params.put("password", password);
            params.put("newpassword", newpassword);

            JSONObject data = new JSONObject(readJSONWSWithPost("Customer/ChangePassword", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

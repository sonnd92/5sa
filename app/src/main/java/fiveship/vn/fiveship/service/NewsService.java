package fiveship.vn.fiveship.service;

import android.content.Context;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Unstopable on 6/7/2016.
 */

public class NewsService extends BaseService{

    private static NewsService _instance;

    public NewsService(Context context){
        super(context);
    }

    public synchronized static NewsService get_instance(Context context){
        if(_instance == null){

            _instance = new NewsService(context);
        }
        return _instance;
    }

    public JSONObject GetListNews(String keywords, int skip, int top){

        try {

            Map<String, String> params = new HashMap<>();

            params.put("keyword", keywords);

            params.put("skip", String.valueOf(skip));

            params.put("top", String.valueOf(top));

            JSONObject data = new JSONObject(readJSONWS("Article/GetNews", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject GetPolicies(String keywords, int skip, int top){

        try {

            Map<String, String> params = new HashMap<>();

            params.put("keyword", keywords);

            params.put("skip", String.valueOf(skip));

            params.put("top", String.valueOf(top));

            JSONObject data = new JSONObject(readJSONWS("Article/GetPolicy", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject GetNewsDetail(int newsId){

        try {

            Map<String, String> params = new HashMap<>();

            params.put("id", String.valueOf(newsId));

            JSONObject data = new JSONObject(readJSONWS("Article/GetDetail", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

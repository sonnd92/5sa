package fiveship.vn.fiveship.service;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sonnd on 08/10/2015.
 */
public class QuestService extends BaseService {

    private static QuestService _instance;

    public QuestService(Context context){
        super(context);
    }

    public synchronized static QuestService get_instance(Context context){
        if(_instance == null){

            _instance = new QuestService(context);
        }
        return _instance;
    }

    public JSONObject getListQuest(int id, boolean isShop, int skip, int top){

        try {

            Map<String, String> params = new HashMap<>();

            params.put("id", String.valueOf(id));

            params.put("isShop", String.valueOf(isShop));

            params.put("skip", String.valueOf(skip));

            params.put("top", String.valueOf(top));

            JSONObject data = new JSONObject(readJSONWS("Quest/GetListQuest", params));

            return data;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

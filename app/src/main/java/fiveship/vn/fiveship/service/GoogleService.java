package fiveship.vn.fiveship.service;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sonnd on 14/01/2016.
 */
public class GoogleService extends BaseService {
    private static GoogleService _instance;

    public GoogleService(Context context){
        super(context);
    }

    public synchronized static GoogleService get_instance(Context context){
        if(_instance == null){

            _instance = new GoogleService(context);
        }
        return _instance;
    }

    public Document directionRequest(HashMap<String ,String>  params){

        Document data = readDocumentWithGet("https://maps.googleapis.com/maps/api/directions/xml", params);

        return data;
    }
}

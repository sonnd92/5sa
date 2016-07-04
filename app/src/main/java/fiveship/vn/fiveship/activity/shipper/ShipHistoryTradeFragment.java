package fiveship.vn.fiveship.activity.shipper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.ShipHistoryTradeAdapter;
import fiveship.vn.fiveship.service.CustomerService;
import fiveship.vn.fiveship.utility.SessionManager;

public class ShipHistoryTradeFragment extends Fragment {

    private SessionManager sessionManager;

    private CustomerService customerService;
    private ShipHistoryTradeAdapter shipHistoryTradeAdapter;
    private ListView lvDataBinding;
    private ViewStub vs_noResult;
    private ArrayList<JSONObject> list = new ArrayList();
    private JSONObject data;
    private Animation fadeIn, fadeOut;
    private View view;

    public static synchronized ShipHistoryTradeFragment newInstance() {
        ShipHistoryTradeFragment instance = new ShipHistoryTradeFragment();
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_account, container, false);

        customerService = CustomerService.get_instance(getActivity());

        sessionManager = new SessionManager(getActivity());

        lvDataBinding = (ListView) view.findViewById(R.id.list_trade);

        new GetListTrade().execute();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                //MainActivity.openNavigator();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void bindData()
    {
        try {

            ((TextView) view.findViewById(R.id.account_total)).setText(data.getString("Total"));

            ((TextView) view.findViewById(R.id.account_total_first)).setText(data.getString("First"));

            ((TextView) view.findViewById(R.id.account_total_second)).setText(data.getString("Second"));

            ((TextView) view.findViewById(R.id.account_total_deposit)).setText(data.getString("Deposit"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        shipHistoryTradeAdapter = new ShipHistoryTradeAdapter(getActivity(), list);
        //lvDataBinding.setOnScrollListener(this);
        //lvDataBinding.setOnItemClickListener(this);
        // setup loader
        vs_noResult = (ViewStub) view.findViewById(R.id.vs_no_result);
        //footerLoading =  inflater.inflate(R.layout.listview_footer_loadmore, lvDataBinding, false);
        //fade anim
        fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_fade_in);
        fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_fade_out);
        if(list.size() > 0 || data != null) {
            //show list rootView when data is loaded
            lvDataBinding.setAdapter(shipHistoryTradeAdapter);
            view.findViewById(R.id.history_container).startAnimation(fadeIn);
            view.findViewById(R.id.layout_loading).setAnimation(fadeOut);
            view.findViewById(R.id.layout_loading).setVisibility(View.GONE);
        }else {
            view.findViewById(R.id.layout_loading).setAnimation(fadeOut);
            view.findViewById(R.id.layout_loading).setVisibility(View.GONE);
            vs_noResult.inflate();
        }
    }

    private class GetListTrade extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return customerService.getHistoryTrade(sessionManager.isShipper() ? sessionManager.getShipperId() : sessionManager.getShopId(), !sessionManager.isShipper(), 1, 1);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null && result.has("error") && !result.getBoolean("error")) {

                    data = result.getJSONObject("account");

                    JSONArray listJO = result.getJSONArray("history");

                    for (int i = 0; i < listJO.length(); i++){
                        list.add(listJO.getJSONObject(i));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            bindData();
        }
    }

}

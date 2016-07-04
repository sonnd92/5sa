package fiveship.vn.fiveship.activity.shop;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.ShipTemplateAdapter;
import fiveship.vn.fiveship.model.ShipTemplateItem;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.utility.SessionManager;

public class ShopShipTemplateFragment extends Fragment{

    private SessionManager sessionManager;

    private ShippingOrderService shippingOrderService;
    private ShipTemplateAdapter shipTemplateAdapter;
    private ListView lvDataBinding;
    private View footerLoading;
    private View vs_noResult;
    private EditText txtSearch;
    private ArrayList<ShipTemplateItem> data = new ArrayList();
    private Animation fadeIn, fadeOut;
    private String keyword = "";
    private View rootView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int skip = 0;
    private int top = 100;
    private int total = 0;
    private int preLast;
    boolean isLoading = false;
    boolean isLoadMore = false;
    boolean isRefresh = false;

    public static ShopShipTemplateFragment newInstance() {

        ShopShipTemplateFragment instance = new ShopShipTemplateFragment();
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.shop_ship_template, container, false);

        shippingOrderService = ShippingOrderService.get_instance(getActivity());

        sessionManager = new SessionManager(getActivity());

        footerLoading = inflater.inflate(R.layout.listview_footer_loadmore, lvDataBinding, false);

        fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_fade_in);

        fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_fade_out);

        vs_noResult = rootView.findViewById(R.id.vs_no_result);

        lvDataBinding = (ListView) rootView.findViewById(R.id.ship_template_list);

        lvDataBinding.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int idTemp = shipTemplateAdapter.getIdItem(position);

                Intent create = new Intent(getActivity(), ShopShipCreateTemplateActivity.class);
                create.putExtra("id", idTemp);
                startActivity(create);

            }
        });

        rootView.findViewById(R.id.btn_shop_ship_create_template).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent create = new Intent(getActivity(), ShopShipCreateTemplateActivity.class);
                startActivity(create);
            }
        });

        rootView.findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRefresh = true;
                skip = 0;
                loadData();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_list);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                swipeRefreshLayout.setColorSchemeColors(R.color.grey_DA);
                isRefresh = true;
                skip = 0;
                loadData();
            }
        });

        txtSearch = (EditText)rootView.findViewById(R.id.txt_ship_search);

        txtSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    keyword = txtSearch.getText().toString();
                } else {
                    keyword = "";
                }
            }
        });

        txtSearch.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            isRefresh = true;
                            loadData();
                            return true;
                        }
                        return false;
                    }
                });

        loadData();

        return rootView;
    }

    private void loadData(){

        if(!isLoading){
            new GetListShipTemplate().execute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void bindData(ArrayList<ShipTemplateItem> list)
    {
        if(isRefresh){
            data.clear();
        }

        data.addAll(list);

        if(shipTemplateAdapter == null){
            shipTemplateAdapter = new ShipTemplateAdapter(getActivity(), data);
            lvDataBinding.setAdapter(shipTemplateAdapter);
        }else{
            shipTemplateAdapter.notifyDataSetChanged();
        }

        if(data.size() > 0) {

            lvDataBinding.startAnimation(fadeIn);
            rootView.findViewById(R.id.layout_loading).setAnimation(fadeOut);
            rootView.findViewById(R.id.layout_loading).setVisibility(View.GONE);
            vs_noResult.setVisibility(View.GONE);

        }else {

            rootView.findViewById(R.id.layout_loading).setAnimation(fadeOut);
            rootView.findViewById(R.id.layout_loading).setVisibility(View.GONE);
            vs_noResult.setVisibility(View.VISIBLE);
        }

        isLoading = false;

        isRefresh = false;

        isLoadMore = false;

        swipeRefreshLayout.setRefreshing(false);

        lvDataBinding.removeFooterView(footerLoading);
    }

    private class GetListShipTemplate extends AsyncTask<String, Integer, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return shippingOrderService.getListShipTemplate(sessionManager.getShopId(), keyword, skip, top);
        }

        @Override
        protected void onPreExecute (){
            isLoading = true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                ShipTemplateItem item;

                ArrayList<ShipTemplateItem> list = new ArrayList();

                if (result != null && result.length() > 0 && ((result.has("error") && !result.getBoolean("error")) || !result.has("error"))) {

                    total = result.getInt("total");

                    for (int i = 0; i < result.getJSONArray("data").length(); i++) {

                        item = new ShipTemplateItem();

                        item.setId(result.getJSONArray("data").getJSONObject(i).getInt("Id"));
                        item.setShopId(result.getJSONArray("data").getJSONObject(i).getInt("ShopId"));
                        item.setName(result.getJSONArray("data").getJSONObject(i).getString("Name"));
                        item.setCostShip(result.getJSONArray("data").getJSONObject(i).getString("StrTotalValue"));
                        item.setDate(result.getJSONArray("data").getJSONObject(i).getString("DateEnd"));
                        item.setNote(result.getJSONArray("data").getJSONObject(i).getString("Note"));
                        item.setPrepayShip(result.getJSONArray("data").getJSONObject(i).getString("StrCostShip"));
                        item.setStrProperty(result.getJSONArray("data").getJSONObject(i).getString("StrProperty"));
                        item.setIsBig(result.getJSONArray("data").getJSONObject(i).getBoolean("IsBig"));
                        item.setIsFood(result.getJSONArray("data").getJSONObject(i).getBoolean("IsFood"));
                        item.setIsHeavy(result.getJSONArray("data").getJSONObject(i).getBoolean("IsHeavy"));
                        item.setIsBreak(result.getJSONArray("data").getJSONObject(i).getBoolean("IsBreak"));
                        item.setIsLight(result.getJSONArray("data").getJSONObject(i).getBoolean("IsLight"));

                        list.add(item);

                    }

                    bindData(list);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

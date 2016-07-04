package fiveship.vn.fiveship.activity.shop;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.ShopListShipperAssignShipAdapter;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.service.ShipperService;
import fiveship.vn.fiveship.utility.SessionManager;

public class ShopListShipperAssignShip extends AppCompatActivity {


    private int id;

    private ShipperService shipperService;
    private SessionManager sessionManager;

    private ArrayList<CustomerItem> data;

    Animation fadeIn;
    Animation fadeOut;
    View vs_noResult;
    Toolbar toolbar;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    ShopListShipperAssignShipAdapter adapter;

    boolean isRefresh = false;
    boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.shop_list_shipper_assign_ship);

        toolbar = (Toolbar) findViewById(R.id.shop_list_shipper_assign_ship_toolbar);

        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        shipperService = ShipperService.get_instance(this);

        sessionManager = new SessionManager(this);
        data = new ArrayList();

        id = getIntent().getIntExtra("id", 0);

        recyclerView = (RecyclerView) findViewById(R.id.shop_list_shipper_assign_ship);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
                isRefresh = true;
                loadData();
            }
        });

        loadData();
    }

    public void loadData(){

        if(isLoading){
            return;
        }

        isLoading = true;

        new GetListShipperAssignShip().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_order, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void bindData()
    {
        if(isRefresh){
            adapter.notifyDataSetChanged();
            isRefresh = false;
        }

        adapter = new ShopListShipperAssignShipAdapter(this, data, sessionManager.getShopId());

        vs_noResult = findViewById(R.id.vs_no_result);;
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);
        if(data.size() > 0) {
            //show list rootView when data is loaded
            recyclerView.startAnimation(fadeIn);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter);
            findViewById(R.id.layout_loading).setAnimation(fadeOut);
            findViewById(R.id.layout_loading).setVisibility(View.GONE);
        }else {
            findViewById(R.id.layout_loading).setAnimation(fadeOut);
            findViewById(R.id.layout_loading).setVisibility(View.GONE);
            vs_noResult.setVisibility(View.GONE);
        }
    }

    private class GetListShipperAssignShip extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return shipperService.GetListShipperAssignShip(id);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                swipeRefreshLayout.setRefreshing(false);

                isLoading = false;

                if(isRefresh){
                    data.clear();
                }

                if (result != null && result.length() > 0 && ((result.has("error") && !result.getBoolean("error")) || !result.has("error"))) {

                    for (int i = 0; i < result.getJSONArray("data").length(); i++) {
                        JSONObject jObject = result.getJSONArray("data").getJSONObject(i);
                        CustomerItem customer = new CustomerItem();

                        customer.setId(jObject.getInt("Id"));
                        customer.setShipId(jObject.getInt("ShipId"));
                        customer.setShipperId(jObject.getInt("ShipperId"));
                        customer.setFirstName(jObject.getString("FirstName"));
                        customer.setLastName(jObject.getString("LastName"));
                        customer.setPhone(jObject.getString("Phone"));
                        customer.setDateCreated(jObject.getString("DateCreated"));
                        customer.setMotorId(jObject.getString("MotorId"));
                        customer.setShipStatistics(jObject.getString("ShipStatistics"));
                        customer.setDistance(jObject.getString("Distance"));
                        customer.setAvatar(jObject.getString("Avatar"));
                        customer.setAvatarLabel(jObject.getString("AvatarLabel"));
                        customer.setNote(jObject.getString("Note"));
                        customer.setIsAccess(jObject.getBoolean("IsAccess"));
                        customer.setIsOnline(jObject.getBoolean("IsOnline"));
                        customer.setRecommendShippingCost(jObject.getString("CostShip"));
                        customer.setConfirm(jObject.getBoolean("IsConfirm"));
                        customer.setNumberStar(Float.parseFloat(jObject.getString("NumberStar")));
                        customer.setNumberRating(jObject.getString("NumberRating"));

                        data.add(customer);
                    }
                    bindData();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

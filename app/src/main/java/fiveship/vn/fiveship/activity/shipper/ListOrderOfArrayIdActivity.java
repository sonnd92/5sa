package fiveship.vn.fiveship.activity.shipper;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.ShippingOrderAdapter;
import fiveship.vn.fiveship.interfaces.OnTaskCompleted;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.service.adapter.GetListShipOfListId;
import fiveship.vn.fiveship.utility.SessionManager;

public class ListOrderOfArrayIdActivity extends ActionBarActivity {

    private ShippingOrderAdapter shippingOrderAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    private View vs_noResult;
    private String listId = "";
    private Animation fadeIn, fadeOut;

    private ArrayList<ShippingOrderItem> list;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_order_of_array_id);

        setupToolBar();

        Bundle bundle = getIntent().getExtras();
        listId = bundle.getString("listId");

        recyclerView = (RecyclerView) findViewById(R.id.rv_data_binding);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        // setup loader
        vs_noResult = findViewById(R.id.vs_no_result);

        // fade anim
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);

        findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        loadData();
    }

    public void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void loadData(){

        if(isLoading){
            return;
        }

        isLoading = true;

        new GetListShipOfListId(this, new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList result, int total) {

                list = result;

                bindData();
            }
        }).execute(listId);
    }

    public void bindData()
    {
        isLoading = false;

        if(list.size() > 0) {
            shippingOrderAdapter = new ShippingOrderAdapter(this, list);
            recyclerView.setAdapter(shippingOrderAdapter);
            recyclerView.startAnimation(fadeIn);
            findViewById(R.id.layout_loading).setAnimation(fadeOut);
            findViewById(R.id.layout_loading).setVisibility(View.GONE);
        }else {
            findViewById(R.id.layout_loading).setAnimation(fadeOut);
            findViewById(R.id.layout_loading).setVisibility(View.GONE);
            vs_noResult.startAnimation(fadeIn);
            vs_noResult.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_order_in_place, menu);
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

}

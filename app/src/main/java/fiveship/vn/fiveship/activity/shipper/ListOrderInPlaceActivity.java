package fiveship.vn.fiveship.activity.shipper;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.ShippingOrderAdapter;
import fiveship.vn.fiveship.model.ShippingOrderItem;

public class ListOrderInPlaceActivity extends AppCompatActivity {

    private ShippingOrderAdapter shippingOrderAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    private View vs_noResult;
    private ArrayList data = new ArrayList();
    private Animation fadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_order_in_place);

        setupToolBar();

        Bundle bundle = getIntent().getExtras();
        data = (ArrayList<ShippingOrderItem>) bundle.getSerializable("ListOrders");


        recyclerView = (RecyclerView) findViewById(R.id.rv_data_binding);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        // setup loader
        vs_noResult = findViewById(R.id.vs_no_result);

        // fade anim
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        //fadeOut = AnimationUtils.loadAnimation(this, R.anim.abc_fade_out);

        if(data != null && data.size() > 0) {
            shippingOrderAdapter = new ShippingOrderAdapter(this, data);
            recyclerView.setAdapter(shippingOrderAdapter);
            recyclerView.startAnimation(fadeIn);
        }else{
            vs_noResult.setVisibility(View.VISIBLE);
        }
    }

    public void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

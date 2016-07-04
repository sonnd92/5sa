package fiveship.vn.fiveship.activity.shop;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.validator.Field;
import fiveship.vn.fiveship.utility.validator.Form;
import fiveship.vn.fiveship.utility.validator.validation.NotEmpty;


public class ShopShipCreateTemplateActivity extends ActionBarActivity implements TimePickerDialog.OnTimeSetListener{

    private EditText txtShipName;
    private EditText txtShipCost;
    private EditText txtShipCostS;
    private TextView txtShipDate;
    private TextView txtShipProperty;
    private TextView txtShipNote;

    private TextView lbl_shop_ship_create_property_light;
    private TextView lbl_shop_ship_create_property_heavy;
    private TextView lbl_shop_ship_create_property_big;
    private TextView lbl_shop_ship_create_property_break;
    private TextView lbl_shop_ship_create_property_food;

    private TextView lbl_shop_ship_create_time1;
    private TextView lbl_shop_ship_create_time2;
    private TextView lbl_shop_ship_create_time3;
    private TextView lbl_shop_ship_create_time4;
    private TextView lbl_shop_ship_create_time5;
    private TextView lbl_shop_ship_create_time6;
    private TextView lbl_shop_ship_create_time7;
    private TextView lbl_shop_ship_create_time8;

    private LinearLayout box_shop_ship_create_property;
    private LinearLayout box_shop_ship_create_time;

    private ShippingOrderService serviceShip;
    private HashMap shipItem;
    private Dialog loadingDialog;

    private Boolean isLight = false;
    private Boolean isHeavy = false;
    private Boolean isBig = false;
    private Boolean isBreak = false;
    private Boolean isFood = false;

    private Boolean isShowProperty = false;
    private Boolean isShowdDate = false;

    private int positionDate;

    private int id;

    private SessionManager session;
    private Form form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_ship_create_template);

        id = getIntent().getIntExtra("id", 0);

        setupToolBar();

        session = new SessionManager(getApplicationContext());

        loadingDialog = Utils.setupLoadingDialog(this);

        serviceShip = ShippingOrderService.get_instance(this);

        shipItem = new HashMap<>();

        initView();

        if(id > 0){
            shipItem.put("id", id);
            ((Button)findViewById(R.id.btn_shop_ship_create_detail_submit)).setText("Cập nhật mẫu đơn hàng");
            findViewById(R.id.btn_shop_ship_create_detail_delete).setVisibility(View.VISIBLE);
            new GetShipTemplate().execute();
        }

    }

    private void initView(){

        form = new Form(this);

        box_shop_ship_create_property = (LinearLayout)findViewById(R.id.box_shop_ship_create_property);
        box_shop_ship_create_time = (LinearLayout)findViewById(R.id.box_shop_ship_create_time);

        txtShipName = (AutoCompleteTextView)findViewById(R.id.txt_shop_ship_create_name);
        txtShipCost = (EditText)findViewById(R.id.txt_shop_ship_create_cost);
        txtShipCostS = (EditText)findViewById(R.id.txt_shop_ship_create_cost_ship);
        txtShipDate = (TextView)findViewById(R.id.txt_shop_ship_create_date);
        txtShipProperty = (TextView)findViewById(R.id.txt_shop_ship_create_property);
        txtShipNote = (TextView)findViewById(R.id.txt_shop_ship_create_note);

        txtShipCost.addTextChangedListener(new Utils.MoneyTextWatcher(txtShipCost));
        txtShipCostS.addTextChangedListener(new Utils.MoneyTextWatcher(txtShipCostS));

        lbl_shop_ship_create_time1 = (TextView)findViewById(R.id.lbl_shop_ship_create_time1);
        lbl_shop_ship_create_time2 = (TextView)findViewById(R.id.lbl_shop_ship_create_time2);
        lbl_shop_ship_create_time3 = (TextView)findViewById(R.id.lbl_shop_ship_create_time3);
        lbl_shop_ship_create_time4 = (TextView)findViewById(R.id.lbl_shop_ship_create_time4);
        lbl_shop_ship_create_time5 = (TextView)findViewById(R.id.lbl_shop_ship_create_time5);
        lbl_shop_ship_create_time6 = (TextView)findViewById(R.id.lbl_shop_ship_create_time6);
        lbl_shop_ship_create_time7 = (TextView)findViewById(R.id.lbl_shop_ship_create_time7);
        lbl_shop_ship_create_time8 = (TextView)findViewById(R.id.lbl_shop_ship_create_time8);

        txtShipProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShowProperty) {
                    isShowProperty = true;
                    box_shop_ship_create_property.setVisibility(View.VISIBLE);
                    txtShipProperty.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_up, 0);
                } else {
                    isShowProperty = false;
                    box_shop_ship_create_property.setVisibility(View.GONE);
                    txtShipProperty.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_down, 0);
                }

            }
        });

        txtShipDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isShowdDate) {
                    isShowdDate = true;
                    box_shop_ship_create_time.setVisibility(View.VISIBLE);
                    txtShipDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_up, 0);
                } else {
                    isShowdDate = false;
                    box_shop_ship_create_time.setVisibility(View.GONE);
                    txtShipDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_down, 0);
                }

            }
        });

        lbl_shop_ship_create_property_light = (TextView)findViewById(R.id.lbl_shop_ship_create_property_light);

        findViewById(R.id.lbl_shop_ship_create_property_light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isLight = isLight ? false : true;

                lbl_shop_ship_create_property_light.setCompoundDrawablesWithIntrinsicBounds(0, 0, isLight ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);

                setTextShipProperty();
            }
        });

        lbl_shop_ship_create_property_heavy = (TextView)findViewById(R.id.lbl_shop_ship_create_property_heavy);

        findViewById(R.id.lbl_shop_ship_create_property_heavy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isHeavy = isHeavy ? false : true;

                lbl_shop_ship_create_property_heavy.setCompoundDrawablesWithIntrinsicBounds(0, 0, isHeavy ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);

                setTextShipProperty();
            }
        });

        lbl_shop_ship_create_property_big = (TextView)findViewById(R.id.lbl_shop_ship_create_property_big);

        findViewById(R.id.lbl_shop_ship_create_property_big).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isBig = isBig ? false : true;

                lbl_shop_ship_create_property_big.setCompoundDrawablesWithIntrinsicBounds(0, 0, isBig ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);

                setTextShipProperty();
            }
        });

        lbl_shop_ship_create_property_break = (TextView)findViewById(R.id.lbl_shop_ship_create_property_break);

        findViewById(R.id.lbl_shop_ship_create_property_break).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isBreak = isBreak ? false : true;

                lbl_shop_ship_create_property_break.setCompoundDrawablesWithIntrinsicBounds(0, 0, isBreak ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);

                setTextShipProperty();
            }
        });

        lbl_shop_ship_create_property_food = (TextView)findViewById(R.id.lbl_shop_ship_create_property_food);

        findViewById(R.id.lbl_shop_ship_create_property_food).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isFood = isFood ? false : true;

                lbl_shop_ship_create_property_food.setCompoundDrawablesWithIntrinsicBounds(0, 0, isFood ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);

                setTextShipProperty();
            }
        });

        findViewById(R.id.btn_shop_ship_create_detail_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postShipOrder();
            }
        });

        findViewById(R.id.btn_shop_ship_create_detail_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();
                new DeleteShippingOrderTemplateTask().execute();
            }
        });

    }

    private void postShipOrder(){

        form.addField(Field.using(txtShipName).validate(NotEmpty.build(this, getString(R.string.title_order_field))));
        if (form.isValid()) {

            shipItem.put("shopId", session.getShopId());
            shipItem.put("name", txtShipName.getText().toString());
            shipItem.put("cost", txtShipCostS.getText().toString());
            shipItem.put("prepay", txtShipCost.getText().toString());
            shipItem.put("date", txtShipDate.getText().toString());
            shipItem.put("note", txtShipNote.getText().toString());
            shipItem.put("isBig", isBig);
            shipItem.put("isBreak", isBreak);
            shipItem.put("isFood", isFood);
            shipItem.put("isLight", isLight);
            shipItem.put("isHeavy", isHeavy);

            loadingDialog.show();

            new PostShippingOrderTemplateTask().execute(shipItem);
        }
    }

    private void setTextShipProperty(){

        String txtProperty = "";

        if(isLight){
            txtProperty += " gọn nhẹ";
        }

        if(isHeavy){
            txtProperty += (txtProperty != "" ? "," : "") + " nặng";
        }

        if(isBig){
            txtProperty += (txtProperty != "" ? "," : "") + " cồng kềng";
        }

        if(isBreak){
            txtProperty += (txtProperty != "" ? "," : "") + " dễ vỡ";
        }

        if(isFood){
            txtProperty += (txtProperty != "" ? "," : "") + " đồ ăn";
        }

        txtProperty = txtProperty != "" ? "Đặc tính:" + txtProperty : txtProperty;

        txtShipProperty.setText(txtProperty);

    }

    public void setTextShipDate(View view){

        positionDate = view.getId();

        TextView v = (TextView)view;

        lbl_shop_ship_create_time1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        lbl_shop_ship_create_time2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        lbl_shop_ship_create_time3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        lbl_shop_ship_create_time4.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        lbl_shop_ship_create_time5.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        lbl_shop_ship_create_time6.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        lbl_shop_ship_create_time7.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        lbl_shop_ship_create_time8.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);

        v.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checked, 0);

        if(positionDate == R.id.lbl_shop_ship_create_time4
                || view.getId() == R.id.lbl_shop_ship_create_time8){
            showTimePickerDialog();
        }

        txtShipDate.setText(v.getText().toString());

        isShowdDate = false;
        box_shop_ship_create_time.setVisibility(View.GONE);
        txtShipDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_down, 0);

    }

    private void showTimePickerDialog() {

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, this, hour, minute, DateFormat.is24HourFormat(this));

        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        txtShipDate.setText(hourOfDay + "h" + (minute < 10 ? "0" : "") + minute + (positionDate == R.id.lbl_shop_ship_create_time4 ? " hôm nay" : positionDate == R.id.lbl_shop_ship_create_time8 ? " ngày mai" : ""));
    }

    public void setupToolBar()
    {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.shop_ship_create_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //getSupportActionBar().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case android.R.id.home:
                finish();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class GetShipTemplate extends AsyncTask<HashMap, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(HashMap... params) {
            return serviceShip.getShipTemplateById(session.getShopId(), id);
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            loadingDialog.dismiss();

            try {
                if (result != null && !result.getBoolean("error")) {

                    txtShipName.setText(result.getJSONObject("data").getString("Name"));

                    txtShipCost.setText(result.getJSONObject("data").getString("StrCostShip"));

                    txtShipCostS.setText(result.getJSONObject("data").getString("StrTotalValue"));

                    txtShipDate.setText(result.getJSONObject("data").getString("DateEnd"));

                    txtShipProperty.setText(result.getJSONObject("data").getString("StrProperty"));

                    txtShipNote.setText(result.getJSONObject("data").getString("Note"));

                    isBig = result.getJSONObject("data").getBoolean("IsBig");

                    isLight = result.getJSONObject("data").getBoolean("IsLight");

                    isBreak = result.getJSONObject("data").getBoolean("IsBreak");

                    isHeavy = result.getJSONObject("data").getBoolean("IsHeavy");

                    isFood = result.getJSONObject("data").getBoolean("IsFood");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PostShippingOrderTemplateTask extends AsyncTask<HashMap, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(HashMap... params) {
            return serviceShip.modifyShipTemplate(params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            loadingDialog.dismiss();

            try {
                if (result != null) {
                    Toast.makeText(getApplication(), result.getString("message"), Toast.LENGTH_LONG).show();
                }

                if (result != null && !result.getBoolean("error")) {
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class DeleteShippingOrderTemplateTask extends AsyncTask<HashMap, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(HashMap... params) {
            return serviceShip.deleteShipTemplateById(session.getShopId(), id);
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            loadingDialog.dismiss();

            try {
                if (result != null) {
                    Toast.makeText(getApplication(), result.getString("message"), Toast.LENGTH_LONG).show();
                }

                if (result != null && !result.getBoolean("error")) {
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

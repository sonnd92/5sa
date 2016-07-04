package fiveship.vn.fiveship.activity.shop;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.RegisterActivity;
import fiveship.vn.fiveship.adapter.PlacesAutoCompleteAdapter;
import fiveship.vn.fiveship.adapter.SuggestInfoToAdapter;
import fiveship.vn.fiveship.adapter.SuggestShipAdapter;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.model.ShipTemplateItem;
import fiveship.vn.fiveship.service.OtherService;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.service.adapter.VerifyPromotionCode;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.ImageProcess;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.validator.Field;
import fiveship.vn.fiveship.utility.validator.Form;
import fiveship.vn.fiveship.utility.validator.validation.IsPhoneNumber;
import fiveship.vn.fiveship.utility.validator.validation.NotEmpty;
import fiveship.vn.fiveship.widget.DelayAutocompleteTextView;


public class ShopShipCreateActivity extends ActionBarActivity implements
        ShopShipCreateInviteShipperDialogFragment.ShopShipCreateInviteShipperDialog,
        ShopShipCreateChosenShipDialogFragment.ShopShipCreateChosenShipDialog,
        GoogleApiClient.OnConnectionFailedListener,
        TimePickerDialog.OnTimeSetListener {

    private PlacesAutoCompleteAdapter mSendAdAdapter;

    private GoogleApiClient mGoogleApiClient;

    private SuggestInfoToAdapter suggestInfoToAdapter;

    private SuggestShipAdapter suggestShipAdapter;

    private SessionManager sessionManager;

    private LatLng latlong;

    private EditText txtFromName;
    private EditText txtFromPhone;
    private EditText txtFromHome;
    private EditText txtToName;
    private DelayAutocompleteTextView txtToPhone;
    private EditText txtToHome;
    private DelayAutocompleteTextView txtToAddress;
    private DelayAutocompleteTextView txtFromAddress;
    private DelayAutocompleteTextView txtShipName;
    private EditText txtShipCost;
    private EditText txtShipCostS;
    private EditText txtShipDate;
    private EditText txtShipProperty;
    private EditText txtShipNote;
    private EditText etPromotionCode;

    private TextView lbl_shop_ship_create_property_light;
    private TextView lbl_shop_ship_create_property_heavy;
    private TextView lbl_shop_ship_create_property_big;
    private TextView lbl_shop_ship_create_property_break;
    private TextView lbl_shop_ship_create_property_food;
    private TextView tv_shop_info_summary;

    private LinearLayout box_shop_ship_create_property;
    private LinearLayout box_shop_ship_create_time;
    private ScrollView scrollView; //scroll view container
    private View shopInfoBox;

    private ImageView img_shop_ship_create;
    private String encodedImage;

    private HashMap shipItem;

    private ShippingOrderService serviceShip;
    private OtherService otherService;
    private ShopService shopService;

    private Dialog loadingDialog;

    private Boolean isLight = false;
    private Boolean isHeavy = false;
    private Boolean isBig = false;
    private Boolean isBreak = false;
    private Boolean isFood = false;

    private String fromX = "";
    private String fromY = "";
    private String toX = "";
    private String toY = "";

    private int positionDate;
    private Calendar currentTime;
    private Form form;
    private boolean validateTime = true;

    private final int MARK_MORNING_HOUR_OF_DAY = 11;
    private final int MARK_AFTERNOON_HOUR_OF_DAY = 18;

    private Handler mHandlerVerifyCode;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_ship_create);

        setupToolBar();

        getComponentUI();

        loadingDialog = Utils.setupLoadingDialog(this);

        serviceShip = ShippingOrderService.get_instance(this);

        otherService = OtherService.get_instance(this);

        shopService = ShopService.get_instance(this);
        mHandlerVerifyCode = new Handler();

        sessionManager = new SessionManager(getApplication());

        form = new Form(this);

        shipItem = new HashMap<>();

        shipItem.put("shopId", sessionManager.getShopId());

        suggestInfoToAdapter = new SuggestInfoToAdapter(getApplication(), txtToPhone.getText().toString(), sessionManager.getShopId());

        txtToPhone.setLoadingIndicator((RotateLoading) findViewById(R.id.li_user));

        txtToPhone.setAdapter(suggestInfoToAdapter);

        txtToPhone.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CustomerItem item = suggestInfoToAdapter.getCustomerItem(position);

                txtToName.setText(item.getFullName());
                txtToPhone.setText(item.getPhone());
                txtToAddress.setText(item.getAddress());
                txtToHome.setText(item.getAddressDetail());
                toX = item.getXPoint();
                toY = item.getYPoint();

                new GetDistanceAndCost().execute(fromX, fromY, toX, toY);
            }
        });

        suggestShipAdapter = new SuggestShipAdapter(getApplication(), sessionManager.getShopId());

        txtShipName.setAdapter(suggestShipAdapter);
        txtShipName.setLoadingIndicator(
                (RotateLoading) findViewById(R.id.li_order_title));

        txtShipName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ShipTemplateItem item = suggestShipAdapter.getShipItem(position);

                fillDataShip(item);

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(getApplication())
                .enableAutoManage(ShopShipCreateActivity.this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        initAutocompleteAddress();

        loadingDialog.show();

        new PrepareCreateShip().execute();
    }

    private void getComponentUI() {

        scrollView = (ScrollView) findViewById(R.id.fiveship_form_input);
        box_shop_ship_create_property = (LinearLayout) findViewById(R.id.box_shop_ship_create_property);
        box_shop_ship_create_time = (LinearLayout) findViewById(R.id.box_shop_ship_create_time);

        txtFromName = (EditText) findViewById(R.id.txt_shop_ship_create_from_name);
        txtFromPhone = (EditText) findViewById(R.id.txt_shop_ship_create_from_phone);
        txtFromHome = (EditText) findViewById(R.id.txt_shop_ship_create_from_home);
        txtFromAddress = (DelayAutocompleteTextView) findViewById(R.id.txt_shop_ship_create_from_address);
        tv_shop_info_summary = (TextView) findViewById(R.id.tv_summary_shop_info);

        txtToName = (EditText) findViewById(R.id.txt_shop_ship_create_to_name);
        txtToPhone = (DelayAutocompleteTextView) findViewById(R.id.txt_shop_ship_create_to_phone);
        txtToAddress = (DelayAutocompleteTextView) findViewById(R.id.txt_shop_ship_create_to_address);
        txtToHome = (EditText) findViewById(R.id.txt_shop_ship_create_to_home);

        txtShipName = (DelayAutocompleteTextView) findViewById(R.id.txt_shop_ship_create_name);
        txtShipCost = (EditText) findViewById(R.id.txt_shop_ship_create_cost);
        txtShipCostS = (EditText) findViewById(R.id.txt_shop_ship_create_cost_ship);
        txtShipDate = (EditText) findViewById(R.id.txt_shop_ship_create_date);
        txtShipProperty = (EditText) findViewById(R.id.txt_shop_ship_create_property);
        txtShipNote = (EditText) findViewById(R.id.txt_shop_ship_create_note);
        etPromotionCode = (EditText) findViewById(R.id.txt_shop_ship_promotion_code);

        txtShipCost.addTextChangedListener(new Utils.MoneyTextWatcher(txtShipCost));
        txtShipCostS.addTextChangedListener(new Utils.MoneyTextWatcher(txtShipCostS));

        shopInfoBox = findViewById(R.id.box_shop_create_from_info);

        currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);

        if (currentHour >= MARK_MORNING_HOUR_OF_DAY) {
            findViewById(R.id.lbl_this_morning).setVisibility(View.GONE);
            findViewById(R.id.lbl_this_morning_line).setVisibility(View.GONE);
        }

        if (currentHour >= MARK_AFTERNOON_HOUR_OF_DAY) {
            findViewById(R.id.lbl_this_morning).setVisibility(View.GONE);
            findViewById(R.id.lbl_this_morning_line).setVisibility(View.GONE);
            findViewById(R.id.lbl_this_afternoon).setVisibility(View.GONE);
            findViewById(R.id.lbl_this_afternoon_line).setVisibility(View.GONE);
        }

        txtShipProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (box_shop_ship_create_property.getVisibility() == View.GONE) {
                    box_shop_ship_create_property.setVisibility(View.VISIBLE);
                    txtShipProperty.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_up, 0);
                } else {
                    box_shop_ship_create_property.setVisibility(View.GONE);
                    txtShipProperty.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_down, 0);
                }

            }
        });

        txtShipDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (box_shop_ship_create_time.getVisibility() == View.GONE) {
                    Utils.expand(box_shop_ship_create_time);
                    txtShipDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_up, 0);
                } else {
                    Utils.collapse(box_shop_ship_create_time);
                    txtShipDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_down, 0);
                }

            }
        });

        img_shop_ship_create = (ImageView) findViewById(R.id.shop_ship_create_backdrop);

        img_shop_ship_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Utils.capturePictureProduct(ShopShipCreateActivity.this, getString(R.string.choose_image_product));
                ImageProcess.capturePictureProduct(ShopShipCreateActivity.this, "Chọn ảnh gói hàng");
            }
        });

        lbl_shop_ship_create_property_light = (TextView) findViewById(R.id.lbl_shop_ship_create_property_light);

        findViewById(R.id.lbl_shop_ship_create_property_light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isLight = !isLight;

                lbl_shop_ship_create_property_light.setCompoundDrawablesWithIntrinsicBounds(0, 0, isLight ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);

                setTextShipProperty();
            }
        });

        lbl_shop_ship_create_property_heavy = (TextView) findViewById(R.id.lbl_shop_ship_create_property_heavy);

        findViewById(R.id.lbl_shop_ship_create_property_heavy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isHeavy = !isHeavy;

                lbl_shop_ship_create_property_heavy.setCompoundDrawablesWithIntrinsicBounds(0, 0, isHeavy ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);

                setTextShipProperty();
            }
        });

        lbl_shop_ship_create_property_big = (TextView) findViewById(R.id.lbl_shop_ship_create_property_big);

        findViewById(R.id.lbl_shop_ship_create_property_big).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isBig = !isBig;

                lbl_shop_ship_create_property_big.setCompoundDrawablesWithIntrinsicBounds(0, 0, isBig ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);

                setTextShipProperty();
            }
        });

        lbl_shop_ship_create_property_break = (TextView) findViewById(R.id.lbl_shop_ship_create_property_break);

        findViewById(R.id.lbl_shop_ship_create_property_break).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isBreak = !isBreak;

                lbl_shop_ship_create_property_break.setCompoundDrawablesWithIntrinsicBounds(0, 0, isBreak ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);

                setTextShipProperty();
            }
        });

        lbl_shop_ship_create_property_food = (TextView) findViewById(R.id.lbl_shop_ship_create_property_food);

        findViewById(R.id.lbl_shop_ship_create_property_food).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isFood = !isFood;

                lbl_shop_ship_create_property_food.setCompoundDrawablesWithIntrinsicBounds(0, 0, isFood ? R.drawable.ic_checked : R.drawable.ic_nocheck, 0);

                setTextShipProperty();
            }
        });

        findViewById(R.id.btn_shop_create_chosen_ship).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupChosenShip();
            }
        });

        findViewById(R.id.btn_shop_ship_create_detail_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postShipOrder();
            }
        });

        findViewById(R.id.btn_local_cus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build((Activity) getApplicationContext()), Utils.PLACE_PICKER_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        final RotateLoading rlPromotionCode = (RotateLoading) findViewById(R.id.li_promotion_code);

        etPromotionCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if (mRunnable != null) {
                    mHandlerVerifyCode.removeCallbacks(mRunnable);
                }

                if (s.length() > 0) {
                    if (!rlPromotionCode.isStart()) {
                        rlPromotionCode.start();
                    }
                    mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            new VerifyPromotionCode(
                                    ShopShipCreateActivity.this,
                                    ShippingOrderService.get_instance(ShopShipCreateActivity.this),
                                    s.toString(),
                                    new OnSendRequestCompleted() {
                                        @Override
                                        public void onSendRequestCompleted(boolean error, String message) {
                                            Toast.makeText(ShopShipCreateActivity.this, message, Toast.LENGTH_SHORT).show();
                                            rlPromotionCode.stop();
                                        }
                                    }).execute();
                        }
                    };

                    mHandlerVerifyCode.postDelayed(mRunnable, 2000);
                } else {
                    rlPromotionCode.stop();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    public void setupToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.shop_ship_create_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bạn có muốn tiếp tục đăng đơn hàng ?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        builder.create();

        builder.show();

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
        switch (id) {
            case android.R.id.home:
                finish();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Utils.REQUEST_IMAGE_CAPTURE) {
                Bitmap bitmap = ImageProcess.decodeResizeImage(600);//mCurrentPhotoPath
                img_shop_ship_create.setImageBitmap(bitmap);
                encodedImage = ImageProcess.bitmapToBase64(bitmap, 100);

            } else if (requestCode == Utils.SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                ImageProcess.mFilePath = cursor.getString(column_index);
                Bitmap bm = ImageProcess.decodeResizeImage(600);//mCurrentPhotoPath
                encodedImage = ImageProcess.bitmapToBase64(bm, 100);
                img_shop_ship_create.setImageBitmap(bm);
            } else if (requestCode == Utils.PLACE_PICKER_REQUEST) {
                Place place = PlacePicker.getPlace(data, this);
                txtToAddress.setText(place.getAddress());
                toX = String.valueOf(place.getLatLng().latitude);
                toY = String.valueOf(place.getLatLng().longitude);
                new GetDistanceAndCost().execute(fromX, fromY, toX, toY);
            }
        }
    }

    private void setTextShipProperty() {

        String txtProperty = "";

        if (isLight) {
            txtProperty += " gọn nhẹ";
        }

        if (isHeavy) {
            txtProperty += (txtProperty != "" ? "," : "") + " nặng";
        }

        if (isBig) {
            txtProperty += (txtProperty != "" ? "," : "") + " cồng kềng";
        }

        if (isBreak) {
            txtProperty += (txtProperty != "" ? "," : "") + " dễ vỡ";
        }

        if (isFood) {
            txtProperty += (txtProperty != "" ? "," : "") + " đồ ăn";
        }

        txtProperty = txtProperty != "" ? "Đặc tính:" + txtProperty : txtProperty;

        txtShipProperty.setText(txtProperty);

    }

    public void setTextShipDate(View view) {

        positionDate = view.getId();

        TextView v = (TextView) view;

        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);

        ((TextView) findViewById(R.id.lbl_this_morning)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        ((TextView) findViewById(R.id.lbl_this_afternoon)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        ((TextView) findViewById(R.id.lbl_tonight)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        ((TextView) findViewById(R.id.lbl_specific_time)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        ((TextView) findViewById(R.id.lbl_tomorrow_morning)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        ((TextView) findViewById(R.id.lbl_tomorrow_afternoon)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        ((TextView) findViewById(R.id.lbl_tomorrow_night)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        ((TextView) findViewById(R.id.lbl_specific_tomorrow_time)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        ((TextView) findViewById(R.id.lbl_now)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);

        v.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checked, 0);

        if (positionDate == R.id.lbl_specific_time
                || view.getId() == R.id.lbl_specific_tomorrow_time) {
            showTimePickerDialog();
        } else {
            validateTime = true;
            ((TextInputLayout) txtShipDate.getParent()).setErrorEnabled(false);
        }

        String stringOfDate = v.getText().toString();

        if ((stringOfDate.equals("Sáng nay") && currentHour > MARK_MORNING_HOUR_OF_DAY)
                || (stringOfDate.equals("Chiều nay") && currentHour > MARK_AFTERNOON_HOUR_OF_DAY)) {
            validateTime = false;
        }

        txtShipDate.setText(stringOfDate);

        box_shop_ship_create_time.setVisibility(View.GONE);
        txtShipDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_down, 0);

    }

    public void scrollToInputInvalid(final EditText view, final String message) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (scrollView != null) {
                    scrollView.smoothScrollTo(0, Utils.getRelativeTop(view) - view.getHeight() - 125);
                }
                view.requestFocus();
                ((TextInputLayout) view.getParent()).setError(message);
            }
        });
    }

    private void fillDataShip(ShipTemplateItem item) {

        txtShipName.setText(item.getName());

        txtShipCost.setText(String.valueOf(item.getPrepayShip()));

        txtShipCostS.setText(String.valueOf(item.getCostShip()));

        txtShipDate.setText(item.getDate());

        txtShipNote.setText(item.getNote());

        txtShipProperty.setText(item.getStrProperty());

        isBig = item.isBig();

        isLight = item.isLight();

        isHeavy = item.isHeavy();

        isBreak = item.isBreak();

        isFood = item.isFood();

    }

    private void showTimePickerDialog() {

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, this, hour, minute,
                DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        Calendar c = Calendar.getInstance();
        int minHour = c.get(Calendar.HOUR_OF_DAY);
        int minMinute = c.get(Calendar.MINUTE);
        if (positionDate == R.id.lbl_specific_time && hourOfDay < minHour || (hourOfDay == minHour && minute < minMinute)) {
            validateTime = false;
            ((TextInputLayout) txtShipDate.getParent()).setErrorEnabled(false);
            ((TextInputLayout) txtShipDate.getParent()).setError(getString(R.string.invalid_time_delivery_message));
        } else {
            validateTime = true;
            ((TextInputLayout) txtShipDate.getParent()).setErrorEnabled(false);
            txtShipDate.setText(hourOfDay + "h" + (minute < 10 ? "0" : "") + minute + (positionDate == R.id.lbl_specific_time ? " hôm nay" : positionDate == R.id.lbl_specific_tomorrow_time ? " ngày mai" : ""));
        }
        txtShipDate.setText(hourOfDay + "h" + (minute < 10 ? "0" : "") + minute + (positionDate == R.id.lbl_specific_time ? " hôm nay" : positionDate == R.id.lbl_specific_tomorrow_time ? " ngày mai" : ""));

    }

    private void postShipOrder() {

        form.addField(Field.using(txtFromPhone)
                .validate(NotEmpty.build(this, getString(R.string.shop_phone_field)))
                .validate(IsPhoneNumber.build(this)));
        form.addField(Field.using(txtFromAddress)
                .validate(NotEmpty.build(this, getString(R.string.address_shop_field))));
        //form.addField(Field.using(txtToPhone)
        //.validate(NotEmpty.build(this, getString(R.string.customer_phone_field)))
        //.validate(IsPhoneNumber.build(this)));
        form.addField(Field.using(txtToAddress)
                .validate(NotEmpty.build(this, getString(R.string.customer_address_field))));
        form.addField(Field.using(txtShipName)
                .validate(NotEmpty.build(this, getString(R.string.order_title_field))));

        if (form.isValid()) {
            if (fromX == null || fromY == null) {
                scrollToInputInvalid(txtFromAddress, getString(R.string.suggest_to_use_place_by_google));
                return;
            }
            if (toX.isEmpty() || toY.isEmpty()) {
                scrollToInputInvalid(txtToAddress, getString(R.string.suggest_to_use_place_by_google));
                return;
            }

            if (!validateTime) {
                scrollToInputInvalid(txtShipDate, getString(R.string.invalid_time_delivery_message));
                return;
            }

            shipItem.put("name", txtFromName.getText().toString());
            shipItem.put("sendPhone", txtFromPhone.getText().toString());
            shipItem.put("sendAddressDetail", txtFromHome.getText().toString());
            shipItem.put("sendAddress", txtFromAddress.getText().toString());
            shipItem.put("sendX", fromX);
            shipItem.put("sendY", fromY);

            shipItem.put("receiveName", txtToName.getText().toString());
            shipItem.put("receivePhone", txtToPhone.getText().toString());
            shipItem.put("receiveAddressDetail", txtToHome.getText().toString());
            shipItem.put("receiveAddress", txtToAddress.getText().toString());
            shipItem.put("receiveX", toX);
            shipItem.put("receiveY", toY);

            shipItem.put("shipName", txtShipName.getText().toString());
            shipItem.put("shipCostPackage", txtShipCost.getText().toString());
            shipItem.put("shipCostShip", txtShipCostS.getText().toString());
            shipItem.put("shipEnd", txtShipDate.getText().toString());
            shipItem.put("shipNote", txtShipNote.getText().toString());
            shipItem.put("isBig", isBig);
            shipItem.put("isBreak", isBreak);
            shipItem.put("isFood", isFood);
            shipItem.put("isLight", isLight);
            shipItem.put("isHeavy", isHeavy);

            shipItem.put("img", encodedImage);

            shipItem.put("promotionCode", etPromotionCode.getText().toString());

            showFromInviteShipper();
        }

    }

    public void showPopupChosenShip() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ShopShipCreateChosenShipDialogFragment();

        dialog.show(getSupportFragmentManager(), "ShopShipCreateChosenShipDialogFragment");
    }

    @Override
    public void onChosenShip(ShipTemplateItem item) {
        fillDataShip(item);
    }

    public void showFromInviteShipper() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = ShopShipCreateInviteShipperDialogFragment.getInstance(sessionManager.getShopId());

        dialog.show(getSupportFragmentManager(), "ShopShipCreateInviteShipperDialogFragment");
    }

    @Override
    public void onSuccessInvite(boolean isNear, boolean isSafe, String arrShipper) {

        shipItem.put("isNear", isNear);
        shipItem.put("isSafe", isSafe);
        shipItem.put("arrShipper", arrShipper);

        loadingDialog.show();

        new PostShippingOrder().execute(shipItem);

    }

    private void initAutocompleteAddress() {

        // Register a listener that receives callbacks when a suggestion has been selected
        txtFromAddress.setOnItemClickListener(mSendAdAutocompleteClickListener);

        txtFromAddress.setLoadingIndicator((RotateLoading) findViewById(R.id.li_received_add));

        txtToAddress.setOnItemClickListener(mSendAdAutocompleteClickListener1);

        txtToAddress.setLoadingIndicator((RotateLoading) findViewById(R.id.li_delivery_add));

        mSendAdAdapter = new PlacesAutoCompleteAdapter(getApplication(), mGoogleApiClient, RegisterActivity.BOUNDS_GREATER_VIETNAM, null);

        txtFromAddress.setAdapter(mSendAdAdapter);

        txtToAddress.setAdapter(mSendAdAdapter);
    }

    private AdapterView.OnItemClickListener mSendAdAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = mSendAdAdapter.getItem(position);

            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdateSendAddressCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdateSendAddressCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            //Get coordinates of place
            latlong = place.getLatLng();

            fromX = String.valueOf(latlong.latitude);

            fromY = String.valueOf(latlong.longitude);

            places.release();
        }
    };

    private AdapterView.OnItemClickListener mSendAdAutocompleteClickListener1
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = mSendAdAdapter.getItem(position);

            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdateSendAddressCallback1);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdateSendAddressCallback1
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            //Get coordinates of place
            latlong = place.getLatLng();

            toX = String.valueOf(latlong.latitude);

            toY = String.valueOf(latlong.longitude);

            places.release();

            new GetDistanceAndCost().execute(fromX, fromY, toX, toY);
        }
    };

    public void EditShopInfo(View v) {
        if (shopInfoBox.getVisibility() == View.GONE) {
            Utils.expand(shopInfoBox);
            tv_shop_info_summary.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private class PrepareCreateShip extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            return shopService.prepareCreateShip(String.valueOf(sessionManager.getShopId()));
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                loadingDialog.dismiss();

                if (result != null && !result.getBoolean("error")) {

                    fromX = result.getJSONObject("data").getJSONObject("customer").getString("ShopX");
                    fromY = result.getJSONObject("data").getJSONObject("customer").getString("ShopY");

                    txtFromName.setText(result.getJSONObject("data").getJSONObject("customer").getString("ShopName"));
                    txtFromPhone.setText(result.getJSONObject("data").getJSONObject("customer").getString("ShopPhone"));
                    txtFromHome.setText(result.getJSONObject("data").getJSONObject("customer").getString("ShopHome"));
                    txtFromAddress.setText(result.getJSONObject("data").getJSONObject("customer").getString("ShopAddress"));

                    tv_shop_info_summary
                            .setText(
                                    result.getJSONObject("data").getJSONObject("customer").getString("ShopName")
                                            + " - " +
                                            result.getJSONObject("data").getJSONObject("customer").getString("ShopPhone")
                                            + " - " +
                                            (result.getJSONObject("data").getJSONObject("customer").getString("ShopHome").isEmpty() ? result.getJSONObject("data").getJSONObject("customer").getString("ShopHome") + ", " : "")
                                            +
                                            result.getJSONObject("data").getJSONObject("customer").getString("ShopAddress")
                            );

                    if (txtFromAddress.getText().toString().isEmpty() && txtFromPhone.getText().toString().isEmpty()) {
                        shopInfoBox.setVisibility(View.VISIBLE);
                        tv_shop_info_summary.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class GetDistanceAndCost extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            return otherService.getDistanceAndCostShip(params[0], params[1], params[2], params[3]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null && !result.getBoolean("error")) {
                    findViewById(R.id.lbl_shop_ship_create_to_distance).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.txt_shop_ship_create_to_distance)).setText(result.getString("distance"));
                    txtShipCostS.setText(result.getString("cost"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PostShippingOrder extends AsyncTask<HashMap, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(HashMap... params) {
            return serviceShip.CreateShippingOrder(params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            loadingDialog.dismiss();

            try {
                if (result != null) {
                    Toast.makeText(getApplication(), result.getString("message"), Toast.LENGTH_LONG).show();
                }

                if (result != null && !result.getBoolean("error")) {
                    //Intent callback = new Intent(Config.RECEIVE_SHOP_MOVE_TAB_WAIT);
                    //LocalBroadcastManager.getInstance(ShopShipCreateActivity.this).sendBroadcast(callback);
                    finish();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplication(), "Có lỗi xảy ra bạn vui lòng thử lại", Toast.LENGTH_LONG).show();
            }
        }
    }
}

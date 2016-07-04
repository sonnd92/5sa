package fiveship.vn.fiveship.activity.shop;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.RegisterActivity;
import fiveship.vn.fiveship.adapter.PlacesAutoCompleteAdapter;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.model.DeliveryToItem;
import fiveship.vn.fiveship.model.ShipTemplateItem;
import fiveship.vn.fiveship.service.OtherService;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.service.adapter.AddNewGroupShippingOrder;
import fiveship.vn.fiveship.service.adapter.VerifyPromotionCode;
import fiveship.vn.fiveship.utility.ImageProcess;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.validator.Field;
import fiveship.vn.fiveship.utility.validator.Form;
import fiveship.vn.fiveship.utility.validator.validation.IsPhoneNumber;
import fiveship.vn.fiveship.utility.validator.validation.NotEmpty;
import fiveship.vn.fiveship.widget.DelayAutocompleteTextView;

public class ShopCreateGroupOrderActivity extends AppCompatActivity
        implements
        ShopShipCreateInviteShipperDialogFragment.ShopShipCreateInviteShipperDialog,
        TimePickerDialog.OnTimeSetListener {
    final String DELIVERY_LAYOUT_PREFIX_TAG = "DeliveryInfo";
    final String DELIVERY_EDIT_FORM_TAG = "EditForm";
    private GoogleApiClient mGoogleApiClient;
    private PlacesAutoCompleteAdapter mAdapter;
    private SessionManager sessionManager;
    private ShopService shopService;
    private OtherService otherService;

    private String encodedImage;

    // View component
    private ScrollView scrollView;
    private EditText txtFromName;
    private EditText txtFromPhone;
    private EditText txtFromHome;
    private DelayAutocompleteTextView txtFromAddress;
    private Dialog loadingDialog;
    private LinearLayout listInfoLayout;
    private ImageView selectImage;
    private EditText txtShipName;
    private EditText txtTotalPrePay;
    private EditText txtTotalShippingCost;
    private TextView txtTitleFixed;
    private Button mEditButton;
    //Edit form
    private DelayAutocompleteTextView txtToAddressEdit;
    private EditText txtNoteEdit, txtShipCostEdit, txtPrePayEdit, txtTimeEndEdit, txtDistanceEdit;//, txtPhoneEdit,txtDetailAddEdit
    private TextView tvTimePreCheck, tvTimePreCheckEdit;

    //Delivery form
    private DelayAutocompleteTextView txtToAddress;
    private EditText txtNote, txtShipCost, txtPrePay, etPromotionCode, txtShipDate, txtDistance;//, txtPhone, txtDetailAdd
    private View shopInfoBox;
    private TextView tv_shop_info_summary;

    private LinearLayout box_shop_ship_create_time, box_shop_ship_create_time_edit;

    //
    private double fromX;
    private double fromY;
    private double toX;
    private double toY;
    private double toEditX;
    private double toEditY;
    private boolean validateTime, validateTimeEdit;
    private boolean onTimeSetEdit;
    private int mEditRecommendShipCost;
    private int mTotalRecommendShipCost;
    private int mRecommendShipCost;
    private int mIdItemEditing;

    private ArrayList<DeliveryToItem> listDelivery;
    private ShipTemplateItem shipTemplateItem;
    private int oIndex;
    private boolean isGroup;
    private boolean mIsPending;

    private Form form;
    private Form editForm;
    private Form gForm;

    private Animation fadeIn;

    private Handler mHandlerVerifyCode;
    private Runnable mRunnable;

    private int mTimeSelectedViewId, mTimeEditSelectedViewId;
    private Calendar currentTime;

    private final int MARK_MORNING_HOUR_OF_DAY = 11;
    private final int MARK_AFTERNOON_HOUR_OF_DAY = 18;

    //region On Create View
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_create_group_order);
        validateTime = true;
        validateTimeEdit = true;
        mTotalRecommendShipCost = 0;

        mHandlerVerifyCode = new Handler();

        sessionManager = new SessionManager(getApplication());

        loadingDialog = Utils.setupLoadingDialog(this);
        loadingDialog.show();

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);

        new PrepareCreateShip().execute();
        shopService = ShopService.get_instance(this);
        otherService = OtherService.get_instance(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();

        Bundle bdl = getIntent().getExtras();
        isGroup = bdl != null && bdl.getBoolean("isGroup");

        oIndex = 1;

        setupToolBar();

        fromX = sessionManager.getXPoint().isEmpty() ? 0 : Double.parseDouble(sessionManager.getXPoint());
        fromY = sessionManager.getYPoint().isEmpty() ? 0 : Double.parseDouble(sessionManager.getYPoint());

        listDelivery = new ArrayList<>();
        shipTemplateItem = new ShipTemplateItem();

        form = new Form(this);
        editForm = new Form(this);
        gForm = new Form(this);

        getComponentUI();

        initAutocompleteAddress();
        initAdAutocompleteDeliveryForm();

        validation();
        validateGroupForm();
    }


    public void setupToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.shop_ship_create_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                showDialogConfirmExit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //init view
    public void getComponentUI() {

        scrollView = (ScrollView) findViewById(R.id.fiveship_form_input);

        box_shop_ship_create_time = (LinearLayout) findViewById(R.id.box_shop_ship_create_time);

        txtFromName = (EditText) findViewById(R.id.txt_shop_ship_create_from_name);
        txtFromPhone = (EditText) findViewById(R.id.txt_shop_ship_create_from_phone);
        txtFromHome = (EditText) findViewById(R.id.txt_shop_ship_create_from_home);
        listInfoLayout = (LinearLayout) findViewById(R.id.list_ship_delivery);
        txtFromAddress = (DelayAutocompleteTextView) findViewById(R.id.txt_shop_ship_create_from_address);
        txtTitleFixed = (TextView) findViewById(R.id.title_order_fx);
        txtDistance = (EditText) findViewById(R.id.txt_shop_ship_create_to_distance) ;

        //Delivery form
        txtNote = (EditText) findViewById(R.id.txt_shop_ship_create_note);
        //txtDetailAdd = (EditText) findViewById(R.id.txt_shop_ship_create_to_home);
        txtShipCost = (EditText) findViewById(R.id.txt_shop_ship_create_cost_ship);
        txtPrePay = (EditText) findViewById(R.id.txt_shop_ship_create_cost);
        //txtPhone = (EditText) findViewById(R.id.txt_customer_phone);
        tv_shop_info_summary = (TextView) findViewById(R.id.tv_summary_shop_info);
        shopInfoBox = findViewById(R.id.box_shop_create_from_info);
        etPromotionCode = (EditText) findViewById(R.id.txt_shop_ship_promotion_code);
        txtShipDate = (EditText) findViewById(R.id.txt_shop_ship_create_date);

        selectImage = (ImageView) findViewById(R.id.shop_ship_create_backdrop);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageProcess.capturePictureProduct(ShopCreateGroupOrderActivity.this, "Chọn ảnh gói hàng");
            }
        });

        txtTotalPrePay = (EditText) findViewById(R.id.txt_total_pre_pay_cost);
        txtTotalShippingCost = (EditText) findViewById(R.id.txt_total_shipping_cost);
        txtShipName = (EditText) findViewById(R.id.txt_shop_ship_create_name);

        View groupLayout = findViewById(R.id.is_group_layout);

        if (!isGroup) {
            groupLayout.setVisibility(View.GONE);
        } else {
            findViewById(R.id.pre_pay_and_shipping_cost_layout).setVisibility(View.GONE);
            txtTotalPrePay.addTextChangedListener(new Utils.MoneyTextWatcher(txtTotalPrePay));
            txtTotalShippingCost.addTextChangedListener(new Utils.MoneyTextWatcher(txtTotalShippingCost));
        }

        //set on click event for add new delivery button
        findViewById(R.id.add_new_delivery_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findViewById(R.id.delivery_input_form_box).getVisibility() == View.VISIBLE) {
                    if (form.isValid()) {
                        if (toX == 0 || toY == 0) {
                            Toast.makeText(ShopCreateGroupOrderActivity.this, getString(R.string.suggest_to_use_place_by_google), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(!validateTime){
                            showTimeErrorCreateForm();
                            return;
                        }else{
                            ((TextInputLayout) txtShipDate.getParent()).setErrorEnabled(false);
                        }

                        addNewItemAndUI(getDeliveryItem());
                        clearEtDeliveryForm();
                        scrollToView(listInfoLayout);
                    }
                } else {
                    Utils.expand(findViewById(R.id.delivery_input_form_box));
                    findViewById(R.id.delivery_input_form_title).setVisibility(View.VISIBLE);

                    //Update recommend shipping cost
                    mTotalRecommendShipCost += mRecommendShipCost;
                    txtTotalShippingCost.setText(String.valueOf(mTotalRecommendShipCost));
                }
            }
        });

        //set on click for submit button
        findViewById(R.id.btn_shop_ship_create_group_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (form.isValid() && (!isGroup || gForm.isValid())) {
                    if(isEditFormOpened()) {
                        showConfirmEditDialog();
                    }else{
                        if (!validateTime) {
                            scrollToInputInvalid(txtShipDate);
                            return;
                        }

                        if(toX == 0 && toY == 0 && !Any(listDelivery)){
                            scrollToInputInvalid(txtToAddress);
                            Toast.makeText(ShopCreateGroupOrderActivity.this, getString(R.string.suggest_to_use_place_by_google), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        showFromInviteShipper();
                    }
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
                            new VerifyPromotionCode(ShopCreateGroupOrderActivity.this, ShippingOrderService.get_instance(ShopCreateGroupOrderActivity.this), s.toString(), new OnSendRequestCompleted() {
                                @Override
                                public void onSendRequestCompleted(boolean error, String message) {
                                    Toast.makeText(ShopCreateGroupOrderActivity.this, message, Toast.LENGTH_SHORT).show();
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
    }



    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Utils.REQUEST_IMAGE_CAPTURE) {

                Bitmap bitmap = ImageProcess.decodeResizeImage(600);//mCurrentPhotoPath
                selectImage.setImageBitmap(bitmap);
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
                selectImage.setImageBitmap(bm);
            }
        }
    }
    //endregion

    public void setTextShipDate(View view) {

        mTimeSelectedViewId = view.getId();

        TextView v = (TextView) view;

        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        if (tvTimePreCheck != null)
            tvTimePreCheck.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);

        v.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checked, 0);
        tvTimePreCheck = v;

        if (mTimeSelectedViewId == R.id.lbl_specific_time
                || view.getId() == R.id.lbl_specific_tomorrow_time) {
            showTimePickerDialog(false);
        } else {
            clearTimeErrorCreateForm();
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

    public void setTextShipDateEdit(View view) {

        mTimeEditSelectedViewId = view.getId();

        TextView v = (TextView) view;

        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);

        if (tvTimePreCheckEdit != null)
            tvTimePreCheckEdit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);

        v.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checked, 0);
        tvTimePreCheckEdit = v;

        if (mTimeEditSelectedViewId == R.id.lbl_specific_time_edit
                || view.getId() == R.id.lbl_specific_tomorrow_time_edit) {
            showTimePickerDialog(true);
        } else {
            validateTimeEdit = true;
            ((TextInputLayout) txtTimeEndEdit.getParent()).setErrorEnabled(false);
        }

        String stringOfDate = v.getText().toString();

        if ((stringOfDate.equals("Sáng nay") && currentHour > MARK_MORNING_HOUR_OF_DAY)
                || (stringOfDate.equals("Chiều nay") && currentHour > MARK_AFTERNOON_HOUR_OF_DAY)) {
            validateTimeEdit = false;
        }

        txtTimeEndEdit.setText(stringOfDate);

        box_shop_ship_create_time_edit.setVisibility(View.GONE);
        txtTimeEndEdit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_down, 0);

    }

    private void showTimePickerDialog(boolean isEdit) {
        onTimeSetEdit = isEdit;
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, this, hour, minute,
                DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    /*
     * check edit delivery form has opened or not
     */
    private boolean isEditFormOpened(){
        return listInfoLayout.findViewWithTag(DELIVERY_EDIT_FORM_TAG) != null
                && listInfoLayout.findViewWithTag(DELIVERY_EDIT_FORM_TAG).getVisibility() == View.VISIBLE;
    }

    /*
     * show dialog confirm submit edit form
     */
    private void showConfirmEditDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bạn đang chỉnh sửa thông tin nhận hàng. Bạn có muốn lưu thay đổi?");
        builder.setPositiveButton("Lưu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mEditButton != null)
                    mEditButton.performClick();
            }
        })
        .setNegativeButton("Bỏ qua", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                collapseEditForm();
            }
        });

        builder.show();
    }

    private void collapseEditForm(){
        Utils.collapse(listInfoLayout.findViewWithTag(DELIVERY_EDIT_FORM_TAG));

        listInfoLayout
                .findViewWithTag(DELIVERY_LAYOUT_PREFIX_TAG + mIdItemEditing)
                .findViewById(R.id.summary_content)
                .startAnimation(fadeIn);

        listInfoLayout
                .findViewWithTag(DELIVERY_LAYOUT_PREFIX_TAG + mIdItemEditing)
                .findViewById(R.id.summary_content)
                .setVisibility(View.VISIBLE);
    }

    public void getComponentEditFormUI(final int currentEditIdx) {

        currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);

        if (currentHour >= MARK_MORNING_HOUR_OF_DAY) {
            findViewById(R.id.lbl_this_morning_edit).setVisibility(View.GONE);
            findViewById(R.id.lbl_this_morning_line_edit).setVisibility(View.GONE);
        }

        if (currentHour >= MARK_AFTERNOON_HOUR_OF_DAY) {
            findViewById(R.id.lbl_this_morning_edit).setVisibility(View.GONE);
            findViewById(R.id.lbl_this_morning_line_edit).setVisibility(View.GONE);
            findViewById(R.id.lbl_this_afternoon_edit).setVisibility(View.GONE);
            findViewById(R.id.lbl_this_afternoon_line_edit).setVisibility(View.GONE);
        }

        box_shop_ship_create_time_edit = (LinearLayout) findViewById(R.id.box_shop_ship_create_time_edit);
        txtNoteEdit = (EditText) findViewById(R.id.txt_shop_ship_create_note_edit);
        //txtDetailAddEdit = (EditText) findViewById(R.id.txt_shop_ship_create_to_home_edit);
        txtTimeEndEdit = (EditText) findViewById(R.id.txt_shop_ship_create_date_edit);
        txtShipCostEdit = (EditText) findViewById(R.id.txt_shop_ship_create_cost_ship_edit);
        txtPrePayEdit = (EditText) findViewById(R.id.txt_shop_ship_create_cost_edit);
        //txtPhoneEdit = (EditText) findViewById(R.id.txt_customer_phone_edit);
        txtDistanceEdit = (EditText) findViewById(R.id.txt_shop_ship_create_to_distance_edit) ;
        txtToAddressEdit = (DelayAutocompleteTextView) findViewById(R.id.txt_shop_ship_create_to_address_edit);
        mEditButton = (Button) findViewById(R.id.edit_delivery_address);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editForm.isValid()) {
                    if (!validateTimeEdit) {
                        scrollToInputInvalid(txtTimeEndEdit);
                        return;
                    }
                    DeliveryToItem obj = new DeliveryToItem();
                    obj.setId(currentEditIdx);
                    obj.setNote(txtNoteEdit.getText().toString());
                    obj.setDetailAddress("");//txtDetailAddEdit.getText().toString()
                    obj.setShipCost(txtShipCostEdit.getText().toString().isEmpty() ? 0 : Integer.parseInt(txtShipCostEdit.getText().toString()));
                    obj.setPrePay(txtPrePayEdit.getText().toString().isEmpty() ? 0 : Integer.parseInt(txtPrePayEdit.getText().toString()));
                    obj.setDateEnd(txtTimeEndEdit.getText().toString());
                    obj.setViewSelectDateEnd(mTimeEditSelectedViewId);
                    obj.setRecommendShippingCost(mEditRecommendShipCost);
                    obj.setDistance(txtDistanceEdit.getText().toString());

                    mTotalRecommendShipCost = mTotalRecommendShipCost
                                                - listDelivery.get(currentEditIdx - 1).getRecommendShippingCost()
                                                + mEditRecommendShipCost;
                    //obj.setPhone(txtPhoneEdit.getText().toString());
                    if (!listDelivery.get(currentEditIdx - 1).getAddress().equals(txtToAddressEdit.getText().toString())) {
                        if (toEditX != 0 && toEditY != 0) {
                            obj.setAddress(txtToAddressEdit.getText().toString());
                            obj.setXPoint(toEditX);
                            obj.setYPoint(toEditY);
                        } else {
                            Toast.makeText(ShopCreateGroupOrderActivity.this, getString(R.string.suggest_to_use_place_by_google), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        obj.setAddress(listDelivery.get(currentEditIdx - 1).getAddress());
                        obj.setXPoint(listDelivery.get(currentEditIdx - 1).getXPoint());
                        obj.setYPoint(listDelivery.get(currentEditIdx - 1).getYPoint());
                    }

                    //submit change
                    //update object
                    listDelivery.set(currentEditIdx - 1, obj);
                    txtTotalShippingCost.setText(String.valueOf(mTotalRecommendShipCost));
                    //update view
                    editItemAndUI(obj);
                    // show summary content view and collapse edit form
                    collapseEditForm();

                    toEditY = 0;
                    toEditX = 0;
                }
            }
        });

        // show time select view
        txtTimeEndEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (box_shop_ship_create_time_edit.getVisibility() == View.GONE) {
                    Utils.expand(box_shop_ship_create_time_edit);
                    txtTimeEndEdit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_up, 0);
                } else {
                    Utils.collapse(box_shop_ship_create_time_edit);
                    txtTimeEndEdit.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info, 0, R.drawable.ic_arrow_down, 0);
                }

            }
        });
    }

    public void HideInputDelivery(View v) {
        if (Any(listDelivery)) {
            Utils.collapse(findViewById(R.id.delivery_input_form_box));
            findViewById(R.id.delivery_input_form_title).setVisibility(View.GONE);

            //update total recommend shipping cost
            mTotalRecommendShipCost -= mRecommendShipCost;
            txtTotalShippingCost.setText(String.valueOf(mTotalRecommendShipCost));
        }
    }

    public void updateTitleFx() {
        txtTitleFixed.setText("Thông tin đơn hàng " + String.format("%02d", oIndex));
        findViewById(R.id.remove_order_info).setVisibility(View.VISIBLE);
    }

    public void editItemAndUI(final DeliveryToItem infoItem) {
        View root = listInfoLayout.findViewWithTag(DELIVERY_LAYOUT_PREFIX_TAG + infoItem.getId());
        ((TextView) root.findViewById(R.id.info_title)).setText("Thông tin đơn hàng " + String.format("%02d", infoItem.getId()));
        ((TextView) root
                .findViewById(R.id.info_summary))
                .setText((infoItem.getDetailAddress().isEmpty() ? "" : infoItem.getDetailAddress() + ", ")
                        + infoItem.getAddress());

        listDelivery.set(infoItem.getId() - 1, infoItem);
    }

    // add new delivery action
    public void addNewItemAndUI(final DeliveryToItem infoItem) {

        final LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View infoView = layoutInflater.inflate(R.layout.delivery_info_summary_layout, listInfoLayout, false);

        ((TextView) infoView.findViewById(R.id.info_title)).setText("Thông tin đơn hàng " + String.format("%02d", infoItem.getId()));
        ((TextView) infoView
                .findViewById(R.id.info_summary))
                .setText((infoItem.getDetailAddress().isEmpty() ? "" : infoItem.getDetailAddress() + ", ")
                        + infoItem.getAddress());
        //infoItem.getPhone() + " - "
        infoView.setTag(DELIVERY_LAYOUT_PREFIX_TAG + infoItem.getId());

        // on click event when click into summary content
        infoView.findViewById(R.id.summary_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isEditFormOpened()){
                    showConfirmEditDialog();
                }else {
                    mIdItemEditing = infoItem.getId();

                    v.setVisibility(View.GONE);

                    DeliveryToItem currentItem = listDelivery.get(mIdItemEditing - 1);
                    View editForm = layoutInflater.inflate(R.layout.delivery_info_edit_form_layout, listInfoLayout, false);
                    editForm.setTag(DELIVERY_EDIT_FORM_TAG);
                    if (isGroup) {
                        editForm.findViewById(R.id.pre_pay_and_shipping_cost_layout_edit).setVisibility(View.GONE);
                    }

                    listInfoLayout.addView(editForm, Integer.parseInt(infoView.getTag().toString().replace(DELIVERY_LAYOUT_PREFIX_TAG, "")));
                    Utils.expand(editForm);
                    txtToAddressEdit = (DelayAutocompleteTextView) editForm.findViewById(R.id.txt_shop_ship_create_to_address_edit);

                    initAdAutocompleteEditForm();
                    getComponentEditFormUI(mIdItemEditing);
                    validationEditForm();
                    bindingData(currentItem);
                }
            }
        });

        infoView.findViewById(R.id.btn_delete_delivery_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get delivery item deleted
                DeliveryToItem currentItem = listDelivery.get(infoItem.getId() - 1);
                currentItem.setIsDeleted(true);
                listDelivery.set(infoItem.getId() - 1, currentItem);

                //update total recommend shipping cost
                mTotalRecommendShipCost -= currentItem.getRecommendShippingCost();
                txtTotalShippingCost.setText(String.valueOf(mTotalRecommendShipCost));

                infoView.setVisibility(View.GONE);

                if (listInfoLayout.findViewWithTag(DELIVERY_EDIT_FORM_TAG) != null
                        && listInfoLayout.indexOfChild(listInfoLayout.findViewWithTag(DELIVERY_EDIT_FORM_TAG)) == infoItem.getId()) {
                    listInfoLayout.removeView(listInfoLayout.findViewWithTag(DELIVERY_EDIT_FORM_TAG));
                }

                // if don't have any delivery address, show delivery input form
                if (!Any(listDelivery) && findViewById(R.id.delivery_input_form_box).getVisibility() != View.VISIBLE) {
                    Utils.expand(findViewById(R.id.delivery_input_form_box));
                    findViewById(R.id.remove_order_info).setVisibility(View.GONE);
                    findViewById(R.id.delivery_input_form_title).setVisibility(View.VISIBLE);

                    //update total recommend shipping cost
                    mTotalRecommendShipCost += mRecommendShipCost;
                    txtTotalShippingCost.setText(String.valueOf(mTotalRecommendShipCost));
                }
            }
        });

        listInfoLayout.addView(infoView);
        listDelivery.add(infoItem);

        //scrollToBottom();
        oIndex++;
        updateTitleFx();
    }

    public boolean Any(ArrayList<DeliveryToItem> arrayList) {
        for (DeliveryToItem item : arrayList) {
            if (!item.isDeleted()) {
                return true;
            }
        }
        return false;
    }

    /**
     * get delivery info from view
     * @return DeliveryToItem
     */
    public DeliveryToItem getDeliveryItem() {


        final DeliveryToItem infoItem = new DeliveryToItem();
        infoItem.setId(oIndex);
        infoItem.setAddress(txtToAddress.getText().toString());
        infoItem.setDetailAddress("");//txtDetailAdd.getText().toString()
        infoItem.setName((txtFromName.getText().toString().isEmpty() ? sessionManager.getName() : txtFromName.getText().toString())
                + " Đơn " + String.format("%02d", oIndex));
        infoItem.setDateEnd(txtShipDate.getText().toString());
        //infoItem.setPhone(txtPhone.getText().toString());

        if (!isGroup) {
            infoItem.setPrePay(txtPrePay.getText().toString().isEmpty() ? 0 : Integer.parseInt(txtPrePay.getText().toString()));
            infoItem.setShipCost(txtShipCost.getText().toString().isEmpty() ? 0 : Integer.parseInt(txtShipCost.getText().toString()));
        }
        infoItem.setNote(txtNote.getText().toString());
        infoItem.setXPoint(toX);
        infoItem.setYPoint(toY);
        infoItem.setDistance(txtDistance.getText().toString());

        infoItem.setViewSelectDateEnd(getEditViewDateEndSelected());
        infoItem.setRecommendShippingCost(mRecommendShipCost);

        return infoItem;
    }

    // set time view selected in edit form which choose in create form
    public int getEditViewDateEndSelected(){
        switch (mTimeSelectedViewId){
            case R.id.lbl_now:
                return R.id.lbl_now_edit;
            case R.id.lbl_this_morning:
                return R.id.lbl_this_morning_edit;
            case R.id.lbl_this_afternoon:
                return R.id.lbl_this_afternoon_edit;
            case R.id.lbl_tonight:
                return R.id.lbl_tonight_edit;
            case R.id.lbl_specific_time:
                return R.id.lbl_specific_time_edit;
            case R.id.lbl_tomorrow_morning:
                return R.id.lbl_tomorrow_morning_edit;
            case R.id.lbl_tomorrow_afternoon:
                return R.id.lbl_tomorrow_afternoon_edit;
            case R.id.lbl_specific_tomorrow_time:
                return R.id.lbl_specific_tomorrow_time_edit;
            default:
                return 0;
        }
    }

    public void bindingData(DeliveryToItem item) {
        //txtPhoneEdit.setText(item.getPhone());
        //txtDetailAddEdit.setText(item.getDetailAddress());
        txtNoteEdit.setText(item.getNote());
        txtTimeEndEdit.setText(item.getDateEnd());
        tvTimePreCheckEdit = ((TextView) findViewById(item.getViewSelectDateEnd()));

        if (tvTimePreCheckEdit != null) {
            tvTimePreCheckEdit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checked, 0);
        }

        if (!isGroup) {
            txtPrePayEdit.setText(String.valueOf(item.getPrePay()));
            txtShipCostEdit.setText(String.valueOf(item.getShipCost()));
        }

        txtToAddressEdit.setText(item.getAddress());
        mEditRecommendShipCost = item.getRecommendShippingCost();
        txtDistanceEdit.setText(item.getDistance());
    }

    public void initAdAutocompleteEditForm() {
        txtToAddressEdit.setAdapter(mAdapter);
        txtToAddressEdit.setThreshold(2);

        txtToAddressEdit.setOnItemClickListener(mToAdAutocompleteClickListener);
        txtToAddressEdit.setLoadingIndicator((RotateLoading) findViewById(R.id.li_delivery_add_edit));
    }

    public void initAdAutocompleteDeliveryForm() {
        txtToAddress = (DelayAutocompleteTextView) findViewById(R.id.txt_shop_ship_create_to_address);
        txtToAddress.setAdapter(mAdapter);
        txtToAddress.setThreshold(2);

        txtToAddress.setOnItemClickListener(mToAdFixedAutocompleteClickListener);
        txtToAddress.setLoadingIndicator((RotateLoading) findViewById(R.id.li_delivery_add));
    }

    public void clearEtDeliveryForm() {
        txtNote.setText("");
        //txtDetailAdd.setText("");
        txtShipCost.setText("");
        txtPrePay.setText("");
        //txtPhone.setText("");
        txtToAddress.setText("");
        txtShipDate.setText("");
        txtDistance.setText("");
        if (tvTimePreCheck != null)
            tvTimePreCheck.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_nocheck, 0);
        toX = toY = 0;
        mRecommendShipCost = 0;
    }

    public void validation() {

        form.addField(Field.using(txtFromName)
                .validate(NotEmpty.build(this, getString(R.string.shop_name_field))));
        form.addField(Field.using(txtFromPhone)
                .validate(NotEmpty.build(this, getString(R.string.shop_phone_field)))
                .validate(IsPhoneNumber.build(this)));
        form.addField(Field.using(txtFromAddress)
                .validate(NotEmpty.build(this, getString(R.string.address_shop_field))));

        //form.addField(Field.using(txtPhone)
        //.validate(NotEmpty.build(this, getString(R.string.phone_field)))
        //.validate(IsPhoneNumber.build(this)));
        form.addField(Field.using(txtToAddress).validate(NotEmpty.build(this, getString(R.string.street_field))));

        if (!isGroup) {
            form.addField(Field.using(txtShipCost)
                    .validate(NotEmpty.build(this, getString(R.string.shipping_cost_field))));
        }
    }

    public void validateGroupForm() {

        //gForm.addField(Field.using(txtShipName).validate(NotEmpty.build(this, getString(R.string.order_title_field))));
        gForm.addField(Field.using(txtTotalShippingCost).validate(NotEmpty.build(this, getString(R.string.shipping_cost_field))));
    }

    public void validationEditForm() {

        //editForm.addField(Field.using(txtPhoneEdit)
        //.validate(NotEmpty.build(this, getString(R.string.phone_field)))
        //.validate(IsPhoneNumber.build(this)));
        if (!isGroup) {
            editForm.addField(Field.using(txtShipCostEdit)
                    .validate(NotEmpty.build(this, getString(R.string.shipping_cost_field))));
        }
        editForm.addField(Field.using(txtToAddressEdit).validate(NotEmpty.build(this, getString(R.string.street_field))));
    }

    //region autocomplete address
    private void initAutocompleteAddress() {

        mAdapter = new PlacesAutoCompleteAdapter(this, mGoogleApiClient, RegisterActivity.BOUNDS_GREATER_VIETNAM, null);

        txtFromAddress.setAdapter(mAdapter);
        txtFromAddress.setThreshold(2);

        // Register a listener that receives callbacks when a suggestion has been selected
        txtFromAddress.setOnItemClickListener(mSendAdAutocompleteClickListener);
        txtFromAddress.setLoadingIndicator((RotateLoading) findViewById(R.id.li_received_add));

    }

    private AdapterView.OnItemClickListener mSendAdAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = mAdapter.getItem(position);

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
            LatLng latlng = place.getLatLng();

            fromX = latlng.latitude;

            fromY = latlng.longitude;

            places.release();
        }
    };
    private AdapterView.OnItemClickListener mToAdAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = mAdapter.getItem(position);

            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdateToAddressCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdateToAddressCallback
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
            LatLng latlng = place.getLatLng();

            toEditX = latlng.latitude;

            toEditY = latlng.longitude;

            places.release();

            //if it's edit form send 1 else 0
            new GetDistanceAndCost().execute(fromX, fromY, toEditX, toEditY, 1d);
        }
    };

    private AdapterView.OnItemClickListener mToAdFixedAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = mAdapter.getItem(position);

            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdateToAddressFixedCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdateToAddressFixedCallback
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
            LatLng latlng = place.getLatLng();

            toX = latlng.latitude;

            toY = latlng.longitude;

            places.release();
            mTotalRecommendShipCost -= mRecommendShipCost;
            //if it's edit form send 1 else 0
            new GetDistanceAndCost().execute(fromX, fromY, toX, toY, 0d);
        }
    };
    //endregion

    public void showFromInviteShipper() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = ShopShipCreateInviteShipperDialogFragment.getInstance(sessionManager.getShopId());

        dialog.show(getSupportFragmentManager(), "ShopShipCreateInviteShipperDialogFragment");
    }

    public void showDialogConfirmExit(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Bạn có muốn tiếp tục đăng đơn hàng ?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do nothing
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
    public void onBackPressed() {
        showDialogConfirmExit();
    }

    @Override
    public void onSuccessInvite(boolean isNear, boolean isSafe, String arrShipper) {
        loadingDialog.show();
        //from
        DeliveryToItem from = new DeliveryToItem();
        from.setName(txtFromName.getText().toString());
        from.setId(sessionManager.getShopId());
        from.setAddress(txtFromAddress.getText().toString());
        from.setDetailAddress(txtFromHome.getText().toString());
        from.setXPoint(fromX);
        from.setYPoint(fromY);
        from.setPhone(txtFromPhone.getText().toString());
        //Shipping order
        shipTemplateItem.setName(txtShipName.getText().toString().isEmpty() ? txtFromName.getText().toString() : txtShipName.getText().toString());
        shipTemplateItem.setShopId(sessionManager.getShopId());
        shipTemplateItem.setCostShip(txtTotalShippingCost.getText().toString().replace(".", ""));

        shipTemplateItem.setPrepayShip(
                txtTotalPrePay.getText().toString().isEmpty() ? "0" : txtTotalPrePay.getText().toString().replace(".", ""));

        //add delivery info in view to list
        if (findViewById(R.id.delivery_input_form_box).getVisibility() == View.VISIBLE)
            listDelivery.add(getDeliveryItem());

        shipTemplateItem.setShipInfoItem(listDelivery);
        shipTemplateItem.setImage(encodedImage);
        shipTemplateItem.setIsSafe(isSafe);
        shipTemplateItem.setIsNear(isNear);
        shipTemplateItem.setIsGroup(true);//isGroup
        shipTemplateItem.setShipFrom(from);

        shipTemplateItem.setPromotionCode(etPromotionCode.getText().toString());

        OnSendRequestCompleted callback = new OnSendRequestCompleted() {
            @Override
            public void onSendRequestCompleted(boolean error, String message) {
                loadingDialog.dismiss();
                Toast.makeText(ShopCreateGroupOrderActivity.this
                        , message, Toast.LENGTH_SHORT).show();
                if (!error) {
                    finish();
                };
                mIsPending = false;
            }
        };
        if(!mIsPending) {
            new AddNewGroupShippingOrder(ShopCreateGroupOrderActivity.this, callback, shipTemplateItem).execute();
            mIsPending = true;
        }
    }

    private void scrollToView(final View v) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo(0, v.getBottom() - 360);
            }
        });
    }

    public void EditShopInfo(View v) {
        shopInfoBox = findViewById(R.id.box_shop_create_from_info);
        if (shopInfoBox.getVisibility() == View.GONE) {
            Utils.expand(shopInfoBox);
            tv_shop_info_summary.setVisibility(View.GONE);
        }
    }

    // show error when user choose invalid time
    public void showTimeErrorCreateForm(){

        validateTime = false;
        ((TextInputLayout) txtShipDate.getParent()).setErrorEnabled(true);
        ((TextInputLayout) txtShipDate.getParent()).setError(getString(R.string.invalid_time_delivery_message));
        Toast.makeText(ShopCreateGroupOrderActivity.this, getString(R.string.invalid_time_delivery_message), Toast.LENGTH_SHORT).show();

    }

    // clear error invalid time
    public void clearTimeErrorCreateForm(){

        validateTime = true;
        ((TextInputLayout) txtShipDate.getParent()).setErrorEnabled(false);
    }
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        int minHour = c.get(Calendar.HOUR_OF_DAY);
        int minMinute = c.get(Calendar.MINUTE);
        if (!onTimeSetEdit) {

            if (mTimeSelectedViewId == R.id.lbl_specific_time && (hourOfDay < minHour || (hourOfDay == minHour && minute < minMinute))) {
                showTimeErrorCreateForm();
            } else {
                clearTimeErrorCreateForm();
            }

            txtShipDate.setText(hourOfDay + "h" + (minute < 10 ? "0" : "") + minute + (mTimeSelectedViewId == R.id.lbl_specific_time ? " hôm nay" : mTimeSelectedViewId == R.id.lbl_specific_tomorrow_time ? " ngày mai" : ""));

        } else {

            if (mTimeEditSelectedViewId == R.id.lbl_specific_time_edit && (hourOfDay < minHour || (hourOfDay == minHour && minute < minMinute))) {

                validateTimeEdit = false;
                ((TextInputLayout) txtTimeEndEdit.getParent()).setError(getString(R.string.invalid_time_delivery_message));
                Toast.makeText(ShopCreateGroupOrderActivity.this, getString(R.string.invalid_time_delivery_message), Toast.LENGTH_SHORT).show();

            } else {

                validateTimeEdit = true;
                ((TextInputLayout) txtTimeEndEdit.getParent()).setErrorEnabled(false);

            }

            txtTimeEndEdit.setText(hourOfDay + "h" + (minute < 10 ? "0" : "") + minute + (mTimeEditSelectedViewId == R.id.lbl_specific_time_edit ? " hôm nay" : mTimeEditSelectedViewId == R.id.lbl_specific_tomorrow_time_edit ? " ngày mai" : ""));
        }
    }

    public void scrollToInputInvalid(final EditText view) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (scrollView != null) {
                    scrollView.smoothScrollTo(0, Utils.getRelativeTop(view) - view.getHeight() - 200);
                }

                view.requestFocus();
            }
        });
    }

    //region async request
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

                    if (result.getJSONObject("data").getJSONObject("customer").getString("ShopX").isEmpty()
                            && result.getJSONObject("data").getJSONObject("customer").getString("ShopY").isEmpty()) {

                        sessionManager.setXPoint(result.getJSONObject("data").getJSONObject("customer").getString("ShopX"));
                        sessionManager.setYPoint(result.getJSONObject("data").getJSONObject("customer").getString("ShopY"));

                    }

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

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (txtFromName.getText().toString().isEmpty() || txtFromAddress.getText().toString().isEmpty() || txtFromPhone.getText().toString().isEmpty()) {
                shopInfoBox.setVisibility(View.VISIBLE);
                tv_shop_info_summary.setVisibility(View.GONE);
            }

        }
    }

    private class GetDistanceAndCost extends AsyncTask<Double, Void, JSONObject> {
        private boolean mIsEdit;
        @Override
        protected JSONObject doInBackground(Double... params) {
            mIsEdit = params[4] == 1;
            return otherService.getDistanceAndCostShip(String.valueOf(params[0]), String.valueOf(params[1]), String.valueOf(params[2]), String.valueOf(params[3]));
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null && !result.getBoolean("error")) {
                    if(mIsEdit) {
                        mEditRecommendShipCost = Integer.parseInt(result.getString("cost"));
                        txtDistanceEdit.setText(result.getString("distance"));
                    }else {
                        mRecommendShipCost = Integer.parseInt(result.getString("cost"));
                        txtDistance.setText(result.getString("distance"));

                        mTotalRecommendShipCost += mRecommendShipCost;
                        txtTotalShippingCost.setText(String.valueOf(mTotalRecommendShipCost));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //endregion

}

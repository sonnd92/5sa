package fiveship.vn.fiveship.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.victor.loading.rotate.RotateLoading;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.shipper.MainActivity;
import fiveship.vn.fiveship.activity.shop.ShopActivity;
import fiveship.vn.fiveship.adapter.PlacesAutoCompleteAdapter;
import fiveship.vn.fiveship.interfaces.OnRegisterCompleted;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.service.adapter.Register;
import fiveship.vn.fiveship.utility.ImageProcess;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.validator.Field;
import fiveship.vn.fiveship.utility.validator.Form;
import fiveship.vn.fiveship.utility.validator.validation.InRange;
import fiveship.vn.fiveship.utility.validator.validation.IsEmail;
import fiveship.vn.fiveship.utility.validator.validation.IsPhoneNumber;
import fiveship.vn.fiveship.utility.validator.validation.NotEmpty;
import fiveship.vn.fiveship.widget.DelayAutocompleteTextView;

public class RegisterActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private boolean isShipper;

    public static final LatLngBounds BOUNDS_GREATER_VIETNAM = new LatLngBounds(new LatLng(20.580836, 104.674108), new LatLng(21.8557098, 106.6769645));

    LatLng addressCoordinates;

    private ScrollView scrollView;
    EditText etEmail;
    EditText etPassword;
    TextView btnSubmit;
    DelayAutocompleteTextView etAddress;
    EditText etPhone;
    //shop view
    EditText etName;
    EditText etShopAddressDetail;
    //shipper view
    EditText etShipperLicenseNumber;
    EditText etReferralCode;

    CheckBox cbProvision;

    SessionManager sessionManager;
    String encodedImage = "";
    ImageView bannerBackground;
    ImageView avartarImage;

    String provider = "5ship";
    Dialog loadingDlg;
    // Form used for validation
    private Form mForm;

    protected GoogleApiClient mGoogleApiClient;
    private PlacesAutoCompleteAdapter mAdapter;
    public static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isShipper = getIntent().getBooleanExtra("isShipper", false);

        sessionManager = new SessionManager(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        if (isShipper) {
            setContentView(R.layout.activity_shipper_register);
            initShipperView();
            initValidationShipperForm();
        } else {
            setContentView(R.layout.activity_shop_register);
            initShopView();
            initValidationShopForm();
            initAutocompleteAddress();
        }

        loadingDlg = Utils.setupLoadingDialog(this);
        // find the radiobutton by returned id
        //radioShipper = (RadioButton) findViewById(selectedId);
    }

    public void initView(){
        etName = (EditText) findViewById(R.id.et_name);
        etEmail = (EditText) findViewById(R.id.et_email);
        etPassword = (EditText) findViewById(R.id.et_password);
        etAddress = (DelayAutocompleteTextView) findViewById(R.id.et_address);
        etAddress.setLoadingIndicator((RotateLoading) findViewById(R.id.li_et_address));
        etPhone = (EditText) findViewById(R.id.et_phone);
        btnSubmit = (TextView) findViewById(R.id.btn_register_submit);
        scrollView = (ScrollView) findViewById(R.id.fiveship_form_input);
        bannerBackground = (ImageView) findViewById(R.id.register_banner_bg);
        avartarImage = (ImageView) findViewById(R.id.banner_up_image);
        cbProvision = (CheckBox) findViewById(R.id.cb_provision);
        etReferralCode = (EditText) findViewById(R.id.et_shipper_referral_code);

        findViewById(R.id.txt_provision).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_to_term_of_use_in_website)));
                startActivity(browserIntent);
            }
        });

    }

    public void initShopView() {
        initView();
        etShopAddressDetail = (EditText) findViewById(R.id.et_shop_address_detail);

        avartarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Utils.capturePictureProduct(RegisterActivity.this, getString(R.string.title_dialog_choose_shop_avatar));
                ImageProcess.capturePictureProduct(RegisterActivity.this, "Chọn ảnh đại diện");
            }
        });
    }

    public void initShipperView() {
        initView();

        etShipperLicenseNumber = (EditText) findViewById(R.id.et_shipper_license);

        btnSubmit = (TextView) findViewById(R.id.btn_register_submit);
//        bannerBackground = (ImageView) findViewById(R.id.tap_to_up);

        avartarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Utils.capturePictureProduct(RegisterActivity.this, getString(R.string.title_dialog_choose_shipper_motobike));
                ImageProcess.capturePictureProduct(RegisterActivity.this, "Chọn ảnh đại diện");
            }
        });
    }

    public void initAutocompleteAddress() {

        // Register a listener that receives callbacks when a suggestion has been selected

        if(etAddress != null){
            etAddress.setOnItemClickListener(mAutocompleteClickListener);
            etAddress.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    addressCoordinates = null;
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            mAdapter = new PlacesAutoCompleteAdapter(this, mGoogleApiClient, RegisterActivity.BOUNDS_GREATER_VIETNAM,
                    null);
            etAddress.setAdapter(mAdapter);
        }

    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details rootView on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
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
            addressCoordinates = place.getLatLng();

            places.release();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Utils.REQUEST_IMAGE_CAPTURE:
                    Bitmap bitmap = ImageProcess.decodeResizeImage(600);
                    avartarImage.setImageBitmap(bitmap);
                    encodedImage = ImageProcess.bitmapToBase64(bitmap, 100);
                    break;
                case Utils.SELECT_FILE:
                    Uri selectedImageUri = data.getData();
                    String[] projection = {MediaStore.MediaColumns.DATA};
                    Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();
                    ImageProcess.mFilePath = cursor.getString(column_index);
                    Bitmap bm = ImageProcess.decodeResizeImage(600);
                    avartarImage.setImageBitmap(bm);
                    encodedImage = ImageProcess.bitmapToBase64(bm, 100);
                    break;
            }
        }
    }

    /*
    * Validation's register form
    * */
    public void initValidation(){

        mForm.addField(Field.using(etName).validate(NotEmpty.build(this, "Bạn vui lòng nhập tên")));

        mForm.addField(Field.using(etEmail)
                .validate(NotEmpty.build(this, getString(R.string.email_field)))
                .validate(IsEmail.build(this)));

        mForm.addField(Field.using(etPassword)
                .validate(NotEmpty.build(this, getString(R.string.password_field)))
                .validate(InRange.build(this, getString(R.string.password_field), 6, 26)));


        mForm.addField(Field.using(etAddress).validate(NotEmpty.build(this, getString(R.string.address_field))));

        mForm.addField(Field.using(etPhone)
                .validate(NotEmpty.build(this, getString(R.string.phone_field)))
                .validate(IsPhoneNumber.build(this)));
    }

    private void initValidationShopForm() {
        mForm = new Form(this);

        initValidation();

        submission();

    }

    private void initValidationShipperForm() {
        mForm = new Form(this);

        initValidation();

        mForm.addField(Field.using(etShipperLicenseNumber).validate(NotEmpty.build(this, getString(R.string.license_motobike_field))));

        submission();

    }

    /*
    * Validation coordinates address
    * */
    public boolean checkNullAddressCoordinates(){
        final TextInputLayout addressInputLayout = (TextInputLayout) etAddress.getParent();
        if(addressCoordinates == null) {

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (scrollView != null) {
                        scrollView.smoothScrollTo(0, Utils.getRelativeTop(etAddress) - etAddress.getHeight() - 125);
                    }

                    etAddress.requestFocus();
                    etAddress.getBackground().setColorFilter(etAddress.getResources().getColor(R.color.fiveship_text_red), PorterDuff.Mode.SRC_ATOP);
                    addressInputLayout.setError(getString(R.string.suggest_to_use_place_by_google));
                    addressInputLayout.setErrorEnabled(true);
                }
            });
            return false;
        }else{
            addressInputLayout.setErrorEnabled(false);
            return true;
        }
    }

    /*
    * end
    * */

    /*
    * Submit
    * */
    public void submission(){
        final OnRegisterCompleted callback = new OnRegisterCompleted() {
            @Override
            public void onRegisterCompleted(boolean isNull, CustomerItem cus, String message) {
                if (!isNull) {

                    sessionManager.createLoginSessionCustomer(cus.getId(),
                            cus.getShipperId(),
                            cus.getShopId(),
                            cus.getFullName(),
                            cus.getAvatar(),
                            cus.getAvatarLabel(),
                            cus.getReferralCode(),
                            cus.isShipper());

                    if (cus.isShipper()) {

                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();

                    }

                    if (cus.isShop()) {

                        sessionManager.setXPoint(cus.getXPoint());
                        sessionManager.setYPoint(cus.getYPoint());

                        Intent mainIntent = new Intent(RegisterActivity.this, ShopActivity.class);
                        startActivity(mainIntent);
                        finish();
                    }

                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                }
                loadingDlg.dismiss();
            }
        };

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mForm.isValid()) {
                    if(isShipper || checkNullAddressCoordinates()) { //check address coordinates has value

                        if(!cbProvision.isChecked()){
                            Toast.makeText(getApplication(), "Bạn vui lòng đồng ý điều khoản sử dụng", Toast.LENGTH_LONG).show();
                            return;
                        }

                        loadingDlg.show();

                        new Register(
                                RegisterActivity.this,
                                callback,
                                etName != null ? etName.getText().toString() : "",
                                etPassword.getText().toString(),
                                etEmail.getText().toString(),
                                etAddress.getText().toString(),
                                addressCoordinates != null ? String.valueOf(addressCoordinates.latitude): "",
                                addressCoordinates != null ? String.valueOf(addressCoordinates.longitude) : "",
                                etPhone.getText().toString(),
                                provider,
                                isShipper,
                                etShopAddressDetail != null ? etShopAddressDetail.getText().toString() : "",
                                etShipperLicenseNumber != null ? etShipperLicenseNumber.getText().toString() : "",
                                encodedImage,
                                "",
                                etReferralCode.getText().toString()).execute();
                    }
                }
            }
        });
    }

    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    @Override
    public void onBackPressed() {
        Intent helloIntent = new Intent(RegisterActivity.this, HelloActivity.class);
        startActivity(helloIntent);
        finish();
    }
}


package fiveship.vn.fiveship.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.android.gms.maps.model.LatLng;
import com.makeramen.roundedimageview.RoundedImageView;
import com.victor.loading.rotate.RotateLoading;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.adapter.PlacesAutoCompleteAdapter;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.service.ShipperService;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.ImageProcess;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.validator.Field;
import fiveship.vn.fiveship.utility.validator.Form;
import fiveship.vn.fiveship.utility.validator.validation.IsPhoneNumber;
import fiveship.vn.fiveship.utility.validator.validation.NotEmpty;
import fiveship.vn.fiveship.widget.DelayAutocompleteTextView;

public class EditCustomerInfoActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    SessionManager sessionManager;
    EditText etPhone;
    EditText etName;
    EditText etAddress;
    EditText etMotorId;
    EditText etShopCarer;

    EditText shopName;
    EditText shopPhone;
    EditText shopAddressDetail;
    DelayAutocompleteTextView shopAddress;
    RoundedImageView cusImage;

    TextView lblEdtName;
    TextView totalShip;
    TextView totalShipSuccess;

    Button btnSubmit;
    CustomerItem cus;

    Form mForm;

    Dialog loadingDlg;
    Dialog alertDlg;
    LatLng addressCoordinates;

    String encodedImage;

    ShipperService shipperService;
    ShopService shopService;

    Boolean isFocusUpdate = false;

    protected GoogleApiClient mGoogleApiClient;
    private PlacesAutoCompleteAdapter mAdapter;
    public static final String TAG = "EditCustomerInfoActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFocusUpdate = getIntent().getBooleanExtra("isFocusUpdate", false);

        sessionManager = new SessionManager(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        if(sessionManager.isShipper()) {

            setContentView(R.layout.activity_edit_shipper_info);

        }else{

            setContentView(R.layout.activity_edit_shop_info);

        }

        cus = sessionManager.getCustomerInfo();

        shipperService = ShipperService.get_instance(this);

        shopService = ShopService.get_instance(this);

        initView();

        initAutocompleteAddress();

        initValidate();

        setupToolBar();

        new GetCustomerInfo().execute(String.valueOf(sessionManager.isShipper() ? sessionManager.getShipperId() : sessionManager.getShopId()), "0");
    }

    @Override
    public void onBackPressed() {
        if (!isFocusUpdate) {
            finish();
        }

        if(isFocusUpdate){
            sessionManager.logoutUser();
            //Toast.makeText(getApplicationContext(), "Bạn vui lòng cập nhật thông tin", Toast.LENGTH_LONG).show();
        }
    }

    public void initView(){

        loadingDlg = Utils.setupLoadingDialog(this);
        alertDlg = Utils.setupAlertDialog(this);
        alertDlg.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDlg.dismiss();
            }
        });
        cusImage = (RoundedImageView) findViewById(R.id.cus_image);

        cusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageProcess.capturePictureProduct(EditCustomerInfoActivity.this, "Chọn ảnh đại diện");
            }
        });

        etPhone = (EditText) findViewById(R.id.et_phone);
        etAddress = (EditText) findViewById(R.id.et_address);
        etName = (EditText) findViewById(R.id.et_full_name);
        etMotorId = (EditText) findViewById(R.id.et_motor_id);
        btnSubmit = (Button) findViewById(R.id.btn_update);

        etShopCarer = (EditText) findViewById(R.id.et_shop_carer);
        shopName = (EditText) findViewById(R.id.et_shop_full_name);
        shopPhone = (EditText) findViewById(R.id.et_shop_phone);
        shopAddressDetail = (EditText) findViewById(R.id.et_shop_address_detail);
        shopAddress = (DelayAutocompleteTextView) findViewById(R.id.et_shop_address);

        lblEdtName = (TextView) findViewById(R.id.lbl_edt_name);
        totalShip = (TextView) findViewById(R.id.total_ship);
        totalShipSuccess = (TextView) findViewById(R.id.total_ship_success);

        findViewById(R.id.btn_show_change_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangePasswordDialogFragment changePass = ChangePasswordDialogFragment.getInstance(sessionManager.getCustomerInfo().getId());
                changePass.show(getSupportFragmentManager(), "Change password");
            }
        });

        if(isFocusUpdate){
            findViewById(R.id.et_message).setVisibility(View.VISIBLE);
        }

    }

    public void initValidate(){
        mForm = new Form(this);

        if(sessionManager.isShipper()){
            mForm.addField(Field.using(etName).validate(NotEmpty.build(this, getString(R.string.full_name_field))));

            mForm.addField(Field.using(etPhone)
                    .validate(NotEmpty.build(this, "số điện thoại"))
                    .validate(IsPhoneNumber.build(this)));
            mForm.addField(Field.using(etAddress).validate(NotEmpty.build(this, "địa chỉ")));
        }

        if(!sessionManager.isShipper()){
            mForm.addField(Field.using(shopName).validate(NotEmpty.build(this, "tên shop")));

            mForm.addField(Field.using(shopPhone)
                    .validate(NotEmpty.build(this, "số điện thoại"))
                    .validate(IsPhoneNumber.build(this)));

            mForm.addField(Field.using(shopAddress).validate(NotEmpty.build(this, "địa chỉ")));
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mForm.isValid()) {
                    if (sessionManager.isShipper()) {

                        loadingDlg.show();

                        new UpdateShipperInfo(
                                cus.getId(),
                                sessionManager.getShipperId(),
                                etName.getText().toString(),
                                etPhone.getText().toString(),
                                etAddress.getText().toString(),
                                addressCoordinates != null ? String.valueOf(addressCoordinates.latitude) : "",
                                addressCoordinates != null ? String.valueOf(addressCoordinates.longitude) : "",
                                etMotorId.getText() != null ? etMotorId.getText().toString() : "",
                                encodedImage).execute();
                    } else {

                        String shopX = addressCoordinates != null ? String.valueOf(addressCoordinates.latitude) : sessionManager.getXPoint();

                        String shopY = addressCoordinates != null ? String.valueOf(addressCoordinates.longitude) : sessionManager.getYPoint();

                        if(shopX.equals("null") || shopX.length() < 1 || shopY.equals("null") || shopY.length() < 1){
                            Toast.makeText(EditCustomerInfoActivity.this, "Bạn vui lòng sủ dụng địa chỉ Google gợi ý", Toast.LENGTH_LONG).show();
                            return;
                        }

                        loadingDlg.show();

                        new UpdateShopInfo(
                                cus.getId(),
                                sessionManager.getShopId(),
                                encodedImage,
                                shopName.getText().toString(),
                                shopPhone.getText().toString(),
                                shopAddress.getText().toString(),
                                shopName.getText().toString(),
                                shopPhone.getText().toString(),
                                shopAddressDetail.getText().toString(),
                                shopAddress.getText().toString(),
                                shopX,
                                shopY,
                                etShopCarer.getText() != null ? etShopCarer.getText().toString() : "").execute();
                    }
                }
            }
        });

    }

    public void initAutocompleteAddress() {

        // Register a listener that receives callbacks when a suggestion has been selected

        if(shopAddress != null){
            shopAddress.setOnItemClickListener(mAutocompleteClickListener);
            shopAddress.setLoadingIndicator((RotateLoading) findViewById(R.id.li_et_address));
            mAdapter = new PlacesAutoCompleteAdapter(this, mGoogleApiClient, RegisterActivity.BOUNDS_GREATER_VIETNAM,
                    null);
            shopAddress.setAdapter(mAdapter);
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
                    cusImage.setImageBitmap(bitmap);
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
                    cusImage.setImageBitmap(bm);
                    encodedImage = ImageProcess.bitmapToBase64(bm, 100);
                    break;
            }
        }
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

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(isFocusUpdate){
            //Toast.makeText(getApplicationContext(), "Bạn vui lòng cập nhật thông tin", Toast.LENGTH_LONG).show();
            sessionManager.logoutUser();
        }

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home && !isFocusUpdate) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void setupToolBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private class UpdateShipperInfo extends AsyncTask<String, Void, JSONObject> {
        private ShipperService service;
        int accountId;
        int shipperId;
        String name;
        String sendPhone;
        String sendAddress;
        String sendX;
        String sendY;
        String motorId;
        String licenseMotor;
        String avatar;

        public UpdateShipperInfo(int accountId, int shipperId, String name, String sendPhone, String sendAddress, String sendX, String sendY, String motorId, String avatar) {
            this.service = ShipperService.get_instance(EditCustomerInfoActivity.this);
            this.accountId = accountId;
            this.shipperId = shipperId;
            this.name = name;
            this.sendPhone = sendPhone;
            this.sendAddress = sendAddress;
            this.sendX = sendX;
            this.sendY = sendY;
            this.motorId = motorId;
            this.avatar = avatar;
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            return service.UpdateShipperInfo(accountId, shipperId, name, sendPhone, sendAddress, sendX, sendY, motorId, licenseMotor, avatar);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                loadingDlg.dismiss();

                Toast.makeText(EditCustomerInfoActivity.this, result.getString("message"), Toast.LENGTH_LONG).show();

                if (result != null && !result.getBoolean("error")) {

                    isFocusUpdate = false;

                    sessionManager.setName(name);

                    if(result.getBoolean("hasAvatar")){
                        sessionManager.setAvatar(result.getString("urlAvatar"));
                    }

                    Intent callback = new Intent(Config.RECEIVE_UPDATE_NAVIGATOR);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(callback);

                    finish();

                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(EditCustomerInfoActivity.this, "Có lỗi xảy ra! Bạn vui lòng thử lại.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class UpdateShopInfo extends AsyncTask<String, Void, JSONObject> {
        private ShopService service;
        int accountId;
        int shopId;
        String name;
        String phone;
        String address;
        String shopName;
        String shopPhone;
        String shopAddressDetail;
        String shopAddress;
        String shopX;
        String shopY;
        String carer;
        String avatar;

        public UpdateShopInfo(int accountId, int shopId, String avatar, String name, String phone, String address, String shopName, String shopPhone, String shopAddressDetail, String shopAddress, String shopX, String shopY, String shopCarer) {
            this.service = ShopService.get_instance(EditCustomerInfoActivity.this);
            this.accountId = accountId;
            this.shopId = shopId;
            this.avatar = avatar;
            this.name = name;
            this.phone = phone;
            this.address = address;
            this.shopName = shopName;
            this.shopPhone = shopPhone;
            this.shopAddressDetail = shopAddressDetail;
            this.shopAddress = shopAddress;
            this.shopX = shopX;
            this.shopY = shopY;
            this.carer = shopCarer;
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            return service.UpdateShopInfo(accountId, shopId, name, phone, address, shopName, shopPhone, shopAddressDetail, shopAddress, shopX, shopY, carer, avatar);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                Toast.makeText(EditCustomerInfoActivity.this, result.getString("message"), Toast.LENGTH_LONG).show();

                loadingDlg.dismiss();

                if (result != null && !result.getBoolean("error")) {

                    isFocusUpdate = false;

                    sessionManager.setName(name);

                    if(result.getBoolean("hasAvatar")){
                        sessionManager.setAvatar(result.getString("urlAvatar"));
                    }

                    if(addressCoordinates != null)
                    {
                        sessionManager.setXPoint(String.valueOf(addressCoordinates.latitude));
                        sessionManager.setYPoint(String.valueOf(addressCoordinates.longitude));
                    }

                    Intent callback = new Intent(Config.RECEIVE_UPDATE_NAVIGATOR);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(callback);

                    finish();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(EditCustomerInfoActivity.this, "Có lỗi xảy ra! Bạn vui lòng thử lại.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class GetCustomerInfo extends AsyncTask<String, Integer, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {

            return sessionManager.isShipper() ? shipperService.getShipperInfo(params[0], params[1]) : shopService.getShopInfo(params[0]);
        }

        @Override
        protected void onPreExecute (){
            loadingDlg.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                loadingDlg.dismiss();

                if (!result.getBoolean("error")) {

                    lblEdtName.setText(result.getJSONObject("data").getString("Name"));
                    Utils.setImageToImageView(getApplicationContext(), result.getJSONObject("data").getString("UrlAvatar"), cusImage);
                    if(sessionManager.isShipper()){
                        etName.setText(result.getJSONObject("data").getString("Name"));
                        etAddress.setText(result.getJSONObject("data").getString("Address"));
                        etPhone.setText(result.getJSONObject("data").getString("Phone"));
                        etMotorId.setText(result.getJSONObject("data").getString("MotorId"));
                        totalShip.setText("Tổng đơn: " + result.getJSONObject("data").getString("NumberShip"));
                        totalShipSuccess.setText("Thành công: " + result.getJSONObject("data").getString("NumberShipSuccess"));
                    }

                    if(!sessionManager.isShipper()){
                        shopName.setText(result.getJSONObject("data").getString("ShopName"));
                        shopPhone.setText(result.getJSONObject("data").getString("ShopPhone"));
                        shopAddressDetail.setText(result.getJSONObject("data").getString("ShopHome"));
                        shopAddress.setText(result.getJSONObject("data").getString("ShopAddress"));
                        etShopCarer.setText(result.getJSONObject("data").getString("ShopCarrer"));
                        totalShip.setText("Đã đăng: " + result.getJSONObject("data").getString("NumberShip"));
                        totalShipSuccess.setText("Thành công: " + result.getJSONObject("data").getString("NumberShipSuccess"));
                        sessionManager.setXPoint(result.getJSONObject("data").getString("ShopX"));
                        sessionManager.setYPoint(result.getJSONObject("data").getString("ShopY"));
                    }
                }

                if (result.getBoolean("error")) {
                    Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Có lỗi xảy ra. Bạn vui lòng thử lại!", Toast.LENGTH_LONG).show();
            }
        }
    }
}

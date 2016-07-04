package fiveship.vn.fiveship.activity;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.shipper.MainActivity;
import fiveship.vn.fiveship.activity.shop.ShopActivity;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.model.ShipperItem;
import fiveship.vn.fiveship.model.ShopItem;
import fiveship.vn.fiveship.other.AlarmReceiver;
import fiveship.vn.fiveship.service.CustomerService;
import fiveship.vn.fiveship.service.LoginRegisterService;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.validator.Field;
import fiveship.vn.fiveship.utility.validator.Form;
import fiveship.vn.fiveship.utility.validator.validation.InRange;
import fiveship.vn.fiveship.utility.validator.validation.IsEmail;
import fiveship.vn.fiveship.utility.validator.validation.NotEmpty;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    private CallbackManager callbackManager;
    private CustomerItem customer;
    private ShipperItem shipper;
    private ShopItem shop;
    private Dialog dialog;
    private Context context;
    private SessionManager sessionManager;

    private Form mForm;
    private EditText etEmail;
    private EditText etPassword;
    private Dialog loadingDlg;
    private Dialog alertDlg;
    private LoginButton btnLoginFB;
    private Dialog forgotPasswordDialog;
    private TextView btnSigninFB;
    private boolean exit = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getApplicationContext());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        context = this;

        sessionManager = new SessionManager(context);

        callbackManager = CallbackManager.Factory.create();

        dialog = Utils.setupLoadingDialog(this);
        /*
        * LOGIN FIVE SHIP SYSTEM
        * */
        loadingDlg = Utils.setupLoadingDialog(this);
        alertDlg = Utils.setupAlertDialog(this);

        findViewById(R.id.forgot_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initForgotPasswordDialog();
            }
        });

        initLoginForm();
        /*
        * LOGIN VIA FACEBOOK
        * */
        btnLoginFB = (LoginButton) findViewById(R.id.signin_facebook);
        //btnLoginFB.setOnClickListener(this);
        btnSigninFB = (TextView) findViewById(R.id.sign_in_facebook);
        btnSigninFB.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_link:
                Intent registerIntent = new Intent(LoginActivity.this, HelloActivity.class);
                startActivity(registerIntent);
                finish();
                break;
            case R.id.btn_login:
                LoginByFiveShip();
                break;
            case R.id.sign_in_facebook:
                LoginViaFb();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Sign-in into facebook
     */
    public void LoginViaFb() {

        try {
            LoginManager.getInstance().logOut();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        btnLoginFB.performClick();
        btnLoginFB.setReadPermissions(Arrays.asList("email", "public_profile", "user_location"));
        // Other app specific specialization

        // Callback registration
        btnLoginFB.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (loginResult != null) {
                    dialog.show();
                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(final JSONObject user, GraphResponse response) {
                                    //Login by 5ship
                                    try {
                                        final JSONObject objUser = user;
                                        onLoginCompleted callback = new onLoginCompleted() {
                                            @Override
                                            public void onLoginCompleted(boolean isNull, ShipperItem shipperItem) {
                                                if (!isNull) {
                                                    shipper = shipperItem;
                                                    createAuthenticationPref(true);
                                                } else {
                                                    HashMap<String, String> userInfo = new HashMap<>();
                                                    try {
                                                        userInfo.put("id", objUser.getString("id"));
                                                        userInfo.put("name", objUser.getString("name"));
                                                        userInfo.put("email", objUser.has("email") ? objUser.getString("email") : "");
                                                        userInfo.put("gender", String.valueOf(objUser.has("gender") ? objUser.getString("gender") : "male"));
                                                        userInfo.put("address", objUser.has("user_location") ? objUser.getString("user_location") : "");
                                                        redirectRegister(userInfo);
                                                    } catch (Exception e) {

                                                        e.printStackTrace();
                                                    }
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onLoginCompleted(boolean isNull, ShopItem shopItem) {
                                                if (!isNull) {
                                                    shop = shopItem;
                                                    createAuthenticationPref(false);
                                                } else {
                                                    HashMap<String, String> userInfo = new HashMap<>();
                                                    try {
                                                        userInfo.put("id", objUser.getString("id"));
                                                        userInfo.put("name", objUser.getString("name"));
                                                        userInfo.put("email", objUser.has("email") ? objUser.getString("email") : "");
                                                        userInfo.put("gender", String.valueOf(objUser.has("gender") ? objUser.getString("gender") : "male"));
                                                        userInfo.put("address", objUser.has("user_location") ? objUser.getString("user_location") : "");
                                                        redirectRegister(userInfo);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                dialog.dismiss();
                                            }
                                        };

                                        new Login(callback,user.getString("id"), objUser.has("email") ? user.getString("email") : "", "facebook_default_password", "facebook").execute();

                                    } catch (Exception e) {
                                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_facebook_fail), Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }

                                    Log.v("LoginActivity", response.toString());
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,gender");
                    request.setParameters(parameters);
                    request.executeAsync();
                }else {
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_facebook_fail), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_cancel), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_facebook_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sign-in into five ship system
     */

    public void initLoginForm() {

        findViewById(R.id.register_link).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);

        mForm = new Form(this);

        etEmail = (EditText) findViewById(R.id.et_email);
        etPassword = (EditText) findViewById(R.id.et_password);

        etPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    findViewById(R.id.btn_login).performClick();
                    return true;
                }
                return false;
            }
        });

        mForm.addField(Field.using(etEmail)
                .validate(NotEmpty.build(this, getString(R.string.email_field)))
                .validate(IsEmail.build(this)));

        mForm.addField(Field.using(etPassword)
                .validate(NotEmpty.build(this, getString(R.string.password_field)))
                .validate(InRange.build(this, getString(R.string.password_field), 6, 26)));

    }

    public void LoginByFiveShip() {

        final onLoginCompleted listen = new onLoginCompleted() {
            @Override
            public void onLoginCompleted(boolean isNull, ShipperItem shipperItem) {

                loadingDlg.dismiss();

                if (!isNull) {
                    shipper = shipperItem;
                    createAuthenticationPref(true);
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLoginCompleted(boolean isNull, ShopItem shopItem) {
                loadingDlg.dismiss();

                if (!isNull) {
                    shop = shopItem;
                    createAuthenticationPref(false);
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                }
            }
        };

        if (mForm.isValid()) {
            loadingDlg.show();
            new Login(listen, "", etEmail.getText().toString(), etPassword.getText().toString(), "5ship").execute();
        }
    }

    /*
    * CREATE: stored user info in to Shared preferences
    * */
    public void createAuthenticationPref(boolean isShipper) {

        sessionManager.createLoginSessionCustomer(customer.getId(),
                isShipper ? shipper.getId() : 0,
                isShipper ? 0 : shop.getId(),
                customer.getFullName(),
                customer.getAvatar(),
                customer.getAvatarLabel(),
                customer.getReferralCode(),
                isShipper);

        if (isShipper) {
            //start schedule run to post location every specific time
            Intent alarmIntent = new Intent(this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Calendar c = Calendar.getInstance();
            manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 30000, pendingIntent);

            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        } else {
            sessionManager.setXPoint(customer.getXPoint());
            sessionManager.setYPoint(customer.getYPoint());
            Intent mainIntent = new Intent(LoginActivity.this, ShopActivity.class);
            startActivity(mainIntent);
            finish();
        }

    }

    //REDIRECT: redirect to register activity attach user info
    public void redirectRegister(HashMap<String, String> info) {
        Bundle userInfo = new Bundle();
        Intent regIntent = new Intent(LoginActivity.this, HelloActivity.class);
        userInfo.putSerializable("userInfo", info);
        regIntent.putExtras(userInfo);
        startActivity(regIntent);
    }

    public void initForgotPasswordDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.forgot_password_dialog, null);

        final Form form = new Form(this);

        final EditText emailEd = (EditText) dialogView.findViewById(R.id.fg_email);

        form.addField(Field.using(emailEd)
                .validate(NotEmpty.build(this, getString(R.string.email_field)))
                .validate(IsEmail.build(this)));

        builder.setView(dialogView);

        dialogView.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPasswordDialog.dismiss();
            }
        });

        dialogView.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (form.isValid()) {
                    forgotPasswordDialog.dismiss();
                    loadingDlg.show();
                    new ForgotPassword(new OnSendRequestCompleted() {
                        @Override
                        public void onSendRequestCompleted(boolean error, String message) {
                            loadingDlg.dismiss();
                            ((TextView) alertDlg.findViewById(R.id.tv_content)).setText(message);
                            alertDlg.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDlg.dismiss();
                                }
                            });
                            alertDlg.show();
                        }
                    }, emailEd.getText().toString()).execute();
                }
            }
        });

        builder.create();

        forgotPasswordDialog = builder.show();

        forgotPasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            finish();
        }

        this.exit = true;
        Toast.makeText(this, getString(R.string.exit_message), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                exit = false;
            }
        }, 2000);
    }


    private class Login extends AsyncTask<String, Void, JSONObject> {
        private onLoginCompleted listener;
        private LoginRegisterService service;
        String username;
        String password;
        String provider;
        String oauthId;
        ShipperItem shipper;
        ShopItem shop;


        public Login(onLoginCompleted lstn,String oauthId, String usn, String pwd, String pvd) {
            service = LoginRegisterService.get_instance(LoginActivity.this);
            listener = lstn;
            username = usn;
            password = pwd;
            this.provider = pvd;
            this.oauthId = oauthId;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return service.Login(oauthId, username, password, provider);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null && !result.getBoolean("error")) {

                    if (result.has("data") && !result.getString("data").isEmpty()) {

                        result = new JSONObject(result.getString("data"));

                    }

                    customer = new CustomerItem();
                    customer.setId(result.getInt("Id"));
                    customer.setAvatar(result.getString("Avatar"));
                    customer.setAvatarLabel(result.getString("AvatarLabel"));
                    customer.setFirstName(result.getString("FirstName"));
                    customer.setLastName(result.getString("LastName"));
                    customer.setPhone(result.getString("Phone"));
                    customer.setEmail(result.getString("Email"));
                    customer.setAddress(result.getString("Address"));
                    customer.setXPoint(result.getString("XPoint"));
                    customer.setYPoint(result.getString("YPoint"));
                    customer.setReferralCode(result.getString("ReferralCode"));

                    if (result.getBoolean("IsShipper")) {
                        shipper = new ShipperItem();
                        shipper.setId(result.getInt("ShipperId"));
                        shipper.setMotorId(result.getJSONObject("Shipper").getString("MotorId"));
                        shipper.setMotorId(result.getJSONObject("Shipper").getString("LicenseMotor"));
                        listener.onLoginCompleted(false, shipper);
                    } else {
                        shop = new ShopItem();
                        shop.setId(result.getInt("ShopId"));
                        shop.setName(result.getJSONObject("Shop").getString("Name"));
                        shop.setPhone(result.getJSONObject("Shop").getString("Phone"));
                        shop.setAddressDetail(result.getJSONObject("Shop").getString("AddressDetail"));
                        shop.setAddress(result.getJSONObject("Shop").getString("Address"));
                        shop.setCarrer(result.getJSONObject("Shop").getString("Carrer"));
                        listener.onLoginCompleted(false, shop);
                    }

                } else {
                    listener.onLoginCompleted(true, shipper);
                    listener.onLoginCompleted(true, shop);
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onLoginCompleted(true, shipper);
                listener.onLoginCompleted(true, shop);
            }
        }
    }

    private class ForgotPassword extends AsyncTask<String, Void, JSONObject> {
        private OnSendRequestCompleted listener;
        private CustomerService service;
        String email;

        public ForgotPassword(OnSendRequestCompleted listener, String email) {
            this.email = email;
            this.listener = listener;
            service = CustomerService.get_instance(LoginActivity.this);
        }


        @Override
        protected JSONObject doInBackground(String... params) {
            return service.forgotPassword(email);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                listener.onSendRequestCompleted(result != null && result.getBoolean("error"), result.getString("message"));

            } catch (JSONException e) {
                e.printStackTrace();
                listener.onSendRequestCompleted(true, getString(R.string.exception_message));
            }
        }

    }

    interface onLoginCompleted {
        void onLoginCompleted(boolean isNull, ShipperItem shipperItem);

        void onLoginCompleted(boolean isNull, ShopItem shopItem);
    }
}

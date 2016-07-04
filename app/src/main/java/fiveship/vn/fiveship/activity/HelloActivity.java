package fiveship.vn.fiveship.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.shipper.MainActivity;
import fiveship.vn.fiveship.activity.shop.ShopActivity;
import fiveship.vn.fiveship.interfaces.OnRegisterCompleted;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.service.adapter.Register;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

public class HelloActivity extends ActionBarActivity {

    SessionManager sessionManager;
    Dialog loadingDlg;
    Bundle info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hello);

        info = getIntent().getExtras();

        final Intent intentRegister = new Intent(this, RegisterActivity.class);

        sessionManager = new SessionManager(this);
        loadingDlg = Utils.setupLoadingDialog(this);

        findViewById(R.id.btn_back_chosen_type).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backLogin();
            }
        });

        findViewById(R.id.is_shipper_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info != null && info.containsKey("userInfo")) {
                    register(true);
                }else {
                    intentRegister.putExtra("isShipper", true);
                    startActivity(intentRegister);
                    finish();
                }
            }
        });

        findViewById(R.id.is_shop_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info != null && info.containsKey("userInfo")) {
                    register(false);
                }else {
                    intentRegister.putExtra("isShipper", false);
                    startActivity(intentRegister);
                    finish();
                }
            }
        });
    }
    public void register(boolean isShipper){
        loadingDlg.show();
        final OnRegisterCompleted callback = new OnRegisterCompleted() {
            @Override
            public void onRegisterCompleted(boolean isNull, CustomerItem cus, String message) {
                if (!isNull) {
                    loadingDlg.dismiss();

                    sessionManager.createLoginSessionCustomer(cus.getId(),
                            cus.getShipperId(),
                            cus.getShopId(),
                            cus.getFullName(),
                            cus.getAvatar(),
                            cus.getAvatarLabel(),
                            cus.getReferralCode(),
                            cus.isShipper());

                    if (cus.isShipper()) {

                        Intent mainIntent = new Intent(HelloActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();

                    }

                    if (cus.isShop()) {
                        sessionManager.setXPoint(cus.getXPoint());
                        sessionManager.setYPoint(cus.getYPoint());

                        Intent mainIntent = new Intent(HelloActivity.this, ShopActivity.class);
                        startActivity(mainIntent);
                        finish();
                    }

                    finish();
                } else {
                    Toast.makeText(HelloActivity.this, message, Toast.LENGTH_LONG).show();
                }
                loadingDlg.dismiss();
            }
        };

        HashMap userInfo = (HashMap) info.getSerializable("userInfo");

        new Register(this, callback,
                userInfo.get("name").toString(),
                "facebook_default_password",
                userInfo.get("email").toString(),
                userInfo.get("address").toString(),
                "",
                "",
                "",
                "facebook",
                isShipper,
                "",
                "",
                "",
                userInfo.get("id").toString(),
                "").execute();
    }

    private void backLogin(){
        Intent loginIntent = new Intent(HelloActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }


    @Override
    public void onBackPressed() {
        backLogin();
    }
}

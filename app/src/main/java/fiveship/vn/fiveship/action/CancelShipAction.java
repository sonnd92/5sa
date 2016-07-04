package fiveship.vn.fiveship.action;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by BVN on 08/11/2015.
 */
public class CancelShipAction implements View.OnClickListener {

    private Context mContext;
    private int mShipId;
    private int mShopId;
    private String mShipName;
    private ShippingOrderService mService;
    private Dialog mLoadingDialog;
    private CancelShipActionCallback mListener;
    private AlertDialog mDialog;
    private String mReason;
    private View mRootView;

    public CancelShipAction(Context context, int shipId, int shopId, String shipName) {
        this.mContext = context;
        this.mShipId = shipId;
        this.mShopId = shopId;
        this.mShipName = shipName;
        mService = ShippingOrderService.get_instance(context);
        mLoadingDialog = Utils.setupLoadingDialog(mContext);
    }

    @Override
    public void onClick(View v) {
        mRootView.findViewById(R.id.input_other_reason_box).setVisibility(View.GONE);
        if (v.getId() == R.id.rb_other_reason) {
            mRootView.findViewById(R.id.input_other_reason_box).setVisibility(View.VISIBLE);
        }
    }

    public interface CancelShipActionCallback {
        void callBackSuccess();
    }

    public void setCancelShipActionCallback(CancelShipActionCallback listener) {
        mListener = listener;
    }

    public void cancel() {

        // Use the Builder class for convenient mDialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        mRootView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.prompt_cancel_ship, null);

        builder.setView(mRootView);

        ((TextView) mRootView.findViewById(R.id.tv_shipping_order_name))
                .setText("Đơn hàng: " + mShipName);

        mRootView.findViewById(R.id.rb_1).setOnClickListener(this);
        mRootView.findViewById(R.id.rb_2).setOnClickListener(this);
        mRootView.findViewById(R.id.rb_3).setOnClickListener(this);
        mRootView.findViewById(R.id.rb_4).setOnClickListener(this);
        mRootView.findViewById(R.id.rb_other_reason).setOnClickListener(this);

        mRootView.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mRootView.findViewById(R.id.ic_close_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mRootView.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = ((RadioGroup) mRootView.findViewById(R.id.rg_select_reason)).getCheckedRadioButtonId();
                if(selectedId > 0) {
                    if (selectedId == R.id.rb_other_reason) {
                        mReason = ((EditText) mRootView.findViewById(R.id.tv_other_reason)).getText().toString();
                        if (mReason.isEmpty()) {
                            Toast.makeText(mContext, "Bạn vui lòng nhập lý do hủy", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        mReason = ((RadioButton) mRootView.findViewById(selectedId)).getText().toString();
                    }
                }else{
                    Toast.makeText(mContext, "Bạn vui cho chúng tôi biết lý do hủy đơn hàng của bạn", Toast.LENGTH_SHORT).show();
                    return;
                }
                mLoadingDialog.show();
                new CancelShipActionTask().execute(mReason);
                mDialog.dismiss();
            }
        });

        builder.create();

        mDialog = builder.show();

    }

    private class CancelShipActionTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return mService.cancelShip(mShopId, mShipId, params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                mLoadingDialog.dismiss();
                Toast.makeText(mContext, result.getString("message"), Toast.LENGTH_LONG).show();

                if (!result.getBoolean("error")) {
                    mListener.callBackSuccess();
                    Intent callback = new Intent(Config.RECEIVE_SHOP_LIST_SHIP);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(callback);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

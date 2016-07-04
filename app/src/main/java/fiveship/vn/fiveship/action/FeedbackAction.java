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
import fiveship.vn.fiveship.service.OtherService;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by Unstopable on 6/17/2016.
 */
public class FeedbackAction{
    private Context mContext;
    private int mShipId;
    private int mShopId;
    private int mShipperId;
    private String mShipName;
    private ShippingOrderService mService;
    private Dialog mLoadingDialog;
    private AlertDialog mDialog;
    private View mRootView;

    public FeedbackAction(Context context, int shipId, int shopId, int shipperId, String shipName) {
        this.mContext = context;
        this.mShipId = shipId;
        this.mShopId = shopId;
        this.mShipperId = shipperId;
        this.mShipName = shipName;
        mService = ShippingOrderService.get_instance(context);
        mLoadingDialog = Utils.setupLoadingDialog(mContext);
    }

    public void show() {

        // Use the Builder class for convenient mDialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        mRootView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.contact_and_support_dialog, null);

        builder.setView(mRootView);

        ((TextView) mRootView.findViewById(R.id.tv_shipping_order_name))
                .setText( mShipName);

        mRootView.findViewById(R.id.ic_close_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        mRootView.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String report  = ((EditText)mRootView.findViewById(R.id.et_comment_content)).getText().toString();
                String feedback  = ((EditText)mRootView.findViewById(R.id.et_comment_content)).getText().toString();

                if(report.isEmpty()){
                    Toast.makeText(mContext, "Bạn vui lòng nhập nội dung phản hồi", Toast.LENGTH_SHORT).show();
                    return;
                }
                mLoadingDialog.show();
                new SendCustomerFeedback().execute(report, feedback);
                mDialog.dismiss();
            }
        });

        builder.create();

        mDialog = builder.show();

    }

    private class SendCustomerFeedback extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return mService.sendCustomerFeedback(mShipId, mShopId, mShipperId, params[0], params[1]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                mLoadingDialog.dismiss();
                Toast.makeText(mContext, result.getString("message"), Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

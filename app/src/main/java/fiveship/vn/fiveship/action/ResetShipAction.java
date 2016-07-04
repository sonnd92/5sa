package fiveship.vn.fiveship.action;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
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
public class ResetShipAction {

    Context context;
    int shipId;
    int shopId;
    ShippingOrderService shippingOrderService;
    Dialog loadingDialog;
    ResetShipActionCallback mListener;
    AlertDialog dialogReset;
    NumberPicker txtNumberHour;

    public ResetShipAction(Context context, int shipId, int shopId){
        this.context = context;
        this.shipId = shipId;
        this.shopId = shopId;
        shippingOrderService = ShippingOrderService.get_instance(context);
        loadingDialog = Utils.setupLoadingDialog(context);
    }

    public interface ResetShipActionCallback {
        void callBackSuccess(String date);
    }

    public void setResetShipActionCallback(ResetShipActionCallback listener) {
        mListener = listener;
    }

    public void reset(){

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View rootView = inflater.inflate(R.layout.fragment_reset, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Gia hạn đơn hàng");

        builder.setView(rootView);

        txtNumberHour = (NumberPicker)rootView.findViewById(R.id.txt_number_hour);
        txtNumberHour.setMinValue(1);
        txtNumberHour.setValue(1);
        txtNumberHour.setMaxValue(10);

        rootView.findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView txtShip = (TextView) rootView.findViewById(R.id.txt_reset_ship);

                new ResetShipActionTask().execute(txtShip.getText().toString(), String.valueOf(txtNumberHour.getValue()));

            }
        });

        rootView.findViewById(R.id.btn_close_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogReset.dismiss();
            }
        });

        builder.create();
        dialogReset = builder.show();
    }

    private class ResetShipActionTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return shippingOrderService.resetShip(shipId, shopId, params[0], params[1]);
        }

        @Override
        protected void onPreExecute() {
            loadingDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                loadingDialog.dismiss();
                Toast.makeText(context, result.getString("message"), Toast.LENGTH_LONG).show();

                if(!result.getBoolean("error")){
                    dialogReset.dismiss();
                    mListener.callBackSuccess(result.getString("date"));
                    Intent callback = new Intent(Config.RECEIVE_SHOP_LIST_SHIP);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(callback);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Có lỗi xảy ra bạn vui lòng thử lại", Toast.LENGTH_LONG).show();
            }
        }

    }

}

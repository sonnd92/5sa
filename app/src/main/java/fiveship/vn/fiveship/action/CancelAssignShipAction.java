package fiveship.vn.fiveship.action;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.service.ShipperService;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by BVN on 08/11/2015.
 */
public class CancelAssignShipAction {

    Context context;
    int shipId;
    int shipperId;
    String message;
    ShipperService shipperService;
    Dialog loadingDialog;
    CancelAssignShipActionCallback mListener;

    public CancelAssignShipAction(Context context, int shipId, int shipperId, String message){
        this.context = context;
        this.shipId = shipId;
        this.shipperId = shipperId;
        this.message = message;
        shipperService = ShipperService.get_instance(context);
        loadingDialog = Utils.setupLoadingDialog(context);
    }

    public interface CancelAssignShipActionCallback {
        void callBackSuccess();
    }

    public void setCancelAssignShipActionCallback(CancelAssignShipActionCallback listener) {
        mListener = listener;
    }

    public void cancel(){

        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the dialog title
        builder.setMessage(message)
                .setPositiveButton("Hủy đăng ký nhận đơn", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int id) {

                        loadingDialog.show();
                        new CancelAssignShipActionTask().execute();

                    }
                })
                .setNegativeButton("Đóng", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        builder.create();

        builder.show();

    }

    private class CancelAssignShipActionTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return shipperService.cancelAssignShip(shipperId, shipId);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                loadingDialog.dismiss();
                Toast.makeText(context, result.getString("message"), Toast.LENGTH_LONG).show();
                if(!result.getBoolean("error")){
                    mListener.callBackSuccess();
                    Intent callback = new Intent(Config.RECEIVE_SHIPPER_LIST_SHIP);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(callback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

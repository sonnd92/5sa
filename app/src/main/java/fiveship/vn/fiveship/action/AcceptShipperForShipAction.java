package fiveship.vn.fiveship.action;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by BVN on 08/11/2015.
 */
public class AcceptShipperForShipAction {

    Context context;
    int shopId;
    CustomerItem shipper;
    ShopService shopService;
    Dialog loadingDialog;
    AcceptShipperForShipActionCallback mListener;

    public AcceptShipperForShipAction(Context context, int shopId, CustomerItem shipper){
        this.context = context;
        this.shopId = shopId;
        this.shipper = shipper;
        shopService = ShopService.get_instance(context);
        loadingDialog = Utils.setupLoadingDialog(context);
    }

    public interface AcceptShipperForShipActionCallback {
        void callBackSuccess();
    }

    public void setAcceptShipperForShipActionCallback(AcceptShipperForShipActionCallback listener) {
        mListener = listener;
    }

    public void accept(){

        if(shipper.isConfirm()) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(shipper.getNote())
                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            loadingDialog.show();
                            new AcceptShipperForShipTask().execute();
                        }
                    })
                    .setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

            builder.create();

            builder.show();
        }else{
            final AlertDialog.Builder rBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.NonActivityDialog));

            final View dialogView = LayoutInflater.from(context)
                    .inflate(R.layout.warning_shipper_unidefined_popup_layout, null);
            rBuilder.setView(dialogView);

            ((TextView) dialogView.findViewById(R.id.shipper_name)).setText(shipper.getLastName());
            ((TextView) dialogView.findViewById(R.id.shipper_note)).setText(shipper.getNote());

            final AlertDialog dialog = rBuilder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialogView.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadingDialog.show();
                    dialog.dismiss();
                    new AcceptShipperForShipTask().execute();
                }
            });

            dialogView.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }

            });

            dialog.show();
        }

    }

    private class AcceptShipperForShipTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return shopService.acceptShipperForShip(shopId, shipper.getShipperId(), shipper.getShipId());
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                loadingDialog.dismiss();
                Toast.makeText(context, result.getString("message"), Toast.LENGTH_LONG).show();

                if(!result.getBoolean("error")){
                    mListener.callBackSuccess();
                    Intent callback = new Intent(Config.RECEIVE_SHOP_LIST_SHIP);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(callback);
                    if(result.has("link") && !result.getString("link").isEmpty()) {
                        Utils.shareJourneyDialog(context, result.getString("link")).show();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

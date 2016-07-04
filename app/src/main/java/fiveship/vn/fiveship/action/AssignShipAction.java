package fiveship.vn.fiveship.action;

import android.app.Dialog;
import android.content.Context;
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

import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShipperService;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.GPSTracker;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by BVN on 08/11/2015.
 */
public class AssignShipAction {

    private Context context;
    private ShippingOrderItem orderItem;
    private int shipperId;
    private int recommendCost;
    private String message;
    private boolean isReliableShop;
    private ShipperService shipperService;
    private Dialog loadingDialog;
    private AssignShipActionCallback mListener;
    private GPSTracker gpsTracker;

    public AssignShipAction(Context context, ShippingOrderItem orderItem, int shipperId, String message, boolean isReliableShop) {
        this.context = context;
        this.orderItem = orderItem;
        this.shipperId = shipperId;
        this.message = message;
        shipperService = ShipperService.get_instance(context);
        loadingDialog = Utils.setupLoadingDialog(context);
        gpsTracker = new GPSTracker(context);
        this.isReliableShop = isReliableShop;
        this.recommendCost = 0;
    }

    public AssignShipAction(Context context, ShippingOrderItem orderItem, int shipperId, String message, boolean isReliableShop, int recommendCost) {
        this.context = context;
        this.orderItem = orderItem;
        this.shipperId = shipperId;
        this.message = message;
        shipperService = ShipperService.get_instance(context);
        loadingDialog = Utils.setupLoadingDialog(context);
        gpsTracker = new GPSTracker(context);
        this.isReliableShop = isReliableShop;
        this.recommendCost = recommendCost;
    }

    public interface AssignShipActionCallback {
        void callBackSuccess();
    }

    public void setAssignShipActionCallback(AssignShipActionCallback listener) {
        mListener = listener;
    }

    public void assign() {
        if (recommendCost == 0) {
            final AlertDialog.Builder rBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.NonActivityDialog));

            final View dialogView = LayoutInflater.from(context)
                    .inflate(R.layout.warning_shop_undefined_popup_layout, null);

            if (!isReliableShop) {
                dialogView.findViewById(R.id.warning_for_reliable).setVisibility(View.VISIBLE);
            }

            ((TextView) dialogView.findViewById(R.id.order_name)).setText(orderItem.getName());

            if (orderItem.getDateEnd() != null && !orderItem.getDateEnd().isEmpty()) {
                ((TextView) dialogView.findViewById(R.id.order_end_time)).setText(orderItem.getDateEnd());
            } else {
                dialogView.findViewById(R.id.order_end_time_box).setVisibility(View.GONE);
            }

            if (orderItem.getDetails() != null && !orderItem.getDetails().isEmpty()) {
                ((TextView) dialogView.findViewById(R.id.order_note)).setText(orderItem.getDetails());
            } else {
                dialogView.findViewById(R.id.order_note_box).setVisibility(View.GONE);
            }

            rBuilder.setView(dialogView);
            final AlertDialog dialog = rBuilder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialogView.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadingDialog.show();
                    dialog.dismiss();
                    new AssignShipActionTask().execute();
                }
            });

            dialogView.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }

            });

            dialog.show();

        } else {

            loadingDialog.show();
            new AssignShipActionTask().execute();

        }
    }

    private class AssignShipActionTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return shipperService.ReceptionOrder(shipperId, orderItem.getId(), String.valueOf(gpsTracker.getLatitude()), String.valueOf(gpsTracker.getLongitude()), recommendCost);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                loadingDialog.dismiss();
                Toast.makeText(context, result.getString("message"), Toast.LENGTH_LONG).show();

                if (!result.getBoolean("error")) {

                    if (mListener != null) {
                        mListener.callBackSuccess();
                    }

                    Intent callback = new Intent(Config.RECEIVE_SHIPPER_LIST_SHIP);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(callback);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, context.getString(R.string.exception_message), Toast.LENGTH_LONG).show();
            }
        }
    }
}

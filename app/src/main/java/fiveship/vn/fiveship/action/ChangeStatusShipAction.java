package fiveship.vn.fiveship.action;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import org.json.JSONObject;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by BVN on 08/11/2015.
 */
public class ChangeStatusShipAction {

    Context context;
    int statusId;
    int shipId;
    int shopId;
    int shipperId;
    String message;
    ShippingOrderService shippingOrderService;
    Dialog loadingDialog;
    ChangeStatusShipActionCallback mListener;
    AlertDialog dialogRating;

    public ChangeStatusShipAction(Context context, ShippingOrderService service, int shipId, int shopId, int shipperId, int statusId, String message){
        this.context = context;
        this.statusId = statusId;
        this.shipId = shipId;
        this.shopId = shopId;
        this.shipperId = shipperId;
        this.message = message;
        this.shippingOrderService = service;
        loadingDialog = Utils.setupLoadingDialog(context);
    }

    public interface ChangeStatusShipActionCallback {
        void callBackSuccess();
    }

    public void setChangeStatusShipActionCallback(ChangeStatusShipActionCallback listener) {
        mListener = listener;
    }

    public void showRatingDialog(){

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View rootView = inflater.inflate(R.layout.fragment_rating, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Đánh giá shipper");

        builder.setView(rootView);

        rootView.findViewById(R.id.btn_send_rating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);

                EditText etComment = (EditText) rootView.findViewById(R.id.et_comment);

                CheckBox cbIsAmity = (CheckBox) rootView.findViewById(R.id.cb_shipper_amity);

                if(ratingBar.getRating() <= 3 && etComment.getText().toString().equals("null") && etComment.getText().toString().length() == 0){
                    Toast.makeText(context, "Bạn vui lòng nhập nhận xét", Toast.LENGTH_LONG).show();
                    return;
                }

                new RatingShipperAction(context, shipId, shopId, ratingBar.getRating(), etComment.getText().toString(), cbIsAmity.isChecked())
                        .post()
                        .setRatingActionCallback(new RatingShipperAction.RatingActionCallback() {
                            @Override
                            public void callBackSuccess(String message) {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                dialogRating.dismiss();
                            }
                        });
            }
        });

        rootView.findViewById(R.id.btn_close_rating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogRating.dismiss();
            }
        });

        builder.create();
        dialogRating = builder.show();
    }

    public void update(){

        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the dialog title
        builder.setMessage(message)
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int id) {

                        loadingDialog.show();
                        new UpdateStatusShipActionTask().execute();


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

    private class UpdateStatusShipActionTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            return shippingOrderService.updateStatusShip(shipId, shipperId, shopId, statusId);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                loadingDialog.dismiss();
                Toast.makeText(context, result.getString("message"), Toast.LENGTH_LONG).show();

                if(!result.getBoolean("error")){
                    mListener.callBackSuccess();
                    if(statusId == OrderStatusEnum.END.getStatusCode()){
                        showRatingDialog();
                    }
                    Intent callback = new Intent(Config.RECEIVE_SHOP_LIST_SHIP);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(callback);
                    Intent callback1 = new Intent(Config.RECEIVE_SHIPPER_LIST_SHIP);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(callback1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}

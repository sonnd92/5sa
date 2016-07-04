package fiveship.vn.fiveship.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.CancelShipAction;
import fiveship.vn.fiveship.action.ChangeStatusShipAction;
import fiveship.vn.fiveship.action.ResetShipAction;
import fiveship.vn.fiveship.activity.shop.ShopListShipperAssignShip;
import fiveship.vn.fiveship.activity.shop.ShopShipDetailActivity;
import fiveship.vn.fiveship.activity.shop.ShopTrackingShipperActivity;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.model.DeliveryToItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.service.adapter.ChangeShippingCost;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;

/**
 * Created by sonnd on 13/10/2015.
 */

public class ShopShippingOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private Context context;
    private ArrayList<ShippingOrderItem> shippingOrderItems;
    private SessionManager session;

    AlertDialog dialogCall;
    Dialog loadingDialog;

    public ShopShippingOrderAdapter() {
    }

    public ShopShippingOrderAdapter(Context context, ArrayList<ShippingOrderItem> newsItemArrayList) {
        this.context = context;
        this.shippingOrderItems = newsItemArrayList;
        session = new SessionManager(context);
        loadingDialog = Utils.setupLoadingDialog(context);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public TextView tvOrderTime
                , tvShippingFrom
                , tvOrderStart
                , tvPrepay
                , tvDistance
                , tvShippingCost
                , tvMessageWait
                , tvMessageExpired
                , tvMessageReceived
                , tvMessageShipping
                , tvMessageEnd
                , tvMessageComplete
                , tvMessageCancel
                , tvMessageUrgent;
        public ImageView ivOrder, ivUrgent;
        public TextView btnListShipper, btnCompleted, btnCancelShip, btnTracking, btnCallShipper, btnRefresh, btnChangeShipCost;
        public LinearLayout addressBox;

        public MyViewHolder(View view) {
            super(view);

            tvOrderTime = (TextView) view.findViewById(R.id.order_time_create);
            tvShippingFrom = (TextView) view.findViewById(R.id.shipping_from);
            tvDistance = (TextView) view.findViewById(R.id.order_distance);
            tvOrderStart = (TextView) view.findViewById(R.id.order_start_date);
            tvPrepay = (TextView) view.findViewById(R.id.order_pre_pay);
            tvShippingCost = (TextView) view.findViewById(R.id.order_shipping_cost);
            btnListShipper = (TextView) view.findViewById(R.id.btn_list_shipper);
            btnCompleted = (TextView) view.findViewById(R.id.btn_confirm_complete);
            btnCancelShip = (TextView) view.findViewById(R.id.btn_cancel_ship);
            btnTracking = (TextView) view.findViewById(R.id.btn_tracking_ship);
            btnCallShipper = (TextView) view.findViewById(R.id.btn_call_ship);
            btnRefresh = (TextView) view.findViewById(R.id.btn_refresh_ship);
            btnChangeShipCost = (TextView) view.findViewById(R.id.action_change_shipping_cost);

            tvMessageWait = (TextView) view.findViewById(R.id.message_item_list_ship_wait);
            tvMessageExpired = (TextView) view.findViewById(R.id.message_item_list_ship_expired);
            tvMessageReceived = (TextView) view.findViewById(R.id.message_item_list_ship_receive);
            tvMessageShipping = (TextView) view.findViewById(R.id.message_item_list_ship_shipping);
            tvMessageEnd = (TextView) view.findViewById(R.id.message_item_list_ship_end);
            tvMessageComplete = (TextView) view.findViewById(R.id.message_item_list_ship_complete);
            tvMessageCancel = (TextView) view.findViewById(R.id.message_item_list_ship_cancel);
            tvMessageUrgent = (TextView) view.findViewById(R.id.message_urgent_order);

            ivOrder = (ImageView) view.findViewById(R.id.order_image);
            ivUrgent = (ImageView) view.findViewById(R.id.iv_urgent_order);

            addressBox = (LinearLayout) view.findViewById(R.id.delivery_to_address_box);

            view.setOnClickListener(this);
            btnListShipper.setOnClickListener(this);
            btnCompleted.setOnClickListener(this);
            btnCancelShip.setOnClickListener(this);
            btnTracking.setOnClickListener(this);
            btnCallShipper.setOnClickListener(this);
            btnRefresh.setOnClickListener(this);
            btnChangeShipCost.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final ShippingOrderItem orderItem = shippingOrderItems.get(getAdapterPosition());

            switch (v.getId()){
                case R.id.btn_list_shipper:
                    if(orderItem.getTotalShipper() > 0) {
                        Intent listShipperAssignShip = new Intent(context, ShopListShipperAssignShip.class);
                        listShipperAssignShip.putExtra("id", orderItem.getId());
                        context.startActivity(listShipperAssignShip);
                    }
                    break;
                case R.id.btn_cancel_ship :
                    CancelShipAction cancelShip = new CancelShipAction(context, orderItem.getId(), session.getShopId(), orderItem.getName());

                    cancelShip.cancel();

                    cancelShip.setCancelShipActionCallback(new CancelShipAction.CancelShipActionCallback() {
                        @Override
                        public void callBackSuccess() {
                            shippingOrderItems.remove(getAdapterPosition());
                            ShopShippingOrderAdapter.this.notifyDataSetChanged();
                        }
                    });
                    break;
                case R.id.btn_confirm_complete:
                    ChangeStatusShipAction changeStatus = new ChangeStatusShipAction(
                            context,
                            ShippingOrderService.get_instance(context),
                            orderItem.getId(),
                            session.getShopId(),
                            session.getShipperId(),
                            orderItem.getStatusId(),
                            orderItem.getStatusNote());

                    changeStatus.update();

                    changeStatus.setChangeStatusShipActionCallback(new ChangeStatusShipAction.ChangeStatusShipActionCallback() {
                        @Override
                        public void callBackSuccess() {
                            shippingOrderItems.remove(getAdapterPosition());
                            ShopShippingOrderAdapter.this.notifyDataSetChanged();
                        }
                    });
                    break;
                case R.id.btn_tracking_ship:
                        Intent tracking = new Intent(context, ShopTrackingShipperActivity.class);
                        tracking.putExtra("id", orderItem.getId());
                        tracking.putExtra("shopId", session.getShopId());
                        context.startActivity(tracking);
                    break;
                case R.id.btn_call_ship:
                    // Use the Builder class for convenient dialog construction
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    View viewCall = inflater.inflate(R.layout.dialog_shop_call, null);

                    builder.setView(viewCall);

                    viewCall.findViewById(R.id.btn_shipper_call_shipper).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utils.callPhone(context, orderItem.getShipperPhone());
                            dialogCall.dismiss();
                        }
                    });

                    viewCall.findViewById(R.id.btn_shipper_call_cus).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utils.callPhone(context, orderItem.getShippingTo().getPhone());
                            dialogCall.dismiss();
                        }
                    });

                    builder.create();

                    dialogCall = builder.show();
                    break;
                case R.id.btn_refresh_ship:
                    ResetShipAction resetShipAction = new ResetShipAction(context, orderItem.getId(), session.getShopId());

                    resetShipAction.reset();

                    resetShipAction.setResetShipActionCallback(new ResetShipAction.ResetShipActionCallback() {
                        @Override
                        public void callBackSuccess(String date) {
                            orderItem.setIsExpired(false);
                            orderItem.setDateEnd(date);
                            ShopShippingOrderAdapter.this.notifyDataSetChanged();
                        }
                    });
                    break;
                case R.id.action_change_shipping_cost:
                    final AlertDialog.Builder rBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.NonActivityDialog));

                    final View dialogView = LayoutInflater.from(context)
                            .inflate(R.layout.change_shipping_cost_popup_layout, null);

                    final EditText et = (EditText) dialogView.findViewById(R.id.tv_change_shipping_cost);
                    rBuilder.setView(dialogView);
                    final AlertDialog dialog = rBuilder.create();
                    dialogView.findViewById(R.id.submit_change_shipping_cost).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!et.getText().toString().isEmpty())
                            {
                                loadingDialog.show();
                                new ChangeShippingCost(new OnSendRequestCompleted() {
                                    @Override
                                    public void onSendRequestCompleted(boolean error, String message) {
                                        loadingDialog.dismiss();
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                        if(!error){
                                            tvShippingCost.setText(et.getText().toString() + "k");
                                        }

                                        dialog.dismiss();
                                    }
                                }, ShopService.get_instance(context), context, session.getShopId(), orderItem.getId(), Integer.parseInt(et.getText().toString())).execute();
                            }else {
                                dialog.dismiss();
                            }
                        }
                    });

                    dialog.show();

                    break;
                default:
                    Intent detail = new Intent(context, ShopShipDetailActivity.class);
                    detail.putExtra("id", orderItem.getId());
                    context.startActivity(detail);
                    break;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.shop_shipping_order_item, parent, false);
            return new MyViewHolder(itemView);
        }else{
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.fiveship_progress_bar_footer, parent, false);

            return new Utils.ProgressViewHolder(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return shippingOrderItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MyViewHolder) {
            MyViewHolder convertVh = (MyViewHolder) holder;
            final ShippingOrderItem orderItem = shippingOrderItems.get(position);

            convertVh.tvOrderTime.setText(String.valueOf(orderItem.getDateCreated()));
            convertVh.tvOrderStart.setText(orderItem.getDateEnd());
            convertVh.tvShippingFrom.setText(orderItem.getOrderFrom().getAddress());
            convertVh.tvPrepay.setText(String.valueOf(orderItem.getStrTotalValue()));
            convertVh.tvShippingCost.setText(String.valueOf(orderItem.getStrCostShip()));
            convertVh.btnListShipper.setEnabled(true);

            Utils.setImageToImageView(context, orderItem.getUrlPicture(), convertVh.ivOrder);

            ((View) convertVh.tvDistance.getParent()).setVisibility(View.GONE);

            convertVh.btnListShipper.setVisibility(View.GONE);
            convertVh.btnRefresh.setVisibility(View.GONE);
            convertVh.btnCancelShip.setVisibility(View.GONE);
            convertVh.btnCallShipper.setVisibility(View.GONE);
            convertVh.btnTracking.setVisibility(View.GONE);
            convertVh.btnChangeShipCost.setVisibility(View.GONE);
            convertVh.btnCompleted.setVisibility(View.GONE);
            convertVh.tvMessageWait.setVisibility(View.GONE);
            convertVh.tvMessageExpired.setVisibility(View.GONE);
            convertVh.tvMessageReceived.setVisibility(View.GONE);
            convertVh.tvMessageShipping.setVisibility(View.GONE);
            convertVh.tvMessageEnd.setVisibility(View.GONE);
            convertVh.tvMessageComplete.setVisibility(View.GONE);
            convertVh.tvMessageCancel.setVisibility(View.GONE);
            convertVh.tvMessageUrgent.setVisibility(View.GONE);
            convertVh.ivUrgent.setVisibility(View.GONE);

            if (!orderItem.isGroup()) {
                ((View) convertVh.tvDistance.getParent()).setVisibility(View.VISIBLE);
                convertVh.tvDistance.setText(orderItem.getDistance());
            }

            if (orderItem.getTotalShipper() > 0) {
                convertVh.btnListShipper.setText(context.getString(R.string.view_list_shipper_assign_ship_text, orderItem.getTotalShipper()));
            } else {
                convertVh.btnListShipper.setText(context.getString(R.string.no_any_shipper_assign_ship_text));
                convertVh.btnListShipper.setTextColor(context.getResources().getColor(R.color.grey_DA_lightest));
                convertVh.btnListShipper.setEnabled(false);
                convertVh.btnChangeShipCost.setVisibility(View.VISIBLE);
            }

            if(orderItem.isUrgent()){
                convertVh.ivUrgent.setVisibility(View.VISIBLE);
            }

            if (orderItem.getStatusId() == OrderStatusEnum.PENDING.getStatusCode() && !orderItem.getIsExpired()) {
                convertVh.btnListShipper.setVisibility(View.VISIBLE);
                convertVh.tvMessageWait.setVisibility(View.VISIBLE);
            }

            if (orderItem.getIsExpired() && orderItem.getStatusId() == OrderStatusEnum.PENDING.getStatusCode()) {
                convertVh.btnRefresh.setVisibility(View.VISIBLE);
                convertVh.btnCancelShip.setVisibility(View.VISIBLE);
                convertVh.tvMessageExpired.setVisibility(View.VISIBLE);
            }

            if (orderItem.getStatusId() == OrderStatusEnum.RECEIVED.getStatusCode()) {
                convertVh.btnCancelShip.setVisibility(View.VISIBLE);
                convertVh.btnCallShipper.setVisibility(View.VISIBLE);
                convertVh.tvMessageReceived.setVisibility(View.VISIBLE);
                //((TextView)convertView.findViewById(R.id.shipping_to)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.directions, 0);
            }

            if (orderItem.getStatusId() == OrderStatusEnum.SHIPPING.getStatusCode()) {
                convertVh.btnCancelShip.setVisibility(View.VISIBLE);
                convertVh.btnCallShipper.setVisibility(View.VISIBLE);
                convertVh.btnTracking.setVisibility(View.VISIBLE);
                convertVh.tvMessageShipping.setVisibility(View.VISIBLE);
                //(TextView)convertView.findViewById(R.id.shipping_to)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.directions, 0);
            }

            if (orderItem.getStatusId() == OrderStatusEnum.END.getStatusCode()) {
                convertVh.btnCancelShip.setVisibility(View.VISIBLE);
                convertVh.btnCallShipper.setVisibility(View.VISIBLE);
                convertVh.btnCompleted.setVisibility(View.VISIBLE);
                convertVh.tvMessageEnd.setVisibility(View.VISIBLE);
            }

            if (orderItem.getStatusId() == OrderStatusEnum.COMPLETED.getStatusCode()) {
                convertVh.tvMessageComplete.setVisibility(View.VISIBLE);
            }

            if (orderItem.getStatusId() == OrderStatusEnum.CANCEL.getStatusCode()) {
                convertVh.tvMessageCancel.setVisibility(View.VISIBLE);
            }

            //branch view when order is group or not
            branching(convertVh.addressBox, orderItem);
        }else{
            ((Utils.ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

    }

    public void branching(LinearLayout addressBox,final ShippingOrderItem model) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addressBox.removeAllViews();
        if (model.isGroup()) {

            for (int i = 0; i < model.getListShipToOfGroup().size(); i++) {
                final DeliveryToItem item = model.getListShipToOfGroup().get(i);

                //delivery address view
                View deliveryView = layoutInflater.inflate(R.layout.delivery_address_info_layout_item,addressBox, false);
                ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setText(item.getAddress());
                ((TextView) deliveryView.findViewById(R.id.add_index)).setText(String.valueOf(i + 1));
                if (i == model.getListShipToOfGroup().size() - 1) {
                    deliveryView.findViewById(R.id.dead_line).setVisibility(View.INVISIBLE);
                }

                addressBox.addView(deliveryView);
            }
        } else {
            View deliveryView = layoutInflater.inflate(R.layout.delivery_address_info_layout_item, addressBox, false);
            ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setText(model.getShippingTo().getAddress());
            deliveryView.findViewById(R.id.dead_line).setVisibility(View.INVISIBLE);

            addressBox.addView(deliveryView);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return shippingOrderItems.size();
    }
}

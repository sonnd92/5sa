package fiveship.vn.fiveship.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.AssignShipAction;
import fiveship.vn.fiveship.action.CancelAssignShipAction;
import fiveship.vn.fiveship.action.ChangeStatusShipAction;
import fiveship.vn.fiveship.activity.shipper.DetailOrderActivity;
import fiveship.vn.fiveship.activity.shipper.DirectionActivity;
import fiveship.vn.fiveship.model.DeliveryToItem;
import fiveship.vn.fiveship.model.ShippingOrderItem;
import fiveship.vn.fiveship.service.ShippingOrderService;
import fiveship.vn.fiveship.utility.enums.OrderStatusEnum;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by sonnd on 13/10/2015.
 */

public class ShippingOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private Context context;
    private ArrayList<ShippingOrderItem> shippingOrderItems;
    private SessionManager session;

    //AlertDialog dialogCall;

    public ShippingOrderAdapter(Context context, ArrayList<ShippingOrderItem> newsItemArrayList) {
        this.context = context;
        this.shippingOrderItems = newsItemArrayList;
        session = new SessionManager(context);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public TextView tvMessageUrgent,
                tvShippingFrom,
                tvDateCreated,
                tvPrepay,
                tvShippingCost,
                tvStartDate,
                tvDistance,
                tvMessageWait,
                tvMessageReceived,
                tvMessageShipping,
                tvMessageEnd,
                tvMessageComplete,
                tvMessageCancel,
                tvMinRecommendCost;

        public ImageView ivOrder, ivLabel, ivUrgent, ivPromotionLabel;

        public TextView btnDone, btnCall, btnAccept, btnCancelAccept, btnReceived, btnRecommend;

        public LinearLayout addressBox, recommendBox;

        public MyViewHolder(View convertView) {
            super(convertView);

            addressBox = (LinearLayout) convertView.findViewById(R.id.delivery_to_address_box);
            recommendBox = (LinearLayout) convertView.findViewById(R.id.recommend_cost_box);

            ivOrder = (ImageView) convertView.findViewById(R.id.order_image);
            ivLabel = (ImageView) convertView.findViewById(R.id.order_image_label);
            ivUrgent = (ImageView) convertView.findViewById(R.id.iv_urgent_order);
            ivPromotionLabel = (ImageView) convertView.findViewById(R.id.iv_promotion_label);

            tvShippingFrom = (TextView) convertView.findViewById(R.id.shipping_from);
            tvDateCreated = (TextView) convertView.findViewById(R.id.order_time_create);
            tvPrepay = (TextView) convertView.findViewById(R.id.order_pre_pay);
            tvShippingCost = (TextView) convertView.findViewById(R.id.order_shipping_cost);
            tvStartDate = (TextView) convertView.findViewById(R.id.order_start_date);
            tvDistance = (TextView) convertView.findViewById(R.id.order_distance);
            tvMessageWait = (TextView) convertView.findViewById(R.id.message_item_list_ship_wait);
            tvMessageReceived = (TextView) convertView.findViewById(R.id.message_item_list_ship_receive);
            tvMessageShipping = (TextView) convertView.findViewById(R.id.message_item_list_ship_shipping);
            tvMessageEnd = (TextView) convertView.findViewById(R.id.message_item_list_ship_end);
            tvMessageComplete = (TextView) convertView.findViewById(R.id.message_item_list_ship_complete);
            tvMessageCancel = (TextView) convertView.findViewById(R.id.message_item_list_ship_cancel);
            tvMessageUrgent = (TextView) convertView.findViewById(R.id.message_urgent_order);
            tvMinRecommendCost = (TextView) convertView.findViewById(R.id.tv_recommended_shipping_cost);

            btnAccept = (TextView) convertView.findViewById(R.id.action_accept_order);
            btnCancelAccept = (TextView) convertView.findViewById(R.id.action_cancel_accept_order);
            btnCall = (TextView) convertView.findViewById(R.id.action_call_order);
            btnReceived = (TextView) convertView.findViewById(R.id.action_receive_order);
            btnDone = (TextView) convertView.findViewById(R.id.action_done_order);
            btnRecommend = (TextView) convertView.findViewById(R.id.action_recommend_shipping_cost);

            btnAccept.setOnClickListener(this);
            btnCancelAccept.setOnClickListener(this);
            btnCall.setOnClickListener(this);
            btnReceived.setOnClickListener(this);
            btnDone.setOnClickListener(this);
            btnRecommend.setOnClickListener(this);
            convertView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final ShippingOrderItem orderItem = shippingOrderItems.get(getAdapterPosition());
            switch (v.getId()) {
                case R.id.action_accept_order:
                    AssignShipAction assignShipAction
                            = new AssignShipAction(context, orderItem, session.getShipperId(), orderItem.getStatusNote(), orderItem.isShopReliable());

                    assignShipAction.assign();

                    assignShipAction.setAssignShipActionCallback(new AssignShipAction.AssignShipActionCallback() {
                        @Override
                        public void callBackSuccess() {
                            orderItem.setIsAssign(true);
                            notifyDataSetChanged();
                            Intent detail = new Intent(context, DetailOrderActivity.class);
                            detail.putExtra("OrderId", orderItem.getId());
                            context.startActivity(detail);
                        }
                    });
                    break;
                case R.id.action_cancel_accept_order:
                    CancelAssignShipAction cancelAssignShipAction = new CancelAssignShipAction(context, orderItem.getId(), session.getShipperId(), orderItem.getStatusNote());

                    cancelAssignShipAction.cancel();

                    cancelAssignShipAction.setCancelAssignShipActionCallback(new CancelAssignShipAction.CancelAssignShipActionCallback() {
                        @Override
                        public void callBackSuccess() {
                            shippingOrderItems.remove(getAdapterPosition());
                            notifyDataSetChanged();
                        }
                    });
                    break;
                case R.id.action_receive_order:
                    ChangeStatusShipAction changeStatus = new ChangeStatusShipAction(context,
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
                            notifyDataSetChanged();
                        }
                    });
                    break;
                case R.id.action_done_order:
                    ChangeStatusShipAction _changeStatus = new ChangeStatusShipAction(context,
                            ShippingOrderService.get_instance(context),
                            orderItem.getId(),
                            session.getShopId(),
                            session.getShipperId(),
                            orderItem.getStatusId(),
                            orderItem.getStatusNote());

                    _changeStatus.update();

                    _changeStatus.setChangeStatusShipActionCallback(new ChangeStatusShipAction.ChangeStatusShipActionCallback() {
                        @Override
                        public void callBackSuccess() {
                            shippingOrderItems.remove(getAdapterPosition());
                            notifyDataSetChanged();
                        }
                    });
                    break;
                case R.id.action_call_order:
                    // Use the Builder class for convenient dialog construction
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//
//                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//                    View viewCall = inflater.inflate(R.layout.dialog_shipper_call, null);
//
//                    builder.setView(viewCall);
//
//                    viewCall.findViewById(R.id.btn_shipper_call_shop).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Utils.callPhone(context, orderItem.getOrderFrom().getPhone());
//                            dialogCall.dismiss();
//                        }
//                    });
//
//                    viewCall.findViewById(R.id.btn_shipper_call_cus).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Utils.callPhone(context, orderItem.getShippingTo().getPhone());
//                            dialogCall.dismiss();
//                        }
//                    });
//
//                    builder.create();
//
//                    dialogCall = builder.show();
                    Utils.callPhone(context, orderItem.getOrderFrom().getPhone());
                    break;
                case R.id.action_recommend_shipping_cost:
                    Utils.setupRecommendShippingCostDialog(context, orderItem).show();
                    break;
                default:
                    Intent detail = new Intent(context, DetailOrderActivity.class);
                    detail.putExtra("OrderId", orderItem.getId());
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
                    .inflate(R.layout.shipping_order_item, parent, false);
            vh = new MyViewHolder(itemView);
        }else{
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.fiveship_progress_bar_footer, parent, false);

            vh = new Utils.ProgressViewHolder(itemView);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MyViewHolder) {

            MyViewHolder convertVh = (MyViewHolder) holder;

            ShippingOrderItem orderItem = shippingOrderItems.get(position);

            Utils.setImageToImageView(context, orderItem.getUrlPicture(), convertVh.ivOrder);
            Utils.setImageToImageViewNoDefault(context, orderItem.getUrlPictureLabel(), convertVh.ivLabel);

            convertVh.tvShippingFrom.setText(orderItem.getOrderFrom().getAddress());
            convertVh.tvDateCreated.setText(orderItem.getDateCreated());
            convertVh.tvPrepay.setText(orderItem.getStrTotalValue());
            convertVh.tvShippingCost.setText(orderItem.getStrCostShip());
            convertVh.tvStartDate.setText(orderItem.getDateEnd());
            ((View) convertVh.tvDistance.getParent()).setVisibility(View.GONE);

            if (!orderItem.isGroup()) {
                ((View) convertVh.tvDistance.getParent()).setVisibility(View.VISIBLE);
                convertVh.tvDistance.setText(orderItem.getDistance());
            }
            convertVh.btnAccept.setVisibility(View.GONE);
            convertVh.btnCancelAccept.setVisibility(View.GONE);
            convertVh.btnCall.setVisibility(View.GONE);
            convertVh.btnReceived.setVisibility(View.GONE);
            convertVh.btnDone.setVisibility(View.GONE);
            convertVh.btnRecommend.setVisibility(View.GONE);
            convertVh.recommendBox.setVisibility(View.GONE);

            convertVh.tvMessageWait.setVisibility(View.GONE);
            convertVh.tvMessageReceived.setVisibility(View.GONE);
            convertVh.tvMessageShipping.setVisibility(View.GONE);
            convertVh.tvMessageEnd.setVisibility(View.GONE);
            convertVh.tvMessageComplete.setVisibility(View.GONE);
            convertVh.tvMessageCancel.setVisibility(View.GONE);
            convertVh.ivUrgent.setVisibility(View.GONE);
            convertVh.ivPromotionLabel.setVisibility(View.GONE);
            convertVh.tvMessageUrgent.setVisibility(View.GONE);

            if(orderItem.isUrgent()){
                convertVh.ivUrgent.setVisibility(View.VISIBLE);
            }
            if (orderItem.getStatusId() == OrderStatusEnum.PENDING.getStatusCode() && !orderItem.getIsAssign()) {
                convertVh.btnAccept.setVisibility(View.VISIBLE);
                convertVh.btnRecommend.setVisibility(View.VISIBLE);
                if(orderItem.isUrgent()){
                    convertVh.tvMessageUrgent.setVisibility(View.VISIBLE);
                }
            }

            if (orderItem.getStatusId() == OrderStatusEnum.PENDING.getStatusCode() && orderItem.getIsAssign()) {
                convertVh.btnCancelAccept.setVisibility(View.VISIBLE);
                convertVh.tvMessageWait.setVisibility(View.VISIBLE);
            }

            if (orderItem.getStatusId() == OrderStatusEnum.RECEIVED.getStatusCode()) {
                convertVh.btnCall.setVisibility(View.VISIBLE);
                convertVh.btnReceived.setVisibility(View.VISIBLE);
                convertVh.tvMessageReceived.setVisibility(View.VISIBLE);
            }

            if (orderItem.getStatusId() == OrderStatusEnum.SHIPPING.getStatusCode()) {
                convertVh.btnCall.setVisibility(View.VISIBLE);
                convertVh.btnDone.setVisibility(View.VISIBLE);
                convertVh.tvMessageShipping.setVisibility(View.VISIBLE);
            }

            if (orderItem.getStatusId() == OrderStatusEnum.END.getStatusCode()) {
                convertVh.tvMessageEnd.setVisibility(View.VISIBLE);
            }

            if (orderItem.getStatusId() == OrderStatusEnum.COMPLETED.getStatusCode()) {
                convertVh.tvMessageComplete.setVisibility(View.VISIBLE);
            }

            if (orderItem.getStatusId() == OrderStatusEnum.CANCEL.getStatusCode()) {
                convertVh.tvMessageCancel.setVisibility(View.VISIBLE);
            }

            if(orderItem.getMinRecommendShippingCost() != null && !orderItem.getMinRecommendShippingCost().isEmpty()){
                convertVh.recommendBox.setVisibility(View.VISIBLE);
                convertVh.tvMinRecommendCost.setText(orderItem.getMinRecommendShippingCost());
            }
            if(orderItem.isHasPromotionCode()){
                convertVh.ivPromotionLabel.setVisibility(View.VISIBLE);
            }
            //branch view when order is group or not
            branching(convertVh.addressBox, orderItem);
        }else{
            ((Utils.ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemViewType(int position) {
        return shippingOrderItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public int getItemCount() {
        return shippingOrderItems.size();
    }

    public void branching(LinearLayout addressBox, final ShippingOrderItem model) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addressBox.removeAllViews();
        if (model.isGroup()) {

            for (int i = 0; i < model.getListShipToOfGroup().size(); i++) {
                final DeliveryToItem item = model.getListShipToOfGroup().get(i);
                //delivery address view
                View deliveryView = layoutInflater.inflate(R.layout.delivery_address_info_layout_item ,addressBox, false);

                ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.directions, 0);
                ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setText(item.getAddress());
                ((TextView) deliveryView.findViewById(R.id.add_index)).setText(String.valueOf(i + 1));
                if (i == model.getListShipToOfGroup().size() - 1) {
                    deliveryView.findViewById(R.id.dead_line).setVisibility(View.INVISIBLE);
                }

                deliveryView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<LatLng> locations = new ArrayList<>();
                        Intent mapIntent = new Intent(context, DirectionActivity.class);
                        Bundle locationsBnl = new Bundle();

                        if (model.getStatusId() != OrderStatusEnum.SHIPPING.getStatusCode())
                        {

                            locations.add(new LatLng(Double.parseDouble(model.getOrderFrom().getLatitude()),
                                    Double.parseDouble(model.getOrderFrom().getLongitude())));
                        }

                        locations.add(new LatLng(item.getXPoint(), item.getYPoint()));
                        locationsBnl.putSerializable("locations", locations);
                        mapIntent.putExtras(locationsBnl);
                        context.startActivity(mapIntent);
                    }
                });
                addressBox.addView(deliveryView);
            }
        } else {
            View deliveryView = layoutInflater.inflate(R.layout.delivery_address_info_layout_item, addressBox, false);

            ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setText(model.getShippingTo().getAddress());
            deliveryView.findViewById(R.id.dead_line).setVisibility(View.INVISIBLE);

            ((TextView) deliveryView.findViewById(R.id.order_dt_to_address)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.directions, 0);
            deliveryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<LatLng> locations = new ArrayList<>();
                    Intent mapIntent = new Intent(context, DirectionActivity.class);
                    Bundle locationsBnl = new Bundle();

                    if (model.getStatusId() != OrderStatusEnum.SHIPPING.getStatusCode()) {

                        locations.add(new LatLng(Double.parseDouble(model.getOrderFrom().getLatitude()),
                                Double.parseDouble(model.getOrderFrom().getLongitude())));
                    }

                    locations.add(new LatLng(Double.parseDouble(model.getShippingTo().getLatitude()),
                            Double.parseDouble(model.getShippingTo().getLongitude())));

                    locationsBnl.putSerializable("locations", locations);
                    mapIntent.putExtras(locationsBnl);
                    context.startActivity(mapIntent);
                }
            });

            addressBox.addView(deliveryView);
        }
    }
}

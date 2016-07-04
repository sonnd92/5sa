package fiveship.vn.fiveship.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.shipper.DetailOrderActivity;
import fiveship.vn.fiveship.activity.shipper.ShipHistoryTradeActivity;
import fiveship.vn.fiveship.activity.shop.ShopListShipperAssignShip;
import fiveship.vn.fiveship.activity.shop.ShopShipDetailActivity;
import fiveship.vn.fiveship.model.NewsItem;
import fiveship.vn.fiveship.model.NotificationItem;
import fiveship.vn.fiveship.service.NotificationService;
import fiveship.vn.fiveship.service.adapter.UpdateIsViewedNotification;
import fiveship.vn.fiveship.utility.Notification;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by sonnd on 18/10/2015.
 */
public class NotificationAdapter extends BaseRecyclerAdapter {

    private Context mContext;
    private ArrayList<NotificationItem> mItems;
    private boolean mIsShipper;

    public NotificationAdapter() {
    }

    public NotificationAdapter(Context context, ArrayList<NotificationItem> items, boolean isShipper) {
        this.mContext = context;
        this.mItems = items;
        this.mIsShipper = isShipper;
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        private View mItemView;
        private ImageView mCoverImage;
        private TextView mMessageContentTV, mMessageDateCreatedTV;
        

        public MyViewHolder(View itemView) {            
            super(itemView);
            mItemView = itemView;
            mCoverImage = (ImageView) itemView.findViewById(R.id.message_avatar);
            mMessageContentTV = (TextView) itemView.findViewById(R.id.notification_item_name);
            mMessageDateCreatedTV = ((TextView) itemView.findViewById(R.id.notification_item_date));
            
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final NotificationItem notifyItem = mItems.get(getAdapterPosition());

                    new UpdateIsViewedNotification(NotificationService.get_instance(mContext))
                            .execute(String.valueOf(notifyItem.getId()));

                    if (notifyItem.getTypeId() == 7) {
                        Intent historyTrade = new Intent(mContext, ShipHistoryTradeActivity.class);
                        mContext.startActivity(historyTrade);
                    }

                    //notice change status
                    if (notifyItem.getTypeId() == 6) {
                        showDetailShip(notifyItem.getShipId());
                    }

                    //notice change status
                    if (notifyItem.getTypeId() == 5) {
                        showDetailShip(notifyItem.getShipId());
                    }

                    //notice change status
                    if (notifyItem.getTypeId() == 4) {
                        showListSipperAssignShip(notifyItem.getShipId());
                    }

                    //notice change status
                    if (notifyItem.getTypeId() == 1) {
                        showDetailShip(notifyItem.getShipId());
                    }

                    notifyItem.setIsView(true);

                    NotificationAdapter.this.notifyDataSetChanged();
                }
            });
        }
    }

    public void showDetailShip(int shipId){

        if(mIsShipper){
            Intent detailActivity = new Intent(mContext, DetailOrderActivity.class);
            detailActivity.putExtra("OrderId", shipId);
            mContext.startActivity(detailActivity);
        }else{
            Intent detailActivity = new Intent(mContext, ShopShipDetailActivity.class);
            detailActivity.putExtra("id", shipId);
            mContext.startActivity(detailActivity);
        }

    }

    public void showListSipperAssignShip(int shipId){

        Intent listShipperAssignShip = new Intent(mContext, ShopListShipperAssignShip.class);
        listShipperAssignShip.putExtra("id", shipId);
        mContext.startActivity(listShipperAssignShip);

    }

    @Override
    public RecyclerView.ViewHolder setViewHolder() {
        return null;
    }

    @Override
    public void bindMyViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof MyViewHolder){

            MyViewHolder convertVh = (MyViewHolder) holder;
            NotificationItem notify = mItems.get(position);

            convertVh.mItemView.setBackgroundColor(Color.WHITE);

            if(!notify.getIsView()){
                convertVh.mItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.fb_notify_item_viewed));
            }

            Utils.setImageToImageView(mContext, notify.getUrlAvatar(), convertVh.mCoverImage);

            Spanned sp1 = Html.fromHtml(notify.getName(), null, null);
            convertVh.mMessageContentTV.setText(sp1);
            convertVh.mMessageDateCreatedTV.setText(notify.getDateCreated());

        }else{
            ((Utils.ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getTotal() {
        return mItems.size();
    }

    @Override
    public ArrayList getArrayList() {
        return mItems;
    }

    @Override
    public RecyclerView.ViewHolder getCustomViewHolder(ViewGroup parent) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.notification_item, parent, false);

        return new MyViewHolder(itemView);
    }


}

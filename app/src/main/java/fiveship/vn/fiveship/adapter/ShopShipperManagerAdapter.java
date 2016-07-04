package fiveship.vn.fiveship.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.model.ShipperItem;
import fiveship.vn.fiveship.service.ShopService;
import fiveship.vn.fiveship.service.adapter.AddShipperIntoFavorite;
import fiveship.vn.fiveship.service.adapter.BlockShipper;
import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;
import fiveship.vn.fiveship.utility.enums.ShipperMngDataChangeEnum;

/**
 * Created by Unstoppable on 4/8/2016.
 */
public class ShopShipperManagerAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private Context mContext;
    private ArrayList<ShipperItem> shipperItems;
    private int mTabType;

    public ShopShipperManagerAdapter(Context mContext, ArrayList<ShipperItem> shipperItems, int mTabType) {
        this.mContext = mContext;
        this.shipperItems = shipperItems;
        this.mTabType = mTabType;
    }

    public ShopShipperManagerAdapter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.shipper_in_manager_item, parent, false);
            vh = new MyViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.fiveship_progress_bar_footer, parent, false);

            vh = new Utils.ProgressViewHolder(itemView);
        }

        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        return shipperItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ShipperItem item = shipperItems.get(position);
        if (holder instanceof MyViewHolder) {
            MyViewHolder convertVh = (MyViewHolder) holder;
            convertVh.tvShipperShipRate.setText(item.getShipStatistic());
            convertVh.tvShipperName.setText(item.getName());
            convertVh.tvShipperMotor.setText(item.getMotorId());
            Utils.setImageToImageView(mContext, item.getAvatarUrl(), convertVh.ivShipperAvatar);
            convertVh.ivAction.setVisibility(View.GONE);
            convertVh.ivFavorite.setVisibility(View.GONE);
            convertVh.ivRemoveFav.setVisibility(View.GONE);
            convertVh.ivDelete.setVisibility(View.GONE);
            switch (mTabType) {
                case 0:

                    if (item.isBlock()) {
                        convertVh.ivDelete.setVisibility(View.VISIBLE);
                    } else {
                        convertVh.ivAction.setVisibility(View.VISIBLE);

                        if (item.isFavorite()) {
                            convertVh.ivRemoveFav.setVisibility(View.VISIBLE);
                        } else {
                            convertVh.ivFavorite.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case 1:
                    convertVh.ivRemoveFav.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    convertVh.ivDelete.setVisibility(View.VISIBLE);
                    break;
            }

        } else {
            ((Utils.ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return shipperItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvShipperName, tvShipperMotor, tvShipperShipRate;
        private ImageView ivShipperAvatar, ivFavorite, ivAction, ivDelete, ivRemoveFav;
        private SessionManager mSessionManager;
        private Dialog mLoadingDialog;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvShipperMotor = (TextView) itemView.findViewById(R.id.shipper_motor_license);
            tvShipperName = (TextView) itemView.findViewById(R.id.shipper_name);
            tvShipperShipRate = (TextView) itemView.findViewById(R.id.shipper_order_rate);

            ivShipperAvatar = (ImageView) itemView.findViewById(R.id.shipper_avatar);
            ivAction = (ImageView) itemView.findViewById(R.id.iv_shipper_action);
            ivFavorite = (ImageView) itemView.findViewById(R.id.iv_shipper_favorite_false);
            ivRemoveFav = (ImageView) itemView.findViewById(R.id.iv_shipper_favorite_true);
            ivDelete = (ImageView) itemView.findViewById(R.id.iv_shipper_delete);

            ivAction.setOnClickListener(this);
            ivFavorite.setOnClickListener(this);
            ivRemoveFav.setOnClickListener(this);
            ivDelete.setOnClickListener(this);

            mSessionManager = new SessionManager(mContext);
            mLoadingDialog = Utils.setupLoadingDialog(mContext);
        }

        @Override
        public void onClick(View v) {

            final ShipperItem mCurrentShipper = shipperItems.get(getAdapterPosition());

            switch (v.getId()) {
                case R.id.iv_shipper_action:
                    PopupMenu popup = new PopupMenu(mContext, v);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.shipper_manager_action_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_block:
                                    mLoadingDialog.show();
                                    new BlockShipper(new OnSendRequestCompleted() {
                                        @Override
                                        public void onSendRequestCompleted(boolean error, String message) {
                                            if (!error)
                                            {
                                                Intent callback = new Intent(Config.RECEIVE_SHIPPER_MANAGER);
                                                callback.putExtra("shipperItem", mCurrentShipper);
                                                callback.putExtra("state", ShipperMngDataChangeEnum.BLOCK.getStatusCode());
                                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(callback);
                                            }
                                            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                            mLoadingDialog.dismiss();

                                        }
                                    }, ShopService.get_instance(mContext), mContext, mSessionManager.getShopId(), mCurrentShipper.getId(), true).execute();
                                    break;
                            }
                            return true;
                        }
                    });

                    popup.show();//showing popup menu
                    break;
                case R.id.iv_shipper_favorite_false:
                    final Dialog tConfirmDlg = Utils.setupConfirmDialog(mContext);
                    tConfirmDlg.show();
                    ((TextView) tConfirmDlg.findViewById(R.id.tv_content)).setText("Bạn có chắc muốn thêm người này vào danh sách shipper ruột?");
                    tConfirmDlg.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            tConfirmDlg.dismiss();
                            mLoadingDialog.show();

                            new AddShipperIntoFavorite(new OnSendRequestCompleted() {
                                @Override
                                public void onSendRequestCompleted(boolean error, String message) {
                                    if (!error)
                                    {
                                        Intent callback = new Intent(Config.RECEIVE_SHIPPER_MANAGER);
                                        callback.putExtra("shipperItem", mCurrentShipper);
                                        callback.putExtra("state",ShipperMngDataChangeEnum.FAVORITE.getStatusCode());
                                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(callback);
                                    }
                                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                    mLoadingDialog.dismiss();

                                }
                            }, ShopService.get_instance(mContext), mContext, mSessionManager.getShopId(), mCurrentShipper.getId(), true).execute();

                        }
                    });
                    tConfirmDlg.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tConfirmDlg.dismiss();
                        }
                    });
                    break;
                case R.id.iv_shipper_favorite_true:
                    final Dialog fConfirmDlg = Utils.setupConfirmDialog(mContext);
                    fConfirmDlg.show();
                    ((TextView) fConfirmDlg.findViewById(R.id.tv_content)).setText("Bạn có chắc muốn bỏ người này khỏi danh sách shipper ruột?");
                    fConfirmDlg.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            fConfirmDlg.dismiss();
                            mLoadingDialog.show();
                            new AddShipperIntoFavorite(new OnSendRequestCompleted() {
                                @Override
                                public void onSendRequestCompleted(boolean error, String message) {
                                    if (!error)
                                    {
                                        Intent callback = new Intent(Config.RECEIVE_SHIPPER_MANAGER);
                                        callback.putExtra("shipperItem", mCurrentShipper);
                                        callback.putExtra("state", ShipperMngDataChangeEnum.REMOVE_FAVORITE.getStatusCode());
                                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(callback);
                                    }
                                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                    mLoadingDialog.dismiss();

                                }
                            }, ShopService.get_instance(mContext), mContext, mSessionManager.getShopId(), mCurrentShipper.getId(), false).execute();
                        }
                    });
                    fConfirmDlg.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fConfirmDlg.dismiss();
                        }
                    });
                    break;
                case R.id.iv_shipper_delete:
                    final Dialog confirmDlg = Utils.setupConfirmDialog(mContext);
                    confirmDlg.show();
                    ((TextView) confirmDlg.findViewById(R.id.tv_content)).setText("Bạn có chắc chắn muốn bỏ người này khỏi danh sách chặn?");
                    confirmDlg.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            confirmDlg.dismiss();
                            mLoadingDialog.show();
                            new BlockShipper(new OnSendRequestCompleted() {
                                @Override
                                public void onSendRequestCompleted(boolean error, String message) {
                                    if (!error) {
                                        Intent callback = new Intent(Config.RECEIVE_SHIPPER_MANAGER);
                                        callback.putExtra("shipperItem", mCurrentShipper);
                                        callback.putExtra("state",ShipperMngDataChangeEnum.REMOVE_BLOCK.getStatusCode());
                                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(callback);
                                    }
                                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                    mLoadingDialog.dismiss();

                                }
                            }, ShopService.get_instance(mContext), mContext, mSessionManager.getShopId(), mCurrentShipper.getId(), false).execute();
                        }
                    });
                    confirmDlg.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmDlg.dismiss();
                        }
                    });
                    break;
            }
        }
    }
}

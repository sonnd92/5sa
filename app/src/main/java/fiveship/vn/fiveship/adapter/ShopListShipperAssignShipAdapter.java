package fiveship.vn.fiveship.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.action.AcceptShipperForShipAction;
import fiveship.vn.fiveship.activity.shipper.ShipperInfoDialogFragment;
import fiveship.vn.fiveship.activity.shop.RatingAndReviewShipperActivity;
import fiveship.vn.fiveship.model.CustomerItem;
import fiveship.vn.fiveship.utility.Utils;
import it.sephiroth.android.library.tooltip.Tooltip;

/**
 * Created by BVN on 04/11/2015.
 */
public class ShopListShipperAssignShipAdapter extends RecyclerView.Adapter<ShopListShipperAssignShipAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<CustomerItem> list;
    private int shopId;

    public class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private TextView tvFullName, tvDateCreated, tvMotorId, tvStatistics, tvDistance, tvRecommendCost, tvTotalStar;
        private ImageView ivAvatar, ivLabel, ivRecommend;
        private View btnAccept;
        private LinearLayout recommendBox, ratingBox, starBox;

        public MyViewHolder(View view) {
            super(view);

            recommendBox = (LinearLayout) view.findViewById(R.id.recommend_cost_box);

            tvFullName = (TextView) view.findViewById(R.id.shipper_assign_ship_item_name);
            tvDateCreated = (TextView) view.findViewById(R.id.shipper_assign_ship_item_date);
            tvMotorId = (TextView) view.findViewById(R.id.shipper_assign_ship_item_motor);
            tvStatistics = (TextView) view.findViewById(R.id.shipper_assign_ship_item_ship);
            tvDistance = (TextView) view.findViewById(R.id.shipper_assign_ship_item_distance);
            tvRecommendCost = (TextView) view.findViewById(R.id.tv_recommended_shipping_cost);
            tvTotalStar = (TextView) view.findViewById(R.id.tv_total_rating);

            ivAvatar = (ImageView) view.findViewById(R.id.cus_image);
            ivLabel = (ImageView) view.findViewById(R.id.cus_image_label);
            ivRecommend = (ImageView) view.findViewById(R.id.iv_recommend_note);

            btnAccept = view.findViewById(R.id.btn_shipper_assign_ship_item_accept);
            ratingBox = (LinearLayout) view.findViewById(R.id.rating_box);
            starBox = (LinearLayout) view.findViewById(R.id.rating_star_box);

            view.setOnClickListener(this);
            ratingBox.setOnClickListener(this);
            btnAccept.setOnClickListener(this);
            ivRecommend.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            CustomerItem customerItem = list.get(getAdapterPosition());
            switch (view.getId()) {
                case R.id.btn_shipper_assign_ship_item_accept:
                    AcceptShipperForShipAction acceptShipper = new AcceptShipperForShipAction(context, shopId, customerItem);

                    acceptShipper.accept();

                    acceptShipper.setAcceptShipperForShipActionCallback(new AcceptShipperForShipAction.AcceptShipperForShipActionCallback() {
                        @Override
                        public void callBackSuccess() {
                            for (CustomerItem item : list) {
                                item.setIsAccess(true);
                            }
                            ShopListShipperAssignShipAdapter.this.notifyDataSetChanged();
                        }
                    });
                    break;
                case R.id.rating_box:
                    Intent reviewDetail = new Intent(context, RatingAndReviewShipperActivity.class);
                    reviewDetail.putExtra("shipper", customerItem);
                    context.startActivity(reviewDetail);

                    break;
                case R.id.iv_recommend_note:
//                    new TooltipDialog(context, "(*) Chấp nhận shipper này " +
//                            "tương đương với việc thay đổi phí ship theo đề xuất của shipper").show(view);
                    Tooltip.make(context,
                            new Tooltip.Builder(101)
                                    .anchor(view, Tooltip.Gravity.BOTTOM)
                                    .closePolicy(new Tooltip.ClosePolicy()
                                            .insidePolicy(true, false)
                                            .outsidePolicy(true, false), 3000)
                                    .text("(*) Chấp nhận shipper này tương đương với việc thay đổi phí ship theo đề xuất của shipper")
                                    .withArrow(true)
                                    .withStyleId(R.style.TooltipLayoutStlye)
                                    .withOverlay(true)
                                    .build()
                    ).show();
                    break;
                default:
                    DialogFragment dialog = new ShipperInfoDialogFragment(customerItem.getId());
                    dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "ShipperInfoDialogFragment");
                    break;
            }
        }
    }

    public ShopListShipperAssignShipAdapter(Context context, ArrayList<CustomerItem> list, int shopId) {
        this.context = context;
        this.list = list;
        this.shopId = shopId;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shipper_assign_ship_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        CustomerItem customerItem = list.get(position);

        holder.recommendBox.setVisibility(View.GONE);
        holder.ratingBox.setVisibility(View.GONE);

        holder.tvFullName.setText(customerItem.getFullName());
        holder.tvDateCreated.setText(customerItem.getDateCreated());
        holder.tvMotorId.setText(customerItem.getMotorId());
        holder.tvStatistics.setText(customerItem.getShipStatistics());
        holder.tvDistance.setText(customerItem.getDistance());
        holder.btnAccept.setEnabled(true);

        Utils.setImageToImageView(context, customerItem.getAvatar(), holder.ivAvatar);
        Utils.setImageToImageViewNoDefault(context, customerItem.getAvatarLabel(), holder.ivLabel);

        if (customerItem.isAccess()) {
            holder.btnAccept.setEnabled(false);
        }

        if (customerItem.getRecommendShippingCost() != null && !customerItem.getRecommendShippingCost().isEmpty()) {
            holder.recommendBox.setVisibility(View.VISIBLE);
            holder.tvRecommendCost.setText(customerItem.getRecommendShippingCost());
        }

        holder.starBox.removeAllViews();

        if (!customerItem.getNumberRating().isEmpty()) {

            holder.ratingBox.setVisibility(View.VISIBLE);
            int maxStar = 5;
            int fractional = (int) customerItem.getNumberStar();
            float decimalPart = customerItem.getNumberStar() - fractional;
            for (int i = 0; i < maxStar; i++) {
                ImageView star = new ImageView(context);

                if (i < fractional) {
                    star.setImageResource(R.drawable.ic_full_orange_star);
                } else if (i == fractional) {
                    if (decimalPart <= 0.25) {
                        star.setImageResource(R.drawable.ic_empty_orange_star);
                    } else if (decimalPart > 0.25 && decimalPart <= 0.5) {
                        star.setImageResource(R.drawable.ic_half_orange_star);
                    } else {
                        star.setImageResource(R.drawable.ic_full_orange_star);
                    }
                } else {
                    star.setImageResource(R.drawable.ic_empty_orange_star);
                }
                final float scale = context.getResources().getDisplayMetrics().density;
                int dpWidthInPx = (int) (16 * scale);
                int dpHeightInPx = (int) (15 * scale);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dpWidthInPx, dpHeightInPx);
                lp.setMargins(0, 0, 2, 0);

                star.setLayoutParams(lp);

                holder.starBox.addView(star);
            }

            holder.tvTotalStar.setText(customerItem.getNumberRating());
        }
    }

}

package fiveship.vn.fiveship.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.model.ReviewOfShipperItem;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by Unstopable on 6/15/2016.
 */
public class RatingAndReviewShipperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private Context mContext;
    private ArrayList<ReviewOfShipperItem> reviewItems;

    public RatingAndReviewShipperAdapter(Context mContext, ArrayList<ReviewOfShipperItem> reviewItems) {
        this.mContext = mContext;
        this.reviewItems = reviewItems;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivShopAvt;
        private TextView tvShopName, tvDateCreated, tvReview;
        private LinearLayout ratingBox;

        public MyViewHolder(View itemView) {
            super(itemView);
            ivShopAvt = (ImageView) itemView.findViewById(R.id.shop_avatar);
            tvShopName = (TextView) itemView.findViewById(R.id.shop_name);
            tvDateCreated = (TextView) itemView.findViewById(R.id.tv_date_time);
            tvReview = (TextView) itemView.findViewById(R.id.tv_review);
            ratingBox = (LinearLayout) itemView.findViewById(R.id.rating_star_box);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rating_and_review_of_shipper_item, parent, false);
            vh = new MyViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.fiveship_progress_bar_footer, parent, false);

            vh = new Utils.ProgressViewHolder(itemView);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {

            MyViewHolder convertVh = (MyViewHolder) holder;

            ReviewOfShipperItem review = reviewItems.get(position);
            Utils.setImageToImageView(mContext, review.getReviewerAvatarUrl(), convertVh.ivShopAvt);
            convertVh.tvShopName.setText(review.getReviewer());
            convertVh.tvDateCreated.setText(review.getDateTime());
            convertVh.tvReview.setText(review.getReviewContent());
            final int maxStar = 5;
            convertVh.ratingBox.removeAllViews();
            for (int i = 0; i < maxStar; i++) {
                ImageView star = new ImageView(mContext);

                if (i < review.getNumberRating()) {
                    star.setImageResource(R.drawable.ic_full_orange_star);
                } else {
                    star.setImageResource(R.drawable.ic_empty_orange_star);
                }

                final float scale = mContext.getResources().getDisplayMetrics().density;
                int dpWidthInPx  = (int) (16 * scale);
                int dpHeightInPx = (int) (15 * scale);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dpWidthInPx, dpHeightInPx);
                lp.setMargins(0, 0, 2, 0);

                star.setLayoutParams(lp);
                convertVh.ratingBox.addView(star);
            }


        } else {
            ((Utils.ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return reviewItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return reviewItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }
}

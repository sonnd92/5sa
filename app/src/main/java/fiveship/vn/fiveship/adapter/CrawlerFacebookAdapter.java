package fiveship.vn.fiveship.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.shipper.DetailOrderActivity;
import fiveship.vn.fiveship.interfaces.OnSendRequestCompleted;
import fiveship.vn.fiveship.model.FacebookFeedCrawlerItem;
import fiveship.vn.fiveship.service.adapter.PinFacebookFeed;
import fiveship.vn.fiveship.service.adapter.RemovePinnedFbFeed;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by Unstoppable on 5/12/2016.
 */
public class CrawlerFacebookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private Context mContext;
    private ArrayList<FacebookFeedCrawlerItem> crawlerItems;

    public CrawlerFacebookAdapter(Context context, ArrayList<FacebookFeedCrawlerItem> crawlerItems) {
        this.mContext = context;
        this.crawlerItems = crawlerItems;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTvCreator, mTvTimeCreated, mTvDistance, mTvFeedContent;
        private View mBtnInbox, mBtnComment, mBtnCall, mBtnPin;
        private ImageView mIvPinOrder, mIvRemovePinned;
        private SessionManager mSessionMng;

        public MyViewHolder(View itemView) {
            super(itemView);

            mSessionMng = new SessionManager(mContext);

            mIvPinOrder = (ImageView) itemView.findViewById(R.id.iv_not_pin);
            mIvRemovePinned = (ImageView) itemView.findViewById(R.id.iv_pinned);

            mTvCreator = (TextView) itemView.findViewById(R.id.fb_feed_creator_name);
            mTvTimeCreated = (TextView) itemView.findViewById(R.id.fb_feed_time_created);
            mTvDistance = (TextView) itemView.findViewById(R.id.fb_feed_distance);
            mTvFeedContent = (TextView) itemView.findViewById(R.id.fb_feed_content);

            mBtnInbox = itemView.findViewById(R.id.fb_inbox_btn);
            mBtnComment = itemView.findViewById(R.id.fb_comment_btn);
            mBtnCall = itemView.findViewById(R.id.fb_call_btn);
            mBtnPin = itemView.findViewById(R.id.btn_pin_order);


            mBtnInbox.setOnClickListener(this);
            mBtnComment.setOnClickListener(this);
            mBtnCall.setOnClickListener(this);
            mBtnPin.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            final FacebookFeedCrawlerItem crawlerItem = crawlerItems.get(getAdapterPosition());
            switch (v.getId()) {
                case R.id.fb_inbox_btn:

                    String facebookUrl = "https://m.facebook.com/messages/thread/" + crawlerItem.getCreatorId();
                    String facebookUrlScheme = "fb://messages/" + crawlerItem.getCreatorId();

                    try {
                        int versionCode = mContext.getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
                        Intent messageIntent;

                        if (versionCode >= 3002850) {
                            Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                            messageIntent = new Intent(Intent.ACTION_VIEW, uri);
                        } else {
                            messageIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrlScheme));
                        }

                        mContext.startActivity(messageIntent);

                    } catch (PackageManager.NameNotFoundException | ActivityNotFoundException e) {
                        e.printStackTrace();
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
                    }

                    break;
                case R.id.fb_comment_btn:

                    String facebookCmtUrl = "https://www.facebook.com/" + crawlerItem.getFeedId();
                    String facebookCmtUrlScheme = "fb://page/" + crawlerItem.getFeedId();

                    try {
                        int versionCode = mContext.getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
                        Intent cmtMessage;

                        if (versionCode >= 3002850) {
                            Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookCmtUrl);
                            cmtMessage = new Intent(Intent.ACTION_VIEW, uri);
                        } else {

                            cmtMessage = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookCmtUrlScheme));
                        }

                        mContext.startActivity(cmtMessage);
                    } catch (PackageManager.NameNotFoundException | ActivityNotFoundException e) {
                        e.printStackTrace();
                        String groupId = crawlerItem.getFeedId().split("_")[0];
                        String feedId = crawlerItem.getFeedId().split("_")[1];

                        mContext.startActivity(new Intent(Intent.ACTION_VIEW
                                , Uri.parse("https://m.facebook.com/groups/" + groupId + "?view=permalink&id=" + feedId + "")));
                    }

                    break;
                case R.id.fb_call_btn:
                    if (crawlerItem.getPhoneNumbers().size() == 1) {
                        Utils.callPhone(mContext, crawlerItem.getPhoneNumbers().get(0));
                    } else {

                        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.NonActivityDialog));
                        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View dialogView = inflater.inflate(R.layout.popup_call_from_fb_feed, null);

                        builder.setView(dialogView);

                        final AlertDialog dialog = builder.create();
                        // Set the dialog negative button
                        LinearLayout container = (LinearLayout) dialogView.findViewById(R.id.container_call_popup);

                        for (final String phone : crawlerItem.getPhoneNumbers()) {
                            View callItem = inflater.inflate(R.layout.phone_number_item_layout, container, false);
                            ((TextView) callItem.findViewById(R.id.tv_phone_number)).setText(phone);
                            callItem.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Utils.callPhone(mContext, phone);
                                }
                            });
                            container.addView(callItem);
                        }

                        dialogView.findViewById(R.id.submit_change_shipping_cost).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                    break;
                case R.id.btn_pin_order:

                    if (crawlerItem.isPinned()) {

                        new RemovePinnedFbFeed(mContext, new OnSendRequestCompleted() {
                            @Override
                            public void onSendRequestCompleted(boolean error, String message) {
                                if (error) {
                                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                } else {
                                    crawlerItem.setPinned(false);
                                    Toast.makeText(mContext, "Đã bỏ ghim đơn hàng của " + crawlerItem.getCreatorName(), Toast.LENGTH_SHORT).show();
                                    notifyItemChanged(getAdapterPosition());
                                }
                            }
                        }).execute(
                                String.valueOf(mSessionMng.getShipperId())
                                , String.valueOf(crawlerItem.getId())
                                , crawlerItem.getFeedId());


                    } else {

                        new PinFacebookFeed(mContext, new OnSendRequestCompleted() {
                            @Override
                            public void onSendRequestCompleted(boolean error, String message) {
                                if (error) {
                                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                } else {
                                    crawlerItem.setPinned(true);
                                    Toast.makeText(mContext, "Đã ghim đơn hàng của " + crawlerItem.getCreatorName(), Toast.LENGTH_SHORT).show();
                                    notifyItemChanged(getAdapterPosition());
                                }
                            }
                        }).execute(
                                String.valueOf(mSessionMng.getShipperId())
                                , String.valueOf(crawlerItem.getId())
                                , crawlerItem.getFeedId());
                    }

                    break;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.facebook_crawler_item, parent, false);
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

            if (position == 0) {
                Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
                holder.itemView.startAnimation(animation);
            }

            MyViewHolder convertVh = (MyViewHolder) holder;

            FacebookFeedCrawlerItem crawlerItem = crawlerItems.get(position);

            convertVh.mTvCreator.setText(crawlerItem.getCreatorName());
            convertVh.mTvTimeCreated.setText(crawlerItem.getTimeCreated());
            convertVh.mTvDistance.setText(crawlerItem.getDistance());
            convertVh.mTvFeedContent.setText(crawlerItem.getContent());

            convertVh.mBtnCall.setVisibility(View.GONE);
            convertVh.mIvPinOrder.setVisibility(View.GONE);
            convertVh.mIvRemovePinned.setVisibility(View.GONE);
            convertVh.mBtnPin.setVisibility(View.GONE);

            if (crawlerItem.isShowPinnedIcon()) {
                convertVh.mBtnPin.setVisibility(View.VISIBLE);
            }

            if (crawlerItem.getPhoneNumbers().size() >= 1) {
                convertVh.mBtnCall.setVisibility(View.VISIBLE);
            }

            if (crawlerItem.isPinned()) {

                convertVh.mIvRemovePinned.setVisibility(View.VISIBLE);
            } else {

                convertVh.mIvPinOrder.setVisibility(View.VISIBLE);
            }

        } else {
            ((Utils.ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return crawlerItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return crawlerItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }
}

package fiveship.vn.fiveship.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.activity.NewsDetailActivity;
import fiveship.vn.fiveship.model.NewsItem;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by Unstopable on 6/7/2016.
 */
public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private ArrayList<NewsItem> newsItems;
    private Context mContext;

    public NewsAdapter(ArrayList<NewsItem> newsItems, Context mContext) {
        this.newsItems = newsItems;
        this.mContext = mContext;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView mTVTitle;
        TextView mTVDateCreated;
        ImageView mIVCover;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTVTitle = (TextView) itemView.findViewById(R.id.news_title);
            mTVDateCreated = (TextView) itemView.findViewById(R.id.news_date_created);
            mIVCover = (ImageView) itemView.findViewById(R.id.news_cover_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            final NewsItem newsItem = newsItems.get(getAdapterPosition());
            Intent detailIntent = new Intent(mContext, NewsDetailActivity.class);
            detailIntent.putExtra("newsId", newsItem.getId());
            mContext.startActivity(detailIntent);

            //Utils.shareJourneyDialog(mContext, "http://5ship.vn").show();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.other_news_item_layout, parent, false);
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
        if(holder instanceof MyViewHolder){
            MyViewHolder convertVh = (MyViewHolder) holder;
            NewsItem news = newsItems.get(position);
            convertVh.mTVTitle.setText(news.getTitle());
            convertVh.mTVDateCreated.setText(news.getDateCreated());
            Utils.setImageToImageView(mContext, news.getUrlImageCover(), convertVh.mIVCover);
        }else{

            ((Utils.ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return newsItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }
}

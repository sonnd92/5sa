package fiveship.vn.fiveship.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by Unstopable on 6/30/2016.
 */
public abstract class BaseRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS = 0;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            vh = getCustomViewHolder(parent);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.fiveship_progress_bar_footer, parent, false);

            vh = new Utils.ProgressViewHolder(itemView);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindMyViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return getTotal();
    }

    @Override
    public int getItemViewType(int position) {
        return getArrayList().get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    public abstract RecyclerView.ViewHolder setViewHolder();
    public abstract void bindMyViewHolder(RecyclerView.ViewHolder holder, int position);
    public abstract int getTotal();
    public abstract ArrayList getArrayList();
    public abstract RecyclerView.ViewHolder getCustomViewHolder(ViewGroup parent);
}

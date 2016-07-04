package fiveship.vn.fiveship.utility;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import org.json.JSONObject;

import java.util.ArrayList;

import fiveship.vn.fiveship.interfaces.OnTaskCompleted;

/**
 * Created by Unstoppable on 3/31/2016.
 */
public abstract class OnScrollListenerFactory extends RecyclerView.OnScrollListener {
    private boolean mIsLoading;
    private boolean mIsDataLoaded;
    private boolean mIsLoadMore;

    private int mCurrentPage;
    private int mPreLast;
    private int mPageSize;

    private Context mContext;
    private RecyclerView mRecyclerView;
    private OnTaskCompleted mListener;
    private ArrayList mData;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ItemTouchHelper mItemTouchHelper;

    public OnScrollListenerFactory(Context mContext) {
        this.mContext = mContext;
        mIsLoading = false;
        mIsDataLoaded = false;
        mIsLoadMore = true;
        mCurrentPage = 1;
        mPreLast = 0;
        mPageSize = 10;
        mData = new ArrayList<>();
    }

    public void preFetchData() {

        mRecyclerView = getRecyclerView();
        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if(mItemTouchHelper != null){
            mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        }

        mRecyclerView.addOnScrollListener(this);

        onPreLoad();

        mListener = new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList result, int total) {

                if (mIsDataLoaded) {
                    mData.clear();
                }

                if (result != null && result.size() > 0) {
                    mData.addAll(result);

                    if (mPageSize * mCurrentPage < total) {
                        mIsLoadMore = true;
                        mData.add(result.size(), null);
                    } else {
                        mIsLoadMore = false;
                    }
                }

                mAdapter = getAdapter(mData);

                mRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

                onLoaded(result != null && result.size() > 0, total);
                mIsDataLoaded = true;
            }
        };
    }

    public void refresh() {

        //set default params
        mCurrentPage = 1;
        mPreLast = 0;
        mPageSize = 10;

        fetchData();
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int visibleItemCount = mLayoutManager.getChildCount();
        int totalItemCount = mLayoutManager.getItemCount();
        int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

        if (mIsLoadMore && mIsDataLoaded) {

            final int lastItem = firstVisibleItem + visibleItemCount;

            if (lastItem == totalItemCount && lastItem != 0) {
                if (mPreLast < lastItem) {

                    mCurrentPage = ((lastItem - 1) / mPageSize) + 1;
                    mListener = new OnTaskCompleted() {
                        @Override
                        public void onTaskCompleted(ArrayList result, int total) {

                            mIsLoadMore = false;

                            if (mData.get(mData.size() - 1) == null) {
                                mData.remove(mData.size() - 1);
                                mAdapter.notifyItemRemoved(mData.size());
                            }

                            if (result.size() > 0) {
                                mData.addAll(result);

                                if (mPageSize * mCurrentPage < total) {
                                    mData.add(mData.size(), null);
                                    mIsLoadMore = true;
                                }

                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    };
                    getAsyncRequest().execute(String.valueOf(mCurrentPage), String.valueOf(mPageSize));
                    mPreLast = lastItem;
                }
            }
        }
    }

    public void fetchData() {

        preFetchData();
        getAsyncRequest().execute(String.valueOf(mCurrentPage), String.valueOf(mPageSize));
    }

    public void getIsLoading(boolean mIsLoading) {
        this.mIsLoading = mIsLoading;
    }

    public boolean isDataLoaded() {
        return mIsDataLoaded;
    }

    public void setCanLoadMore(boolean loadMore){
        this.mIsLoadMore = loadMore;
    }

    public boolean canLoadMore() {
        return mIsLoadMore;
    }

    public ArrayList getData() {
        return mData;
    }

    public void setData(ArrayList mData) {
        this.mData = mData;
    }

    public void notifyItemInserted(final int position) {
        mAdapter.notifyItemInserted(position);
        if(mData.size() == 1)
            onLoaded(true, 1);

        mPreLast = mData.size();

    }

    public void notifyItemRemoved(final int position) {
        mAdapter.notifyItemRemoved(position);
        if(mData.isEmpty())
            onLoaded(false, 0);

        mPreLast = mData.size();
    }

    public void notifyItemChanged(final int position, final Object item) {
        mAdapter.notifyItemChanged(position, item);
    }

    public void notifyItemSetChanged() {
        mAdapter.notifyDataSetChanged();
        mPreLast = mData.size();
    }


    public OnTaskCompleted getCallbackMethod() {
        return mListener;
    }

    public void setCurrentPage(int mCurrentPage) {
        this.mCurrentPage = mCurrentPage;
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper){
        this.mItemTouchHelper = itemTouchHelper;
    }

    public RecyclerView.Adapter getAdapter(){
        return mAdapter;
    }

    public void setRowPerPage(int mSkip) {
        this.mPageSize = mSkip;
    }
    public abstract void onPreLoad();

    public abstract void onLoaded(boolean mIsSuccess, int mTotal);

    public abstract void onScrolled(boolean mIsSuccess);

    public abstract RecyclerView.Adapter getAdapter(ArrayList data);

    public abstract AsyncTask<String, Void, JSONObject> getAsyncRequest();

    public abstract RecyclerView getRecyclerView();
}

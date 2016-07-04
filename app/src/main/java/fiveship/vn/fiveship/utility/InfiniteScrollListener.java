package fiveship.vn.fiveship.utility;

import android.widget.AbsListView;

/**
 * Created by BVN on 05/01/2016.
 */
public abstract class InfiniteScrollListener implements AbsListView.OnScrollListener {

    private int bufferItemCount = 10;
    private int currentPage = 0;
    private int itemCount = 0;
    private boolean isLoading = true;

    public InfiniteScrollListener(int bufferItemCount) {
        this.bufferItemCount = bufferItemCount;
    }

    public abstract void onLoadMore(int page, int totalItemsCount);

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Do Nothing
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        try{

            if (totalItemCount < itemCount) {
                this.itemCount = totalItemCount;
                if (totalItemCount == 0) {
                    this.isLoading = true; }
            }

            if (isLoading && (totalItemCount > itemCount)) {
                isLoading = false;
                itemCount = totalItemCount;
                currentPage++;
            }

            if (!isLoading && (totalItemCount - visibleItemCount)<=(firstVisibleItem + bufferItemCount)) {
                onLoadMore(currentPage + 1, totalItemCount);
                isLoading = true;
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void refresh(){
        this.currentPage = 1;
        this.itemCount = this.bufferItemCount;
    }

    public void setIsLoading(boolean isLoading){
        this.isLoading = isLoading;
    }

}

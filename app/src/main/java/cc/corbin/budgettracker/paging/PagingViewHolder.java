package cc.corbin.budgettracker.paging;

import android.support.v7.widget.RecyclerView;

public abstract class PagingViewHolder extends RecyclerView.ViewHolder
{
    protected PagingView _pagingView;

    public PagingViewHolder(PagingView pagingView)
    {
        super(pagingView);
        _pagingView = pagingView;
    }

    public abstract void setDate(int date);
}

package cc.corbin.budgettracker.day;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.time.LocalDate;

import cc.corbin.budgettracker.paging.PagingActivity;
import cc.corbin.budgettracker.paging.PagingView;
import cc.corbin.budgettracker.paging.PagingViewHolder;

public class DayRecyclerAdapter extends RecyclerView.Adapter
{
    private final static String TAG = "DayRecyclerAdapter";

    private PagingActivity _activity;

    public static class DayViewHolder extends PagingViewHolder
    {
        public DayViewHolder(PagingView pagingView)
        {
            super(pagingView);
            _pagingView = pagingView;
        }

        public void setDate(int date)
        {
            LocalDate time = LocalDate.ofEpochDay(date);
            _pagingView.setDate(time.getYear(), time.getMonthValue(), time.getDayOfMonth());
        }
    }

    public DayRecyclerAdapter(PagingActivity activity)
    {
        _activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        DayView v = new DayView(viewGroup.getContext(), _activity);
        DayViewHolder vh = new DayViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i)
    {
        // i represents the number of days since the UNIX epoch
        ((DayViewHolder)viewHolder).setDate(i);
    }

    @Override
    public int getItemCount()
    {
        return Integer.MAX_VALUE;
    }
}

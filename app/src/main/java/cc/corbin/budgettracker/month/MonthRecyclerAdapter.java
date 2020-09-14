package cc.corbin.budgettracker.month;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import cc.corbin.budgettracker.paging.PagingActivity;
import cc.corbin.budgettracker.paging.PagingViewHolder;

public class MonthRecyclerAdapter extends RecyclerView.Adapter
{
    private final static String TAG = "MonthRecyclerAdapter";

    private PagingActivity _activity;

    public static class MonthViewHolder extends PagingViewHolder
    {
        private int _date;
        private MonthView _monthView;

        public MonthViewHolder(MonthView monthView)
        {
            super(monthView);
            _monthView = monthView;
        }

        public void setDate(int date)
        {
            _date = date;
            int year = (_date / 12);
            int month = (_date % 12) + 1;
            _monthView.setDate(year, month);
        }

        public int getDate()
        {
            return _date;
        }
    }

    public MonthRecyclerAdapter(PagingActivity activity)
    {
        _activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        return (new MonthViewHolder(new MonthView(viewGroup.getContext(), _activity)));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i)
    {
        // i represents the number of months since 1
        ((MonthViewHolder)viewHolder).setDate(i);
    }

    @Override
    public int getItemCount()
    {
        return Integer.MAX_VALUE;
    }
}

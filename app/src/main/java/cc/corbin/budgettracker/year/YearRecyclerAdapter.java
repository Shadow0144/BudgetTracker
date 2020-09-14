package cc.corbin.budgettracker.year;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import cc.corbin.budgettracker.paging.PagingActivity;

public class YearRecyclerAdapter extends RecyclerView.Adapter
{
    private final static String TAG = "YearRecyclerAdapter";

    private PagingActivity _activity;

    public static class YearViewHolder extends RecyclerView.ViewHolder
    {
        private int _date;
        private YearView _yearView;

        public YearViewHolder(YearView yearView)
        {
            super(yearView);
            _yearView = yearView;
        }

        public void setDate(int date)
        {
            _date = date;
            _yearView.setDate(date);
        }

        public int getDate()
        {
            return _date;
        }
    }

    public YearRecyclerAdapter(PagingActivity activity)
    {
        _activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        return (new YearViewHolder(new YearView(viewGroup.getContext(), _activity)));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i)
    {
        // i represents the number of years since 0
        ((YearViewHolder)viewHolder).setDate(i);
    }

    @Override
    public int getItemCount()
    {
        return Integer.MAX_VALUE;
    }
}

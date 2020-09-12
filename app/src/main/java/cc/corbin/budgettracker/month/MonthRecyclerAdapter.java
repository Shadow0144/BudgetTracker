package cc.corbin.budgettracker.month;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;

import cc.corbin.budgettracker.auxilliary.PagingActivity;

public class MonthRecyclerAdapter extends RecyclerView.Adapter
{
    private final static String TAG = "MonthRecyclerAdapter";

    private PagingActivity _activity;

    public static class MonthViewHolder extends RecyclerView.ViewHolder
    {
        private MonthView _monthView;

        public MonthViewHolder(MonthView monthView)
        {
            super(monthView);
            _monthView = monthView;
        }

        public void setDate(int date)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, (date / 12));
            calendar.set(Calendar.DATE, 1); // In case the current month is short on weeks
            calendar.set(Calendar.MONTH, ((date % 12) -1));
            _monthView.setDate(calendar.get(Calendar.YEAR),
                    (calendar.get(Calendar.MONTH)+1));
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
        // i represents the number of months since 0
        ((MonthViewHolder)viewHolder).setDate(i);
    }

    @Override
    public int getItemCount()
    {
        return Integer.MAX_VALUE;
    }
}

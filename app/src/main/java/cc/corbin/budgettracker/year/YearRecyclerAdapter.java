package cc.corbin.budgettracker.year;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;

public class YearRecyclerAdapter extends RecyclerView.Adapter
{
    private final static String TAG = "YearRecyclerAdapter";

    public static class YearViewHolder extends RecyclerView.ViewHolder
    {
        private YearView _yearView;

        public YearViewHolder(YearView yearView)
        {
            super(yearView);
            _yearView = yearView;
        }

        public void setDate(int date)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, date);
            _yearView.setDate(calendar.get(Calendar.YEAR));
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        return (new YearViewHolder(new YearView(viewGroup.getContext())));
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

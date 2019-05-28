package cc.corbin.budgettracker.day;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;

public class DayRecyclerAdapter extends RecyclerView.Adapter
{
    private final static String TAG = "DayRecyclerAdapter";

    public static class DayViewHolder extends RecyclerView.ViewHolder
    {
        private DayView _dayView;

        public DayViewHolder(DayView dayView)
        {
            super(dayView);
            _dayView = dayView;
        }

        public void setDate(int date)
        {
            Date time = new Date(((long)date)*24*60*60*1000);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            _dayView.setDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH)+1,
                    calendar.get(Calendar.DATE));
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        DayView v = new DayView(viewGroup.getContext());
        DayViewHolder vh = new DayViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i)
    {
        // i represents the number of milliseconds since the UNIX epoch
        ((DayViewHolder)viewHolder).setDate(i);
    }

    @Override
    public int getItemCount()
    {
        return Integer.MAX_VALUE;
    }
}

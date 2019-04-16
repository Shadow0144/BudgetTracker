package cc.corbin.budgettracker.day;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;

public class DayRecyclerAdapter extends RecyclerView.Adapter
{
    private final static String TAG = "DayRecyclerAdapter";

    public static class DayViewHolder extends RecyclerView.ViewHolder
    {
        // each data item is just a string in this case
        private DayList _dayList;

        public DayViewHolder(DayList dayList)
        {
            super(dayList);
            _dayList = dayList;
        }

        public void setDate(int date)
        {
            Date time = new Date(((long)date)*24*60*60*1000);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            _dayList.setDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH)+1,
                    calendar.get(Calendar.DATE));
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        // create a new view
        DayList v = new DayList(viewGroup.getContext());
        DayViewHolder vh = new DayViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i)
    {
        ((DayViewHolder)viewHolder).setDate(i);
    }

    @Override
    public int getItemCount()
    {
        return Integer.MAX_VALUE;
    }
}

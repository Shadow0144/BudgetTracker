package cc.corbin.budgettracker.day;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Corbin on 1/26/2018.
 */

public class DayFragmentPagerAdapter extends FragmentPagerAdapter
{
    private static final String TAG = "DayFragmentPagerAdapter";

    private DayViewActivity _parent;

    private int _month;
    private int _year;

    private int _count; // Number of days in month

    private ArrayList<DayFragment> _fragments;

    DayFragmentPagerAdapter(DayViewActivity parent, FragmentManager fm, int month, int year)
    {
        super(fm);

        _parent = parent;

        _month = month;
        _year = year;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, _year);
        calendar.set(Calendar.MONTH, _month-1);
        _count = calendar.getActualMaximum(Calendar.DATE);

        _fragments = new ArrayList<DayFragment>();

        for (int i = 1; i <= _count; i++)
        {
            DayFragment fragment = new DayFragment();
            fragment.setParameters(this, _year, _month, i);
            _fragments.add(fragment);
        }
    }

    public int lastDay()
    {
        return _count;
    }

    @Override
    public DayFragment getItem(int position)
    {
        return _fragments.get(position);
    }

    @Override
    public int getCount()
    {
        return _fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return "Day " + (position + 1);
    }

    public void updateTotal(int day, float amount)
    {
        _parent.updateTotal(day, amount);
    }

    public void updateDay(int day)
    {
        _fragments.get(day).refreshView();
    }
}

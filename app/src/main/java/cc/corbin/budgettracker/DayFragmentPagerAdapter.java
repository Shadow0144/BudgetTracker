package cc.corbin.budgettracker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Corbin on 1/26/2018.
 */

public class DayFragmentPagerAdapter extends FragmentPagerAdapter
{
    private static final String TAG = "DayFragmentPagerAdapter";

    private FragmentManager _fm;

    private int _month;
    private int _year;

    private int _count; // Number of days in month

    private ArrayList<DayFragment> _fragments;

    DayFragmentPagerAdapter(FragmentManager fm, int month, int year)
    {
        super(fm);

        _fm = fm;

        _month = month;
        _year = year;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, _year);
        calendar.set(Calendar.MONTH, _month);
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

    public void addExpenditure(int index)
    {
        _fragments.get(index).addExpenditure();
    }

    public void updateExpenditureDatabase(int index)
    {
        _fragments.get(index).updateExpenditureDatabase();
    }

    @Override
    public Fragment getItem(int position)
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

    public void setExpenditures(List<List<ExpenditureEntity>> dailyExpenditures)
    {
        for (int i = 0; i < _count; i++)
        {
            _fragments.get(i).setExpenditures(dailyExpenditures.get(i));
        }
    }

    public List<ExpenditureEntity> getExpenditures(int index)
    {
        return _fragments.get(index).getExpenditures();
    }
}

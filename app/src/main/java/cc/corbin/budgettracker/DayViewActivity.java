package cc.corbin.budgettracker;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DayViewActivity extends AppCompatActivity
{
    private final String TAG = "DayViewActivity";

    public final static String DATE_INTENT = "Date";

    private TextView _dateView;
    private ViewPager _pagerView;
    private DayFragmentPagerAdapter _adapter;

    private Button _previousDay;
    private Button _nextDay;

    private Date _currentDate;

    private int _year;
    private int _month;
    private int _day;

    private static String[] _categories;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view);

        _dateView = findViewById(R.id.dateView);
        _pagerView = findViewById(R.id.itemsPager);

        _previousDay = findViewById(R.id.yesterdayButton);
        _nextDay = findViewById(R.id.tomorrowButton);

        loadCategories();

        _currentDate = new Date();
        _currentDate.setTime(getIntent().getLongExtra(DATE_INTENT, Calendar.getInstance().getTimeInMillis()));
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");

        _dateView.setText("Expenditures for: " + simpleDate.format(_currentDate));

        _year = _currentDate.getYear();
        _month = _currentDate.getMonth();
        _day = _currentDate.getDate();

        _adapter = new DayFragmentPagerAdapter(getSupportFragmentManager(), _currentDate.getMonth(), _currentDate.getYear());
        _pagerView.setAdapter(_adapter);
        _pagerView.setCurrentItem(_day);

        updateDay();
    }

    public void previousDay(View v)
    {
        _day--;

        updateDay();
    }

    public void nextDay(View v)
    {
        _day++;

        updateDay();
    }

    private void updateDay()
    {
        _pagerView.setCurrentItem(_day);

        if (_day == 1)
        {
            _previousDay.setEnabled(false);
            _nextDay.setEnabled(true);
        }
        else if (_day == _adapter.lastDay())
        {
            _previousDay.setEnabled(true);
            _nextDay.setEnabled(false);
        }
        else
        {
            _previousDay.setEnabled(true);
            _nextDay.setEnabled(true);
        }

        _currentDate = new Date(_year, _month, _day);
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");

        _dateView.setText("Expenditures for: " + simpleDate.format(_currentDate));
    }

    public void addItem(View v)
    {
        _adapter.addExpenditure(_pagerView.getCurrentItem());
    }

    private void loadCategories()
    {
        _categories = getResources().getStringArray(R.array.default_categories);
    }

    public static String[] getCategories()
    {
        return _categories;
    }
}

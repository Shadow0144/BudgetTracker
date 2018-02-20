package cc.corbin.budgettracker;

import android.content.Intent;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DayViewActivity extends AppCompatActivity
{
    private final String TAG = "DayViewActivity";

    public final static String DATE_INTENT = "Date";

    private TextView _dateView;
    private ViewPager _pagerView;
    private DayFragmentPagerAdapter _adapter;

    private Button _previousDay;
    private Button _nextDay;

    private Calendar _currentDate;

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

        _currentDate = Calendar.getInstance();

        _currentDate.setTimeInMillis(getIntent().getLongExtra(DATE_INTENT, Calendar.getInstance().getTimeInMillis()));
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");

        _dateView.setText(getString(R.string.expenses) + simpleDate.format(_currentDate.getTime()));

        _year = _currentDate.get(Calendar.YEAR);
        _month = _currentDate.get(Calendar.MONTH)+1;
        _day = _currentDate.get(Calendar.DATE);

        _adapter = new DayFragmentPagerAdapter(this, getSupportFragmentManager(), _month, _year);
        _pagerView.setAdapter(_adapter);
        _pagerView.setCurrentItem(_day);

        final Spinner totalCurrencySpinner = findViewById(R.id.totalCurrencySpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, Currencies.symbols);
        totalCurrencySpinner.setAdapter(spinnerArrayAdapter);
        totalCurrencySpinner.setSelection(Currencies.default_currency);

        final TextView totalAmountTextView = findViewById(R.id.totalAmountTextView);
        String cost;
        if (Currencies.integer[totalCurrencySpinner.getSelectedItemPosition()])
        {
            cost = String.format("%.00f", 0.0f);
        }
        else
        {
            cost = String.format("%.02f", 0.0f);
        }
        totalAmountTextView.setText(cost);

        updateDay();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        _adapter.updateExpenditureDatabase(_pagerView.getCurrentItem());
    }

    public void previousDay(View v)
    {
        _adapter.updateExpenditureDatabase(_pagerView.getCurrentItem());
        _day--;

        if (_day == 0)
        {
            Intent intent = new Intent(getApplicationContext(), DayViewActivity.class);
            Calendar date = Calendar.getInstance();
            date.set(_year, _month-1, 1);
            date.add(Calendar.DATE, -1);

            intent.putExtra(DATE_INTENT, date.getTimeInMillis());
            startActivity(intent);
            finish();
        }
        else
        {
            updateDay();
        }

        updateTotal();
    }

    public void nextDay(View v)
    {
        _adapter.updateExpenditureDatabase(_pagerView.getCurrentItem());
        _day++;

        if (_day == (_adapter.lastDay()+1))
        {
            Intent intent = new Intent(getApplicationContext(), DayViewActivity.class);
            Calendar date = Calendar.getInstance();
            date.set(_year, _month-1, _adapter.lastDay());
            date.add(Calendar.DATE, 1);

            intent.putExtra(DATE_INTENT, date.getTimeInMillis());
            startActivity(intent);
            finish();
        }
        else
        {
            updateDay();
        }

        updateTotal();
    }

    private void updateDay()
    {
        _pagerView.setCurrentItem(_day);

        if (_day == 1)
        {
            _previousDay.setText(getString(R.string.pprevious));
            _nextDay.setText(getString(R.string.next));
        }
        else if (_day == _adapter.lastDay())
        {
            _previousDay.setText(getString(R.string.previous));
            _nextDay.setText(getString(R.string.nnext));
        }
        else
        {
            _previousDay.setText(getString(R.string.previous));
            _nextDay.setText(getString(R.string.next));
        }

        _currentDate = Calendar.getInstance();
        _currentDate.set(_year, _month-1, _day);
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");

        _dateView.setText(simpleDate.format(_currentDate.getTime()));
    }

    public void addItem(View v)
    {
        try
        {
            _adapter.addExpenditure(_pagerView.getCurrentItem());
        }
        catch (IllegalStateException e)
        {
            Log.e(TAG, "Key was not unique");
        }
    }

    public void moveToMonthView(View v)
    {
        Intent intent = new Intent(getApplicationContext(), MonthViewActivity.class);
        intent.putExtra(MonthViewActivity.MONTH_INTENT, _month);
        intent.putExtra(MonthViewActivity.YEAR_INTENT, _year);
        startActivity(intent);
        //finish();
    }

    private void loadCategories()
    {
        _categories = getResources().getStringArray(R.array.default_categories);
    }

    public static String[] getCategories()
    {
        return _categories;
    }

    public void updateTotal()
    {
        float total = 0.0f;
        final TextView totalAmountTextView = findViewById(R.id.totalAmountTextView);
        final Spinner totalCurrencySpinner = findViewById(R.id.totalCurrencySpinner);
        List<ExpenditureEntity> expenditureEntities = _adapter.getExpenditures(_day);
        if (expenditureEntities != null)
        {
            int count = expenditureEntities.size();
            for (int i = 0; i < count; i++)
            {
                total += expenditureEntities.get(i).getAmount();
            }
            String cost;
            if (Currencies.integer[totalCurrencySpinner.getSelectedItemPosition()])
            {
                cost = String.format("%.00f", total);
            }
            else
            {
                cost = String.format("%.02f", total);
            }
            totalAmountTextView.setText(cost);
        }
        else { }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}

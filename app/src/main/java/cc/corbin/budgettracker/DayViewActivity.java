package cc.corbin.budgettracker;

import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.Toast;

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

    public final static int CREATE_EXPENDITURE = 0;
    public final static int EDIT_EXPENDITURE = 1;

    public final static int SUCCEED = 0;
    public final static int CANCEL = 1;
    public final static int DELETE = 2;
    public final static int FAILURE = -1;

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

        final ExpenditureViewModel viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(this), BudgetDatabase.getBudgetDatabase(this));

        loadCategories();

        _currentDate = Calendar.getInstance();

        _currentDate.setTimeInMillis(getIntent().getLongExtra(DATE_INTENT, Calendar.getInstance().getTimeInMillis()));
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");

        //_dateView.setText(getString(R.string.expenses) + simpleDate.format(_currentDate.getTime()));
        _dateView.setText(simpleDate.format(_currentDate.getTime()));

        _year = _currentDate.get(Calendar.YEAR);
        _month = _currentDate.get(Calendar.MONTH)+1;
        _day = _currentDate.get(Calendar.DATE);

        _adapter = new DayFragmentPagerAdapter(this, getSupportFragmentManager(), _month, _year);
        _pagerView.setAdapter(_adapter);
        _pagerView.setCurrentItem(_day-1);

        _pagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {

            }

            @Override
            public void onPageSelected(int position)
            {
                if (position < (_day-1))
                {
                    previousDay(null);
                }
                else if (position > (_day-1))
                {
                    nextDay(null);
                }
                else
                {
                    // Same date
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        // Set the arrows
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

        final Spinner totalCurrencySpinner = findViewById(R.id.totalCurrencySpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, Currencies.symbols);
        totalCurrencySpinner.setAdapter(spinnerArrayAdapter);
        totalCurrencySpinner.setSelection(Currencies.default_currency);

        final TextView totalAmountTextView = findViewById(R.id.totalAmountTextView);
        String cost = Currencies.formatCurrency(Currencies.integer[totalCurrencySpinner.getSelectedItemPosition()], 0.0f);
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
    }

    private void updateDay()
    {
        if (_pagerView.getCurrentItem() != (_day-1))
        {
            _pagerView.setCurrentItem(_day-1);

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
            _currentDate.set(_year, _month - 1, _day);
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy");

            _dateView.setText(simpleDate.format(_currentDate.getTime()));

            getUpdatedTotal(_day);
        }
        else { }
    }

    public void addItem(View v)
    {
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, _year);
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, _month);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, _day);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, CREATE_EXPENDITURE);
        startActivityForResult(intent, CREATE_EXPENDITURE);
    }

    public void editItem(int index, ExpenditureEntity exp)
    {
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.INDEX_INTENT, index);
        intent.putExtra(ExpenditureEditActivity.EXPENDITURE_INTENT, exp);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, EDIT_EXPENDITURE);
        startActivityForResult(intent, EDIT_EXPENDITURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == FAILURE)
        {
            Toast toast = Toast.makeText(this, getString(R.string.failure_expense), Toast.LENGTH_LONG);
            toast.show();
        }
        else
        {
            if (requestCode == CREATE_EXPENDITURE)
            {
                if (resultCode == SUCCEED)
                {
                    ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    if (expenditureEntity.getDay() == _day && expenditureEntity.getMonth() == _month && expenditureEntity.getYear() == _year)
                    {
                        _adapter.addExpenditure(_pagerView.getCurrentItem(), expenditureEntity);
                        getUpdatedTotal(_day);
                    }
                    else
                    {
                        Toast toast = Toast.makeText(this, getString(R.string.failure_create_expense), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                else { }
            }
            else if (requestCode == EDIT_EXPENDITURE)
            {
                if (resultCode == SUCCEED)
                {
                    ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    int index = data.getIntExtra(ExpenditureEditActivity.INDEX_INTENT, -1);
                    if (expenditureEntity != null && index != -1
                            && expenditureEntity.getDay() == _day && expenditureEntity.getMonth() == _month && expenditureEntity.getYear() == _year)
                    {
                        _adapter.updateExpenditure(_pagerView.getCurrentItem(), index, expenditureEntity);
                        getUpdatedTotal(_day);
                    }
                    else
                    {
                        Toast toast = Toast.makeText(this, getString(R.string.failure_edit_expense), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                else if (resultCode == DELETE) // Delete can only occur from an edit
                {
                    ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    int index = data.getIntExtra(ExpenditureEditActivity.INDEX_INTENT, -1);
                    if (expenditureEntity != null && index != -1
                            && expenditureEntity.getDay() == _day && expenditureEntity.getMonth() == _month && expenditureEntity.getYear() == _year)
                    {
                        _adapter.removeExpenditure(_pagerView.getCurrentItem(), index, expenditureEntity);
                        getUpdatedTotal(_day);
                    }
                    else
                    {
                        Toast toast = Toast.makeText(this, getString(R.string.failure_delete_expense), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                else { }
            }
            else { }
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

    public void updateTotal(int day, float amount)
    {
        if (day == _day)
        {
            final TextView totalAmountTextView = findViewById(R.id.totalAmountTextView);
            final Spinner totalCurrencySpinner = findViewById(R.id.totalCurrencySpinner);
            String cost = Currencies.formatCurrency(Currencies.integer[totalCurrencySpinner.getSelectedItemPosition()], amount);
            totalAmountTextView.setText(cost);
        }
        else { }
    }

    private void getUpdatedTotal(int day)
    {
        if (day == _day)
        {
            final TextView totalAmountTextView = findViewById(R.id.totalAmountTextView);
            final Spinner totalCurrencySpinner = findViewById(R.id.totalCurrencySpinner);
            float amount = _adapter.getItem(day-1).calculateTotal();
            String cost = Currencies.formatCurrency(Currencies.integer[totalCurrencySpinner.getSelectedItemPosition()], amount);
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

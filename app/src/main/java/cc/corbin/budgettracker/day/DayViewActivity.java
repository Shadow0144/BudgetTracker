package cc.corbin.budgettracker.day;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.custom.CreateCustomViewActivity;
import cc.corbin.budgettracker.edit.ExpenditureEditActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.importexport.ImportExportActivity;
import cc.corbin.budgettracker.search.CreateSearchActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.year.YearViewActivity;

public class DayViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
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

    private DrawerLayout _drawerLayout;

    private TextView _totalAmountTextView;

    private Calendar _currentDate;

    private int _year;
    private int _month;
    private int _day;

    private ExpenditureViewModel _viewModel;

    private BroadcastReceiver _receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try
        {
            ActionBar actionbar = getSupportActionBar();
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        catch (Exception e)
        {
            // Do nothing
        }
        _drawerLayout = findViewById(R.id.rootLayout);
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        _dateView = findViewById(R.id.dateView);
        _pagerView = findViewById(R.id.itemsPager);

        _previousDay = findViewById(R.id.yesterdayButton);
        _nextDay = findViewById(R.id.tomorrowButton);

        _receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // Update the view when necessary
                _adapter.getItem(_pagerView.getCurrentItem()).refreshView();
            }
        };

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(), BudgetDatabase.getBudgetDatabase());
        _viewModel.subscribeToUpdates(_receiver);

        if (!Categories.areCategoriesLoaded())
        {
            Categories.loadCategories(this);
        }
        else { }

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

        _totalAmountTextView = findViewById(R.id.totalAmountTextView);
        String cost = Currencies.formatCurrency(Currencies.default_currency, 0.0f);
        _totalAmountTextView.setText(cost);

        updateDay();
    }

    @Override
    protected void onPause()
    {
        _viewModel.unsubscribeToUpdates(_receiver);

        super.onPause();
    }

    @Override
    protected void onResume()
    {
        _viewModel.subscribeToUpdates(_receiver);

        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                _drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@Nullable MenuItem item)
    {
        boolean handled = false;
        if (item != null)
        {
            Intent intent;
            switch (item.getItemId())
            {
                case R.id.searchMenuItem:
                    intent = new Intent(getApplicationContext(), CreateSearchActivity.class);
                    startActivity(intent);
                    handled = true;
                    break;
                case R.id.dayMenuItem:
                    intent = new Intent(getApplicationContext(), DayViewActivity.class);
                    Calendar date = Calendar.getInstance();
                    date.set(_year, _month, _day);
                    intent.putExtra(DayViewActivity.DATE_INTENT, date.getTimeInMillis());
                    startActivity(intent);
                    handled = true;
                    break;
                case R.id.monthMenuItem:
                    intent = new Intent(getApplicationContext(), MonthViewActivity.class);
                    intent.putExtra(MonthViewActivity.YEAR_INTENT, _year);
                    intent.putExtra(MonthViewActivity.MONTH_INTENT, _month);
                    startActivity(intent);
                    handled = true;
                    break;
                case R.id.yearMenuItem:
                    intent = new Intent(getApplicationContext(), YearViewActivity.class);
                    intent.putExtra(YearViewActivity.YEAR_INTENT, _year);
                    startActivity(intent);
                    handled = true;
                    break;
                case R.id.totalMenuItem:
                    intent = new Intent(getApplicationContext(), TotalViewActivity.class);
                    startActivity(intent);
                    handled = true;
                    break;
                case R.id.customMenuItem:
                    intent = new Intent(getApplicationContext(), CreateCustomViewActivity.class);
                    startActivity(intent);
                    handled = true;
                    break;
                case R.id.settingsMenuItem:
                    intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                    handled = true;
                    break;
                case R.id.importExportMenuItem:
                    intent = new Intent(getApplicationContext(), ImportExportActivity.class);
                    startActivity(intent);
                    handled = true;
                    break;
            }

            if (handled)
            {
                _drawerLayout.closeDrawer(GravityCompat.START);
            }
            else { }

        }

        return handled;
    }

    public void previousDay(View v)
    {
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
            _pagerView.setCurrentItem(_day - 1);
        }
        else { }

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
                    //ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    //_viewModel.insertExpEntity(expenditureEntity);
                    _adapter.updateDay(_pagerView.getCurrentItem());
                }
                else { }
            }
            else if (requestCode == EDIT_EXPENDITURE)
            {
                if (resultCode == SUCCEED)
                {
                    //ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    //_viewModel.updateExpEntity(expenditureEntity);
                    _adapter.updateDay(_pagerView.getCurrentItem());
                }
                else if (resultCode == DELETE) // Delete can only occur from an edit
                {
                    //ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    //_viewModel.removeExpEntity(expenditureEntity);
                    _adapter.updateDay(_pagerView.getCurrentItem());
                }
                else { }
            }
            else { }

            if (resultCode == CANCEL)
            {
                // Do nothing
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

    public void updateTotal(int day, float amount)
    {
        if (day == _day)
        {
            String cost = Currencies.formatCurrency(Currencies.default_currency, amount);
            _totalAmountTextView.setText(cost);
        }
        else { }
    }

    private void getUpdatedTotal(int day)
    {
        updateTotal(day, _adapter.getItem(day-1).calculateTotal());
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable()
    {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }
}

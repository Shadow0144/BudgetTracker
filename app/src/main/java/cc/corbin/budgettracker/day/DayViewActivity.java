package cc.corbin.budgettracker.day;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.time.LocalDate;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.paging.PagingActivity;
import cc.corbin.budgettracker.edit.ExpenditureEditActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.setup.SetupActivity;
import cc.corbin.budgettracker.R;

public class DayViewActivity extends PagingActivity
{
    private final String TAG = "DayViewActivity";

    public final static String DAY_INTENT = "Day";
    public final static String MONTH_INTENT = "Month";
    public final static String YEAR_INTENT = "Year";

    public final static int CREATE_EXPENDITURE = 0;
    public final static int EDIT_EXPENDITURE = 1;

    public final static int SUCCEED = 0;
    public final static int CANCEL = 1;
    public final static int DELETE = 2;
    public final static int FAILURE = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setup();

        // Layout is set in parent class

        // Launch the first time setup activity if it has not yet created a shared preferences file
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.budget_tracker_preferences_key), 0);
        if (!sharedPreferences.contains(getString(R.string.language_key)))
        {
            // launchFirstTimeSetup(); // TODO
        }
        else { }

        final Button addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setVisibility(View.VISIBLE);

        LocalDate now = LocalDate.now();
        int day = getIntent().getIntExtra(DAY_INTENT, now.getDayOfMonth());
        int month = getIntent().getIntExtra(MONTH_INTENT, now.getMonthValue());
        int year = getIntent().getIntExtra(YEAR_INTENT, now.getYear());
        _currentDate = LocalDate.of(year, month, day);

        setupDayView();

        if (!Categories.areCategoriesLoaded())
        {
            Categories.loadCategories(this);
        }
        else { }
    }

    private void launchFirstTimeSetup()
    {
        Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
        startActivity(intent);
    }

    // Returns the set date as an int for use by the RecyclerAdapter
    private long getDate()
    {
        return _currentDate.toEpochDay(); //((int)(_currentDate.getTimeInMillis() / 1000 / 60 / 60 / 24));
    }

    // Returns the current date as a long int for use by the RecyclerAdapter
    private long getToday()
    {
        return LocalDate.now().toEpochDay(); //((int)(Calendar.getInstance().getTimeInMillis() / 1000 / 60 / 60 / 24));
    }

    private void setupDayView()
    {
        final DayRecyclerAdapter adapter = new DayRecyclerAdapter(this);
        setupAdapterView(adapter);
        _recyclerView.scrollToPosition((int)getDate());
    }

    public void addItem(View v)
    {
        DayView dayView = (DayView)_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition());
        LocalDate date = dayView.getDate();
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, date.getYear());
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, date.getMonthValue());
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, date.getDayOfMonth());
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, CREATE_EXPENDITURE);
        startActivityForResult(intent, CREATE_EXPENDITURE);
    }

    public void editItem(ExpenditureEntity exp)
    {
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.EXPENDITURE_INTENT, exp);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, EDIT_EXPENDITURE);
        startActivityForResult(intent, EDIT_EXPENDITURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == SettingsActivity.DATABASE_UPDATE_INTENT_FLAG)
        {
            _recyclerView.getAdapter().notifyDataSetChanged();
        }
        else if (requestCode == SettingsActivity.DATABASE_NO_UPDATE_INTENT_FLAG)
        {
            // Do nothing
        }
        else
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
                    _recyclerView.getAdapter().notifyDataSetChanged();
                }
                else { }
            }
            else if (requestCode == EDIT_EXPENDITURE)
            {
                if (resultCode == SUCCEED)
                {
                    _recyclerView.getAdapter().notifyDataSetChanged();
                }
                else if (resultCode == DELETE) // Delete can only occur from an edit
                {
                    _recyclerView.getAdapter().notifyDataSetChanged();
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
        DayView dayView = (DayView)_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition());
        LocalDate date = dayView.getDate();
        Intent intent = new Intent(getApplicationContext(), MonthViewActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, date.getYear());
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, date.getMonthValue());
        startActivity(intent);
    }

    public void currentView(View v)
    {
        _recyclerView.smoothScrollToPosition((int)getToday());
    }
}

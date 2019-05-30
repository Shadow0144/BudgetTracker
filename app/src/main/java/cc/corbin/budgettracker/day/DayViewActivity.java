package cc.corbin.budgettracker.day;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.NavigationActivity;
import cc.corbin.budgettracker.auxilliary.PagingActivity;
import cc.corbin.budgettracker.edit.ExpenditureEditActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.month.MonthViewActivity;
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

    private Calendar _currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setup();

        final Button addItemButton = findViewById(R.id.addItemButton);
        addItemButton.setVisibility(View.VISIBLE);

        int day = getIntent().getIntExtra(DAY_INTENT, Calendar.getInstance().get(Calendar.DATE));
        int month = getIntent().getIntExtra(MONTH_INTENT, Calendar.getInstance().get(Calendar.MONTH)+1); // Add 1 in case the intent is set or not
        int year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));
        _currentDate = Calendar.getInstance();
        _currentDate.set(year, month-1, day);

        setupDayView();

        if (!Categories.areCategoriesLoaded())
        {
            Categories.loadCategories(this);
        }
        else { }
    }

    private void setupDayView()
    {
        final DayRecyclerAdapter adapter = new DayRecyclerAdapter();
        setupAdapterView(adapter);
        int time = ((int)(_currentDate.getTimeInMillis() / 1000 / 60 / 60 / 24));
        _recyclerView.scrollToPosition(time);
    }

    public void addItem(View v)
    {
        DayView dayView = (DayView)_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition());
        Calendar date = dayView.getDate();
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, date.get(Calendar.YEAR));
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, date.get(Calendar.MONTH)+1);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, date.get(Calendar.DATE));
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
        Calendar date = dayView.getDate();
        Intent intent = new Intent(getApplicationContext(), MonthViewActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, date.get(Calendar.YEAR));
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, date.get(Calendar.MONTH)+1);
        startActivity(intent);
    }
}

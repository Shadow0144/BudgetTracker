package cc.corbin.budgettracker.day;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.NavigationActivity;
import cc.corbin.budgettracker.auxilliary.NavigationDrawerHelper;
import cc.corbin.budgettracker.edit.ExpenditureEditActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.R;

public class DayViewActivity extends NavigationActivity
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

    private RecyclerView _recyclerView;
    private LinearLayoutManager _layoutManager;

    private Calendar _currentDate;

    private ExpenditureViewModel _viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_day_view);
        super.onCreate(savedInstanceState);

        int day = getIntent().getIntExtra(DAY_INTENT, Calendar.getInstance().get(Calendar.DATE));
        int month = getIntent().getIntExtra(MONTH_INTENT, Calendar.getInstance().get(Calendar.MONTH)+1); // Add 1 in case the intent is set or not
        int year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));
        _currentDate = Calendar.getInstance();
        _currentDate.set(year, month-1, day);

        setupDayView();

        _viewModel = ExpenditureViewModel.getInstance();

        if (!Categories.areCategoriesLoaded())
        {
            Categories.loadCategories(this);
        }
        else { }
    }

    private void setupDayView()
    {
        _recyclerView = findViewById(R.id.itemsPager);
        _recyclerView.setBackgroundColor(Color.BLACK);
        _recyclerView.setHasFixedSize(true);
        _layoutManager = new LinearLayoutManager(this);
        _layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        _recyclerView.setLayoutManager(_layoutManager);
        final DayRecyclerAdapter adapter = new DayRecyclerAdapter();
        _recyclerView.setAdapter(adapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(_recyclerView);
        int time = ((int)(_currentDate.getTimeInMillis() / 1000 / 60 / 60 / 24));
        _recyclerView.scrollToPosition(time);
    }

    public void previousDay(View v)
    {
        _recyclerView.smoothScrollToPosition(_layoutManager.findFirstVisibleItemPosition()-1);
    }

    public void nextDay(View v)
    {
        _recyclerView.smoothScrollToPosition(_layoutManager.findFirstVisibleItemPosition()+1);
    }

    public void addItem(View v)
    {
        DayList dayList = (DayList)_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition());
        Calendar date = dayList.getDate();
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, date.get(Calendar.YEAR));
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, date.get(Calendar.MONTH)+1);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, date.get(Calendar.DATE));
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
        DayList dayList = (DayList)_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition());
        Calendar date = dayList.getDate();
        Intent intent = new Intent(getApplicationContext(), MonthViewActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, date.get(Calendar.YEAR));
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, date.get(Calendar.MONTH)+1);
        startActivity(intent);
        //finish();
    }
}

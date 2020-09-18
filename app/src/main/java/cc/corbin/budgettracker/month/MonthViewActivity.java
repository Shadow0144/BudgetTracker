package cc.corbin.budgettracker.month;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import java.time.LocalDate;

import cc.corbin.budgettracker.paging.PagingActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.year.YearViewActivity;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.R;

/**
 * Created by Corbin on 1/28/2018.
 */

public class MonthViewActivity extends PagingActivity
{
    private final String TAG = "MonthViewActivity";

    public final static String MONTH_INTENT = "Month";
    public final static String YEAR_INTENT = "Year";

    public final static int CREATE_EXT_EXPENDITURE = 0;
    public final static int EDIT_EXT_EXPENDITURE = 1;
    public final static int CREATE_ADJUSTMENT = 2;
    public final static int EDIT_ADJUSTMENT = 3;

    public final static int SUCCEED = 0;
    public final static int CANCEL = 1;
    public final static int DELETE = 2;
    public final static int FAILURE = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setup();

        _hasUpButton = true;

        int month = getIntent().getIntExtra(MONTH_INTENT, LocalDate.now().getMonthValue());
        int year = getIntent().getIntExtra(YEAR_INTENT, LocalDate.now().getYear());
        _currentDate = LocalDate.of(year, month, 1);

        setupMonthView();

        _dateTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectJumpDate(null);
            }
        });
    }

    @Override
    protected void setupAdapterView(RecyclerView.Adapter recyclerAdapter) // TODO
    {
        super.setupAdapterView(recyclerAdapter);
        //_recyclerView.setHasFixedSize(false); // Because the keyboard opens and closes, we need to set this to false here
    }

    private void setupMonthView()
    {
        final MonthRecyclerAdapter adapter = new MonthRecyclerAdapter(this);
        setupAdapterView(adapter);
        int time = (_currentDate.getYear() * 12) + (_currentDate.getMonthValue() - 1);
        _recyclerView.scrollToPosition(time);
    }

    public void createExtraExpenditure()
    {
        ((MonthView)(_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition())))
                .createExtraExpenditure();
    }

    public void editExtraExpenditure(ExpenditureEntity entity)
    {
        ((MonthView)(_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition())))
                .editExtraExpenditure(entity);
    }

    public void editBudgetItem(int categoryNumber)
    {
        final MonthView monthView = ((MonthView)(_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition())));
        monthView.editBudgetItem(monthView.getBudgets(), categoryNumber, monthView.getYear(), monthView.getMonth());
    }

    public void confirmBudgetItemEdit(View v)
    {
        ((MonthView)(_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition())))
                .confirmBudgetItemEdit(v);

    }

    public void cancelBudgetItemEdit(View v)
    {
        ((MonthView)(_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition())))
                .cancelBudgetItemEdit(v);
    }

    public void removeBudgeItem(View v)
    {
        ((MonthView)(_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition())))
                .removeBudgeItem(v);
    }

    public void createAdjustmentExpenditure(int category)
    {
        ((MonthView)(_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition())))
                .createAdjustmentExpenditure(category);
    }

    public void editAdjustmentExpenditure(BudgetEntity entity, int groupIndex, int childIndex)
    {
        ((MonthView)(_layoutManager.findViewByPosition(_layoutManager.findFirstVisibleItemPosition())))
                .editAdjustmentExpenditure(entity, groupIndex, childIndex);
    }

    public void moveToYearView(View v)
    {
        Intent intent = new Intent(getApplicationContext(), YearViewActivity.class);
        intent.putExtra(YearViewActivity.YEAR_INTENT, _currentDate.getYear());
        startActivity(intent);
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
            if (requestCode == CREATE_EXT_EXPENDITURE || requestCode == CREATE_ADJUSTMENT ||
                    requestCode == EDIT_EXT_EXPENDITURE || requestCode == EDIT_ADJUSTMENT)
            {
                if (resultCode == SUCCEED || resultCode == DELETE)
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

    public void selectJumpDate(View v)
    {
        createDialogWithoutDateField().show();
    }

    public void currentView(View v)
    {
        int time = (_currentDate.getYear() * 12) + (_currentDate.getMonthValue() - 1);
        _recyclerView.smoothScrollToPosition(time);
    }

    @Override
    protected void setupUpButton()
    {
        _upButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                moveToYearView(null);
                return true;
            }
        });
    }

    private DatePickerDialog createDialogWithoutDateField()
    {
        DatePickerDialog dpd = new DatePickerDialog(this, null, 2014, 1, 24);
        View day = dpd.getDatePicker().findViewById(Resources.getSystem().getIdentifier("day", "id", "android"));
        day.setVisibility(View.GONE);
        return dpd;
    }
}

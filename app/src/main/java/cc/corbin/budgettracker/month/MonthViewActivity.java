package cc.corbin.budgettracker.month;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.NavigationActivity;
import cc.corbin.budgettracker.auxilliary.LineGraph;
import cc.corbin.budgettracker.auxilliary.PagingActivity;
import cc.corbin.budgettracker.day.DayRecyclerAdapter;
import cc.corbin.budgettracker.edit.AdjustmentEditActivity;
import cc.corbin.budgettracker.auxilliary.PieChart;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedEditText;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.tables.ExpandableBudgetTable;
import cc.corbin.budgettracker.tables.ExtrasTable;
import cc.corbin.budgettracker.edit.ExpenditureEditActivity;
import cc.corbin.budgettracker.tables.CategorySummaryTable;
import cc.corbin.budgettracker.tables.WeeklySummaryTable;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.year.YearViewActivity;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

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

    private Calendar _currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        int month = getIntent().getIntExtra(MONTH_INTENT, Calendar.getInstance().get(Calendar.MONTH)+1);
        int year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));
        _currentDate = Calendar.getInstance();
        _currentDate.set(year, month-1, 1);

        setupMonthView();
    }

    private void setupMonthView()
    {
        final MonthRecyclerAdapter adapter = new MonthRecyclerAdapter();
        setupAdapterView(adapter);
        int time = (_currentDate.get(Calendar.YEAR)*12) + (_currentDate.get(Calendar.MONTH)+1);
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
        intent.putExtra(YearViewActivity.YEAR_INTENT, _currentDate.get(Calendar.YEAR));
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
}

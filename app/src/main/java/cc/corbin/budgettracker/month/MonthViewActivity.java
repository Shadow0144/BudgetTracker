package cc.corbin.budgettracker.month;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import cc.corbin.budgettracker.edit.AdjustmentEditActivity;
import cc.corbin.budgettracker.auxilliary.PieChart;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedEditText;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.tables.ExpandableBudgetTable;
import cc.corbin.budgettracker.tables.ExtrasTable;
import cc.corbin.budgettracker.edit.ExpenditureEditActivity;
import cc.corbin.budgettracker.tables.CategorySummaryTable;
import cc.corbin.budgettracker.tables.TimeSummaryTable;
import cc.corbin.budgettracker.tables.WeeklySummaryTable;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.year.YearViewActivity;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 1/28/2018.
 */

public class MonthViewActivity extends NavigationActivity
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

    private int _month;
    private int _year;

    private WeeklySummaryTable _weeklyTable;
    private CategorySummaryTable _categoryTable;
    private ExtrasTable _extrasTable;
    private ExpandableBudgetTable _expandableBudgetTable;

    private PieChart _weeklyPieChart;
    private PieChart _categoryPieChart;

    private LineGraph _weeklyLineGraph;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _monthExps;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    private MutableLiveData<float[]> _weeklyAmounts;
    private MutableLiveData<float[]> _categoricalAmounts;

    private MonthEditBudgetItemHelper _monthEditBudgetItemHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_month_view);
        super.onCreate(savedInstanceState);

        _month = getIntent().getIntExtra(MONTH_INTENT, Calendar.getInstance().get(Calendar.MONTH)+1);
        _year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));

        _viewModel = ExpenditureViewModel.getInstance();

        setupHeader();

        setupObservers();

        setupViews();

        _viewModel.getMonth(_monthExps, _year, _month);
    }

    public void refreshView()
    {
        _weeklyTable.resetTable();
        _categoryTable.resetTable();
        _expandableBudgetTable.resetTable();
        _weeklyPieChart.clearData();
        _categoryPieChart.clearData();
        _weeklyLineGraph.clearData();

        _viewModel.getMonth(_monthExps, _year, _month);
    }

    private void setupHeader()
    {
        TextView header = findViewById(R.id.monthView);
        DateFormatSymbols dfs = new DateFormatSymbols();
        header.setText(dfs.getMonths()[_month-1] + " " + _year);
        header.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), YearViewActivity.class);
                intent.putExtra(YearViewActivity.YEAR_INTENT, _year);
                startActivity(intent);
            }
        });
    }

    private void setupObservers()
    {
        final Observer<List<ExpenditureEntity>> entityObserver = new Observer<List<ExpenditureEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<ExpenditureEntity> expenditureEntities)
            {
                if (expenditureEntities != null)
                {
                    monthExpsLoaded(expenditureEntities);
                }
                else { }
            }
        };

        final Observer<List<BudgetEntity>> budgetObserver = new Observer<List<BudgetEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<BudgetEntity> budgetEntities)
            {
                if (budgetEntities != null) // returning from a query
                {
                    monthBudgetsLoaded(budgetEntities);
                }
                else { }
            }
        };

        final Observer<float[]> weeklyAmountsObserver = new Observer<float[]>()
        {
            @Override
            public void onChanged(@Nullable float[] amounts)
            {
                _weeklyTable.updateExpenditures(amounts);

                String[] weekLabels = new String[7];
                weekLabels[0] = getString(R.string.extras);
                weekLabels[1] = "Week 1";
                weekLabels[2] = "Week 2";
                weekLabels[3] = "Week 3";
                weekLabels[4] = "Week 4";
                weekLabels[5] = "Week 5";
                weekLabels[6] = "Adjustments";

                _weeklyPieChart.setData(amounts, weekLabels);
                _weeklyLineGraph.setData(amounts, weekLabels);
            }
        };

        final Observer<float[]> categoricalAmountsObserver = new Observer<float[]>()
        {
            @Override
            public void onChanged(@Nullable float[] amounts)
            {
                _categoryTable.updateExpenditures(amounts);

                String[] categoryLabels = Categories.getCategories();
                _categoryPieChart.setData(amounts, categoryLabels);
            }
        };

        _monthExps = new MutableLiveData<List<ExpenditureEntity>>();
        _monthExps.observe(this, entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(this, budgetObserver);

        _weeklyAmounts = new MutableLiveData<float[]>();
        _weeklyAmounts.observe(this, weeklyAmountsObserver);
        _categoricalAmounts = new MutableLiveData<float[]>();
        _categoricalAmounts.observe(this, categoricalAmountsObserver);
    }

    private void setupViews()
    {
        FrameLayout monthsWeeklyContainer = findViewById(R.id.monthWeeklyHolder);
        _weeklyTable = new WeeklySummaryTable(this, false);
        monthsWeeklyContainer.addView(_weeklyTable);

        FrameLayout monthsCategoryContainer = findViewById(R.id.monthCategoryHolder);
        _categoryTable = new CategorySummaryTable(this);
        monthsCategoryContainer.addView(_categoryTable);

        FrameLayout expandableBudgetContainer = findViewById(R.id.monthExpandableBudgetHolder);
        _expandableBudgetTable = new ExpandableBudgetTable(this, _month, _year);
        expandableBudgetContainer.addView(_expandableBudgetTable);

        FrameLayout weeklyPieContainer = findViewById(R.id.monthWeeklyPieHolder);
        _weeklyPieChart = new PieChart(this);
        _weeklyPieChart.setTitle(getString(R.string.weekly_spending));
        weeklyPieContainer.addView(_weeklyPieChart);

        FrameLayout categoryPieContainer = findViewById(R.id.monthCategoryPieHolder);
        _categoryPieChart = new PieChart(this);
        _categoryPieChart.setTitle(getString(R.string.categorical_spending));
        categoryPieContainer.addView(_categoryPieChart);

        FrameLayout weeklyLineGraphHolder = findViewById(R.id.monthWeeklyLineGraphHolder);
        _weeklyLineGraph = new LineGraph(this);
        _weeklyLineGraph.setTitle(getString(R.string.weekly_spending));
        weeklyLineGraphHolder.addView(_weeklyLineGraph);

        FrameLayout extrasContainer = findViewById(R.id.monthExtraHolder);
        _extrasTable = new ExtrasTable(this, _year, _month);
        extrasContainer.addView(_extrasTable);
    }

    private void monthExpsLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        _extrasTable.updateExpenditures(expenditureEntities);

        final SummationAsyncTask weeklyAsyncTask = new SummationAsyncTask(SummationAsyncTask.summationType.weekly, _weeklyAmounts);
        final SummationAsyncTask categoricalAsyncTask = new SummationAsyncTask(SummationAsyncTask.summationType.categorically, _categoricalAmounts);
        weeklyAsyncTask.execute(expenditureEntities);
        categoricalAsyncTask.execute(expenditureEntities);

        _viewModel.getMonthBudget(_budgets, _year, _month);
    }

    private void monthBudgetsLoaded(List<BudgetEntity> budgetEntities)
    {
        _weeklyTable.updateBudgets(budgetEntities);
        _categoryTable.updateBudgets(budgetEntities);
        _expandableBudgetTable.refreshTable(budgetEntities);

        // Create a budget line for the line graph
        float budget = 0;
        int size = budgetEntities.size();
        for (int i = 0; i < size; i++)
        {
            budget += budgetEntities.get(i).getAmount();
        }
        float[] guidelineAmounts = new float[] {budget / 5}; // TODO
        String[] guidelineLabels = new String[] { getString(R.string.budget) };
        _weeklyLineGraph.addGuildelines(guidelineAmounts, guidelineLabels);
    }

    public void previousMonth(View v)
    {
        Intent intent = new Intent(getApplicationContext(), MonthViewActivity.class);
        if (_month > 1)
        {
            intent.putExtra(MonthViewActivity.MONTH_INTENT, _month - 1);
            intent.putExtra(MonthViewActivity.YEAR_INTENT, _year);
        }
        else
        {
            intent.putExtra(MonthViewActivity.MONTH_INTENT, 12);
            intent.putExtra(MonthViewActivity.YEAR_INTENT, _year - 1);
        }
        startActivity(intent);
        finish();
    }

    public void nextMonth(View v)
    {
        Intent intent = new Intent(getApplicationContext(), MonthViewActivity.class);
        if (_month < 12)
        {
            intent.putExtra(MonthViewActivity.MONTH_INTENT, _month + 1);
            intent.putExtra(MonthViewActivity.YEAR_INTENT, _year);
        }
        else
        {
            intent.putExtra(MonthViewActivity.MONTH_INTENT, 1);
            intent.putExtra(MonthViewActivity.YEAR_INTENT, _year + 1);
        }
        startActivity(intent);
        finish();
    }

    public void editBudgetItem(int id)
    {
        _monthEditBudgetItemHelper = new MonthEditBudgetItemHelper(this, _budgets, id, _year, _month, _viewModel);
    }

    public void confirmBudgetItemEdit(View v)
    {
        _monthEditBudgetItemHelper.confirmBudgetItemEdit(v);
        _expandableBudgetTable.clearBudgetEntity(_monthEditBudgetItemHelper.getBudgetId());
    }

    public void cancelBudgetItemEdit(View v)
    {
        _monthEditBudgetItemHelper.cancelBudgetItemEdit(v);
    }

    public void removeBudgeItem(View v)
    {
        _monthEditBudgetItemHelper.removeBudgeItem(v);
        _expandableBudgetTable.clearBudgetEntity(_monthEditBudgetItemHelper.getBudgetId());
    }

    public void createExtraExpenditure()
    {
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, _year);
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, _month);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, 0);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, CREATE_EXT_EXPENDITURE);
        startActivityForResult(intent, CREATE_EXT_EXPENDITURE);
    }

    public void editExtraExpenditure(ExpenditureEntity entity)
    {
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, _year);
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, _month);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, 0);
        intent.putExtra(ExpenditureEditActivity.EXPENDITURE_INTENT, entity);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, EDIT_EXT_EXPENDITURE);
        startActivityForResult(intent, EDIT_EXT_EXPENDITURE);
    }

    public void createAdjustmentExpenditure(int category)
    {
        Intent intent = new Intent(getApplicationContext(), AdjustmentEditActivity.class);
        intent.putExtra(AdjustmentEditActivity.YEAR_INTENT, _year);
        intent.putExtra(AdjustmentEditActivity.MONTH_INTENT, _month);
        intent.putExtra(AdjustmentEditActivity.CATEGORY_INTENT, category);
        intent.putExtra(AdjustmentEditActivity.TYPE_INTENT, CREATE_ADJUSTMENT);
        startActivityForResult(intent, CREATE_ADJUSTMENT);
    }

    public void editAdjustmentExpenditure(BudgetEntity entity, int groupIndex, int childIndex)
    {
        Intent intent = new Intent(getApplicationContext(), AdjustmentEditActivity.class);
        intent.putExtra(AdjustmentEditActivity.YEAR_INTENT, _year);
        intent.putExtra(AdjustmentEditActivity.MONTH_INTENT, _month);
        intent.putExtra(AdjustmentEditActivity.BUDGET_INTENT, entity);
        intent.putExtra(AdjustmentEditActivity.GROUP_INDEX_INTENT, groupIndex);
        intent.putExtra(AdjustmentEditActivity.CHILD_INDEX_INTENT, childIndex);
        intent.putExtra(AdjustmentEditActivity.TYPE_INTENT, EDIT_ADJUSTMENT);
        startActivityForResult(intent, EDIT_ADJUSTMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == SettingsActivity.DATABASE_UPDATE_INTENT_FLAG)
        {
            refreshView();
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
                    refreshView();
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

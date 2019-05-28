package cc.corbin.budgettracker.month;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.LineGraph;
import cc.corbin.budgettracker.auxilliary.PieChart;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask;
import cc.corbin.budgettracker.edit.AdjustmentEditActivity;
import cc.corbin.budgettracker.edit.ExpenditureEditActivity;
import cc.corbin.budgettracker.tables.ExpandableBudgetTable;
import cc.corbin.budgettracker.tables.ExtrasTable;
import cc.corbin.budgettracker.tables.CategorySummaryTable;
import cc.corbin.budgettracker.tables.WeeklySummaryTable;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 1/28/2018.
 */

public class MonthView extends LinearLayout
{
    private final String TAG = "MonthView";

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

    private final int DATE_TEXT_TOP_PADDING = 24;
    private final int DATE_TEXT_BOT_PADDING = 36;

    private final int DATE_TEXT_SIZE = 24;

    private Context _context;

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

    private TextView _dateTextView;

    private boolean _firstRun;

    public MonthView(Context context)
    {
        super(context);
        _context = context;

        _firstRun = true;

        setOrientation(VERTICAL);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(params);

        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.view_month, null);
        addView(view);
    }

    public void setDate(int year, int month)
    {
        _year = year;
        _month = month;

        _viewModel = ExpenditureViewModel.getInstance();

        if (_firstRun)
        {
            setupHeader();
            setupObservers();
            setupViews();
            _firstRun = false;
        }
        else { }

        refreshView();
    }

    public void refreshView()
    {
        DateFormatSymbols dfs = new DateFormatSymbols();
        _dateTextView.setText(dfs.getMonths()[_month-1] + " " + _year);

        _weeklyTable.resetTable();
        _categoryTable.resetTable();
        _expandableBudgetTable.resetTable();
        _weeklyPieChart.clearData();
        _categoryPieChart.clearData();
        _weeklyLineGraph.clearData();

        _viewModel.getMonth(_monthExps, _year, _month);
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
                weekLabels[0] = _context.getString(R.string.extras);
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
        _monthExps.observe(((FragmentActivity)_context), entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(((FragmentActivity)_context), budgetObserver);

        _weeklyAmounts = new MutableLiveData<float[]>();
        _weeklyAmounts.observe(((FragmentActivity)_context), weeklyAmountsObserver);
        _categoricalAmounts = new MutableLiveData<float[]>();
        _categoricalAmounts.observe(((FragmentActivity)_context), categoricalAmountsObserver);
    }

    private void setupHeader() // TODO ?
    {
        _dateTextView = findViewById(R.id.dateTextView);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(1, 1, 1, 0);
        _dateTextView.setLayoutParams(params);
        _dateTextView.setPadding(0, DATE_TEXT_TOP_PADDING, 0, DATE_TEXT_BOT_PADDING);
        _dateTextView.setBackgroundColor(_context.getColor(R.color.colorPrimaryDark));
        _dateTextView.setTextColor(Color.WHITE);
        _dateTextView.setTextSize(DATE_TEXT_SIZE);
        _dateTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        _dateTextView.setTypeface(_dateTextView.getTypeface(), Typeface.BOLD);
        _dateTextView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((MonthViewActivity)_context).moveToYearView(null);
            }
        });
    }

    private void setupViews()
    {
        FrameLayout monthsWeeklyContainer = findViewById(R.id.monthWeeklyHolder);
        _weeklyTable = new WeeklySummaryTable(((FragmentActivity)_context), _year, _month);
        monthsWeeklyContainer.addView(_weeklyTable);

        FrameLayout monthsCategoryContainer = findViewById(R.id.monthCategoryHolder);
        _categoryTable = new CategorySummaryTable(((FragmentActivity)_context));
        monthsCategoryContainer.addView(_categoryTable);

        FrameLayout expandableBudgetContainer = findViewById(R.id.monthExpandableBudgetHolder);
        _expandableBudgetTable = new ExpandableBudgetTable(((FragmentActivity)_context), _month, _year);
        expandableBudgetContainer.addView(_expandableBudgetTable);

        FrameLayout weeklyPieContainer = findViewById(R.id.monthWeeklyPieHolder);
        _weeklyPieChart = new PieChart(((FragmentActivity)_context));
        _weeklyPieChart.setTitle(_context.getString(R.string.weekly_spending));
        weeklyPieContainer.addView(_weeklyPieChart);

        FrameLayout categoryPieContainer = findViewById(R.id.monthCategoryPieHolder);
        _categoryPieChart = new PieChart(((FragmentActivity)_context));
        _categoryPieChart.setTitle(_context.getString(R.string.categorical_spending));
        categoryPieContainer.addView(_categoryPieChart);

        FrameLayout weeklyLineGraphHolder = findViewById(R.id.monthWeeklyLineGraphHolder);
        _weeklyLineGraph = new LineGraph(((FragmentActivity)_context));
        _weeklyLineGraph.setTitle(_context.getString(R.string.weekly_spending));
        weeklyLineGraphHolder.addView(_weeklyLineGraph);

        FrameLayout extrasContainer = findViewById(R.id.monthExtraHolder);
        _extrasTable = new ExtrasTable(((FragmentActivity)_context), _year, _month);
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
        String[] guidelineLabels = new String[] { _context.getString(R.string.budget) };
        _weeklyLineGraph.addGuildelines(guidelineAmounts, guidelineLabels);
    }

    public void createExtraExpenditure()
    {
        Intent intent = new Intent(_context.getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, _year);
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, _month);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, 0);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, CREATE_EXT_EXPENDITURE); // TODO Redundant?
        ((MonthViewActivity)_context).startActivityForResult(intent, CREATE_EXT_EXPENDITURE);
    }

    public void editExtraExpenditure(ExpenditureEntity entity)
    {
        Intent intent = new Intent(_context.getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, _year);
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, _month);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, 0);
        intent.putExtra(ExpenditureEditActivity.EXPENDITURE_INTENT, entity);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, EDIT_EXT_EXPENDITURE); // TODO Redundant?
        ((MonthViewActivity)_context).startActivityForResult(intent, EDIT_EXT_EXPENDITURE);
    }

    public void editBudgetItem(MutableLiveData<List<BudgetEntity>> budgets, int categoryNumber, int year, int month)
    {
        _monthEditBudgetItemHelper = new MonthEditBudgetItemHelper(((MonthViewActivity)_context), budgets, categoryNumber, year, month);
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

    public void createAdjustmentExpenditure(int category)
    {
        Intent intent = new Intent(_context.getApplicationContext(), AdjustmentEditActivity.class);
        intent.putExtra(AdjustmentEditActivity.YEAR_INTENT, _year);
        intent.putExtra(AdjustmentEditActivity.MONTH_INTENT, _month);
        intent.putExtra(AdjustmentEditActivity.CATEGORY_INTENT, category);
        intent.putExtra(AdjustmentEditActivity.TYPE_INTENT, CREATE_ADJUSTMENT); // TODO Redundant?
        ((MonthViewActivity)_context).startActivityForResult(intent, CREATE_ADJUSTMENT);
    }

    public void editAdjustmentExpenditure(BudgetEntity entity, int groupIndex, int childIndex)
    {
        Intent intent = new Intent(_context.getApplicationContext(), AdjustmentEditActivity.class);
        intent.putExtra(AdjustmentEditActivity.YEAR_INTENT, _year);
        intent.putExtra(AdjustmentEditActivity.MONTH_INTENT, _month);
        intent.putExtra(AdjustmentEditActivity.BUDGET_INTENT, entity);
        intent.putExtra(AdjustmentEditActivity.GROUP_INDEX_INTENT, groupIndex);
        intent.putExtra(AdjustmentEditActivity.CHILD_INDEX_INTENT, childIndex);
        intent.putExtra(AdjustmentEditActivity.TYPE_INTENT, EDIT_ADJUSTMENT); // TODO Redundant?
        ((MonthViewActivity)_context).startActivityForResult(intent, EDIT_ADJUSTMENT);
    }

    public MutableLiveData<List<BudgetEntity>> getBudgets()
    {
        return _budgets;
    }

    public int getYear()
    {
        return _year;
    }

    public int getMonth()
    {
        return _month;
    }
}

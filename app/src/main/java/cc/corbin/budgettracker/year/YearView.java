package cc.corbin.budgettracker.year;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.LineGraph;
import cc.corbin.budgettracker.paging.PagingActivity;
import cc.corbin.budgettracker.auxilliary.PieChart;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.paging.PagingView;
import cc.corbin.budgettracker.tables.CategorySummaryTable;
import cc.corbin.budgettracker.tables.MonthlySummaryTable;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask.SummationResult;

/**
 * Created by Corbin on 4/15/2018.
 */

public class YearView extends PagingView
{
    private final String TAG = "YearView";

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _yearExps;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    private MutableLiveData<SummationResult[]> _monthlyAmounts;
    private MutableLiveData<SummationResult[]> _categoricalAmounts;

    private MonthlySummaryTable _monthlyTable;
    private CategorySummaryTable _categoryTable;

    private PieChart _monthlyPieChart;
    private PieChart _categoryPieChart;

    private LineGraph _monthlyLineGraph;

    private boolean _firstRun;

    public YearView(Context context, PagingActivity activity)
    {
        super(context, activity);

        _firstRun = true;

        ignoreMonth = true;
        ignoreDay = true;

        _viewModel = ExpenditureViewModel.getInstance();

        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.view_year, null);
        addView(view);
    }

    public void setDate(int year)
    {
        super.setDate(year, 0, 0);

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
        _monthlyTable.resetTable();
        _categoryTable.resetTable();
        _monthlyPieChart.clearData();
        _categoryPieChart.clearData();
        _monthlyLineGraph.clearData();

        _viewModel.getYear(_yearExps, _year);
    }

    protected void setupHeader()
    {
        super.setupHeader();

        _dateTextView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((YearViewActivity)_context).moveToTotalView(null);
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
                    yearLoaded(expenditureEntities);
                }
                else { }
            }
        };

        final Observer<List<BudgetEntity>> budgetObserver = new Observer<List<BudgetEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<BudgetEntity> budgetEntities)
            {
                if (budgetEntities != null) // Returning from a query
                {
                    refreshTables(budgetEntities);

                    // Create a budget line for the line graph
                    float budget = 0;
                    int size = budgetEntities.size();
                    for (int i = 0; i < size; i++)
                    {
                        budget += budgetEntities.get(i).getAmount();
                    }
                    float[] guidelineAmounts = new float[] {budget / 12}; // TODO
                    String[] guidelineLabels = new String[] { _context.getString(R.string.budget) };
                    _monthlyLineGraph.addGuildelines(guidelineAmounts, guidelineLabels);
                }
                else // else - returning from an add / edit / remove
                {
                    // Call for a refresh
                    // _viewModel.getMonthsBudget(_budgets, _year); // TODO ?
                }
            }
        };

        final Observer<SummationResult[]> monthlyAmountsObserver = new Observer<SummationResult[]>()
        {
            @Override
            public void onChanged(@Nullable SummationResult[] results)
            {
                float[] amounts = new float[results.length];
                for (int i = 0; i < results.length; i++)
                {
                    amounts[i] = results[i].getAmount();
                }

                _monthlyTable.updateExpenditures(amounts);

                String[] monthLabels = new String[12];
                monthLabels[0] = "January";
                monthLabels[1] = "February";
                monthLabels[2] = "March";
                monthLabels[3] = "April";
                monthLabels[4] = "May";
                monthLabels[5] = "June";
                monthLabels[6] = "July";
                monthLabels[7] = "August";
                monthLabels[8] = "September";
                monthLabels[9] = "October";
                monthLabels[10] = "November";
                monthLabels[11] = "December";

                _monthlyPieChart.setData(amounts, monthLabels);
                _monthlyLineGraph.setData(amounts, monthLabels);
            }
        };

        final Observer<SummationResult[]> categoricalAmountsObserver = new Observer<SummationResult[]>()
        {
            @Override
            public void onChanged(@Nullable SummationResult[] results)
            {
                float[] amounts = new float[results.length];
                for (int i = 0; i < results.length; i++)
                {
                    amounts[i] = results[i].getAmount();
                }

                _categoryTable.updateExpenditures(amounts);

                String[] categoryLabels = Categories.getCategories();
                _categoryPieChart.setData(amounts, categoryLabels);
            }
        };

        _yearExps = new MutableLiveData<List<ExpenditureEntity>>();
        _yearExps.observe(((FragmentActivity)_context), entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(((FragmentActivity)_context), budgetObserver);

        _monthlyAmounts = new MutableLiveData<SummationResult[]>();
        _monthlyAmounts.observe(((FragmentActivity)_context), monthlyAmountsObserver);

        _categoricalAmounts = new MutableLiveData<SummationResult[]>();
        _categoricalAmounts.observe(((FragmentActivity)_context), categoricalAmountsObserver);
    }

    private void setupViews()
    {
        _dateTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(_context.getApplicationContext(), TotalViewActivity.class);
                _context.startActivity(intent);
            }
        });

        FrameLayout yearMonthlyContainer = findViewById(R.id.yearMonthlyHolder);
        _monthlyTable = new MonthlySummaryTable(((FragmentActivity)_context), _year);
        yearMonthlyContainer.addView(_monthlyTable);

        FrameLayout yearsCategoryContainer = findViewById(R.id.yearCategoryHolder);
        _categoryTable = new CategorySummaryTable(((FragmentActivity)_context));
        yearsCategoryContainer.addView(_categoryTable);

        FrameLayout monthlyPieContainer = findViewById(R.id.yearMonthlyPieHolder);
        _monthlyPieChart = new PieChart(((FragmentActivity)_context));
        _monthlyPieChart.setTitle(_context.getString(R.string.monthly_spending));
        monthlyPieContainer.addView(_monthlyPieChart);

        FrameLayout categoryPieContainer = findViewById(R.id.yearCategoryPieHolder);
        _categoryPieChart = new PieChart(((FragmentActivity)_context));
        _categoryPieChart.setTitle(_context.getString(R.string.categorical_spending));
        categoryPieContainer.addView(_categoryPieChart);

        FrameLayout monthlyLineGraphHolder = findViewById(R.id.yearMonthlyLineGraphHolder);
        _monthlyLineGraph = new LineGraph(((FragmentActivity)_context));
        _monthlyLineGraph.setTitle(_context.getString(R.string.monthly_spending));
        monthlyLineGraphHolder.addView(_monthlyLineGraph);
    }

    private void yearLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        final SummationAsyncTask monthlyAsyncTask = new SummationAsyncTask(SummationAsyncTask.SummationType.monthly, _monthlyAmounts);
        final SummationAsyncTask categoricalAsyncTask = new SummationAsyncTask(SummationAsyncTask.SummationType.categorically, _categoricalAmounts);
        monthlyAsyncTask.execute(expenditureEntities);
        categoricalAsyncTask.execute(expenditureEntities);

        _viewModel.getYearBudget(_budgets, _year); // Gets the budgets as individual months for displaying in the time breakdown table
    }

    private void refreshTables(List<BudgetEntity> entities)
    {
        _monthlyTable.updateBudgets(entities);
        _categoryTable.updateBudgets(entities);
    }
}
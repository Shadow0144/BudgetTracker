package cc.corbin.budgettracker.total;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;
import java.time.LocalDate;

import cc.corbin.budgettracker.auxilliary.AsyncSummationCallback;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.navigation.NavigationActivity;
import cc.corbin.budgettracker.auxilliary.LineGraph;
import cc.corbin.budgettracker.auxilliary.PieChart;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.tables.CategorySummaryTable;
import cc.corbin.budgettracker.tables.YearlySummaryTable;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask.SummationResult;

/**
 * Created by Corbin on 4/15/2018.
 */

public class TotalViewActivity extends NavigationActivity implements AsyncSummationCallback
{
    private final String TAG = "TotalViewActivity";

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _totalExps;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    private MutableLiveData<SummationResult[]> _yearlyAmounts;
    private MutableLiveData<SummationResult[]> _categoricalAmounts;

    private YearlySummaryTable _yearlyTable;
    private CategorySummaryTable _categoryTable;

    private PieChart _yearlyPieChart;
    private PieChart _categoryPieChart;

    private LineGraph _yearlyLineGraph;

    private int _startYear;
    private int _endYear;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_view);
        setup();

        _viewModel = ExpenditureViewModel.getInstance();

        setupObservers();

        setupViews();

        _viewModel.getTotal(_totalExps);
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
                    totalLoaded(expenditureEntities);
                }
                else { }
            }
        };

        final Observer<List<BudgetEntity>> budgetObserver = new Observer<List<BudgetEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<BudgetEntity> budgetEntities)
            {
                if (budgetEntities != null)
                {
                    budgetsLoaded(budgetEntities);
                }
                else { }
            }
        };

        final Observer<SummationResult[]> yearlyAmountsObserver = new Observer<SummationResult[]>()
        {
            @Override
            public void onChanged(@Nullable SummationResult[] results)
            {
                float[] amounts = new float[results.length];
                for (int i = 0; i < results.length; i++)
                {
                    amounts[i] = results[i].getAmount();
                }

                _yearlyTable.updateExpenditures(amounts);

                String[] yearLabels = new String[amounts.length];
                for (int i = 0; i < amounts.length; i++)
                {
                    yearLabels[i] = "" + (i + _startYear);
                }

                _yearlyPieChart.setData(amounts, yearLabels);
                _yearlyLineGraph.setData(amounts, yearLabels);
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

        _totalExps = new MutableLiveData<List<ExpenditureEntity>>();
        _totalExps.observe(this, entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(this, budgetObserver);

        _yearlyAmounts = new MutableLiveData<SummationResult[]>();
        _yearlyAmounts.observe(this, yearlyAmountsObserver);

        _categoricalAmounts = new MutableLiveData<SummationResult[]>();
        _categoricalAmounts.observe(this, categoricalAmountsObserver);
    }

    private void setupViews()
    {
        TextView header = findViewById(R.id.totalView);
        header.setText(R.string.total);
        header.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        FrameLayout totalYearlyContainer = findViewById(R.id.totalYearlyHolder);
        _yearlyTable = new YearlySummaryTable(this);
        totalYearlyContainer.addView(_yearlyTable);

        FrameLayout totalCategoryContainer = findViewById(R.id.totalCategoryHolder);
        _categoryTable = new CategorySummaryTable(this);
        totalCategoryContainer.addView(_categoryTable);

        FrameLayout yearlyPieContainer = findViewById(R.id.totalYearlyPieHolder);
        _yearlyPieChart = new PieChart(this);
        _yearlyPieChart.setTitle(getString(R.string.yearly_spending));
        yearlyPieContainer.addView(_yearlyPieChart);

        FrameLayout categoryPieContainer = findViewById(R.id.totalCategoryPieHolder);
        _categoryPieChart = new PieChart(this);
        _categoryPieChart.setTitle(getString(R.string.categorical_spending));
        categoryPieContainer.addView(_categoryPieChart);

        FrameLayout yearlyLineGraphHolder = findViewById(R.id.totalYearlyLineGraphHolder);
        _yearlyLineGraph = new LineGraph(this);
        _yearlyLineGraph.setTitle(getString(R.string.yearly_spending));
        yearlyLineGraphHolder.addView(_yearlyLineGraph);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == SettingsActivity.DATABASE_UPDATE_INTENT_FLAG)
        {
            // TODO Update outdated elements
        }
        else if (requestCode == SettingsActivity.DATABASE_NO_UPDATE_INTENT_FLAG)
        {
            // Do nothing
        }
        else { }
    }

    @Override
    public void rowComplete(SummationAsyncTask.SummationType summationType, String header, float amount, int id, boolean finalRow)
    {
        if (summationType == SummationAsyncTask.SummationType.yearly)
        {
            _yearlyTable.addContentRow(header, amount, id, finalRow);
        }
        else { }
    }

    private void totalLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        if (expenditureEntities.size() > 0)
        {
            _startYear = expenditureEntities.get(0).getYear();
            _endYear = expenditureEntities.get(expenditureEntities.size() - 1).getYear();
        }
        else
        {
            LocalDate calendar = LocalDate.now();
            _startYear = calendar.getYear();
            _endYear = calendar.getYear();
        }

        SummationAsyncTask yearlyAsyncTask = new SummationAsyncTask(SummationAsyncTask.SummationType.yearly, _yearlyAmounts, this);
        SummationAsyncTask categoricalAsyncTask = new SummationAsyncTask(SummationAsyncTask.SummationType.categorically, _categoricalAmounts);
        yearlyAsyncTask.execute(expenditureEntities);
        categoricalAsyncTask.execute(expenditureEntities);

        _viewModel.getTotalBudget(_budgets, _startYear, _endYear); // TODO - Check
    }

    private void budgetsLoaded(List<BudgetEntity> budgetEntities)
    {
        refreshTables(budgetEntities);

        // Create a budget line for the line graph
        float budget = 0;
        int size = budgetEntities.size();
        for (int i = 0; i < size; i++)
        {
            budget += budgetEntities.get(i).getAmount();
        }
        int span = (_endYear - _startYear);
        span = (span == 0) ? 1 : span; // Make sure it's not zero // TODO Race condition
        float[] guidelineAmounts = new float[] {budget / span};
        String[] guidelineLabels = new String[] { getString(R.string.budget) };
        _yearlyLineGraph.addGuildelines(guidelineAmounts, guidelineLabels);
    }

    private void refreshTables(List<BudgetEntity> budgetEntities)
    {
        _yearlyTable.updateBudgets(budgetEntities);
        _categoryTable.updateBudgets(budgetEntities);
    }
}
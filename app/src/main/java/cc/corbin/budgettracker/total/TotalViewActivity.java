package cc.corbin.budgettracker.total;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.ExcelExporter;
import cc.corbin.budgettracker.auxilliary.LineGraph;
import cc.corbin.budgettracker.auxilliary.PieChart;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.tables.CategorySummaryTable;
import cc.corbin.budgettracker.tables.TimeSummaryTable;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 4/15/2018.
 */

public class TotalViewActivity extends AppCompatActivity
{
    private final String TAG = "TotalViewActivity";

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _totalExps;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    private MutableLiveData<float[]> _yearlyAmounts;
    private MutableLiveData<float[]> _categoricalAmounts;

    private TimeSummaryTable _yearlyTable;
    private CategorySummaryTable _categoryTable;

    private PieChart _yearlyPieChart;
    private PieChart _categoryPieChart;

    private LineGraph _yearlyLineGraph;

    private int _startYear;
    private int _endYear;

    public static boolean dataInvalid;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_view);

        TotalViewActivity.dataInvalid = true;

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(this), BudgetDatabase.getBudgetDatabase(this));
        _viewModel.setDate(0, 0, 0);

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
                if (budgetEntities != null) // returning from a query
                {
                    refreshTables(budgetEntities);
                }
                else // else - returning from an add / edit / remove
                {
                    // Call for a refresh
                    _viewModel.getTotalBudget(_budgets, _startYear, _endYear);
                }
            }
        };

        final Observer<float[]> yearlyAmountsObserver = new Observer<float[]>()
        {
            @Override
            public void onChanged(@Nullable float[] amounts)
            {
                _yearlyTable.updateExpenditures(_startYear, amounts);

                String[] yearLabels = new String[amounts.length];
                for (int i = 0; i < amounts.length; i++)
                {
                    yearLabels[i] = "" + (i + _startYear);
                }

                _yearlyPieChart.setData(amounts, yearLabels);
                _yearlyLineGraph.setData(amounts, yearLabels);
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

                TotalViewActivity.dataInvalid = false;

                // Update the budgets
                _viewModel.getTotalBudget(_budgets, _startYear, _endYear);
            }
        };

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

        _yearlyAmounts = new MutableLiveData<float[]>();
        _yearlyAmounts.observe(this, yearlyAmountsObserver);

        _categoricalAmounts = new MutableLiveData<float[]>();
        _categoricalAmounts.observe(this, categoricalAmountsObserver);

        FrameLayout totalYearlyContainer = findViewById(R.id.totalYearlyHolder);
        _yearlyTable = new TimeSummaryTable(this);
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

        // No budget table

        _totalExps = new MutableLiveData<List<ExpenditureEntity>>();
        _totalExps.observe(this, entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(this, budgetObserver);

        ExcelExporter.checkPermissions(this);
    }

    @Override
    protected void onResume()
    {
        if (TotalViewActivity.dataInvalid)
        {
            _yearlyTable.resetTable();
            _categoryTable.resetTable();

            _viewModel.setDate(0, 0, 0);
            _viewModel.getTotal(_totalExps);
        }
        else { }

        super.onResume();
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
            Calendar calendar = Calendar.getInstance();
            _startYear = calendar.get(Calendar.YEAR);
            _endYear = calendar.get(Calendar.YEAR);
        }

        SummationAsyncTask summationAsyncTask = new SummationAsyncTask(SummationAsyncTask.summationType.yearly, _yearlyAmounts, _categoricalAmounts);
        summationAsyncTask.execute(expenditureEntities);
    }

    private void refreshTables(List<BudgetEntity> budgetEntities)
    {
        _yearlyTable.updateBudgets(budgetEntities);
        _categoryTable.updateBudgets(budgetEntities);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        ExcelExporter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void exportTotal(View v)
    {
        //List<ExpenditureEntity> monthExp = db.expenditureDao().getTimeSpan(_year, _month, 1, maxDays);
        //ExcelExporter.exportMonth(this, _month, _year, monthExp);
    }
}
package cc.corbin.budgettracker.year;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.NavigationActivity;
import cc.corbin.budgettracker.auxilliary.NavigationDrawerHelper;
import cc.corbin.budgettracker.importexport.ImportExportActivity;
import cc.corbin.budgettracker.auxilliary.LineGraph;
import cc.corbin.budgettracker.auxilliary.PieChart;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.custom.CreateCustomViewActivity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.search.CreateSearchActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.tables.CategorySummaryTable;
import cc.corbin.budgettracker.tables.MonthlySummaryTable;
import cc.corbin.budgettracker.tables.TimeSummaryTable;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;

/**
 * Created by Corbin on 4/15/2018.
 */

public class YearViewActivity extends NavigationActivity
{
    private final String TAG = "YearViewActivity";

    public final static String YEAR_INTENT = "Year";

    private int _year;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _yearExps;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    private MutableLiveData<float[]> _monthlyAmounts;
    private MutableLiveData<float[]> _categoricalAmounts;

    private MonthlySummaryTable _monthlyTable;
    private CategorySummaryTable _categoryTable;

    private PieChart _monthlyPieChart;
    private PieChart _categoryPieChart;

    private LineGraph _monthlyLineGraph;

    private int _budgetId; // ID of the budget entity being edited
    private PopupWindow _popupWindow; // For editing budgets

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_year_view);
        super.onCreate(savedInstanceState);

        _year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));

        _viewModel = ExpenditureViewModel.getInstance();

        setupObservers();

        setupViews();

        _viewModel.getYear(_yearExps, _year);
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
                    String[] guidelineLabels = new String[] { getString(R.string.budget) };
                    _monthlyLineGraph.addGuildelines(guidelineAmounts, guidelineLabels);
                }
                else // else - returning from an add / edit / remove
                {
                    // Call for a refresh
                    // _viewModel.getMonthsBudget(_budgets, _year); // TODO ?
                }
            }
        };

        final Observer<float[]> monthlyAmountsObserver = new Observer<float[]>()
        {
            @Override
            public void onChanged(@Nullable float[] amounts)
            {
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

        _yearExps = new MutableLiveData<List<ExpenditureEntity>>();
        _yearExps.observe(this, entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(this, budgetObserver);

        _monthlyAmounts = new MutableLiveData<float[]>();
        _monthlyAmounts.observe(this, monthlyAmountsObserver);

        _categoricalAmounts = new MutableLiveData<float[]>();
        _categoricalAmounts.observe(this, categoricalAmountsObserver);
    }

    private void setupViews()
    {
        TextView header = findViewById(R.id.yearView);
        header.setText("" + _year);
        header.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), TotalViewActivity.class);
                startActivity(intent);
            }
        });

        FrameLayout yearMonthlyContainer = findViewById(R.id.yearMonthlyHolder);
        _monthlyTable = new MonthlySummaryTable(this, _year);
        yearMonthlyContainer.addView(_monthlyTable);

        FrameLayout yearsCategoryContainer = findViewById(R.id.yearCategoryHolder);
        _categoryTable = new CategorySummaryTable(this);
        yearsCategoryContainer.addView(_categoryTable);

        FrameLayout monthlyPieContainer = findViewById(R.id.yearMonthlyPieHolder);
        _monthlyPieChart = new PieChart(this);
        _monthlyPieChart.setTitle(getString(R.string.monthly_spending));
        monthlyPieContainer.addView(_monthlyPieChart);

        FrameLayout categoryPieContainer = findViewById(R.id.yearCategoryPieHolder);
        _categoryPieChart = new PieChart(this);
        _categoryPieChart.setTitle(getString(R.string.categorical_spending));
        categoryPieContainer.addView(_categoryPieChart);

        FrameLayout monthlyLineGraphHolder = findViewById(R.id.yearMonthlyLineGraphHolder);
        _monthlyLineGraph = new LineGraph(this);
        _monthlyLineGraph.setTitle(getString(R.string.monthly_spending));
        monthlyLineGraphHolder.addView(_monthlyLineGraph);
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

    private void yearLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        final SummationAsyncTask monthlyAsyncTask = new SummationAsyncTask(SummationAsyncTask.summationType.monthly, _monthlyAmounts);
        final SummationAsyncTask categoricalAsyncTask = new SummationAsyncTask(SummationAsyncTask.summationType.categorically, _categoricalAmounts);
        monthlyAsyncTask.execute(expenditureEntities);
        categoricalAsyncTask.execute(expenditureEntities);

        _viewModel.getYearBudget(_budgets, _year); // Gets the budgets as individual months for displaying in the time breakdown table
    }

    private void refreshTables(List<BudgetEntity> entities)
    {
        _monthlyTable.updateBudgets(entities);
        _categoryTable.updateBudgets(entities);
    }

    public void previousYear(View v)
    {
        Intent intent = new Intent(getApplicationContext(), YearViewActivity.class);
        intent.putExtra(YearViewActivity.YEAR_INTENT, _year - 1);
        startActivity(intent);
        finish();
    }

    public void nextYear(View v)
    {
        Intent intent = new Intent(getApplicationContext(), YearViewActivity.class);
        intent.putExtra(YearViewActivity.YEAR_INTENT, _year + 1);
        startActivity(intent);
        finish();
    }
}
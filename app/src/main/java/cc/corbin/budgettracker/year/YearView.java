package cc.corbin.budgettracker.year;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.text.DateFormatSymbols;
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

public class YearView extends LinearLayout
{
    private final String TAG = "YearView";

    private Context _context;

    private int _year;

    private final int DATE_TEXT_TOP_PADDING = 24;
    private final int DATE_TEXT_BOT_PADDING = 36;

    private final int DATE_TEXT_SIZE = 24;

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

    private TextView _dateTextView;

    private boolean _firstRun;

    public YearView(Context context)
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
        final View view = inflater.inflate(R.layout.view_year, null);
        addView(view);
    }

    public void setDate(int year)
    {
        _year = year;

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
        _dateTextView.setText("" + _year);

        // Set the header color
        Calendar date = Calendar.getInstance();
        Calendar compare = ((Calendar)date.clone());
        date.set(Calendar.YEAR, _year);
        if (compare.before(date)) // Future
        {
            _dateTextView.setBackgroundColor(_context.getColor(R.color.colorPrimaryLight));
        }
        else if (compare.after(date)) // Past
        {
            _dateTextView.setBackgroundColor(_context.getColor(R.color.colorPrimaryVeryDark));
        }
        else // Present
        {
            _dateTextView.setBackgroundColor(_context.getColor(R.color.colorPrimaryDark));
        }

        _monthlyTable.resetTable();
        _categoryTable.resetTable();
        _monthlyPieChart.clearData();
        _categoryPieChart.clearData();
        _monthlyLineGraph.clearData();

        _viewModel.getYear(_yearExps, _year);
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
        _yearExps.observe(((FragmentActivity)_context), entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(((FragmentActivity)_context), budgetObserver);

        _monthlyAmounts = new MutableLiveData<float[]>();
        _monthlyAmounts.observe(((FragmentActivity)_context), monthlyAmountsObserver);

        _categoricalAmounts = new MutableLiveData<float[]>();
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

    }

    public void nextYear(View v)
    {

    }
}
package cc.corbin.budgettracker.year;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.ExcelExporter;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.TableCell;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;

/**
 * Created by Corbin on 4/15/2018.
 */

public class YearViewActivity extends AppCompatActivity
{
    private final String TAG = "YearViewActivity";

    public final static String YEAR_INTENT = "Year";

    private int _year;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _yearExps;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    private YearMonthlySummaryTable _monthlyTable;
    private YearCategorySummaryTable _categoryTable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_year_view);

        _year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(this), BudgetDatabase.getBudgetDatabase(this));

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDate(_year, 0, 0);

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
                if (budgetEntities != null) // returning from a query
                {
                    refreshTables(budgetEntities);
                    //_loaded = true;
                }
                else // else - returning from an add / edit / remove
                {
                    // Call for a refresh
                    _viewModel.getMonthBudget(_budgets);
                }
            }
        };

        _yearExps = new MutableLiveData<List<ExpenditureEntity>>();
        _yearExps.observe(this, entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(this, budgetObserver);
        _viewModel.getYear(_yearExps);

        TextView header = findViewById(R.id.yearView);
        DateFormatSymbols dfs = new DateFormatSymbols();
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
        _monthlyTable = new YearMonthlySummaryTable(this, _year);
        yearMonthlyContainer.addView(_monthlyTable);

        FrameLayout yearsCategoryContainer = findViewById(R.id.yearCategoryHolder);
        _categoryTable = new YearCategorySummaryTable(this, _year);
        yearsCategoryContainer.addView(_categoryTable);

        FrameLayout budgetContainer = findViewById(R.id.yearBudgetHolder);
        TableLayout budgetTable = new TableLayout(this);
        setupBudgetTable(budgetTable);
        budgetContainer.addView(budgetTable);

        ExcelExporter.checkPermissions(this);
    }

    private void yearLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        _monthlyTable.updateExpenditures(expenditureEntities);
        _categoryTable.updateExpenditures(expenditureEntities);

        _viewModel.getYearBudget(_budgets);
    }

    private void refreshTables(List<BudgetEntity> entities)
    {
        _monthlyTable.updateBudgets(entities);
        _categoryTable.updateBudgets(entities);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        ExcelExporter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void previousYear(View v)
    {
        Intent intent = new Intent(getApplicationContext(), MonthViewActivity.class);
        intent.putExtra(MonthViewActivity.YEAR_INTENT, _year-1);
        startActivity(intent);
        finish();
    }

    public void nextYear(View v)
    {
        Intent intent = new Intent(getApplicationContext(), YearViewActivity.class);
        intent.putExtra(MonthViewActivity.YEAR_INTENT, _year + 1);
        startActivity(intent);
        finish();
    }

    private void setupBudgetTable(TableLayout budgetTable)
    {
        String[] categories = Categories.getCategories();
        int count = categories.length;

        TableRow titleRow = new TableRow(this);
        TableCell titleCell = new TableCell(this, TableCell.TITLE_CELL);
        titleCell.setText(R.string.year_budget_title);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 2;
        titleCell.setLayoutParams(params);

        titleRow.addView(titleCell);
        budgetTable.addView(titleRow);

        budgetTable.setColumnStretchable(1, true);

        for (int i = 0; i < count; i++)
        {
            TableRow tableRow = new TableRow(this);
            TableCell headerCell = new TableCell(this, TableCell.HEADER_CELL);
            TableCell contentCell = new TableCell(this, TableCell.DEFAULT_CELL);

            headerCell.setText(categories[i]);
            contentCell.setText("0");

            tableRow.addView(headerCell);
            tableRow.addView(contentCell);

            budgetTable.addView(tableRow);
        }
    }

    public void exportYear(View v)
    {
        //List<ExpenditureEntity> monthExp = db.expenditureDao().getTimeSpan(_year, _month, 1, maxDays);
        //ExcelExporter.exportMonth(this, _month, _year, monthExp);
    }
}
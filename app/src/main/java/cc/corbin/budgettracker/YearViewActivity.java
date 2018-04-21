package cc.corbin.budgettracker;

import android.arch.lifecycle.LiveData;
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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

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

        _yearExps = new MutableLiveData<List<ExpenditureEntity>>();
        _viewModel.getYear(_yearExps);
        _yearExps.observe(this, entityObserver);

        TextView header = findViewById(R.id.yearView);
        DateFormatSymbols dfs = new DateFormatSymbols();
        header.setText("" + _year);

        FrameLayout budgetContainer = findViewById(R.id.budgetHolder);
        TableLayout budgetTable = new TableLayout(this);
        setupBudgetTable(budgetTable);
        budgetContainer.addView(budgetTable);

        ExcelExporter.checkPermissions(this);
    }

    private void yearLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        FrameLayout monthsWeeklyContainer = findViewById(R.id.yearMonthlyHolder);
        YearMonthlySummaryTable monthlyTable = new YearMonthlySummaryTable(this, _year);
        monthlyTable.setup(expenditureEntities);
        monthsWeeklyContainer.addView(monthlyTable);

        FrameLayout yearsCategoryContainer = findViewById(R.id.yearCategoryHolder);
        YearCategorySummaryTable categoryTable = new YearCategorySummaryTable(this, _year);
        categoryTable.setup(expenditureEntities);
        yearsCategoryContainer.addView(categoryTable);
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
        String[] categories = DayViewActivity.getCategories();
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
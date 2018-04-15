package cc.corbin.budgettracker;

import android.arch.lifecycle.LiveData;
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
 * Created by Corbin on 1/28/2018.
 */

public class MonthViewActivity extends AppCompatActivity
{
    private final String TAG = "MonthViewActivity";

    public final static String MONTH_INTENT = "Month";
    public final static String YEAR_INTENT = "Year";

    private int _month;
    private int _year;

    private ExpenditureViewModel _viewModel;
    /*private LiveData<List<ExpenditureEntity>> _week1;
    private LiveData<List<ExpenditureEntity>> _week2;
    private LiveData<List<ExpenditureEntity>> _week3;
    private LiveData<List<ExpenditureEntity>> _week4;
    private LiveData<List<ExpenditureEntity>> _week5;*/
    private LiveData<List<ExpenditureEntity>> _monthExps;
    private int _loadedCount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view);

        _month = getIntent().getIntExtra(MONTH_INTENT, Calendar.getInstance().get(Calendar.MONTH)+1);
        _year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));

        _loadedCount = 0;

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDatabase(ExpenditureDatabase.getExpenditureDatabase(this));

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);

        final Observer<List<ExpenditureEntity>> entityObserver = new Observer<List<ExpenditureEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<ExpenditureEntity> expenditureEntities)
            {
                if (expenditureEntities != null)
                {
                    monthLoaded(expenditureEntities);
                }
            }
        };

        _monthExps = _viewModel.getMonth(_year, _month);
        _monthExps.observe(this, entityObserver);

        TextView header = findViewById(R.id.monthView);
        DateFormatSymbols dfs = new DateFormatSymbols();
        header.setText(dfs.getMonths()[_month-1] + " " + _year);

        FrameLayout budgetContainer = findViewById(R.id.budgetHolder);
        TableLayout budgetTable = new TableLayout(this);
        setupBudgetTable(budgetTable);
        budgetContainer.addView(budgetTable);

        ExcelExporter.checkPermissions(this);
    }

    private void monthLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        FrameLayout monthsWeeklyContainer = findViewById(R.id.monthWeeklyHolder);
        MonthWeeklySummaryTable weeklyTable = new MonthWeeklySummaryTable(this, _month, _year);
        weeklyTable.setup(expenditureEntities);
        monthsWeeklyContainer.addView(weeklyTable);

        FrameLayout monthsCategoryContainer = findViewById(R.id.monthCategoryHolder);
        MonthCategorySummaryTable categoryTable = new MonthCategorySummaryTable(this, _month, _year);
        categoryTable.setup(expenditureEntities);
        monthsCategoryContainer.addView(categoryTable);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        ExcelExporter.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    private void setupBudgetTable(TableLayout budgetTable)
    {
        String[] categories = DayViewActivity.getCategories();
        int count = categories.length;

        TableRow titleRow = new TableRow(this);
        TableCell titleCell = new TableCell(this, TableCell.TITLE_CELL);
        titleCell.setText(R.string.budget_title);

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

    public void exportMonth(View v)
    {
        //List<ExpenditureEntity> monthExp = db.expenditureDao().getTimeSpan(_year, _month, 1, maxDays);
        //ExcelExporter.exportMonth(this, _month, _year, monthExp);
    }
}

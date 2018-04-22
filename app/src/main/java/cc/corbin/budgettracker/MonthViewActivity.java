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
    private MutableLiveData<List<ExpenditureEntity>> _monthExps;

    private MutableLiveData<List<BudgetEntity>> _budgets;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view);

        _month = getIntent().getIntExtra(MONTH_INTENT, Calendar.getInstance().get(Calendar.MONTH)+1);
        _year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(this), BudgetDatabase.getBudgetDatabase(this));

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDate(_year, _month, 0);

        final Observer<List<ExpenditureEntity>> entityObserver = new Observer<List<ExpenditureEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<ExpenditureEntity> expenditureEntities)
            {
                if (expenditureEntities != null)
                {
                    monthLoaded(expenditureEntities);
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
                    budgetLoaded(budgetEntities);
                }
                else { }
            }
        };

        _monthExps = new MutableLiveData<List<ExpenditureEntity>>();
        _viewModel.getMonth(_monthExps);
        _monthExps.observe(this, entityObserver);

        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _viewModel.getMonthBudget(_budgets);
        _budgets.observe(this, budgetObserver);

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

    private void budgetLoaded(List<BudgetEntity> budgetEntities)
    {
        FrameLayout budgetContainer = findViewById(R.id.budgetHolder);
        String[] categories = DayViewActivity.getCategories();
        int sizeCat = categories.length;
        int size = budgetEntities.size();
        for (int i = 0; i < size; i++)
        {
            BudgetEntity entity = budgetEntities.get(i);
            String category = entity.getExpenseType();
            Log.e(TAG, "Item #:" + i + " Category: " + category);
            for (int j = 0; j < sizeCat; j++)
            {
                if (category.equals(categories[j]))
                {
                    TextView view = budgetContainer.findViewById(j);
                    view.setText("" + entity.getAmount());
                    break;
                }
                else { }
            }
        }
    }

    private void addBudget()
    {
        BudgetEntity entity = new BudgetEntity(_month, _year, Currencies.default_currency, 30, DayViewActivity.getCategories()[0]);
        _viewModel.insertBudgetEntity(_budgets, entity);
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
            contentCell.setId(i);
            contentCell.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    addBudget();
                }
            });

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

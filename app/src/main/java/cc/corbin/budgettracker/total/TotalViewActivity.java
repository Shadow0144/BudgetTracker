package cc.corbin.budgettracker.total;

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
import java.util.List;

import cc.corbin.budgettracker.auxilliary.ExcelExporter;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.TableCell;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.day.DayViewActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_view);

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(this), BudgetDatabase.getBudgetDatabase(this));

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDate(0, 0, 0);

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

        _totalExps = new MutableLiveData<List<ExpenditureEntity>>();
        _viewModel.getYear(_totalExps);
        _totalExps.observe(this, entityObserver);

        TextView header = findViewById(R.id.totalView);
        DateFormatSymbols dfs = new DateFormatSymbols();
        header.setText("" + "Total"); // TODO
        header.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        FrameLayout budgetContainer = findViewById(R.id.totalBudgetHolder);
        TableLayout budgetTable = new TableLayout(this);
        setupBudgetTable(budgetTable);
        budgetContainer.addView(budgetTable);

        ExcelExporter.checkPermissions(this);
    }

    private void yearLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        FrameLayout totalYearlyContainer = findViewById(R.id.totalYearlyHolder);
        TotalYearlySummaryTable yearlyTable = new TotalYearlySummaryTable(this);
        yearlyTable.setup(expenditureEntities);
        totalYearlyContainer.addView(yearlyTable);

        FrameLayout totalCategoryContainer = findViewById(R.id.totalCategoryHolder);
        TotalCategorySummaryTable categoryTable = new TotalCategorySummaryTable(this);
        categoryTable.setup(expenditureEntities);
        totalCategoryContainer.addView(categoryTable);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        ExcelExporter.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    public void exportTotal(View v)
    {
        //List<ExpenditureEntity> monthExp = db.expenditureDao().getTimeSpan(_year, _month, 1, maxDays);
        //ExcelExporter.exportMonth(this, _month, _year, monthExp);
    }
}
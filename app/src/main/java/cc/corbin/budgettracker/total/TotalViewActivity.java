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
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.ExcelExporter;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.settings.SettingsActivity;
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

    private TotalYearlySummaryTable _yearlyTable;
    private TotalCategorySummaryTable _categoryTable;

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
                    //_loaded = true;
                }
                else // else - returning from an add / edit / remove
                {
                    // Call for a refresh
                    _viewModel.getTotalBudget(_budgets);
                }
            }
        };

        _totalExps = new MutableLiveData<List<ExpenditureEntity>>();
        _totalExps.observe(this, entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(this, budgetObserver);
        _viewModel.getTotal(_totalExps);

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

        FrameLayout totalYearlyContainer = findViewById(R.id.totalYearlyHolder);
        _yearlyTable = new TotalYearlySummaryTable(this);
        totalYearlyContainer.addView(_yearlyTable);

        FrameLayout totalCategoryContainer = findViewById(R.id.totalCategoryHolder);
        _categoryTable = new TotalCategorySummaryTable(this);
        totalCategoryContainer.addView(_categoryTable);

        ExcelExporter.checkPermissions(this);
    }

    private void totalLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        _yearlyTable.updateExpenditures(expenditureEntities);
        _categoryTable.updateExpenditures(expenditureEntities);
        _viewModel.getTotalBudget(_budgets);
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
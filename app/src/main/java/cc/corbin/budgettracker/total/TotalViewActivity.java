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
import java.util.List;
import java.util.Set;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.ExcelExporter;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.tables.CategorySummaryTable;
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
    private CategorySummaryTable _categoryTable;

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
                    //_loaded = true;
                }
                else // else - returning from an add / edit / remove
                {
                    // Call for a refresh
                    _viewModel.getTotalBudget(_budgets, _startYear, _endYear);
                }
            }
        };

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
        _categoryTable = new CategorySummaryTable(this);
        totalCategoryContainer.addView(_categoryTable);

        // TODO - Add total budget table?

        _totalExps = new MutableLiveData<List<ExpenditureEntity>>();
        _totalExps.observe(this, entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(this, budgetObserver);

        /*String cat = Categories.getCategories()[0];
        for (int i = 0; i < 1000; i++)
        {
            ExpenditureEntity entity = new ExpenditureEntity();
            entity.setDay(1);
            entity.setMonth(1);
            entity.setYear(2018);
            entity.setBaseAmount(1.0f);
            entity.setAmount(1.0f);
            entity.setBaseCurrency(Currencies.default_currency);
            entity.setExpenseType(cat);
            _viewModel.insertExpEntity(_totalExps, entity);
        }*/

        //_viewModel.getTotal(_totalExps);

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
        _yearlyTable.updateExpenditures(expenditureEntities);
        _categoryTable.updateExpenditures(expenditureEntities);
        _startYear = _yearlyTable.getStartYear();
        _endYear = _yearlyTable.getEndYear();
        TotalViewActivity.dataInvalid = false;
        _viewModel.getTotalBudget(_budgets, _startYear, _endYear);
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
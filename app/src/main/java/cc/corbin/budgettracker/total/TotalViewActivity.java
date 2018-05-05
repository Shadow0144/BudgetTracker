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

        _totalExps = new MutableLiveData<List<ExpenditureEntity>>();
        _totalExps.observe(this, entityObserver);
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

        ExcelExporter.checkPermissions(this);
    }

    private void totalLoaded(List<ExpenditureEntity> expenditureEntities)
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

    public void exportTotal(View v)
    {
        //List<ExpenditureEntity> monthExp = db.expenditureDao().getTimeSpan(_year, _month, 1, maxDays);
        //ExcelExporter.exportMonth(this, _month, _year, monthExp);
    }
}
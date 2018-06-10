package cc.corbin.budgettracker.year;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.ExcelExporter;
import cc.corbin.budgettracker.auxilliary.LineGraph;
import cc.corbin.budgettracker.auxilliary.PieChart;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.tables.BudgetTable;
import cc.corbin.budgettracker.tables.CategorySummaryTable;
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

public class YearViewActivity extends AppCompatActivity
{
    private final String TAG = "YearViewActivity";

    public final static String YEAR_INTENT = "Year";

    private int _year;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _yearExps;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    private MutableLiveData<float[]> _monthlyAmounts;
    private MutableLiveData<float[]> _categoricalAmounts;

    private TimeSummaryTable _monthlyTable;
    private CategorySummaryTable _categoryTable;

    private PieChart _monthlyPieChart;
    private PieChart _categoryPieChart;

    private LineGraph _monthlyLineGraph;

    private int _budgetId; // ID of the budget entity being edited
    private PopupWindow _popupWindow; // For editing budgets

    public static boolean dataInvalid;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_year_view);

        YearViewActivity.dataInvalid = true;

        _year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(this), BudgetDatabase.getBudgetDatabase(this));
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
                if (budgetEntities != null) // Returning from a query
                {
                    refreshTables(budgetEntities);
                }
                else // else - returning from an add / edit / remove
                {
                    // Call for a refresh
                    _viewModel.getMonthsBudget(_budgets);
                }
            }
        };

        final Observer<float[]> monthlyAmountsObserver = new Observer<float[]>()
        {
            @Override
            public void onChanged(@Nullable float[] amounts)
            {
                _monthlyTable.updateExpenditures(amounts);

                String[] monthLabels = new String[13];
                monthLabels[0] = getString(R.string.extras);
                monthLabels[1] = "January";
                monthLabels[2] = "February";
                monthLabels[3] = "March";
                monthLabels[4] = "April";
                monthLabels[5] = "May";
                monthLabels[6] = "June";
                monthLabels[7] = "July";
                monthLabels[8] = "August";
                monthLabels[9] = "September";
                monthLabels[10] = "October";
                monthLabels[11] = "November";
                monthLabels[12] = "December";

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

                YearViewActivity.dataInvalid = false;

                // Update the budgets
                _viewModel.getMonthsBudget(_budgets);
            }
        };

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

        _monthlyAmounts = new MutableLiveData<float[]>();
        _monthlyAmounts.observe(this, monthlyAmountsObserver);

        _categoricalAmounts = new MutableLiveData<float[]>();
        _categoricalAmounts.observe(this, categoricalAmountsObserver);

        FrameLayout yearMonthlyContainer = findViewById(R.id.yearMonthlyHolder);
        _monthlyTable = new TimeSummaryTable(this, _year);
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

        _yearExps = new MutableLiveData<List<ExpenditureEntity>>();
        _yearExps.observe(this, entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(this, budgetObserver);

        ExcelExporter.checkPermissions(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (YearViewActivity.dataInvalid)
        {
            _monthlyTable.resetTable();
            _categoryTable.resetTable();

            _monthlyPieChart.clearData();
            _categoryPieChart.clearData();

            _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(this), BudgetDatabase.getBudgetDatabase(this));
            _viewModel.setDate(_year, 0, 0);
            _viewModel.getYear(_yearExps);
        }
        else { }
    }

    private void yearLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        SummationAsyncTask summationAsyncTask = new SummationAsyncTask(SummationAsyncTask.summationType.monthly, _monthlyAmounts, _categoricalAmounts);
        summationAsyncTask.execute(expenditureEntities);
    }

    private void refreshTables(List<BudgetEntity> entities)
    {
        _monthlyTable.updateBudgets(entities);
        _categoryTable.updateBudgetsTrim(entities);
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

    public void editBudgetItem(int id)
    {
        _budgetId = id;
        BudgetEntity entity = _budgets.getValue().get(_budgetId);

        final View budgetEditView = getLayoutInflater().inflate(R.layout.popup_set_budget, null);

        final TextView categoryTextView = budgetEditView.findViewById(R.id.categoryTextView);
        categoryTextView.setText(entity.getCategoryName() + ": ");

        final TextView currencyTextView = budgetEditView.findViewById(R.id.currencyTextView);
        currencyTextView.setText(Currencies.symbols[Currencies.default_currency]);

        final EditText budgetEditText = budgetEditView.findViewById(R.id.amountEditText);
        if (Currencies.integer[Currencies.default_currency])
        {
            budgetEditText.setHint("0");
        }
        else
        {
            budgetEditText.setHint("0.00");
        }

        if (entity.getId() != 0)
        {
            final Button removeButton = budgetEditView.findViewById(R.id.removeButton);
            removeButton.setEnabled(true);
        }
        else { }

        _popupWindow = new PopupWindow(budgetEditView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(findViewById(R.id.monthRootLayout), Gravity.CENTER, 0, 0);
    }

    public void confirmBudgetItemEdit(View v)
    {
        BudgetEntity entity = _budgets.getValue().get(_budgetId);

        final EditText amountTextEdit = _popupWindow.getContentView().findViewById(R.id.amountEditText);
        float amount = 0.0f;
        try
        {
            amount = Float.parseFloat(amountTextEdit.getText().toString());
        }
        catch (Exception e)
        {
            Log.e(TAG, "Empty amount");
        }
        entity.setAmount(amount);

        if (entity.getId() != 0) // Edit
        {
            _budgets.getValue().get(_budgetId).setAmount(amount);
            entity.setMonth(13);
            _viewModel.updateBudgetEntity(_budgets, entity);
        }
        else // Add
        {
            entity.setId(0);
            entity.setMonth(13);
            entity.setYear(_year);
            _budgets.getValue().add(entity);
            _viewModel.insertBudgetEntity(_budgets, entity);
        }

        _popupWindow.dismiss();
    }

    public void cancelBudgetItemEdit(View v)
    {
        _popupWindow.dismiss();
    }

    public void removeBudgeItem(View v)
    {
        BudgetEntity entity = _budgets.getValue().get(_budgetId);

        _budgets.getValue().remove(_budgetId);
        _viewModel.removeBudgetEntity(_budgets, entity);

        _popupWindow.dismiss();
    }

    public void exportYear(View v)
    {
        //List<ExpenditureEntity> monthExp = db.expenditureDao().getTimeSpan(_year, _month, 1, maxDays);
        //ExcelExporter.exportMonth(this, _month, _year, monthExp);
    }
}
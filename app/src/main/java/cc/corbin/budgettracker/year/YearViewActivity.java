package cc.corbin.budgettracker.year;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

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

public class YearViewActivity extends NavigationActivity
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_year_view);
        super.onCreate(savedInstanceState);

        _year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));

        _viewModel = ExpenditureViewModel.getInstance();

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
                    String[] guidelineLabels = new String[] { getString(R.string.budget) };
                    _monthlyLineGraph.addGuildelines(guidelineAmounts, guidelineLabels);
                }
                else // else - returning from an add / edit / remove
                {
                    // Call for a refresh
                    _viewModel.getMonthsBudget(_budgets, _year);
                }
            }
        };

        final Observer<float[]> monthlyAmountsObserver = new Observer<float[]>()
        {
            @Override
            public void onChanged(@Nullable float[] amounts)
            {
                _monthlyTable.updateMonthlyExpenditures(amounts);

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

                // Update the budgets
                _viewModel.getMonthsBudget(_budgets, _year);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == SettingsActivity.DATABASE_UPDATE_INTENT_FLAG)
        {
            // TODO Update outdated elements
        }
        else if (requestCode == SettingsActivity.DATABASE_NO_UPDATE_INTENT_FLAG)
        {
            // Do nothing
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

    public void previousYear(View v)
    {
        Intent intent = new Intent(getApplicationContext(), YearViewActivity.class);
        intent.putExtra(YearViewActivity.YEAR_INTENT, _year - 1);
        startActivity(intent);
        finish();
    }

    public void nextYear(View v)
    {
        Intent intent = new Intent(getApplicationContext(), YearViewActivity.class);
        intent.putExtra(YearViewActivity.YEAR_INTENT, _year + 1);
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
        _popupWindow.showAtLocation(findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);
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
            _viewModel.updateBudgetEntity(entity);
        }
        else // Add
        {
            entity.setId(0);
            entity.setMonth(13);
            entity.setYear(_year);
            _budgets.getValue().add(entity);
            _viewModel.insertBudgetEntity(entity, _year, 0); // TODO ?
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
        _viewModel.removeBudgetEntity(entity);

        _popupWindow.dismiss();
    }
}
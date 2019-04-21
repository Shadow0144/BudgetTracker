package cc.corbin.budgettracker.month;

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
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.NavigationDrawerHelper;
import cc.corbin.budgettracker.importexport.ImportExportActivity;
import cc.corbin.budgettracker.auxilliary.LineGraph;
import cc.corbin.budgettracker.edit.AdjustmentEditActivity;
import cc.corbin.budgettracker.auxilliary.PieChart;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask;
import cc.corbin.budgettracker.custom.CreateCustomViewActivity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedEditText;
import cc.corbin.budgettracker.search.CreateSearchActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.tables.ExpandableBudgetTable;
import cc.corbin.budgettracker.tables.ExtrasTable;
import cc.corbin.budgettracker.edit.ExpenditureEditActivity;
import cc.corbin.budgettracker.tables.CategorySummaryTable;
import cc.corbin.budgettracker.tables.TimeSummaryTable;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.year.YearViewActivity;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 1/28/2018.
 */

public class MonthViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private final String TAG = "MonthViewActivity";

    public final static String MONTH_INTENT = "Month";
    public final static String YEAR_INTENT = "Year";

    public final static int CREATE_EXT_EXPENDITURE = 0;
    public final static int EDIT_EXT_EXPENDITURE = 1;
    public final static int CREATE_ADJUSTMENT = 2;
    public final static int EDIT_ADJUSTMENT = 3;

    public final static int SUCCEED = 0;
    public final static int CANCEL = 1;
    public final static int DELETE = 2;
    public final static int FAILURE = -1;

    private int _month;
    private int _year;

    private DrawerLayout _drawerLayout;

    private TimeSummaryTable _weeklyTable;
    private CategorySummaryTable _categoryTable;
    private ExpandableBudgetTable _expandableBudgetTable;
    private ExtrasTable _extrasTable;

    private PieChart _weeklyPieChart;
    private PieChart _categoryPieChart;

    private LineGraph _weeklyLineGraph;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _monthExps;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    private MutableLiveData<float[]> _weeklyAmounts;
    private MutableLiveData<float[]> _categoricalAmounts;

    private int _budgetId; // ID of the budget entity being edited
    private PopupWindow _popupWindow; // For editing budgets
    private NumericalFormattedEditText _amountEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        _drawerLayout = findViewById(R.id.rootLayout);
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        _month = getIntent().getIntExtra(MONTH_INTENT, Calendar.getInstance().get(Calendar.MONTH)+1);
        _year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));

        _viewModel = ExpenditureViewModel.getInstance();

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
                if (budgetEntities != null) // returning from a query
                {
                    refreshTables(budgetEntities);

                    // Create a budget line for the line graph
                    float budget = 0;
                    int size = budgetEntities.size();
                    for (int i = 0; i < size; i++)
                    {
                        budget += budgetEntities.get(i).getAmount();
                    }
                    float[] guidelineAmounts = new float[] {budget / 5}; // TODO
                    String[] guidelineLabels = new String[] { getString(R.string.budget) };
                    _weeklyLineGraph.addGuildelines(guidelineAmounts, guidelineLabels);
                }
                else // else - returning from an add / edit / remove
                {
                    // Call for a refresh
                    _viewModel.getMonthBudget(_budgets, _year, _month);
                }
            }
        };

        final Observer<float[]> weeklyAmountsObserver = new Observer<float[]>()
        {
            @Override
            public void onChanged(@Nullable float[] amounts)
            {
                _weeklyTable.updateWeeklyExpenditures(amounts);

                String[] weekLabels = new String[7];
                weekLabels[0] = getString(R.string.extras);
                weekLabels[1] = "Week 1";
                weekLabels[2] = "Week 2";
                weekLabels[3] = "Week 3";
                weekLabels[4] = "Week 4";
                weekLabels[5] = "Week 5";
                weekLabels[6] = "Adjustments";

                _weeklyPieChart.setData(amounts, weekLabels);
                _weeklyLineGraph.setData(amounts, weekLabels);
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
                _viewModel.getMonthBudget(_budgets, _year, _month);
            }
        };

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

        _weeklyAmounts = new MutableLiveData<float[]>();
        _weeklyAmounts.observe(this, weeklyAmountsObserver);

        _categoricalAmounts = new MutableLiveData<float[]>();
        _categoricalAmounts.observe(this, categoricalAmountsObserver);

        FrameLayout monthsWeeklyContainer = findViewById(R.id.monthWeeklyHolder);
        _weeklyTable = new TimeSummaryTable(this, _month, _year);
        monthsWeeklyContainer.addView(_weeklyTable);

        FrameLayout monthsCategoryContainer = findViewById(R.id.monthCategoryHolder);
        _categoryTable = new CategorySummaryTable(this);
        monthsCategoryContainer.addView(_categoryTable);

        FrameLayout expandableBudgetContainer = findViewById(R.id.monthExpandableBudgetHolder);
        _expandableBudgetTable = new ExpandableBudgetTable(this, _month, _year);
        expandableBudgetContainer.addView(_expandableBudgetTable);

        FrameLayout weeklyPieContainer = findViewById(R.id.monthWeeklyPieHolder);
        _weeklyPieChart = new PieChart(this);
        _weeklyPieChart.setTitle(getString(R.string.weekly_spending));
        weeklyPieContainer.addView(_weeklyPieChart);

        FrameLayout categoryPieContainer = findViewById(R.id.monthCategoryPieHolder);
        _categoryPieChart = new PieChart(this);
        _categoryPieChart.setTitle(getString(R.string.categorical_spending));
        categoryPieContainer.addView(_categoryPieChart);

        FrameLayout weeklyLineGraphHolder = findViewById(R.id.monthWeeklyLineGraphHolder);
        _weeklyLineGraph = new LineGraph(this);
        _weeklyLineGraph.setTitle(getString(R.string.weekly_spending));
        weeklyLineGraphHolder.addView(_weeklyLineGraph);

        _monthExps = new MutableLiveData<List<ExpenditureEntity>>();
        _monthExps.observe(this, entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(this, budgetObserver);

        createExtrasAndAdjustmentsTables();

        _viewModel.getMonth(_monthExps, _year, _month);
        _viewModel.getMonthBudget(_budgets, _year, _month);
    }

    public void refreshView()
    {
        _weeklyTable.resetTable();
        _categoryTable.resetTable();
        _expandableBudgetTable.resetTable();
        _weeklyPieChart.clearData();
        _categoryPieChart.clearData();
        _weeklyLineGraph.clearData();

        _viewModel.getMonth(_monthExps, _year, _month);
        _viewModel.getMonthBudget(_budgets, _year, _month);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                _drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        Intent intent = NavigationDrawerHelper.handleNavigation(item);

        boolean handled = (intent != null);
        if (handled)
        {
            startActivity(intent);
            _drawerLayout.closeDrawer(GravityCompat.START);
        }
        else { }

        return handled;
    }

    private void monthLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        _extrasTable.updateExpenditures(expenditureEntities);

        SummationAsyncTask summationAsyncTask = new SummationAsyncTask(SummationAsyncTask.summationType.weekly, _weeklyAmounts, _categoricalAmounts);
        summationAsyncTask.execute(expenditureEntities);
    }

    private void refreshTables(List<BudgetEntity> entities)
    {
        _weeklyTable.updateBudgets(entities);
        _categoryTable.updateBudgets(entities);
        _expandableBudgetTable.refreshTable(entities);
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

    public void editBudgetItem(int id)
    {
        _budgetId = id;
        BudgetEntity entity = _budgets.getValue().get(_budgetId);

        final View budgetEditView = getLayoutInflater().inflate(R.layout.popup_set_budget, null);

        final TextView categoryTextView = budgetEditView.findViewById(R.id.categoryTextView);
        categoryTextView.setText(entity.getCategoryName() + ": ");

        final TextView currencyTextView = budgetEditView.findViewById(R.id.currencyTextView);
        currencyTextView.setText(Currencies.symbols[Currencies.default_currency]);

        _amountEditText = budgetEditView.findViewById(R.id.amountEditText);
        if (entity.getId() != 0)
        {
            _amountEditText.setup(null, Currencies.default_currency, entity.getAmount());
            if (entity.getMonth() == _month && entity.getYear() == _year) // If the ID is not 0
            {
                final Button removeButton = budgetEditView.findViewById(R.id.removeButton);
                removeButton.setEnabled(true);
            }
            else { }
        }
        else
        {
            _amountEditText.setup(null);
        }

        _popupWindow = new PopupWindow(budgetEditView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);
    }

    public void confirmBudgetItemEdit(View v)
    {
        BudgetEntity entity = _budgets.getValue().get(_budgetId);
        float amount = _amountEditText.getAmount();
        entity.setAmount(amount);

        if (entity.getMonth() == _month && entity.getYear() == _year) // Edit
        {
            _budgets.getValue().get(_budgetId).setAmount(amount);
        }
        else // Add
        {
            entity.setId(0);
            entity.setMonth(_month);
            entity.setYear(_year);
            _budgets.getValue().add(entity);
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

    private void createExtrasAndAdjustmentsTables()
    {
        FrameLayout extrasContainer = findViewById(R.id.monthExtraHolder);

        // Create the extras table
        _extrasTable = new ExtrasTable(this, _year, _month);
        extrasContainer.addView(_extrasTable);
    }

    public void createExtraExpenditure()
    {
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, _year);
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, _month);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, 0);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, CREATE_EXT_EXPENDITURE);
        startActivityForResult(intent, CREATE_EXT_EXPENDITURE);
    }

    public void editExtraExpenditure(ExpenditureEntity entity, int index)
    {
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, _year);
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, _month);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, 0);
        intent.putExtra(ExpenditureEditActivity.INDEX_INTENT, index);
        intent.putExtra(ExpenditureEditActivity.EXPENDITURE_INTENT, entity);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, EDIT_EXT_EXPENDITURE);
        startActivityForResult(intent, EDIT_EXT_EXPENDITURE);
    }

    public void createAdjustmentExpenditure(int category)
    {
        Intent intent = new Intent(getApplicationContext(), AdjustmentEditActivity.class);
        intent.putExtra(AdjustmentEditActivity.YEAR_INTENT, _year);
        intent.putExtra(AdjustmentEditActivity.MONTH_INTENT, _month);
        intent.putExtra(AdjustmentEditActivity.CATEGORY_INTENT, category);
        intent.putExtra(AdjustmentEditActivity.TYPE_INTENT, CREATE_ADJUSTMENT);
        startActivityForResult(intent, CREATE_ADJUSTMENT);
    }

    public void editAdjustmentExpenditure(BudgetEntity entity, int groupIndex, int childIndex)
    {
        Intent intent = new Intent(getApplicationContext(), AdjustmentEditActivity.class);
        intent.putExtra(AdjustmentEditActivity.YEAR_INTENT, _year);
        intent.putExtra(AdjustmentEditActivity.MONTH_INTENT, _month);
        intent.putExtra(AdjustmentEditActivity.BUDGET_INTENT, entity);
        intent.putExtra(AdjustmentEditActivity.GROUP_INDEX_INTENT, groupIndex);
        intent.putExtra(AdjustmentEditActivity.CHILD_INDEX_INTENT, childIndex);
        intent.putExtra(AdjustmentEditActivity.TYPE_INTENT, EDIT_ADJUSTMENT);
        startActivityForResult(intent, EDIT_ADJUSTMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == FAILURE)
        {
            Toast toast = Toast.makeText(this, getString(R.string.failure_expense), Toast.LENGTH_LONG);
            toast.show();
        }
        else
        {
            if (requestCode == CREATE_EXT_EXPENDITURE)
            {
                if (resultCode == SUCCEED)
                {
                    //ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    //_viewModel.insertExpEntity(expenditureEntity);
                    refreshView();
                }
                else { }
            }
            else if (requestCode == EDIT_EXT_EXPENDITURE)
            {
                if (resultCode == SUCCEED)
                {
                    //ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    //_viewModel.updateExpEntity(expenditureEntity);
                    refreshView();
                }
                else if (resultCode == DELETE) // Delete can only occur from an edit
                {
                    //ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    //_viewModel.removeExpEntity(expenditureEntity);
                    refreshView();
                }
                else { }
            }
            else if (requestCode == CREATE_ADJUSTMENT)
            {
                if (resultCode == SUCCEED)
                {
                    boolean transfer = data.getBooleanExtra(AdjustmentEditActivity.TRANSFER_INTENT, false);
                    if (!transfer)
                    {
                        //BudgetEntity budgetEntity = data.getParcelableExtra(AdjustmentEditActivity.BUDGET_INTENT);
                        //_viewModel.insertBudgetEntity(budgetEntity);
                        refreshView();
                    }
                    else
                    {
                        //BudgetEntity budgetEntity = data.getParcelableExtra(AdjustmentEditActivity.BUDGET_INTENT);
                        //BudgetEntity linkedBudgetEntity = data.getParcelableExtra(AdjustmentEditActivity.LINKED_BUDGET_INTENT);
                        //_viewModel.insertLinkedBudgetEntities(budgetEntity, linkedBudgetEntity);
                        refreshView();
                    }
                }
                else { }
            }
            else if (requestCode == EDIT_ADJUSTMENT)
            {
                if (resultCode == SUCCEED)
                {
                    BudgetEntity budgetEntity = data.getParcelableExtra(AdjustmentEditActivity.BUDGET_INTENT);
                    boolean transfer = data.getBooleanExtra(AdjustmentEditActivity.TRANSFER_INTENT, false);

                    if (!transfer)
                    {
                        //_viewModel.updateBudgetEntity(budgetEntity);
                        refreshView();
                    }
                    else
                    {
                        //BudgetEntity linkedBudgetEntity = data.getParcelableExtra(AdjustmentEditActivity.LINKED_BUDGET_INTENT);
                        //_viewModel.updateLinkedBudgetEntities(budgetEntity, linkedBudgetEntity);
                        refreshView();
                    }
                }
                else if (resultCode == DELETE) // Delete can only occur from an edit
                {
                    BudgetEntity budgetEntity = data.getParcelableExtra(AdjustmentEditActivity.BUDGET_INTENT);
                    boolean transfer = data.getBooleanExtra(AdjustmentEditActivity.TRANSFER_INTENT, false);

                    if (!transfer)
                    {
                        //_viewModel.removeBudgetEntity(budgetEntity);
                        refreshView();
                    }
                    else
                    {
                        //BudgetEntity linkedBudgetEntity = data.getParcelableExtra(AdjustmentEditActivity.LINKED_BUDGET_INTENT);
                        //_viewModel.removeLinkedBudgetEntities(budgetEntity, linkedBudgetEntity);
                        refreshView();
                    }
                }
                else { }
            }
            else { }

            if (resultCode == CANCEL)
            {
                // Do nothing
            }
            else { }
        }
    }
}

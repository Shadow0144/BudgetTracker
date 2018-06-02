package cc.corbin.budgettracker.month;

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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.ExcelExporter;
import cc.corbin.budgettracker.auxilliary.PieChart;
import cc.corbin.budgettracker.tables.ExtrasTable;
import cc.corbin.budgettracker.tables.TableCell;
import cc.corbin.budgettracker.day.ExpenditureEditActivity;
import cc.corbin.budgettracker.day.ExpenditureItem;
import cc.corbin.budgettracker.tables.BudgetTable;
import cc.corbin.budgettracker.tables.CategorySummaryTable;
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

public class MonthViewActivity extends AppCompatActivity
{
    private final String TAG = "MonthViewActivity";

    public final static String MONTH_INTENT = "Month";
    public final static String YEAR_INTENT = "Year";

    public final static int CREATE_EXT_EXPENDITURE = 0;
    public final static int EDIT_EXT_EXPENDITURE = 1;

    public final static int CREATE_BUD_EXPENDITURE = 0;
    public final static int EDIT_BUD_EXPENDITURE = 1;

    public final static int SUCCEED = 0;
    public final static int CANCEL = 1;
    public final static int DELETE = 2;
    public final static int FAILURE = -1;

    private int _month;
    private int _year;

    private MonthWeeklySummaryTable _weeklyTable;
    private CategorySummaryTable _categoryTable;
    private BudgetTable _budgetTable;
    private ExtrasTable _extrasTable;
    private ExtrasTable _adjustmentsTable;

    private PieChart _weeklyPieChart;
    private PieChart _categoryPieChart;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _monthExps;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    private int _budgetId; // ID of the budget entity being edited
    private PopupWindow _popupWindow; // For editing budgets

    public static boolean dataInvalid;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view);

        MonthViewActivity.dataInvalid = true;

        _month = getIntent().getIntExtra(MONTH_INTENT, Calendar.getInstance().get(Calendar.MONTH)+1);
        _year = getIntent().getIntExtra(YEAR_INTENT, Calendar.getInstance().get(Calendar.YEAR));

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(this), BudgetDatabase.getBudgetDatabase(this));
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
                if (budgetEntities != null) // returning from a query
                {
                    refreshTables(budgetEntities);
                }
                else // else - returning from an add / edit / remove
                {
                    // Call for a refresh
                    _viewModel.getMonthBudget(_budgets);
                }
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

        FrameLayout monthsWeeklyContainer = findViewById(R.id.monthWeeklyHolder);
        _weeklyTable = new MonthWeeklySummaryTable(this, _month, _year);
        monthsWeeklyContainer.addView(_weeklyTable);

        FrameLayout monthsCategoryContainer = findViewById(R.id.monthCategoryHolder);
        _categoryTable = new CategorySummaryTable(this);
        monthsCategoryContainer.addView(_categoryTable);

        FrameLayout budgetContainer = findViewById(R.id.monthBudgetHolder);
        _budgetTable = new BudgetTable(this, _month, _year);
        budgetContainer.addView(_budgetTable);

        FrameLayout weeklyPieContainer = findViewById(R.id.monthWeeklyPieHolder);
        _weeklyPieChart = new PieChart(this);
        _weeklyPieChart.setTitle("Weekly Spending");
        weeklyPieContainer.addView(_weeklyPieChart);

        FrameLayout categoryPieContainer = findViewById(R.id.monthCategoryPieHolder);
        _categoryPieChart = new PieChart(this);
        _categoryPieChart.setTitle("Categorical Spending");
        categoryPieContainer.addView(_categoryPieChart);

        _monthExps = new MutableLiveData<List<ExpenditureEntity>>();
        _monthExps.observe(this, entityObserver);
        _budgets = new MutableLiveData<List<BudgetEntity>>();
        _budgets.observe(this, budgetObserver);
        //_viewModel.getMonth(_monthExps);

        createExtrasAndAdjustmentsTables();

        ExcelExporter.checkPermissions(this);
    }

    @Override
    protected void onResume()
    {
        if (MonthViewActivity.dataInvalid)
        {
            _weeklyTable.resetTable();
            _categoryTable.resetTable();
            _budgetTable.resetTable();
            _weeklyPieChart.clearData();
            _categoryPieChart.clearData();

            _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(this), BudgetDatabase.getBudgetDatabase(this));
            _viewModel.setDate(_year, _month, 0);
            _viewModel.getMonth(_monthExps);
        }
        else { }

        super.onResume();
    }

    private void monthLoaded(List<ExpenditureEntity> expenditureEntities)
    {
        _weeklyTable.updateExpenditures(expenditureEntities);
        _categoryTable.updateExpenditures(expenditureEntities);
        _extrasTable.updateExpenditures(expenditureEntities);
        _adjustmentsTable.updateExpenditures(expenditureEntities);

        setupPieCharts(expenditureEntities);

        MonthViewActivity.dataInvalid = false;

        _viewModel.getMonthBudget(_budgets);
    }

    private void refreshTables(List<BudgetEntity> entities)
    {
        _weeklyTable.updateBudgets(entities);
        _categoryTable.updateBudgets(entities);
        _budgetTable.refreshTable(entities);
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

        if (entity.getMonth() == _month && entity.getYear() == _year) // If the ID is not 0
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

        if (entity.getMonth() == _month && entity.getYear() == _year) // Edit
        {
            _budgets.getValue().get(_budgetId).setAmount(amount);
            _viewModel.updateBudgetEntity(_budgets, entity);
        }
        else // Add
        {
            entity.setId(0);
            entity.setMonth(_month);
            entity.setYear(_year);
            _budgets.getValue().add(entity);
            _viewModel.insertBudgetEntity(_budgets, entity);
        }

        // Lock the BudgetTable
        _budgetTable.lockTable();

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

        // Lock the BudgetTable
        _budgetTable.lockTable();

        _popupWindow.dismiss();
    }

    private void createExtrasAndAdjustmentsTables()
    {
        FrameLayout extrasContainer = findViewById(R.id.monthExtraHolder);
        FrameLayout adjustmentsContainer = findViewById(R.id.monthAdjustmentHolder);

        // Create the extras table
        _extrasTable = new ExtrasTable(this, ExtrasTable.tableType.extras, _year, _month);
        extrasContainer.addView(_extrasTable);

        // Create the adjustments table
        _adjustmentsTable = new ExtrasTable(this, ExtrasTable.tableType.adjustments, _year, _month);
        adjustmentsContainer.addView(_adjustmentsTable);
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

    public void editExtraExpenditure(ExpenditureEntity entity)
    {
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, _year);
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, _month);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, 0);
        intent.putExtra(ExpenditureEditActivity.EXPENDITURE_INTENT, entity);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, EDIT_EXT_EXPENDITURE);
        startActivityForResult(intent, EDIT_EXT_EXPENDITURE);
    }

    public void createAdjustmentExpenditure()
    {
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, _year);
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, _month);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, 32);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, CREATE_EXT_EXPENDITURE);
        startActivityForResult(intent, CREATE_EXT_EXPENDITURE);
    }

    public void editAdjustmentExpenditure(ExpenditureEntity entity)
    {
        Intent intent = new Intent(getApplicationContext(), ExpenditureEditActivity.class);
        intent.putExtra(ExpenditureEditActivity.YEAR_INTENT, _year);
        intent.putExtra(ExpenditureEditActivity.MONTH_INTENT, _month);
        intent.putExtra(ExpenditureEditActivity.DAY_INTENT, 0);
        intent.putExtra(ExpenditureEditActivity.EXPENDITURE_INTENT, entity);
        intent.putExtra(ExpenditureEditActivity.TYPE_INTENT, EDIT_EXT_EXPENDITURE);
        startActivityForResult(intent, EDIT_EXT_EXPENDITURE);
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
            //lock();
            if (requestCode == CREATE_EXT_EXPENDITURE)
            {
                if (resultCode == SUCCEED)
                {
                    ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    _viewModel.insertExpEntity(_monthExps, expenditureEntity);
                }
                else { }
            }
            else if (requestCode == EDIT_EXT_EXPENDITURE)
            {
                if (resultCode == SUCCEED)
                {
                    ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    _viewModel.updateExpEntity(_monthExps, expenditureEntity);
                }
                else if (resultCode == DELETE) // Delete can only occur from an edit
                {
                    ExpenditureEntity expenditureEntity = data.getParcelableExtra(ExpenditureEditActivity.EXPENDITURE_INTENT);
                    _viewModel.removeExpEntity(_monthExps, expenditureEntity);
                }
                else { }
            }
            else { }

            if (resultCode == CANCEL)
            {
                //unlockAll();
            }
            else { }
        }
    }

    private void setupPieCharts(List<ExpenditureEntity> entities)
    {
        int size = entities.size();

        // Setup Weekly Pie

        float[] weeks = new float[7];
        for (int i = 0; i < weeks.length; i++)
        {
            weeks[i] = 0.0f;
        }

        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = entities.get(i);
            int day = entity.getDay();
            if (day == 0)
            {
                weeks[0] += entity.getAmount();
            }
            else if (day < 8)
            {
                weeks[1] += entity.getAmount();
            }
            else if (day < 15)
            {
                weeks[2] += entity.getAmount();
            }
            else if (day < 22)
            {
                weeks[3] += entity.getAmount();
            }
            else if (day < 29)
            {
                weeks[4] += entity.getAmount();
            }
            else if (day < 32)
            {
                weeks[5] += entity.getAmount();
            }
            else // day == 32
            {
                weeks[6] += entity.getAmount();
            }
        }

        String[] weekLabels = new String[7];
        weekLabels[0] = "Extras";
        weekLabels[1] = "Week 1";
        weekLabels[2] = "Week 2";
        weekLabels[3] = "Week 3";
        weekLabels[4] = "Week 4";
        weekLabels[5] = "Week 5";
        weekLabels[6] = "Adjustments";

        _weeklyPieChart.setData(weeks, weekLabels);

        // Setup Category Pie

        String[] categoryLabels = Categories.getCategories();
        float[] categories = new float[categoryLabels.length];
        for (int i = 0; i < categories.length; i++)
        {
            categories[i] = 0.0f;
        }

        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = entities.get(i);
            int category = entity.getCategory();
            categories[category] += entity.getAmount();
        }

        _categoryPieChart.setData(categories, categoryLabels);
    }

    public void exportMonth(View v)
    {
        //List<ExpenditureEntity> monthExp = db.expenditureDao().getTimeSpan(_year, _month, 1, maxDays);
        //ExcelExporter.exportMonth(this, _month, _year, monthExp);
    }
}

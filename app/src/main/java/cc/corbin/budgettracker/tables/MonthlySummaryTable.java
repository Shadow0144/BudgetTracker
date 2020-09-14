package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TableRow;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import cc.corbin.budgettracker.BudgetTrackerApplication;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;

public class MonthlySummaryTable extends TimeSummaryTable
{
    private final String TAG = "MonthlySummaryTable";

    private TableRow _loadingRow;
    private TableCell _labelCell;
    private TableCell _expenseCell;
    private TableCell _budgetCell;
    private TableCell _remainingCell;
    private boolean _expendituresFullyLoaded;
    private boolean _budgetsLoaded;

    public MonthlySummaryTable(Context context, int year)
    {
        super(context, year, 0, 0);
    }

    public MonthlySummaryTable(Context context, int[] years)
    {
        super(context, years, new int[0], new int[0]);
    }

    @Override
    public void onClick(View v)
    {
        Intent intent = new Intent(BudgetTrackerApplication.getInstance(), MonthViewActivity.class);

        int month = ((int)v.getTag());

        intent.putExtra(MonthViewActivity.YEAR_INTENT, _year);
        intent.putExtra(MonthViewActivity.MONTH_INTENT, month);

        _context.startActivity(intent);
    }

    protected void setupTitle()
    {
        setupTitle(_context.getString(R.string.year_monthly_title));
    }

    protected void setupHeaders()
    {
        setupHeaders(_context.getString(R.string.month));
    }

    protected void setupContent()
    {
        _expendituresFullyLoaded = false;
        _budgetsLoaded = false;

        if (_multiTime)
        {
            // Setup the loading row
            addLoadingRow();
        }
        else
        {
            setupMonthRows();
        }

        _rows = 0;
    }

    // Called by a multi-year view
    public void addYearRow(int year)
    {
        TableRow yearRow = new TableRow(_context);
        TableCell labelCell = new TableCell(_context, TableCell.HEADER_CELL);

        labelCell.setText("" + year);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 4;
        labelCell.setLayoutParams(params);

        yearRow.addView(labelCell);

        addView(yearRow, _rows+2);
        _rows++;
    }

    // Called by a multi-year view
    public void addContentRow(String label, float amount, int id, boolean finalRow)
    {
        _labelCell.setText(label);
        _expenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, amount));
        _labelCell.setLoading(false);
        _expenseCell.setLoading(false);

        _expenses.add(amount);
        _expenseCells.add(_expenseCell);
        _budgetCells.add(_budgetCell);
        _remainingCells.add(_remainingCell);
        _totalExpenses += amount;

        _labelCell.setTag(id);
        _labelCell.setOnClickListener(this);

        _rows++;

        if (!finalRow)
        {
            addLoadingRow();
        }
        else { }
    }

    private void addLoadingRow()
    {
        _loadingRow = new TableRow(_context);
        _labelCell = new TableCell(_context, TableCell.HEADER_CELL);
        _expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        _budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        _remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        _labelCell.setLoading(true);
        _expenseCell.setLoading(true);
        _budgetCell.setLoading(true);
        _remainingCell.setLoading(true);

        _loadingRow.addView(_labelCell);
        _loadingRow.addView(_expenseCell);
        _loadingRow.addView(_budgetCell);
        _loadingRow.addView(_remainingCell);

        addView(_loadingRow, _rows+2);
    }

    private void setupMonthRows()
    {
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();

        _rows = 12;
        for (int i = 0; i < _rows; i++) // Add a row for each month
        {
            addMonthRow(months[i], (i + 1));
        }
    }

    private void addMonthRow(String label, int id)
    {
        TableRow tableRow = new TableRow(_context);
        TableCell labelCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        labelCell.setText(label);
        expenseCell.setLoading(true);
        budgetCell.setLoading(true);
        remainingCell.setLoading(true);

        _expenseCells.add(expenseCell);
        _budgetCells.add(budgetCell);
        _remainingCells.add(remainingCell);

        labelCell.setTag(id);
        labelCell.setOnClickListener(this);

        tableRow.addView(labelCell);
        tableRow.addView(expenseCell);
        tableRow.addView(budgetCell);
        tableRow.addView(remainingCell);

        addView(tableRow);
    }

    @Override
    public void updateExpenditures(float[] amounts)
    {
        _expenses = new ArrayList<Float>();
        if (_rows == 0)
        {
            // TODO - If nothing was added, replace the loading row with the current year
        }
        else { }

        // If not multitime, then this was the first time numbers arrived, otherwise, it was setup using addContentRow
        if (!_multiTime)
        {
            _totalExpenses = 0.0f;
            ((ArrayList<Float>) _expenses).ensureCapacity(amounts.length);
            for (int i = 0; i < amounts.length; i++)
            {
                _expenses.add(amounts[i]);
                _expenseCells.get(i).setText(Currencies.formatCurrency(Currencies.default_currency, amounts[i]));
                _expenseCells.get(i).setLoading(false);
                _totalExpenses += amounts[i];
            }
        }
        else { }

        _totalExpenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, _totalExpenses));
        _totalExpenseCell.setLoading(false);

        if (_budgetsLoaded)
        {
            updateBudgetCells();
        }
        else { }
        _expendituresFullyLoaded = true;
    }

    @Override
    public void updateBudgets(List<BudgetEntity> budgetEntities)
    {
        int size = budgetEntities.size(); // Includes the time and category splits
        _budgets = new ArrayList<Float>();
        ((ArrayList<Float>) _budgets).ensureCapacity(size);
        for (int i = 0; i < size; i++)
        {
            _budgets.add(budgetEntities.get(i).getAmount());
        }

        if (_expendituresFullyLoaded)
        {
            updateBudgetCells();
        }
        else { }
        _budgetsLoaded = true;
    }

    private void updateBudgetCells()
    {
        int timeSize = _expenses.size();
        int catSize = Categories.getCategories().length;
        float total = 0.0f;
        // Loop across time
        for (int i = 0; i < timeSize; i++)
        {
            float budget = 0.0f;
            // Sum up across categories
            for (int j = 0; j < catSize; j++)
            {
                budget += _budgets.get((i * catSize) + j);
            }
            _budgetCells.get(i).setText(Currencies.formatCurrency(Currencies.default_currency, budget));
            _budgetCells.get(i).setLoading(false);
            float remaining = budget - _expenses.get(i); // No extras
            _remainingCells.get(i).setText(Currencies.formatCurrency(Currencies.default_currency, remaining));
            _remainingCells.get(i).setLoading(false);
            total += budget;
        }
        _totalBudgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
        _totalBudgetCell.setLoading(false);
        float totalRemaining = total - _totalExpenses;
        _totalRemainingCell.setText(Currencies.formatCurrency(Currencies.default_currency, totalRemaining));
        _totalRemainingCell.setLoading(false);
    }
}
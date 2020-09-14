package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

import cc.corbin.budgettracker.BudgetTrackerApplication;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.year.YearViewActivity;

public class YearlySummaryTable extends TimeSummaryTable
{
    private final String TAG = "YearlySummaryTable";

    private TableCell _labelCell;
    private TableCell _expenseCell;
    private TableCell _budgetCell;
    private TableCell _remainingCell;
    private boolean _expendituresFullyLoaded;
    private boolean _budgetsLoaded;

    public YearlySummaryTable(Context context)
    {
        super(context, 0, 0, 0);
    }

    public YearlySummaryTable(Context context, int[] years)
    {
        super(context, years, new int[0], new int[0]);
    }

    @Override
    public void onClick(View v)
    {
        Intent intent = new Intent(BudgetTrackerApplication.getInstance(), YearViewActivity.class);

        int year = ((int)v.getTag());

        intent.putExtra(MonthViewActivity.YEAR_INTENT, year);

        _context.startActivity(intent);
    }

    protected void setupTitle()
    {
        setupTitle(_context.getString(R.string.total_yearly_title));
    }

    protected void setupHeaders()
    {
        setupHeaders(_context.getString(R.string.year));
    }

    protected void setupContent()
    {
        _expendituresFullyLoaded = false;
        _budgetsLoaded = false;

        // Setup the loading row
        addLoadingRow();

        _rows = 0;
    }

    // Called by Total View
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
        TableRow loadingRow = new TableRow(_context);
        _labelCell = new TableCell(_context, TableCell.HEADER_CELL);
        _expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        _budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        _remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        _labelCell.setLoading(true);
        _expenseCell.setLoading(true);
        _budgetCell.setLoading(true);
        _remainingCell.setLoading(true);

        loadingRow.addView(_labelCell);
        loadingRow.addView(_expenseCell);
        loadingRow.addView(_budgetCell);
        loadingRow.addView(_remainingCell);

        addView(loadingRow, _rows+2);
    }

    @Override
    public void updateExpenditures(float[] amounts)
    {
        if (_rows == 0)
        {
            // TODO - If nothing was added, replace the loading row with the current year
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
        int catSize = Categories.getCategories().length;
        float total = 0.0f;
        // Loop through the years
        for (int i = 0; i < _rows; i++)
        {
            float budget = 0.0f;
            // Sum up across categories
            for (int j = 0; j < catSize; j++)
            {
                budget += _budgets.get((i * catSize) + j); // TODO: Error returning from Settings passing index, also budget appears to be wrong
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
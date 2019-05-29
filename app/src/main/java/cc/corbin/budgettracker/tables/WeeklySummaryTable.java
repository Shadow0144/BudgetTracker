package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TableRow;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.BudgetTrackerApplication;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.day.DayViewActivity;

public class WeeklySummaryTable extends TimeSummaryTable
{
    private final String TAG = "WeeklySummaryTable";

    private TableRow _loadingRow;
    private TableCell _labelCell;
    private TableCell _expenseCell;
    private TableCell _budgetCell;
    private TableCell _remainingCell;
    private boolean _expendituresFullyLoaded;
    private boolean _budgetsLoaded;

    private boolean _shortMonth;

    public WeeklySummaryTable(Context context, int year, int month)
    {
        super(context, year, month, 0);
    }

    public WeeklySummaryTable(Context context, int[] years, int[] months)
    {
        super(context, years, months, new int[0]);
    }

    @Override
    public void onClick(View v)
    {
        Intent intent = new Intent(BudgetTrackerApplication.getInstance(), DayViewActivity.class);

        int week = ((int)v.getTag());
        int day = ((week - 1) * 7) + 1;

        intent.putExtra(DayViewActivity.YEAR_INTENT, _year);
        intent.putExtra(DayViewActivity.MONTH_INTENT, _month);
        intent.putExtra(DayViewActivity.DAY_INTENT, day);

        _context.startActivity(intent);
    }

    protected void setupTitle()
    {
        setupTitle(_context.getString(R.string.year_monthly_title));
    }

    protected void setupHeaders()
    {
        setupHeaders(_context.getString(R.string.week));
    }

    protected void setupContent()
    {
        _expendituresFullyLoaded = false;
        _budgetsLoaded = false;
        _rows = 0;

        if (_multiTime)
        {
            // Setup the loading row
            addLoadingRow();
        }
        else
        {
            setupWeekRows();
        }
    }

    // Called by a multi-month view
    public void addMonthRow(int month)
    {
        TableRow monthRow = new TableRow(_context);
        TableCell labelCell = new TableCell(_context, TableCell.HEADER_CELL);

        labelCell.setText("" + month); // TODO
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 4;
        labelCell.setLayoutParams(params);

        monthRow.addView(labelCell);

        addView(monthRow, _rows+2);
        _rows++;
    }

    // Called by a multi-month view
    public void addMonthRow(int year, int month)
    {
        TableRow monthRow = new TableRow(_context);
        TableCell labelCell = new TableCell(_context, TableCell.HEADER_CELL);

        labelCell.setText("" + year + " / " + month); // TODO
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 4;
        labelCell.setLayoutParams(params);

        monthRow.addView(labelCell);

        addView(monthRow, _rows+2);
        _rows++;
    }

    public void addExtrasRow()
    {
        TableRow tableRow = new TableRow(_context);
        TableCell labelCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        labelCell.setText("Extras"); // TODO - String
        expenseCell.setLoading(true);
        budgetCell.setLoading(true);
        remainingCell.setLoading(true);

        budgetCell.setType(TableCell.ITALIC_CELL);

        _expenseCells.add(expenseCell);
        _budgetCells.add(budgetCell);
        _remainingCells.add(remainingCell);

        tableRow.addView(labelCell);
        tableRow.addView(expenseCell);
        tableRow.addView(budgetCell);
        tableRow.addView(remainingCell);

        addView(tableRow);
    }

    // Called by a multi-month view
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

    private void setupWeekRows()
    {
        _shortMonth = checkForShortMonth();
        _rows = _shortMonth ? 5 : 6; // 5 weeks + extras (4 for February in non-leap year)
        String[] weeks = new String[_rows]; // TODO - Stringify
        weeks[0] = "Extras";
        weeks[1] = "Week 1";
        weeks[2] = "Week 2";
        weeks[3] = "Week 3";
        weeks[4] = "Week 4";
        if (!_shortMonth)
        {
            weeks[5] = "Week 5";
        }
        else { }

        addExtrasRow();
        for (int i = 1; i < _rows; i++) // Add a row for each week
        {
            addWeekRow(weeks[i], i);
        }

        if (_shortMonth)
        {
            addEmptyWeek();
        }
        else { }
    }

    private boolean checkForShortMonth()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(_year, _month-1, 1);
        boolean shortMonth = (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) <= 28);
        return shortMonth;
    }

    private void addEmptyWeek()
    {
        TableRow tableRow = new TableRow(_context);
        TableCell labelCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        labelCell.setText("---");
        expenseCell.setText("---");
        budgetCell.setText("---");
        remainingCell.setLoading(true);

        budgetCell.setType(TableCell.ITALIC_CELL);

        _expenseCells.add(expenseCell);
        _budgetCells.add(budgetCell);
        _remainingCells.add(remainingCell);

        tableRow.addView(labelCell);
        tableRow.addView(expenseCell);
        tableRow.addView(budgetCell);
        tableRow.addView(remainingCell);

        addView(tableRow);
    }

    private void addWeekRow(String label, int id)
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

        budgetCell.setType(TableCell.ITALIC_CELL);

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
        if (_rows == 0)
        {
            // TODO - If nothing was added, replace the loading row with the current year
        }
        else { }

        // If not multitime, then this was the first time numbers arrived, otherwise, it was setup using addContentRow
        if (!_multiTime)
        {
            _totalExpenses = 0.0f;
            for (int i = 0; i < _rows; i++)
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
        // One set of category-amount budgets per month
        // Six rows per month (5 weeks plus extras)
        int rows = 6; // Rows per month (the budgets include an empty entry for week 5 even in short months)
        int catSize = Categories.getCategories().length;
        int buds = _budgets.size() / catSize; // The number of months
        float total = 0.0f;
        DecimalFormat percentage = new DecimalFormat("###,###,###,###,##0.00%");
        for (int i = 0; i < buds; i++)
        {
            float monthTotal = 0.0f;
            for (int j = 0; j < catSize; j++)
            {
                monthTotal += _budgets.get((i*catSize) + j);
            }
            total += monthTotal;
            float remaining = monthTotal;
            for (int j = 0; j < _rows; j++) // The extras row is set to dashes
            {
                int index = (i*rows)+j;
                float spent = _expenses.get(index);
                remaining -= spent;
                float budget = (monthTotal == 0.0f) ? 0.0f : (spent / monthTotal); // If it's zero, then don't divide by it
                _budgetCells.get(index).setText(percentage.format(budget));
                _budgetCells.get(index).setLoading(false);
                _remainingCells.get(index).setText(Currencies.formatCurrency(Currencies.default_currency, remaining));
                _remainingCells.get(index).setLoading(false);
            }
        }
        float totalRemaining = total - _totalExpenses;
        if (_shortMonth) // If a short month, add the remaining into the last column
        {
            _remainingCells.get(_rows).setText(Currencies.formatCurrency(Currencies.default_currency, totalRemaining));
            _remainingCells.get(_rows).setLoading(false);
        }
        else { }
        _totalBudgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
        _totalBudgetCell.setLoading(false);
        _totalRemainingCell.setText(Currencies.formatCurrency(Currencies.default_currency, totalRemaining));
        _totalRemainingCell.setLoading(false);
    }
}
package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.year.YearViewActivity;

public class TimeSummaryTable extends TableLayout implements View.OnClickListener
{
    private final String TAG = "TimeSummaryTable";

    protected Context _context;

    public enum timeframe
    {
        day,
        month,
        year,
        total,
        custom
    }

    private timeframe _timeframe;

    private int _day;
    private int _month;
    private int _year;

    private int _startDay;
    private int _startMonth;
    private int _startYear;
    private int _endDay;
    private int _endMonth;
    private int _endYear;

    private List<Float> _expenses;
    private float _totalExpenses;
    private List<String> _expenseStrings;
    private List<String> _expensePercentStrings;
    private List<String> _remainingStrings;
    private List<String> _remainingPercentStrings;

    protected List<TableCell> _expenseCells;
    protected TableCell _totalExpenseCell;

    protected List<TableCell> _budgetCells;
    protected TableCell _totalBudgetCell;

    protected List<TableCell> _remainingCells;
    protected TableCell _totalRemainingCell;

    protected int _rows;

    public TimeSummaryTable(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Table,
                0, 0);

        try
        {
            _timeframe = timeframe.values()[a.getInteger(R.styleable.Table_timeframe, timeframe.day.ordinal())];
            _day = a.getInteger(R.styleable.Table_day, 1);
            _month = a.getInteger(R.styleable.Table_month, 1);
            _year = a.getInteger(R.styleable.Table_year, 2018);
        }
        catch (Exception e)
        {
            _timeframe = timeframe.day;
            _day = 1;
            _month = 1;
            _year = 2018;
        }
        finally
        {
            a.recycle();
        }

        setupTable();
    }

    // Day
    public TimeSummaryTable(Context context, int day, int month, int year)
    {
        super(context);
        _context = context;

        _timeframe = timeframe.day;
        _day = day;
        _month = month;
        _year = year;

        setupTable();
    }

    // Month
    public TimeSummaryTable(Context context, int month, int year)
    {
        super(context);
        _context = context;

        _timeframe = timeframe.month;
        _day = 0;
        _month = month;
        _year = year;

        setupTable();
    }

    // Year
    public TimeSummaryTable(Context context, int year)
    {
        super(context);
        _context = context;

        _timeframe = timeframe.year;
        _day = 0;
        _month = 0;
        _year = year;

        setupTable();
    }

    // Total
    public TimeSummaryTable(Context context)
    {
        super(context);
        _context = context;

        _timeframe = timeframe.total;
        _day = 0;
        _month = 0;
        _year = 0;

        setupTable();
    }

    // Range
    public TimeSummaryTable(Context context, int day, int month, int year, int endDay, int endMonth, int endYear, timeframe frame)
    {
        super(context);
        _context = context;

        _timeframe = frame;
        _day = day;
        _month = month;
        _year = year;

        _endDay = endDay;
        _endMonth = endMonth;
        _endYear = endYear;

        setupTable();
    }

    public void onClick(View v)
    {
        Intent intent;

        switch (_timeframe)
        {
            case day:

                break;

            case month:
                intent = new Intent(_context, DayViewActivity.class);
                int week = v.getId();
                int day = ((week - 1) * 7) + 1;
                intent.putExtra(DayViewActivity.YEAR_INTENT, _year);
                intent.putExtra(DayViewActivity.MONTH_INTENT, _month);
                intent.putExtra(DayViewActivity.DAY_INTENT, day);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                _context.startActivity(intent);
                ((MonthViewActivity) _context).finish();
                break;

            case year:
                intent = new Intent(_context, MonthViewActivity.class);
                int month = v.getId();
                intent.putExtra(MonthViewActivity.MONTH_INTENT, month);
                intent.putExtra(MonthViewActivity.YEAR_INTENT, _year);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                _context.startActivity(intent);
                ((YearViewActivity) _context).finish();
                break;

            case total:

                break;

            case custom:

                break;
        }
    }

    private void setupTable()
    {
        _expenses = new ArrayList<Float>();
        _expenseCells = new ArrayList<TableCell>();
        _budgetCells = new ArrayList<TableCell>();
        _remainingCells = new ArrayList<TableCell>();

        // Setup the table
        setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        setStretchAllColumns(true);
        setColumnShrinkable(0, true);

        // Setup the title
        setupTitle();

        // Setup the headers
        setupHeaders();

        // Setup the content
        setupContent();

        // Add the final totals row
        setupTotalRow();
    }

    private void setupTitle()
    {
        TableRow titleRow = new TableRow(_context);
        TableCell titleCell = new TableCell(_context, TableCell.TITLE_CELL);

        switch (_timeframe)
        {
            case day:
                titleCell.setText(R.string.daily_title);
                break;
            case month:
                titleCell.setText(R.string.month_weekly_title);
                break;
            case year:
                titleCell.setText(R.string.year_monthly_title);
                break;
            case total:
                titleCell.setText(R.string.total_yearly_title);
                break;
        }

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 4;
        titleCell.setLayoutParams(params);
        titleRow.addView(titleCell);
        addView(titleRow);
    }

    private void setupHeaders()
    {
        TableRow headerRow = new TableRow(_context);
        TableCell labelCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.HEADER_CELL);

        // Set the text
        switch (_timeframe)
        {
            case day:
                labelCell.setText(R.string.day);
                break;
            case month:
                labelCell.setText(R.string.week);
                break;
            case year:
                labelCell.setText(R.string.month);
                break;
            case total:
                labelCell.setText(R.string.year);
                break;
        }
        expenseCell.setText(R.string.spent);
        budgetCell.setText(R.string.budget);
        remainingCell.setText(R.string.remaining);

        // Add the header row
        headerRow.addView(labelCell);
        headerRow.addView(expenseCell);
        headerRow.addView(budgetCell);
        headerRow.addView(remainingCell);
        addView(headerRow);
    }

    private void setupContent()
    {
        switch (_timeframe)
        {
            case day:
                setupDaily();
                break;
            case month:
                setupWeekly();
                break;
            case year:
                setupMonthly();
                break;
            case total:
                setupYearly();
                break;
        }
    }

    private void setupDaily()
    {

    }

    private void setupWeekly()
    {
        // Add the option to jump to the start of a week
        Calendar c = Calendar.getInstance();
        c.set(_year, _month - 1, 1);
        int maxDays = c.getActualMaximum(Calendar.DATE);
        _rows = 4 + (maxDays > 28 ? 1 : 0);

        // Add the extras row
        addExtrasRow();

        String week = getResources().getString(R.string.week) + " ";
        for (int i = 0; i < _rows; i++) // Add a row for each week
        {
            addContentRow((week + (i + 1)), (i + 1));
        }

        if (_rows == 4)
        {
            addContentRow((week + 5), 5);
        }
        else
        {
        }
    }

    private void setupMonthly()
    {
        // Do not add the extras row
        //addExtrasRow();

        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();

        _rows = 12;
        for (int i = 0; i < 12; i++) // Add a row for each month
        {
            addContentRow(months[i], (i + 1));
        }
    }

    private void setupYearly()
    {
        // Setup the loading row
        TableRow tableRow = new TableRow(_context);
        TableCell yearCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        yearCell.setLoading(true);
        expenseCell.setLoading(true);
        budgetCell.setLoading(true);
        remainingCell.setLoading(true);

        tableRow.addView(yearCell);
        tableRow.addView(expenseCell);
        tableRow.addView(budgetCell);
        tableRow.addView(remainingCell);

        addView(tableRow);

        _rows = 0;
    }

    private void setupTotalRow()
    {
        TableRow totalRow = new TableRow(_context);
        TableCell contentCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.BOLD_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.BOLD_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.BOLD_CELL);

        contentCell.setText(R.string.total);
        expenseCell.setLoading(true);
        budgetCell.setLoading(true);
        remainingCell.setLoading(true);

        _totalExpenses = 0.0f;
        _totalExpenseCell = expenseCell;
        _totalBudgetCell = budgetCell;
        _totalRemainingCell = remainingCell;

        totalRow.addView(contentCell);
        totalRow.addView(expenseCell);
        totalRow.addView(budgetCell);
        totalRow.addView(remainingCell);
        addView(totalRow);
    }

    public void resetTable() // TODO
    {
        //removeAllViews();
        //setupTable();
    }

    public void updateWeeklyExpenditures(float[] amounts)
    {
        if (_timeframe == timeframe.month)
        {
            float total = 0.0f;
            for (int i = 0; i < amounts.length; i++)
            {
                total += amounts[i];
                if (!((_rows == 4) && (i == 4))) // Flag for last row of February
                {
                    _expenses.set(i, amounts[i]);
                    _expenseCells.get(i).setText(Currencies.formatCurrency(Currencies.default_currency, amounts[i]));
                    _expenseCells.get(i).setLoading(false);
                }
                else { }
            }
            _totalExpenses = total;
            _totalExpenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
            _totalExpenseCell.setLoading(false);
        }
        else
        {
            Log.e(TAG, "Do not use this function with this table type");
        }
    }

    public void updateMonthlyExpenditures(float[] amounts)
    {
        int rowsToInclude = _rows; // Do not include months in the future in the budget
        if (_year == Calendar.getInstance().get(Calendar.YEAR))
        {
            rowsToInclude = Calendar.getInstance().get(Calendar.MONTH)+1;
        }
        else { }
        TableCell expenseCell;
        if (_timeframe == timeframe.year)
        {
            float total = 0.0f;
            for (int i = 0; i < amounts.length; i++)
            {
                _expenses.set(i, amounts[i]);
                expenseCell = _expenseCells.get(i);
                expenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, amounts[i]));
                expenseCell.setLoading(false);
                if (i < rowsToInclude) // Ignore future purchases
                {
                    total += amounts[i];
                }
                else
                {
                    expenseCell.setTextGray();
                }
            }
            _totalExpenses = total;
            _totalExpenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
            _totalExpenseCell.setLoading(false);
        }
        else
        {
            Log.e(TAG, "Do not use this function with this table type");
        }
    }

    // For use in the total table
    public void updateTotalExpenditures(int startYear, float[] amounts)
    {
        if (_timeframe == timeframe.total)
        {
            removeAllViews();

            setupTitle();

            setupHeaders();

            _expenses = new ArrayList<Float>();
            _budgetCells = new ArrayList<TableCell>();
            _remainingCells = new ArrayList<TableCell>();

            float total = 0.0f;
            _startYear = startYear;
            if (amounts.length == 0)
            {
                _endYear = _startYear;
                addContentRow("" + _endYear, 0.0f, startYear);
            }
            else
            {
                _endYear = startYear + amounts.length - 1;
                for (int i = 0; i < amounts.length; i++)
                {
                    // One row per year
                    addContentRow(("" + (i + startYear)), amounts[i], (i + startYear));

                    total += amounts[i];
                }
            }

            setupTotalRow();
            _totalExpenses = total;
            _totalExpenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
            _totalExpenseCell.setLoading(false);
        }
        else
        {
            Log.e(TAG, "Do not use this function with this table type");
        }
    }

    private void addExtrasRow()
    {
        TableRow weekRow = new TableRow(_context);
        TableCell weekCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        weekCell.setText(R.string.extras);
        budgetCell.setText(R.string.empty);
        remainingCell.setText(R.string.empty);

        expenseCell.setLoading(true);

        _expenses.add(0.0f);
        _expenseCells.add(expenseCell);

        weekRow.addView(weekCell);
        weekRow.addView(expenseCell);
        weekRow.addView(budgetCell);
        weekRow.addView(remainingCell);
        addView(weekRow);
    }

    private void addContentRow(String label, int id)
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

        _expenses.add(0.0f);
        _expenseCells.add(expenseCell);
        _budgetCells.add(budgetCell);
        _remainingCells.add(remainingCell);

        labelCell.setId(id);
        labelCell.setOnClickListener(this);

        tableRow.addView(labelCell);
        tableRow.addView(expenseCell);
        tableRow.addView(budgetCell);
        tableRow.addView(remainingCell);

        addView(tableRow);
    }

    // Called by Total View
    private void addContentRow(String label, float amount, int id)
    {
        TableRow tableRow = new TableRow(_context);
        TableCell labelCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        labelCell.setText(label);
        expenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, amount));
        budgetCell.setLoading(true);
        remainingCell.setLoading(true);

        _expenses.add(amount);
        _expenseCells.add(expenseCell);
        _budgetCells.add(budgetCell);
        _remainingCells.add(remainingCell);

        _rows++;

        labelCell.setId(id);
        labelCell.setOnClickListener(this);

        tableRow.addView(labelCell);
        tableRow.addView(expenseCell);
        tableRow.addView(budgetCell);
        tableRow.addView(remainingCell);

        addView(tableRow);
    }

    public void updateBudgets(List<BudgetEntity> budgetEntities)
    {
        if (_budgetCells != null)
        {
            switch (_timeframe)
            {
                case month:
                    updateBudgetsWeekly(budgetEntities);
                    break;
                case year:
                    updateBudgetsMonthly(budgetEntities);
                    break;
                case total:
                    updateBudgetsYearly(budgetEntities);
                    break;
            }
        }
        else { }
    }

    private void updateBudgetsWeekly(List<BudgetEntity> budgetEntities)
    {
        int size = budgetEntities.size();
        float total = 0.0f;
        // Sum up across categories and then divide the result by the number of weeks
        for (int i = 0; i < size; i++)
        {
            BudgetEntity entity = budgetEntities.get(i);
            total += entity.getAmount();
        }
        float budget = total / _rows;
        for (int i = 0; i < _rows; i++)
        {
            _budgetCells.get(i).setText(Currencies.formatCurrency(Currencies.default_currency, budget));
            _budgetCells.get(i).setLoading(false);
            float remaining = budget - _expenses.get(i+1); // +1 for extras
            _remainingCells.get(i).setText(Currencies.formatCurrency(Currencies.default_currency, remaining));
            _remainingCells.get(i).setLoading(false);
        }
        _totalBudgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
        _totalBudgetCell.setLoading(false);
        float totalRemaining = total - _totalExpenses;
        _totalRemainingCell.setText(Currencies.formatCurrency(Currencies.default_currency, totalRemaining));
        _totalRemainingCell.setLoading(false);
    }

    private void updateBudgetsMonthly(List<BudgetEntity> budgetEntities)
    {
        int catSize = Categories.getCategories().length;
        float total = 0.0f;

        int rowsToInclude = _rows; // Do not include months in the future in the budget
        if (_year == Calendar.getInstance().get(Calendar.YEAR))
        {
            rowsToInclude = Calendar.getInstance().get(Calendar.MONTH)+1;
        }
        else { }

        // Loop over the months
        for (int i = 0; i < _rows; i++)
        {
            float budget = 0.0f;
            TableCell budgetTableCell = _budgetCells.get(i);
            TableCell remainingTableCell = _remainingCells.get(i);

            // Sum up across categories
            for (int j = 0; j < catSize; j++)
            {
                BudgetEntity entity = budgetEntities.get(i + (j * 13) + 1); // Skip the first item
                budget += entity.getAmount();
            }

            budgetTableCell.setText(Currencies.formatCurrency(Currencies.default_currency, budget));
            budgetTableCell.setLoading(false);

            float remaining = budget - _expenses.get(i); // No extras in monthly
            remainingTableCell.setText(Currencies.formatCurrency(Currencies.default_currency, remaining));
            remainingTableCell.setLoading(false);

            // Do not include rows occurring in the future
            if (i < rowsToInclude)
            {
                total += budget;
            }
            else
            {
                // Expense cell is set to gray elsewhere
                budgetTableCell.setTextGray();
                remainingTableCell.setTextGray();
            }
        }

        // Update the totals
        _totalBudgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
        _totalBudgetCell.setLoading(false);
        float totalRemaining = total - _totalExpenses;
        _totalRemainingCell.setText(Currencies.formatCurrency(Currencies.default_currency, totalRemaining));
        _totalRemainingCell.setLoading(false);
    }

    private void updateBudgetsYearly(List<BudgetEntity> budgetEntities)
    {
        int catSize = Categories.getCategories().length;
        float total = 0.0f;
        for (int i = 0; i < _rows; i++)
        {
            float budget = 0.0f;
            // Sum up across categories
            for (int j = 0; j < catSize; j++)
            {
                BudgetEntity entity = budgetEntities.get(i + (j * _rows)); // TODO: Error returning from Settings passing index, also budget appears to be wrong
                budget += entity.getAmount();
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

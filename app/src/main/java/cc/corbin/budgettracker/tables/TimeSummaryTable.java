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

public abstract class TimeSummaryTable extends TableLayout implements View.OnClickListener // TODO Temp
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
    protected timeframe _timeframe;

    protected List<Float> _expenses;
    protected List<Float> _budgets;

    protected List<TableCell> _expenseCells;
    protected TableCell _totalExpenseCell;
    protected List<TableCell> _budgetCells;
    protected TableCell _totalBudgetCell;
    protected List<TableCell> _remainingCells;
    protected TableCell _totalRemainingCell;

    protected float _totalExpenses;

    protected int _rows;

    protected boolean _multiTime;
    protected int _year;
    protected int _month;
    protected int _day;
    private int[] _years;
    private int[] _months;
    private int[] _days;

    public TimeSummaryTable(Context context, int year, int month, int day)
    {
        super(context);
        _context = context;
        _multiTime = false;
        _year = year;
        _month = month;
        _day = day;

        setupTable();
    }

    public TimeSummaryTable(Context context, int[] years, int[] months, int[] days)
    {
        super(context);
        _context = context;
        _multiTime = false;
        _years = years;
        _months = months;
        _days = days;

        setupTable();
    }

    private void setupTable()
    {
        _totalExpenses = 0.0f;
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

    protected abstract void setupTitle();

    protected void setupTitle(String title)
    {
        TableRow titleRow = new TableRow(_context);
        TableCell titleCell = new TableCell(_context, TableCell.TITLE_CELL);

        titleCell.setText(title);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 4;
        titleCell.setLayoutParams(params);
        titleRow.addView(titleCell);
        addView(titleRow);
    }

    protected abstract void setupHeaders();

    protected void setupHeaders(String timeHeader)
    {
        TableRow headerRow = new TableRow(_context);
        TableCell labelCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.HEADER_CELL);

        // Set the text
        labelCell.setText(timeHeader);
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

    protected abstract void setupContent();

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

    public abstract void updateExpenditures(float[] amounts);

    public abstract void updateBudgets(List<BudgetEntity> budgetEntities);
}
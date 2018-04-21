package cc.corbin.budgettracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Corbin on 1/29/2018.
 */

public class YearMonthlySummaryTable extends TableLayout
{
    private final String TAG = "YearMonthlySummaryTable";

    private Context _context;

    private int _month;
    private int _year;

    public YearMonthlySummaryTable(Context context)
    {
        super(context);
        _context = context;

        _year = 2018;
    }

    public YearMonthlySummaryTable(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MonthTable,
                0, 0);

        try
        {
            _year = a.getInteger(R.styleable.MonthTable_year, 2018);
        }
        catch (Exception e)
        {
            _year = 2018;
        }
        finally
        {
            a.recycle();
        }
    }

    public YearMonthlySummaryTable(Context context, int year)
    {
        super(context);
        _context = context;

        _year = year;
    }

    public void setup(List<ExpenditureEntity> yearExpenditures)
    {
        boolean integer = Currencies.integer[Currencies.default_currency];

        // Setup the table
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setStretchAllColumns(true);
        setColumnShrinkable(0, true);

        // Setup the title
        TableRow titleRow = new TableRow(_context);
        TableCell titleCell = new TableCell(_context, TableCell.TITLE_CELL);
        titleCell.setText(R.string.year_monthly_title);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 4;
        titleCell.setLayoutParams(params);
        titleRow.addView(titleCell);
        addView(titleRow);

        // Setup the header
        TableRow headerRow = new TableRow(_context);
        TableCell monthCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.HEADER_CELL);

        // Set the text
        monthCell.setText("Month");
        expenseCell.setText("Spent");
        budgetCell.setText("Budget");
        remainingCell.setText("Remaining");

        // Add the header row
        headerRow.addView(monthCell);
        headerRow.addView(expenseCell);
        headerRow.addView(budgetCell);
        headerRow.addView(remainingCell);
        addView(headerRow);

        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();

        float yearBudget = 0.0f;
        float budget = yearBudget / 12;

        float yearTotal = 0.0f;
        for (int i = 0; i < 12; i++) // Add a row for each month
        {
            TableRow weekRow = new TableRow(_context);
            monthCell = new TableCell(_context, TableCell.HEADER_CELL);
            expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

            float total = getMonthTotal(yearExpenditures, i+1);
            yearTotal += total;

            monthCell.setText(months[i]);
            expenseCell.setText(Currencies.formatCurrency(integer, total));
            budgetCell.setText(Currencies.formatCurrency(integer, budget));
            remainingCell.setText(Currencies.formatCurrency(integer, (budget - total)));

            weekRow.addView(monthCell);
            weekRow.addView(expenseCell);
            weekRow.addView(budgetCell);
            weekRow.addView(remainingCell);
            addView(weekRow);
        }

        // Add the final totals row
        TableRow totalRow = new TableRow(_context);
        monthCell = new TableCell(_context, TableCell.HEADER_CELL);
        expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        monthCell.setText(R.string.total);
        expenseCell.setText(Currencies.formatCurrency(integer, yearTotal));
        budgetCell.setText(Currencies.formatCurrency(integer, yearBudget));
        remainingCell.setText(Currencies.formatCurrency(integer, (yearBudget - yearTotal)));

        totalRow.addView(monthCell);
        totalRow.addView(expenseCell);
        totalRow.addView(budgetCell);
        totalRow.addView(remainingCell);
        addView(totalRow);
    }

    private float getMonthTotal(List<ExpenditureEntity> expenditureEntities, int monthNum)
    {
        float total = 0.0f;
        int count = expenditureEntities.size();
        for (int i = 0; i < count; i++)
        {
            ExpenditureEntity exp = expenditureEntities.get(i);
            if (exp.getMonth() == monthNum) // Items are not sorted
            {
                total += exp.getAmount();
            }
            else { }
        }

        return total;
    }
}

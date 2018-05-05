package cc.corbin.budgettracker.total;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.text.DateFormatSymbols;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.TableCell;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 1/29/2018.
 */

public class TotalYearlySummaryTable extends TableLayout
{
    private final String TAG = "TotalYearlySummaryTable";

    private Context _context;

    public TotalYearlySummaryTable(Context context)
    {
        super(context);
        _context = context;
    }

    public TotalYearlySummaryTable(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MonthTable,
                0, 0);
    }

    public void setup(List<ExpenditureEntity> yearExpenditures)
    {
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
            expenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
            budgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, budget));
            remainingCell.setText(Currencies.formatCurrency(Currencies.default_currency, (budget - total)));

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
        expenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, yearTotal));
        budgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, yearBudget));
        remainingCell.setText(Currencies.formatCurrency(Currencies.default_currency, (yearBudget - yearTotal)));

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

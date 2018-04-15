package cc.corbin.budgettracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Corbin on 1/29/2018.
 */

public class MonthCategorySummaryTable extends TableLayout
{
    private final String TAG = "MonthCategorySummaryTable";

    private Context _context;

    private int _month;
    private int _year;

    public MonthCategorySummaryTable(Context context)
    {
        super(context);
        _context = context;

        _month = 1;
        _year = 2018;
    }

    public MonthCategorySummaryTable(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MonthTable,
                0, 0);

        try
        {
            _month = a.getInteger(R.styleable.MonthTable_month, 1);
            _year = a.getInteger(R.styleable.MonthTable_year, 2018);
        }
        catch (Exception e)
        {
            _month = 1;
            _year = 2018;
        }
        finally
        {
            a.recycle();
        }
    }

    public MonthCategorySummaryTable(Context context, int month, int year)
    {
        super(context);
        _context = context;

        _month = month;
        _year = year;
    }

    public void setup(List<ExpenditureEntity> monthExpenditures)
    {
        boolean integer = Currencies.integer[Currencies.default_currency];

        String[] categories = DayViewActivity.getCategories();
        int rows = categories.length;

        // Setup the table
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setStretchAllColumns(true);
        setColumnShrinkable(0, true);

        // Setup the title
        TableRow titleRow = new TableRow(_context);
        TableCell titleCell = new TableCell(_context, TableCell.TITLE_CELL);
        titleCell.setText(R.string.month_category_title);
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
        TableCell categoryCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.HEADER_CELL);

        // Set the text
        categoryCell.setText("Category");
        expenseCell.setText("Spent");
        budgetCell.setText("Budget");
        remainingCell.setText("Remaining");

        // Add the header row
        headerRow.addView(categoryCell);
        headerRow.addView(expenseCell);
        headerRow.addView(budgetCell);
        headerRow.addView(remainingCell);
        addView(headerRow);

        // Add the option to jump to the start of a week
        Calendar c = Calendar.getInstance();
        c.set(_year, _month-1, 1);
        int maxDays = c.getActualMaximum(Calendar.DATE);
        int maxWeeks = 4 + (maxDays > 28 ? 1 : 0);

        float[] budget = new float[categories.length];
        float monthBudget = 0.0f;
        for (int i = 0; i < budget.length; i++)
        {
            budget[i] = 0.0f;
            monthBudget += budget[i];
        }

        float monthTotal = 0.0f;
        for (int i = 0; i < rows; i++) // Add a row for each category
        {
            TableRow weekRow = new TableRow(_context);
            categoryCell = new TableCell(_context, TableCell.HEADER_CELL);
            expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

            float total = getCategoryTotal(monthExpenditures, categories[i]);
            monthTotal += total;

            categoryCell.setText(categories[i]);
            expenseCell.setText(Currencies.formatCurrency(integer, total));
            budgetCell.setText(Currencies.formatCurrency(integer, budget[i]));
            remainingCell.setText(Currencies.formatCurrency(integer, (budget[i] - total)));

            weekRow.addView(categoryCell);
            weekRow.addView(expenseCell);
            weekRow.addView(budgetCell);
            weekRow.addView(remainingCell);
            addView(weekRow);
        }

        // Add the final totals row
        TableRow totalRow = new TableRow(_context);
        categoryCell = new TableCell(_context, TableCell.HEADER_CELL);
        expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        categoryCell.setText(R.string.total);
        expenseCell.setText(Currencies.formatCurrency(integer, monthTotal));
        budgetCell.setText(Currencies.formatCurrency(integer, monthBudget));
        remainingCell.setText(Currencies.formatCurrency(integer, (monthBudget - monthTotal)));

        totalRow.addView(categoryCell);
        totalRow.addView(expenseCell);
        totalRow.addView(budgetCell);
        totalRow.addView(remainingCell);
        addView(totalRow);
    }

    private float getCategoryTotal(List<ExpenditureEntity> expenditureEntities, String category)
    {
        float total = 0.0f;
        int count = expenditureEntities.size();
        for (int i = 0; i < count; i++)
        {
            ExpenditureEntity exp = expenditureEntities.get(i);
            if (exp.getExpenseType().equals(category))
            {
                total += exp.getAmount();
            }
            else { }
        }

        return total;
    }
}

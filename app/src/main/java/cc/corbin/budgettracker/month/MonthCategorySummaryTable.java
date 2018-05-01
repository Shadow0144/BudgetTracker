package cc.corbin.budgettracker.month;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.TableCell;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 1/29/2018.
 */

public class MonthCategorySummaryTable extends TableLayout
{
    private final String TAG = "MonthCategorySummaryTable";

    private Context _context;

    private int _month;
    private int _year;

    private List<TableCell> _budgetCells;
    private TableCell _totalBudgetCell;

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

        _budgetCells = new ArrayList<TableCell>();

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
            TableRow categoryRow = new TableRow(_context);
            categoryCell = new TableCell(_context, TableCell.HEADER_CELL);
            expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

            _budgetCells.add(budgetCell);

            float total = getCategoryTotal(monthExpenditures, categories[i]);
            monthTotal += total;

            categoryCell.setText(categories[i]);
            expenseCell.setText(Currencies.formatCurrency(integer, total));
            budgetCell.setText(Currencies.formatCurrency(integer, budget[i]));
            remainingCell.setText(Currencies.formatCurrency(integer, (budget[i] - total)));

            categoryRow.addView(categoryCell);
            categoryRow.addView(expenseCell);
            categoryRow.addView(budgetCell);
            categoryRow.addView(remainingCell);
            addView(categoryRow);
        }

        // Add the final totals row
        TableRow totalRow = new TableRow(_context);
        categoryCell = new TableCell(_context, TableCell.HEADER_CELL);
        expenseCell = new TableCell(_context, TableCell.BOLD_CELL);
        budgetCell = new TableCell(_context, TableCell.BOLD_CELL);
        remainingCell = new TableCell(_context, TableCell.BOLD_CELL);

        _totalBudgetCell = budgetCell;

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

    public void updateBudgets(List<BudgetEntity> budgetEntities)
    {
        if (_budgetCells != null)
        {
            int size = _budgetCells.size();
            float total = 0.0f;
            for (int i = 0; i < size; i++)
            {
                BudgetEntity entity = budgetEntities.get(i);
                _budgetCells.get(i).setText(Currencies.formatCurrency(entity.getCurrency(), entity.getAmount()));
                total += entity.getAmount();
            }
            _totalBudgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
        }
        else { }
    }
}

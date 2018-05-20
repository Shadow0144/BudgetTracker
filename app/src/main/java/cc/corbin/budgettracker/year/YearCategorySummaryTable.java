package cc.corbin.budgettracker.year;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.TableCell;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 1/29/2018.
 */

public class YearCategorySummaryTable extends TableLayout
{
    private final String TAG = "YearCategorySummaryTable";

    private Context _context;

    private int _year;

    private List<Float> _expenses;
    private float _totalExpenses;

    private List<TableCell> _expenseCells;
    private TableCell _totalExpenseCell;

    private List<TableCell> _budgetCells;
    private TableCell _totalBudgetCell;

    private List<TableCell> _remainingCells;
    private TableCell _totalRemainingCell;

    private String[] _categories;

    public YearCategorySummaryTable(Context context)
    {
        super(context);
        _context = context;

        _year = 2018;

        createTable();
    }

    public YearCategorySummaryTable(Context context, AttributeSet attrs)
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

        createTable();
    }

    public YearCategorySummaryTable(Context context, int year)
    {
        super(context);
        _context = context;

        _year = year;

        createTable();
    }

    private void createTable()
    {
        _expenses = new ArrayList<Float>();
        _expenseCells = new ArrayList<TableCell>();
        _budgetCells = new ArrayList<TableCell>();
        _remainingCells = new ArrayList<TableCell>();

        _categories = Categories.getCategories();
        int rows = _categories.length;

        // Setup the table
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setStretchAllColumns(true);
        setColumnShrinkable(0, true);

        // Setup the title
        TableRow titleRow = new TableRow(_context);
        TableCell titleCell = new TableCell(_context, TableCell.TITLE_CELL);
        titleCell.setText(R.string.year_category_title);
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

        float[] budget = new float[_categories.length];
        float yearBudget = 0.0f;
        for (int i = 0; i < budget.length; i++)
        {
            budget[i] = 0.0f;
            yearBudget += budget[i];
        }

        float yearTotal = 0.0f;
        for (int i = 0; i < rows; i++) // Add a row for each category
        {
            TableRow categoryRow = new TableRow(_context);
            categoryCell = new TableCell(_context, TableCell.HEADER_CELL);
            expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

            float total = 0.0f; //getCategoryTotal(yearExpenditures, categories[i]);
            yearTotal += total;

            _expenses.add(total);
            _expenseCells.add(expenseCell);
            _budgetCells.add(budgetCell);
            _remainingCells.add(remainingCell);

            categoryCell.setText(_categories[i]);
            expenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
            budgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, budget[i]));
            remainingCell.setText(Currencies.formatCurrency(Currencies.default_currency, (budget[i] - total)));

            expenseCell.setLoading(true);
            budgetCell.setLoading(true);
            remainingCell.setLoading(true);

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

        _totalExpenseCell = expenseCell;
        _totalBudgetCell = budgetCell;
        _totalRemainingCell = remainingCell;

        categoryCell.setText(R.string.total);
        expenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, yearTotal));
        budgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, yearBudget));
        remainingCell.setText(Currencies.formatCurrency(Currencies.default_currency, (yearBudget - yearTotal)));

        expenseCell.setLoading(true);
        budgetCell.setLoading(true);
        remainingCell.setLoading(true);

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

    public void updateExpenditures(List<ExpenditureEntity> expenditureEntities)
    {
        float total = 0.0f;
        int size = _expenseCells.size();
        for (int i = 0; i < size; i++)
        {
            float categoryTotal = getCategoryTotal(expenditureEntities, _categories[i]);
            _expenses.set(i, categoryTotal);
            _expenseCells.get(i).setText(Currencies.formatCurrency(Currencies.default_currency, categoryTotal));
            _expenseCells.get(i).setLoading(false);
            total += categoryTotal;
        }
        _totalExpenses = total;
        _totalExpenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
        _totalExpenseCell.setLoading(false);
    }

    public void updateBudgets(List<BudgetEntity> budgetEntities)
    {
        // Budget entities are sorted by month and category
        if (_budgetCells != null)
        {
            float total = 0.0f;
            int size = _budgetCells.size(); // Number of categories
            for (int i = 0; i < size; i++)
            {
                float catAmount = 0.0f;
                for (int j = 0; j < 12; j++)
                {
                    BudgetEntity entity = budgetEntities.get(i+(j*size));
                    catAmount += entity.getAmount();
                    total += entity.getAmount();
                }
                _budgetCells.get(i).setText(Currencies.formatCurrency(Currencies.default_currency, catAmount));
                _budgetCells.get(i).setLoading(false);
                float remaining = catAmount - _expenses.get(i);
                _remainingCells.get(i).setText(Currencies.formatCurrency(Currencies.default_currency, remaining));
                _remainingCells.get(i).setLoading(false);
            }
            _totalBudgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
            _totalBudgetCell.setLoading(false);
            float totalRemaining = total - _totalExpenses;
            _totalRemainingCell.setText(Currencies.formatCurrency(Currencies.default_currency, totalRemaining));
            _totalRemainingCell.setLoading(false);
        }
        else { }
    }
}

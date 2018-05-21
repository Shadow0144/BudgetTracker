package cc.corbin.budgettracker.total;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.TableCell;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 1/29/2018.
 */

public class TotalYearlySummaryTable extends TableLayout
{
    private final String TAG = "TotalYearlySummaryTable";

    private Context _context;

    private List<Float> _expenses;
    private float _totalExpenses;

    private List<TableCell> _budgetCells;
    private TableCell _totalBudgetCell;

    private List<TableCell> _remainingCells;
    private TableCell _totalRemainingCell;

    public TotalYearlySummaryTable(Context context)
    {
        super(context);
        _context = context;

        createTable();
    }

    public TotalYearlySummaryTable(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MonthTable,
                0, 0);

        createTable();
    }

    private void createTable()
    {
        // Setup the table
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setStretchAllColumns(true);
        setColumnShrinkable(0, true);

        createHeaders();

        // Setup the loading bar
        /*TableRow loadRow = new TableRow(_context);
        TableCell loadCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 4;
        loadCell.setLayoutParams(params);
        loadCell.setLoading(true);
        loadRow.addView(loadCell);
        addView(loadRow);*/

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

        // Setup the total row
        tableRow = new TableRow(_context);
        yearCell = new TableCell(_context, TableCell.HEADER_CELL);
        expenseCell = new TableCell(_context, TableCell.BOLD_CELL);
        budgetCell = new TableCell(_context, TableCell.BOLD_CELL);
        remainingCell = new TableCell(_context, TableCell.BOLD_CELL);

        yearCell.setText(R.string.total);
        expenseCell.setLoading(true);
        budgetCell.setLoading(true);
        remainingCell.setLoading(true);

        tableRow.addView(yearCell);
        tableRow.addView(expenseCell);
        tableRow.addView(budgetCell);
        tableRow.addView(remainingCell);

        addView(tableRow);
    }

    private void createHeaders()
    {
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
        TableCell yearCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.HEADER_CELL);

        // Set the text
        yearCell.setText("Year");
        expenseCell.setText("Spent");
        budgetCell.setText("Budget");
        remainingCell.setText("Remaining");

        // Add the header row
        headerRow.addView(yearCell);
        headerRow.addView(expenseCell);
        headerRow.addView(budgetCell);
        headerRow.addView(remainingCell);
        addView(headerRow);
    }

    public void updateExpenditures(List<ExpenditureEntity> expenditureEntities)
    {
        if (expenditureEntities != null)
        {
            int size = expenditureEntities.size();
            if (size > 0)
            {
                removeAllViews();

                createHeaders();

                _expenses = new ArrayList<Float>();
                _budgetCells = new ArrayList<TableCell>();
                _remainingCells = new ArrayList<TableCell>();

                int start = expenditureEntities.get(0).getYear();
                int end = expenditureEntities.get(size - 1).getYear();
                int year = start;
                int index = 0;

                float totalTotal = 0.0f;

                while (year <= end)
                {
                    // One row per year
                    TableRow tableRow = new TableRow(_context);
                    TableCell yearCell = new TableCell(_context, TableCell.HEADER_CELL);
                    TableCell expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
                    TableCell budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
                    TableCell remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

                    float yearTotal = 0.0f;
                    int entityYear = 0;
                    do
                    {
                        if (index < size)
                        {
                            ExpenditureEntity entity = expenditureEntities.get(index);
                            yearTotal += entity.getAmount();
                            entityYear = entity.getYear();
                            index++;
                        }
                        else
                        {
                            break;
                        }
                    }
                    while (entityYear == year && index < size);

                    yearCell.setText(""+(year++));
                    expenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, yearTotal));
                    budgetCell.setLoading(true);
                    remainingCell.setLoading(true);

                    _expenses.add(yearTotal);
                    _budgetCells.add(budgetCell);
                    _remainingCells.add(remainingCell);

                    tableRow.addView(yearCell);
                    tableRow.addView(expenseCell);
                    tableRow.addView(budgetCell);
                    tableRow.addView(remainingCell);

                    addView(tableRow);

                    totalTotal += yearTotal;
                } // end while (year <= end)

                TableRow tableRow = new TableRow(_context);
                TableCell yearCell = new TableCell(_context, TableCell.HEADER_CELL);
                TableCell expenseCell = new TableCell(_context, TableCell.BOLD_CELL);
                TableCell budgetCell = new TableCell(_context, TableCell.BOLD_CELL);
                TableCell remainingCell = new TableCell(_context, TableCell.BOLD_CELL);

                yearCell.setText(R.string.total);
                expenseCell.setText(Currencies.formatCurrency(Currencies.default_currency, totalTotal));
                budgetCell.setLoading(true);
                remainingCell.setLoading(true);

                _totalExpenses = totalTotal;
                _totalBudgetCell = budgetCell;
                _totalRemainingCell = remainingCell;

                tableRow.addView(yearCell);
                tableRow.addView(expenseCell);
                tableRow.addView(budgetCell);
                tableRow.addView(remainingCell);

                addView(tableRow);
            }
            else { }
        }
        else { }
    }

    public void updateBudgets(List<BudgetEntity> budgetEntities)
    {
        /// TODO
    }
}

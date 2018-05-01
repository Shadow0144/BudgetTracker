package cc.corbin.budgettracker.month;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.Calendar;
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

public class MonthWeeklySummaryTable extends TableLayout implements View.OnClickListener
{
    private final String TAG = "MonthWeeklySummaryTable";

    private Context _context;

    private int _month;
    private int _year;

    private List<TableCell> _budgetCells;
    private TableCell _totalBudgetCell;
    private int _weeks;

    public MonthWeeklySummaryTable(Context context)
    {
        super(context);
        _context = context;

        _month = 1;
        _year = 2018;
    }

    public MonthWeeklySummaryTable(Context context, AttributeSet attrs)
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

    public MonthWeeklySummaryTable(Context context, int month, int year)
    {
        super(context);
        _context = context;

        _month = month;
        _year = year;
    }

    public void onClick(View v)
    {
        Intent intent = new Intent(_context, DayViewActivity.class);
        Calendar date = Calendar.getInstance();
        int week = v.getId();
        date.set(_year, _month-1, ((week-1)*7) + 1);
        intent.putExtra(DayViewActivity.DATE_INTENT, date.getTimeInMillis());
        _context.startActivity(intent);
        ((MonthViewActivity)_context).finish();
    }

    public void setup(List<ExpenditureEntity> monthExpenditures)
    {
        boolean integer = Currencies.integer[Currencies.default_currency];

        _budgetCells = new ArrayList<TableCell>();

        // Setup the table
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setStretchAllColumns(true);
        setColumnShrinkable(0, true);

        // Setup the title
        TableRow titleRow = new TableRow(_context);
        TableCell titleCell = new TableCell(_context, TableCell.TITLE_CELL);
        titleCell.setText(R.string.month_weekly_title);
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
        TableCell weekCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell expenseCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell budgetCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell remainingCell = new TableCell(_context, TableCell.HEADER_CELL);

        // Set the text
        weekCell.setText("Week");
        expenseCell.setText("Spent");
        budgetCell.setText("Budget");
        remainingCell.setText("Remaining");

        // Add the header row
        headerRow.addView(weekCell);
        headerRow.addView(expenseCell);
        headerRow.addView(budgetCell);
        headerRow.addView(remainingCell);
        addView(headerRow);

        int id = 0; // For jumping to a week

        // Add the option to jump to the start of a week
        Calendar c = Calendar.getInstance();
        c.set(_year, _month-1, 1);
        int maxDays = c.getActualMaximum(Calendar.DATE);
        _weeks = 4 + (maxDays > 28 ? 1 : 0);

        float monthBudget = 0.0f;
        float budget = monthBudget / _weeks;

        float monthTotal = 0.0f;
        for (int i = 0; i < _weeks; i++) // Add a row for each week
        {
            TableRow weekRow = new TableRow(_context);
            weekCell = new TableCell(_context, TableCell.HEADER_CELL);
            expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            weekCell.setId(++id);

            _budgetCells.add(budgetCell);

            float total = getWeekTotal(monthExpenditures, i+1);
            monthTotal += total;

            weekCell.setText("Week " + (i+1)); // TODO Internationalize
            expenseCell.setText(Currencies.formatCurrency(integer, total));
            budgetCell.setText(Currencies.formatCurrency(integer, budget));
            remainingCell.setText(Currencies.formatCurrency(integer, (budget - total)));

            weekCell.setOnClickListener(this);

            weekRow.addView(weekCell);
            weekRow.addView(expenseCell);
            weekRow.addView(budgetCell);
            weekRow.addView(remainingCell);
            addView(weekRow);
        }

        if (_weeks == 4)
        {
            TableRow weekRow = new TableRow(_context);
            weekCell = new TableCell(_context, TableCell.HEADER_CELL);
            expenseCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            budgetCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            remainingCell = new TableCell(_context, TableCell.DEFAULT_CELL);

            weekCell.setText("Week " + 5); // TODO Internationalize
            expenseCell.setText("---");
            budgetCell.setText("---");
            remainingCell.setText("---");

            weekRow.addView(weekCell);
            weekRow.addView(expenseCell);
            weekRow.addView(budgetCell);
            weekRow.addView(remainingCell);
            addView(weekRow);
        }
        else { }

        // Add the final totals row
        TableRow totalRow = new TableRow(_context);
        weekCell = new TableCell(_context, TableCell.HEADER_CELL);
        expenseCell = new TableCell(_context, TableCell.BOLD_CELL);
        budgetCell = new TableCell(_context, TableCell.BOLD_CELL);
        remainingCell = new TableCell(_context, TableCell.BOLD_CELL);

        _totalBudgetCell = budgetCell;

        weekCell.setText(R.string.total);
        expenseCell.setText(Currencies.formatCurrency(integer, monthTotal));
        budgetCell.setText(Currencies.formatCurrency(integer, monthBudget));
        remainingCell.setText(Currencies.formatCurrency(integer, (monthBudget - monthTotal)));

        totalRow.addView(weekCell);
        totalRow.addView(expenseCell);
        totalRow.addView(budgetCell);
        totalRow.addView(remainingCell);
        addView(totalRow);
    }

    private float getWeekTotal(List<ExpenditureEntity> expenditureEntities, int weekNum)
    {
        // Set the calendar to the correct month for getting the number of days
        Calendar c = Calendar.getInstance();
        c.set(_year, _month-1, 1);
        int maxDays = c.getActualMaximum(Calendar.DATE);

        int sDay = ((weekNum-1)*7)+1; // 1, 8, 15, 22, 29
        int eDay = Math.min((weekNum*7), maxDays); // 7, 14, 21, 28, 28/29/30/31

        float total = 0.0f;
        if (eDay >= sDay)
        {
            int count = expenditureEntities.size();

            for (int i = 0; i < count; i++)
            {
                ExpenditureEntity exp = expenditureEntities.get(i);
                if (exp.getDay() >= sDay && exp.getDay() <= eDay) // Items are not sorted
                {
                    total += exp.getAmount();
                }
                else { }
            }
        }
        else { } // Return 0

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
                total += entity.getAmount();
            }
            float budget = total / _weeks;
            for (int i = 0; i < _weeks; i++)
            {
                _budgetCells.get(i).setText(Currencies.formatCurrency(Currencies.default_currency, budget));
            }
            _totalBudgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
        }
        else { }
    }
}

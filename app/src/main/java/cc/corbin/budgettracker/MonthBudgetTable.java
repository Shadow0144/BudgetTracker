package cc.corbin.budgettracker;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Corbin on 1/29/2018.
 */

public class MonthBudgetTable extends TableLayout
{
    private final String TAG = "MonthBudgetTable";

    private Context _context;

    private int _month;
    private int _year;

    private List<BudgetEntity> _budgets;

    private List<TableCell> _budgetCells;
    private List<TableCell> _dateCells;

    private boolean _setup;

    public MonthBudgetTable(Context context)
    {
        super(context);
        _context = context;

        _month = 1;
        _year = 2018;

        _setup = false;
    }

    public MonthBudgetTable(Context context, AttributeSet attrs)
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

        _setup = false;
    }

    public MonthBudgetTable(Context context, int month, int year)
    {
        super(context);
        _context = context;

        _month = month;
        _year = year;

        _setup = false;
    }

    public void setup(List<BudgetEntity> budgets)
    {
        _budgets = budgets;
        _budgetCells = new ArrayList<TableCell>();
        _dateCells = new ArrayList<TableCell>();

        String[] categories = DayViewActivity.getCategories();
        int count = categories.length;

        TableRow titleRow = new TableRow(_context);
        TableCell titleCell = new TableCell(_context, TableCell.TITLE_CELL);
        titleCell.setText(R.string.budget_title);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 3;
        titleCell.setLayoutParams(params);

        titleRow.addView(titleCell);
        addView(titleRow);

        setColumnStretchable(1, true);

        for (int i = 0; i < count; i++)
        {
            TableRow tableRow = new TableRow(_context);
            TableCell headerCell = new TableCell(_context, TableCell.HEADER_CELL);
            TableCell contentCell = new TableCell(_context, TableCell.DEFAULT_CELL);
            TableCell dateCell = new TableCell(_context, TableCell.DEFAULT_CELL);

            headerCell.setText(categories[i]);
            contentCell.setId(i);
            contentCell.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    editBudgetItem(v.getId());
                }
            });
            contentCell.setEnabled(false);
            _budgetCells.add(contentCell);
            _dateCells.add(dateCell);

            tableRow.addView(headerCell);
            tableRow.addView(contentCell);
            tableRow.addView(dateCell);

            addView(tableRow);
        }

        _setup = true;
    }

    public void refreshTable(List<BudgetEntity> budgets)
    {
        if (budgets != null)
        {
            _budgets = budgets;

            if (!_setup) // Run once the first time
            {
                setup(_budgets);
            }
            else
            {
            }

            int count = _budgets.size();
            for (int i = 0; i < count; i++)
            {
                // The entities and cells should be in the same order
                BudgetEntity entity = _budgets.get(i);
                TableCell cell = _budgetCells.get(i);
                cell.setText(Currencies.formatCurrency(entity.getCurrency(), entity.getAmount()));

                if (entity.getMonth() == _month && entity.getYear() == _year)
                {
                    cell.setup(_context, TableCell.SPECIAL_CELL);
                }
                else
                {
                    cell.setup(_context, TableCell.DEFAULT_CELL);
                }

                TableCell dateCell = _dateCells.get(i);
                if (entity.getId() != 0)
                {
                    String month = String.format("%02d", entity.getMonth());
                    dateCell.setText(_context.getString(R.string.budget_as_of) + month + "/" + entity.getYear());
                }
                else
                {
                    dateCell.setText(_context.getString(R.string.budget_unset));
                }
            }

            unlockTable();
        }
        else { }
    }

    private void editBudgetItem(int id)
    {
        ((MonthViewActivity)_context).editBudgetItem(id);
    }

    public void lockTable()
    {
        int size = _budgetCells.size();
        for (int i = 0; i < size; i++)
        {
            _budgetCells.get(i).setEnabled(false);
        }
    }

    public void unlockTable()
    {
        int size = _budgetCells.size();
        for (int i = 0; i < size; i++)
        {
            _budgetCells.get(i).setEnabled(true);
        }
    }
}

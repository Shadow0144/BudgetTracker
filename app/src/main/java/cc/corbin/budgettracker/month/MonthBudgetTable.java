package cc.corbin.budgettracker.month;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
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
    private TableCell _totalBudgetCell;
    private TableCell _totalDateCell;

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

        String[] categories = Categories.getCategories();
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

        TableRow totalRow = new TableRow(_context);
        TableCell totalHeaderCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell totalContentCell = new TableCell(_context, TableCell.BOLD_CELL);
        TableCell totalDateCell = new TableCell(_context, TableCell.BOLD_CELL);

        _totalBudgetCell = totalContentCell;
        _totalDateCell = totalDateCell;

        totalHeaderCell.setText(_context.getString(R.string.total));

        totalRow.addView(totalHeaderCell);
        totalRow.addView(totalContentCell);
        totalRow.addView(totalDateCell);

        addView(totalRow);

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
            float total = 0.0f;
            int maxYear = 0;
            int maxMonth = 0;
            for (int i = 0; i < count; i++)
            {
                // The entities and cells should be in the same order
                BudgetEntity entity = _budgets.get(i);
                TableCell cell = _budgetCells.get(i);
                cell.setText(Currencies.formatCurrency(entity.getCurrency(), entity.getAmount()));
                total += entity.getAmount();

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
                    if ((entity.getYear() > maxYear) || (entity.getMonth() > maxMonth && entity.getYear() == maxYear))
                    {
                        maxMonth = entity.getMonth();
                        maxYear = entity.getYear();
                    }
                    else { }
                }
                else
                {
                    dateCell.setText(_context.getString(R.string.budget_unset));
                }
            }

            _totalBudgetCell.setText(Currencies.formatCurrency(Currencies.default_currency, total));
            if (maxMonth != 0)
            {
                String month = String.format("%02d", maxMonth);
                _totalDateCell.setText(_context.getString(R.string.budget_as_of) + month + "/" + maxYear);
            }
            else
            {
                _totalDateCell.setText(_context.getString(R.string.budget_unset));
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

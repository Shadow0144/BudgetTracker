package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.year.YearViewActivity;

public class BudgetTable extends LinearLayout
{

    private final String TAG = "BudgetTable";

    private Context _context;

    private int _month;
    private int _year;

    public enum timeframe
    {
        daily,
        monthly,
        yearly,
        total
    };
    private timeframe _timeframe;

    private List<BudgetEntity> _budgets;
    private ExpandableListView _budgetList;
    private BudgetExpandableListAdapter _budgetExpandableListAdapter;

    public BudgetTable(Context context)
    {
        super(context);
        _context = context;

        _month = 1;
        _year = 2018;
        _timeframe = timeframe.daily;

        setupTable();
    }

    public BudgetTable(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Table,
                0, 0);

        try
        {
            _month = a.getInteger(R.styleable.Table_month, 1);
            _year = a.getInteger(R.styleable.Table_year, 2018);
            _timeframe = timeframe.values()[a.getInteger(R.styleable.Table_timeframe, timeframe.daily.ordinal())];
        }
        catch (Exception e)
        {
            _month = 1;
            _year = 2018;
            _timeframe = timeframe.daily;
        }
        finally
        {
            a.recycle();
        }

        setupTable();
    }

    public BudgetTable(Context context, int month, int year)
    {
        super(context);
        _context = context;

        _month = month;
        _year = year;
        _timeframe = timeframe.monthly;

        setupTable();
    }

    public BudgetTable(Context context, int year)
    {
        super(context);
        _context = context;

        _month = 0;
        _year = year;
        _timeframe = timeframe.yearly;

        setupTable();
    }

    public void setupTable()
    {
        setOrientation(LinearLayout.VERTICAL);

        //TableRow titleRow = new TableRow(_context);
        TableCell titleCell = new TableCell(_context, TableCell.TITLE_CELL);
        if (_timeframe == timeframe.monthly)
        {
            titleCell.setText(R.string.monthly_budget_title);
        }
        else
        {
            titleCell.setText(R.string.yearly_budget_title);
        }

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        //params.span = 1;
        titleCell.setLayoutParams(params);

        //titleRow.addView(titleCell);
        //addView(titleRow);
        addView(titleCell);

        //setColumnStretchable(1, true);

        _budgetExpandableListAdapter = new BudgetExpandableListAdapter(_context);
        _budgetList = new ExpandableListView(_context);
        _budgetList.setAdapter(_budgetExpandableListAdapter);
        ViewGroup.LayoutParams lParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        _budgetList.setLayoutParams(lParams);

        addView(_budgetList);

        /*for (int i = 0; i < count; i++)
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

            contentCell.setLoading(true);
            dateCell.setLoading(true);

            tableRow.addView(headerCell);
            tableRow.addView(contentCell);
            tableRow.addView(dateCell);

            addView(tableRow);
        }*/

        /*TableRow totalRow = new TableRow(_context);
        TableCell totalHeaderCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell totalContentCell = new TableCell(_context, TableCell.BOLD_CELL);
        TableCell totalDateCell = new TableCell(_context, TableCell.BOLD_CELL);

        _totalBudgetCell = totalContentCell;
        _totalDateCell = totalDateCell;

        _totalBudgetCell.setLoading(true);
        _totalDateCell.setLoading(true);

        totalHeaderCell.setText(_context.getString(R.string.total));

        totalRow.addView(totalHeaderCell);
        totalRow.addView(totalContentCell);
        totalRow.addView(totalDateCell);

        addView(totalRow);*/
    }

    public void resetTable()
    {
        removeAllViews();
        setupTable();
    }

    public void refreshTable(List<BudgetEntity> budgets)
    {
        if (budgets != null)
        {
            _budgets = budgets;

            _budgetExpandableListAdapter.setBudgets(_budgets);

            invalidate();
            //unlockTable();
        }
        else { }
    }

    private void editBudgetItem(int id)
    {
        if (_timeframe == timeframe.monthly)
        {
            ((MonthViewActivity) _context).editBudgetItem(id);
        }
        else if (_timeframe == timeframe.yearly)
        {
            ((YearViewActivity) _context).editBudgetItem(id);
        }
        else { }
    }

    /*public void lockTable()
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
    }*/
}

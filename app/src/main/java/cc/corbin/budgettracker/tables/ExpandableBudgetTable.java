package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class ExpandableBudgetTable extends ExpandableListView
{
    private final String TAG = "ExpandableBudgetTable";

    private Context _context;
    private int _month;
    private int _year;

    public enum timeframe
    {
        monthly,
        yearly,
        total
    };
    private timeframe _timeframe;

    private TableCell _titleCell;
    private BudgetTableRow _totalCell;

    private BudgetExpandableListAdapter _budgetsListAdapter;

    public ExpandableBudgetTable(Context context)
    {
        super(context);
        _context = context;

        _month = 1;
        _year = 2018;
        _timeframe = timeframe.monthly;

        setupTable();
    }

    public ExpandableBudgetTable(Context context, AttributeSet attrs)
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
            _timeframe = timeframe.values()[a.getInteger(R.styleable.Table_timeframe, timeframe.monthly.ordinal())];
        }
        catch (Exception e)
        {
            _month = 1;
            _year = 2018;
            _timeframe = timeframe.monthly;
        }
        finally
        {
            a.recycle();
        }

        setupTable();
    }

    public ExpandableBudgetTable(Context context, int month, int year)
    {
        super(context);
        _context = context;

        _month = month;
        _year = year;
        _timeframe = timeframe.monthly;

        setupTable();
    }

    public ExpandableBudgetTable(Context context, int year)
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
        setDividerHeight(0); // Remove space between items

        // Setup header
        _titleCell = new TableCell(_context, TableCell.TITLE_CELL);
        if (_timeframe == ExpandableBudgetTable.timeframe.monthly)
        {
            _titleCell.setText(R.string.monthly_budget_title);
        }
        else
        {
            _titleCell.setText(R.string.yearly_budget_title);
        }
        addHeaderView(_titleCell);

        _budgetsListAdapter = new BudgetExpandableListAdapter(_context, _year, _month, _timeframe);
        setAdapter(_budgetsListAdapter);

        _totalCell = new BudgetTableRow(_context, _context.getString(R.string.total), -1, _year, _month, _timeframe);
        _totalCell.setBold();
        addFooterView(_totalCell);
    }

    public void resetTable() /// TODO?
    {
        //removeAllViews();
        //setupTable();
    }

    public void refreshTable(List<BudgetEntity> budgets)
    {
        BudgetEntity totalEntity = _budgetsListAdapter.setBudgets(budgets);

        _totalCell.setAmountAndDate(totalEntity.getAmount(), totalEntity.getYear(), totalEntity.getMonth());
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec
                (Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}

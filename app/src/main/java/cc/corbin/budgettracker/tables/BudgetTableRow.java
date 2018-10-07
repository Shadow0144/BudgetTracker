package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TableRow;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.year.YearViewActivity;

public class BudgetTableRow extends LinearLayout
{
    private final String TAG = "BudgetTableRow";

    private Context _context;
    private String _category;
    private int _categoryNumber;
    private int _year;
    private int _month;
    private ExpandableBudgetTable.timeframe _timeframe;

    private BudgetEntity _budgetEntity;

    private TableCell _contentCell;
    private TableCell _dateCell;

    private float _amount;
    private float _additionalAmount;

    public BudgetTableRow(Context context)
    {
        super(context);
        _context = context;
        _category = "";
        _categoryNumber = 0;
        _year = 0;
        _month = 0;
        _timeframe = ExpandableBudgetTable.timeframe.monthly;
        setup();
    }

    public BudgetTableRow(Context context, String category, int categoryNumber, int year, int month, ExpandableBudgetTable.timeframe timeframe)
    {
        super(context);
        _context = context;
        _category = category;
        _categoryNumber = categoryNumber;
        _year = year;
        _month = month;
        _timeframe = timeframe;
        setup();
    }

    public BudgetTableRow(Context context, String category, int categoryNumber, int year, int month, ExpandableBudgetTable.timeframe timeframe, BudgetEntity budgetEntity)
    {
        super(context);
        _context = context;
        _category = category;
        _categoryNumber = categoryNumber;
        _year = year;
        _month = month;
        _timeframe = timeframe;
        setup();
        setBudgetEntity(budgetEntity);
    }

    public BudgetTableRow(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;
        _category = "";
        _categoryNumber = 0;
        _year = 0;
        _month = 0;
        _timeframe = ExpandableBudgetTable.timeframe.monthly;
        setup();
    }

    public BudgetTableRow(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        _context = context;
        _category = "";
        _categoryNumber = 0;
        _year = 0;
        _month = 0;
        _timeframe = ExpandableBudgetTable.timeframe.monthly;
        setup();
    }

    private void setup()
    {
        setOrientation(LinearLayout.HORIZONTAL);

        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);

        TableCell headerCell = new TableCell(_context, TableCell.HEADER_CELL);
        _contentCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        _dateCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        params.weight = 1;
        headerCell.setLayoutParams(params);
        _contentCell.setLayoutParams(params);
        _dateCell.setLayoutParams(params);

        headerCell.setText(_category);
        _contentCell.setEnabled(false);

        _contentCell.setLoading(true);
        _dateCell.setLoading(true);

        addView(headerCell);
        addView(_contentCell);
        addView(_dateCell);
    }

    public BudgetEntity getBudgetEntity()
    {
        return _budgetEntity;
    }

    public void setBudgetEntity(BudgetEntity budgetEntity)
    {
        _budgetEntity = budgetEntity;
        _contentCell.setType(TableCell.DEFAULT_CELL);

        if (_budgetEntity.getId() != 0)
        {
            setAmountAndDate(_budgetEntity.getAmount(), _budgetEntity.getYear(), _budgetEntity.getMonth());
        }
        else
        {
            setAmountAndDate(0, 0, 0);
        }

        _contentCell.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editBudgetItem();
            }
        });
        _contentCell.setEnabled(true); // Only enabled for rows with entities
    }

    public void setAmountAndDate(float amount, int year, int month)
    {
        _amount = amount;
        _contentCell.setText(Currencies.formatCurrency(Currencies.default_currency, amount));
        if (year != 0 && month != 0)
        {
            if (_timeframe == ExpandableBudgetTable.timeframe.monthly)
            {
                String monthString = String.format("%02d", month);
                _dateCell.setText(_context.getString(R.string.budget_as_of) + monthString + "/" + year);
                if ((year == _year) && (month == _month) && (_budgetEntity != null)) // Do not set the total cell to special
                {
                    _contentCell.setType(TableCell.SPECIAL_CELL);
                }
                else { }
            }
            else
            {
                _dateCell.setText(_context.getString(R.string.budget_as_of) + year);
                if ((year == _year) && (month == _month) && (_budgetEntity != null)) // Do not set the total cell to special
                {
                    _contentCell.setType(TableCell.SPECIAL_CELL);
                }
                else { }
            }
        }
        else
        {
            _dateCell.setText(_context.getString(R.string.budget_unset));
        }

        _contentCell.setLoading(false);
        _dateCell.setLoading(false);
    }

    private void editBudgetItem()
    {
        if (_timeframe == ExpandableBudgetTable.timeframe.monthly)
        {
            ((MonthViewActivity) _context).editBudgetItem(_categoryNumber);
        }
        else if (_timeframe == ExpandableBudgetTable.timeframe.yearly)
        {
            ((YearViewActivity) _context).editBudgetItem(_categoryNumber);
        }
        else { }
    }

    public void setBold()
    {
        _contentCell.setType(TableCell.BOLD_CELL);
        _dateCell.setType(TableCell.BOLD_CELL);
    }

    public void addEllipsis()
    {
        _contentCell.addEllipsis();
    }

    public void removeEllipsis()
    {
        _contentCell.removeEllipsis();
    }

    public void addAmount(float additionalAmount)
    {
        _additionalAmount = additionalAmount;
        _contentCell.setText(Currencies.formatCurrency(Currencies.default_currency, (_amount+_additionalAmount)));
    }
}

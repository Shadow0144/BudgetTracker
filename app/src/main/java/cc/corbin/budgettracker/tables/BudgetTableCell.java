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
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudgetTableCell extends LinearLayout
{
    private final String TAG = "BudgetTableCell";

    private Context _context;
    private BudgetEntity _budgetEntity;

    public BudgetTableCell(Context context)
    {
        super(context);
        _context = context;
    }

    public BudgetTableCell(Context context, BudgetEntity budgetEntity)
    {
        super(context);
        _context = context;

        _budgetEntity = budgetEntity;
        setup();
    }

    public BudgetTableCell(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;
    }

    public BudgetTableCell(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        _context = context;
    }

    public BudgetEntity getBudgetEntity()
    {
        return _budgetEntity;
    }

    public void setBudgetEntity(BudgetEntity budgetEntity)
    {
        _budgetEntity = budgetEntity;
        setup();
    }

    private void setup()
    {
        setOrientation(LinearLayout.HORIZONTAL);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);

        //TableRow tableRow = new TableRow(_context);
        TableCell headerCell = new TableCell(_context, TableCell.HEADER_CELL);
        TableCell contentCell = new TableCell(_context, TableCell.DEFAULT_CELL);
        TableCell dateCell = new TableCell(_context, TableCell.DEFAULT_CELL);

        headerCell.setText(_budgetEntity.getCategoryName());
        //contentCell.setId(i);
        /*contentCell.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editBudgetItem(v.getId());
            }
        });*/
        contentCell.setEnabled(false);
        //_budgetCells.add(contentCell);
        //_dateCells.add(dateCell);

        contentCell.setLoading(true);
        dateCell.setLoading(true);

        addView(headerCell);
        addView(contentCell);
        addView(dateCell);
    }
}

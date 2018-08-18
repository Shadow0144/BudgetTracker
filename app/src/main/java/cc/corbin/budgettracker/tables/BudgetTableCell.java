package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

import cc.corbin.budgettracker.R;

public class BudgetTableCell extends ExpandableListView
{
    public BudgetTableCell(Context context)
    {
        super(context);
    }

    public BudgetTableCell(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public BudgetTableCell(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }
}

package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class AdjustmentTableCell extends ExpandableListView
{
    private final String TAG = "AdjustmentTableCell";

    private BudgetEntity _budgetEntity;

    public AdjustmentTableCell(Context context)
    {
        super(context);
    }

    public AdjustmentTableCell(Context context, BudgetEntity budgetEntity)
    {
        super(context);

        _budgetEntity = budgetEntity;
        setup();
    }

    public AdjustmentTableCell(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public AdjustmentTableCell(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
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

    }
}

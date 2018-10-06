package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class AdjustmentTableCell extends LinearLayout
{
    private final String TAG = "AdjustmentTableCell";

    private Context _context;

    private BudgetEntity _budgetEntity;
    private int _groupIndex;
    private int _childIndex;

    private TextView _amountTextView;
    private TextView _noteTextView;

    public AdjustmentTableCell(Context context)
    {
        super(context);
        _context = context;
        setup();
    }

    public AdjustmentTableCell(Context context, BudgetEntity budgetEntity)
    {
        super(context);
        _context = context;
        setup();
        setBudgetEntity(budgetEntity);
    }

    public AdjustmentTableCell(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _context = context;
        setup();
    }

    public AdjustmentTableCell(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        _context = context;
        setup();
    }

    public BudgetEntity getBudgetEntity()
    {
        return _budgetEntity;
    }

    public void setBudgetEntity(BudgetEntity budgetEntity)
    {
        _budgetEntity = budgetEntity;
        _amountTextView.setText(Currencies.formatCurrency(Currencies.default_currency, _budgetEntity.getAmount()));
        String note = _budgetEntity.getNote();
        _noteTextView.setText(note);
        if (note.length() > 0)
        {
            _noteTextView.setVisibility(VISIBLE);
        }
        else { }
    }

    private void setup()
    {
        LayoutInflater inflater = LayoutInflater.from(_context);
        final View view = inflater.inflate(R.layout.adjustment, null);
        addView(view);

        _amountTextView = findViewById(R.id.amountTextView);
        _noteTextView = findViewById(R.id.noteTextView);
    }

    public void setIndices(int groupIndex, int childIndex)
    {
        _groupIndex = groupIndex;
        _childIndex = childIndex;
    }

    public int getGroupIndex()
    {
        return _groupIndex;
    }

    public int getChildIndex()
    {
        return _childIndex;
    }
}

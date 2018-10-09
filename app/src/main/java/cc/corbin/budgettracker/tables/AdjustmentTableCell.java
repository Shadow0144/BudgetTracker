package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class AdjustmentTableCell extends FrameLayout
{
    private final String TAG = "AdjustmentTableCell";

    private Context _context;

    private BudgetEntity _budgetEntity;
    private int _groupIndex;
    private int _childIndex;

    private TextView _amountTextView;
    private TextView _noteTextView;
    private TextView _linkedTextView;

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
        if (_budgetEntity.getLinkedID() > -1)
        {
            _linkedTextView.setVisibility(VISIBLE);
            String linkedText = formatLinkedDetails(_context, _budgetEntity.getLinkedMonth(), _budgetEntity.getLinkedYear(), _budgetEntity.getLinkedCategory());
            _linkedTextView.setText(linkedText);
        }
        else { }
    }

    private void setup()
    {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        setLayoutParams(params);

        LayoutInflater inflater = LayoutInflater.from(_context);
        final View view = inflater.inflate(R.layout.adjustment, null);
        addView(view);

        _amountTextView = findViewById(R.id.amountTextView);
        _noteTextView = findViewById(R.id.noteTextView);
        _linkedTextView = findViewById(R.id.linkedTextView);
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

    public static String formatLinkedDetails(Context context, int linkedMonth, int linkedYear, int linkedCategory)
    {
        String dateString = String.format("%02d", linkedMonth) + " / " + String.format("%04d", linkedYear);
        String linkedText = context.getString(R.string.linked) + " " + dateString + " : " + Categories.getCategories()[linkedCategory];
        return linkedText;
    }
}

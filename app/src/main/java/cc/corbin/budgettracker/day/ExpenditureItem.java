package cc.corbin.budgettracker.day;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class ExpenditureItem extends LinearLayout
{
    private final String TAG = "ExpenditureItem";

    private ExpenditureEntity _expenditure;

    public ExpenditureItem(Context context)
    {
        super(context);

        setup(context, null);
    }

    public ExpenditureItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        setup(context, null);
    }

    public ExpenditureItem(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        setup(context, null);
    }

    public ExpenditureItem(Context context, ExpenditureEntity entity)
    {
        super(context);

        setup(context, entity);
    }

    private void setup(Context context, ExpenditureEntity expenditure)
    {
        _expenditure = expenditure;

        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.item, null);

        refresh(view);

        addView(view);
    }

    private void refresh(View view)
    {
        final TextView currencyView = view.findViewById(R.id.currencyView);
        currencyView.setText(Currencies.symbols[_expenditure.getCurrency()]);

        final TextView categoryView = view.findViewById(R.id.categoryView);
        categoryView.setText(_expenditure.getExpenseType());

        final TextView costView = view.findViewById(R.id.costView);
        String cost = Currencies.formatCurrency(Currencies.integer[_expenditure.getCurrency()], _expenditure.getAmount());
        costView.setText(cost);

        final Button noteButton = view.findViewById(R.id.noteButton);
        final String note = _expenditure.getNote();
        if (note.length() == 0)
        {
            noteButton.setForegroundTintList(getContext().getColorStateList(R.color.translucent));
        }
        else
        {
            noteButton.setForegroundTintList(getContext().getColorStateList(R.color.black));
        }
    }

    public ExpenditureEntity getExpenditure()
    {
        return _expenditure;
    }

    public void updateExpenditure(ExpenditureEntity expenditure)
    {
        _expenditure.update(expenditure);
        refresh(this);
    }
}

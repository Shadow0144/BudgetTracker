package cc.corbin.budgettracker.day;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import cc.corbin.budgettracker.auxilliary.Categories;
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
        currencyView.setText(Currencies.symbols[Currencies.default_currency]);

        if (_expenditure.getBaseCurrency() != Currencies.default_currency)
        {
            final TextView conversionTextView = view.findViewById(R.id.conversionTextView);
            conversionTextView.setText("(" + Currencies.formatCurrency(_expenditure.getBaseCurrency(), _expenditure.getBaseAmount())
                    + " @ " + _expenditure.getConversionRate() + ")");
        }
        else { }

        final TextView categoryView = view.findViewById(R.id.categoryView);
        categoryView.setText(_expenditure.getCategoryName());

        final TextView costView = view.findViewById(R.id.costView);
        String cost = Currencies.formatCurrency(Currencies.integer[Currencies.default_currency], _expenditure.getAmount()); // TODO - Format better
        costView.setText(cost);

        final TextView noteTextView = view.findViewById(R.id.noteTextView);
        final String note = _expenditure.getNote();
        noteTextView.setVisibility((note.length() == 0) ? GONE : VISIBLE);
        noteTextView.setText(note);
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

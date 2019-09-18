package cc.corbin.budgettracker.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import cc.corbin.budgettracker.R;

public class SetupAdditionalCurrenciesFragment extends SetupFragment
{
    private final String TAG = "SetupAdditionalCurrenciesFragment";

    private View _view;

    private AddCurrencyPopup _popupWindow;

    private LinearLayout _additionalCurrenciesLinearLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        _view = inflater.inflate(R.layout.fragment_setup_additional_currencies, parent, false);

        Button previousButton = _view.findViewById(R.id.previousButton);
        previousButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                previous();
            }
        });

        Button nextButton = _view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                next();
            }
        });

        Button addCurrencyButton = _view.findViewById(R.id.addCurrencyButton);
        addCurrencyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addCurrency();
            }
        });

        _additionalCurrenciesLinearLayout = _view.findViewById(R.id.additionalCurrenciesLinearLayout);

        // TODO - only add currencies not checked previously and any added in the last screen; update this when viewed

        return _view;
    }

    private void addCurrency()
    {
        View confirmSettingsView = getLayoutInflater().inflate(R.layout.popup_new_currency, null);
        View.OnClickListener cancelListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelAddingCurrency();
            }
        };
        View.OnClickListener acceptListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                confirmAddingCurrency();
            }
        };
        _popupWindow = new AddCurrencyPopup(confirmSettingsView, cancelListener, acceptListener);
        _popupWindow.showAtLocation(_view.findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);
    }

    private void cancelAddingCurrency()
    {
        _popupWindow.dismiss();
    }

    private void confirmAddingCurrency()
    {
        _popupWindow.dismiss();
        String ISOText = _popupWindow.getISOText();
        String symbolText = _popupWindow.getSymbolText();
        String combinedText = ISOText + " (" + symbolText + ")";
        CheckBox newCurrency = new CheckBox(getContext());
        newCurrency.setText(combinedText);
        _additionalCurrenciesLinearLayout.addView(newCurrency);
        newCurrency.setChecked(true);
    }

    public String getAdditionalCurrencies()
    {
        String currencies = "";
        final int childCount = _additionalCurrenciesLinearLayout.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            CheckBox checkBox = ((CheckBox)_additionalCurrenciesLinearLayout.getChildAt(i));
            if (checkBox.isChecked())
            {
                String currencyText = checkBox.getText().toString();
                String ISO = currencyText.substring(0, 3);
                String symbol = currencyText.substring(5, 6);
                String currency = symbol + "_" + ISO; // e.g. "$_USD"
                if (currencies.length() > 0) { currencies += "|"; } else { }
                currencies += currency;
            }
        }
        return currencies;
    }
}

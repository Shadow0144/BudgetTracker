package cc.corbin.budgettracker.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Locale;

import cc.corbin.budgettracker.R;

public class SetupBaseCurrencyFragment extends SetupFragment
{
    private final String TAG = "SetupBaseCurrencyFragment";

    private View _view;

    private AddCurrencyPopup _popupWindow;

    private RadioGroup _baseCurrencyRadioGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        _view = inflater.inflate(R.layout.fragment_setup_base_currency, parent, false);

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

        _baseCurrencyRadioGroup = _view.findViewById(R.id.baseCurrencyRadioGroup);

        final RadioButton usdRadioButton = _view.findViewById(R.id.usdRadioButton);
        final RadioButton eurRadioButton = _view.findViewById(R.id.eurRadioButton);
        final RadioButton gbpRadioButton = _view.findViewById(R.id.gbpRadioButton);
        final RadioButton jpyRadioButton = _view.findViewById(R.id.jpyRadioButton);
        final RadioButton krwRadioButton = _view.findViewById(R.id.krwRadioButton);
        final RadioButton rubRadioButton = _view.findViewById(R.id.rubRadioButton);

        String country = Locale.getDefault().getCountry();
        switch (country)
        {
            case "US":
                usdRadioButton.setChecked(true);
                break;
            case "DE": // TODO - Other countries
                eurRadioButton.setChecked(true);
                break;
            case "GB":
                gbpRadioButton.setChecked(true);
                break;
            case "JP":
                jpyRadioButton.setChecked(true);
                break;
            case "KR":
                krwRadioButton.setChecked(true);
                break;
            case "RU":
                rubRadioButton.setChecked(true);
                break;
            default:
                usdRadioButton.setChecked(true);
                break;
        }

        final Button addCurrencyButton = _view.findViewById(R.id.addCurrencyButton);
        addCurrencyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addCurrency();
            }
        });

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
        RadioButton newCurrency = new RadioButton(getContext());
        newCurrency.setText(combinedText);
        _baseCurrencyRadioGroup.addView(newCurrency);
        newCurrency.setChecked(true);
    }
}

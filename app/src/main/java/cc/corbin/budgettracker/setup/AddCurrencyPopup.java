package cc.corbin.budgettracker.setup;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import cc.corbin.budgettracker.R;

public class AddCurrencyPopup extends PopupWindow
{
    private final String TAG = "AddCurrencyPopup";

    private EditText _ISOCodeEditText;
    private EditText _symbolEditText;
    private Button _acceptButton;

    public AddCurrencyPopup(View view, View.OnClickListener cancelListener, View.OnClickListener acceptListener)
    {
        super(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final Button cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(cancelListener);
        _acceptButton = view.findViewById(R.id.acceptButton);
        _acceptButton.setOnClickListener(acceptListener);

        _ISOCodeEditText = view.findViewById(R.id.ISOCodeEditText);
        _symbolEditText = view.findViewById(R.id.symbolEditText);

        // TODO - Check for duplicates

        _ISOCodeEditText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                int ISOLen = s.length();
                int symbolLen = _symbolEditText.getText().length();

                if (ISOLen == 3 && symbolLen == 1)
                {
                    _acceptButton.setEnabled(true);
                }
                else
                {
                    _acceptButton.setEnabled(false);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        _symbolEditText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                int ISOLen = _ISOCodeEditText.getText().length();
                int symbolLen = s.length();

                if (ISOLen == 3 && symbolLen == 1)
                {
                    _acceptButton.setEnabled(true);
                }
                else
                {
                    _acceptButton.setEnabled(false);
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        setFocusable(true);
        update();
    }

    public String getISOText()
    {
        return _ISOCodeEditText.getText().toString();
    }

    public String getSymbolText()
    {
        return _symbolEditText.getText().toString();
    }
}

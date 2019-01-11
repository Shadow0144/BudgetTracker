package cc.corbin.budgettracker.numericalformatting;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.ParseException;

import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class NumericalFormattedEditText extends AppCompatEditText
{
    private final String TAG = "NumericalFormattedEditText";
    private MoneyValueFilter _moneyValueFilter;

    private NumericalFormattedCallback _callback;

    private int _digits;
    private float _baseAmount;
    private float _amount;

    public NumericalFormattedEditText(Context context)
    {
        super(context);
        init(context);
    }

    public NumericalFormattedEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public NumericalFormattedEditText(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context)
    {
        _amount = 0.0f;

        //android:digits='"1234567890,."'
        setKeyListener(DigitsKeyListener.getInstance(false, true));

        //android:inputType="numberDecimal"
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    public void setup()
    {
        _callback = null;

        continueSetup((Currencies.integer[Currencies.default_currency] ? 0 : 2));
    }

    public void setup(final NumericalFormattedCallback callback)
    {
        _callback = callback;

        continueSetup((Currencies.integer[Currencies.default_currency] ? 0 : 2));
    }

    public void setup(final NumericalFormattedCallback callback, int currency, float amount)
    {
        _callback = callback;

        String text = Currencies.formatCurrency(Currencies.integer[currency], amount);
        setText(text);
        setSelection(text.length()); // Place the cursor at the end for convenience

        continueSetup((Currencies.integer[currency] ? 0 : 2));
    }

    private void continueSetup(int digits)
    {
        _baseAmount = 0.0f;
        _moneyValueFilter = new MoneyValueFilter();
        setFilters(new InputFilter[]{_moneyValueFilter});
        setDigits(digits);

        addTextChangedListener(new TextWatcher()
        {
            private boolean _flag = false;

            private String _previousString;
            private int _start;
            private int _previousCount;
            private int _afterCount;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                _previousString = s.toString();
                _start = start;
                _previousCount = count;
                _afterCount = after;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // Check the the data is valid
                if (!_flag)
                {
                    int len = s.length();
                    SpannableStringBuilder text = new SpannableStringBuilder(s.toString(), 0, len);

                    // Check if the deleted character was a comma
                    // If so, delete the number behind it instead
                    if (_afterCount == (_previousCount-1))
                    {
                        if ((_previousString.charAt(_start) == ',') && (_start > 0)) // _start should always be > 0 if it is a comma, but just in case
                        {
                            text.replace(_start-1, _start, "");
                        }
                        else { }
                    }
                    else { }

                    // Working from the back, ensure that all commas are properly placed
                    int commasAdded = 0;
                    int decimal = text.toString().indexOf('.');
                    int i = (decimal > -1) ? (decimal-1) : (len-1);
                    int digits = 0;
                    for (; i >= 0; i--)
                    {
                        if (digits < 3) // Check that the wrong places are not commas
                        {
                            if (s.charAt(i) == ',')
                            {
                                text.replace(i, (i+1), "");
                                commasAdded--;
                                _flag = true;
                            }
                            else
                            {
                                digits++;
                            }
                        }
                        else // Check that the right places are commas
                        {
                            if (s.charAt(i) != ',')
                            {
                                text.insert(i+1, ",");
                                commasAdded++;
                                _flag = true;
                                digits = 1;
                            }
                            else
                            {
                                digits = 0;
                            }
                        }
                    }

                    // Ensure that the first character is not a comma
                    len = text.length(); // Update the length
                    if ((len > 0) && (s.charAt(0) == ','))
                    {
                        text.replace(0, 1, "");
                        len--;
                        commasAdded--;
                    }
                    else { }

                    // Remove any leading zeros
                    while ((len > 1) && (s.charAt(0) == '0'))
                    {
                        text.replace(0, 1, "");
                        len--;
                        _flag = true;
                    }

                    String textString = text.toString();

                    _amount = 0.0f;
                    if (textString.length() > 0)
                    {
                        try
                        {
                            DecimalFormat formatter = new DecimalFormat();
                            _amount = formatter.parse(textString).floatValue();

                            // If the amount is zero, set the text to just "0"
                            if (_amount == 0.0f)
                            {
                                textString = "0";
                            }
                            else { }
                        }
                        catch (ParseException e)
                        {
                            Log.e(TAG, "Parsing error after text changed");
                            textString = "";
                        }
                    }
                    else { }

                    if (_callback != null)
                    {
                        _callback.valueChanged(getId(), _amount);
                    }
                    else { }

                    // Update the displayed value
                    if (_flag)
                    {
                        len = textString.length();
                        int cursorPosition = Math.min(Math.max(0, getSelectionStart() + commasAdded), len);
                        setText(textString);
                        // There's a corner case involving a String which will be reduced for exceeding the maximum length
                        cursorPosition -= len - getText().length();
                        setSelection(cursorPosition);
                        _flag = false;
                    }
                    else { }
                }
                else { }
            }
        });
    }

    public int getDigits()
    {
        return _digits;
    }

    public void setDigits(int digits)
    {
        _digits = digits;
        DecimalFormat formatter;
        if (_digits == 0)
        {
            formatter = new DecimalFormat("");
        }
        else
        {
            formatter = new DecimalFormat(".##");
        }
        setHint(formatter.format(_baseAmount));
        _moneyValueFilter.setDigits(_digits);
    }

    public float getBaseAmount()
    {
        return _baseAmount;
    }

    public void setBaseAmount(float amount)
    {
        if (_amount == _baseAmount)
        {
            _amount = amount;
        }
        else { }
        _baseAmount = amount;
        setDigits(_digits);
    }

    public float getAmount()
    {
        return _amount;
    }

    public void setAmount(float amount)
    {
        setText("" + amount);
    }

    public void clearAmount()
    {
        setText("");
    }
}

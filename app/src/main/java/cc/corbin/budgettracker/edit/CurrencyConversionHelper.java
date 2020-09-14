package cc.corbin.budgettracker.edit;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.ConversionRateAsyncTask;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedCallback;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedEditText;

import static android.Manifest.permission.INTERNET;

public class CurrencyConversionHelper extends PopupWindow implements NumericalFormattedCallback
{
    private final String TAG = "CurrencyConversionHelper";

    private AppCompatActivity _context;

    private int _year;
    private int _month;
    private int _day;

    private final int BASE_AMOUNT_TAG = 0;
    private final int CONVERSION_RATE_TAG = 1;

    private NumericalFormattedEditText _baseAmountEditText;
    private NumericalFormattedEditText _conversionRateEditText;
    private Spinner _currencySpinner;
    private Button _conversionRateButton;
    private ProgressBar _conversionRateProgressBar;

    private TextView _baseCurrencyTextView;
    private EditText _convertedAmountEditText;

    private static final int CONNECT_TO_INTERNET_CODE = 0;
    private static boolean _connectToInternet = false;

    private MutableLiveData<String> _conversionRateStringMLD;

    private float _baseAmount;
    private float _conversionRate;
    private float _convertedAmount;

    private String _note;
    private String _modifiedNote;

    private CurrencyConversionCallback _callback;

    public CurrencyConversionHelper(AppCompatActivity context, CurrencyConversionCallback callback, int year, int month, int day, String note)
    {
        super(null, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        _context = context;
        _year = year;
        _month = month;
        _day = day;
        _callback = callback;
        _note = note;
        _modifiedNote = note;

        _baseAmount = 0.0f;
        _conversionRate = 1.0f;
        _convertedAmount = 0.0f;

        final View conversionView = _context.getLayoutInflater().inflate(R.layout.popup_convert_currency, null);

        _convertedAmountEditText = conversionView.findViewById(R.id.convertedAmountEditText);
        _convertedAmountEditText.setText(Currencies.formatCurrency(Currencies.default_currency, 0.0f));

        _conversionRateEditText = conversionView.findViewById(R.id.conversionRateEditText);
        _conversionRateEditText.setup(this);
        _conversionRateEditText.setDigits(2);
        _conversionRateEditText.setBaseAmount(1.0f);
        _conversionRateEditText.setTag(CONVERSION_RATE_TAG);

        _baseAmountEditText = conversionView.findViewById(R.id.baseAmountEditText);
        _baseAmountEditText.setup(this);
        _baseAmountEditText.setDigits(Currencies.integer[Currencies.default_currency] ? 0 : 2);
        _baseAmountEditText.setBaseAmount(0.0f);
        _baseAmountEditText.setTag(BASE_AMOUNT_TAG);

        final TextView yearTextView = conversionView.findViewById(R.id.yearTextView);
        yearTextView.setText(String.format("%04d", _year));

        final TextView monthTextView = conversionView.findViewById(R.id.monthTextView);
        monthTextView.setText(String.format("%02d", _month));

        // Setup the weeks
        LocalDate calendar = LocalDate.of(_year, _month, _day);
        final int days = calendar.lengthOfMonth();
        String[] daysArray = new String[days];
        for (int i = 0; i < days; i++)
        {
            daysArray[i] = String.format("%02d", (i+1));
        }

        // Setup the spinner
        final Spinner daySpinner = conversionView.findViewById(R.id.daySpinner);
        ArrayAdapter<String> daySpinnerArrayAdapter = new ArrayAdapter<String>(
                _context, android.R.layout.simple_spinner_item, daysArray);
        daySpinner.setAdapter(daySpinnerArrayAdapter);
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                _day = position+1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // Do nothing
            }
        });
        daySpinner.setSelection(0);

        _baseCurrencyTextView = conversionView.findViewById(R.id.baseCurrencyTextView);
        _currencySpinner = conversionView.findViewById(R.id.currencySpinner);
        ArrayAdapter<String> currencySpinnerArrayAdapter = new ArrayAdapter<String>(
                _context, android.R.layout.simple_spinner_item, Currencies.symbols);
        _currencySpinner.setAdapter(currencySpinnerArrayAdapter);
        _currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                _baseCurrencyTextView.setText(Currencies.symbols[position]);
                _baseAmountEditText.setDigits(Currencies.integer[position] ? 0 : 2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // Do nothing
            }
        });

        final TextView convertedCurrencyTextBox = conversionView.findViewById(R.id.convertedCurrencyTextView);
        convertedCurrencyTextBox.setText(Currencies.symbols[Currencies.default_currency]);

        _conversionRateButton = conversionView.findViewById(R.id.conversionRateButton);
        _conversionRateProgressBar = conversionView.findViewById(R.id.conversionRateProgressBar);

        // Setup the buttons to call the appropriate functions
        _conversionRateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getConversionRate();
            }
        });
        final Button addToNoteButton = conversionView.findViewById(R.id.addToNoteButton);
        addToNoteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addToNote(v);
            }
        });
        final Button cancelButton = conversionView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelConversion(v);
            }
        });
        final Button acceptButton = conversionView.findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                acceptConversion(v);
            }
        });

        setContentView(conversionView);
        setFocusable(true);
        update();
    }

    public void setCallback(CurrencyConversionCallback callback)
    {
        _callback = callback;
    }

    public void show()
    {
        showAtLocation( _context.findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);
    }

    public void cancelConversion(View v)
    {
        dismiss();
    }

    public void acceptConversion(View v)
    {
        _callback.setAmount(_convertedAmount);
        dismiss();
    }

    private void setNote(String note)
    {
        _note = note;
        _modifiedNote = note;
    }

    private String getNote()
    {
        return _modifiedNote;
    }

    private void getConversionRate()
    {
        LocalDate currentDate = LocalDate.now();
        LocalDate editDate = LocalDate.of(_year, _month, _day);

        long end = currentDate.toEpochDay();
        long start = editDate.toEpochDay();

        if (end >= start)
        {
            long time = TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));

            if (time <= 365)
            {
                _conversionRateButton.setVisibility(View.INVISIBLE);
                _conversionRateProgressBar.setVisibility(View.VISIBLE);
                if (!_connectToInternet)
                {
                    checkPermissions();
                }
                else
                {
                    connectForConversionRate();
                }
            }
            else
            {
                Toast.makeText(_context, "Dates more than a year old will need to have the conversion rate manually specified", Toast.LENGTH_LONG);
            }
        }
        else
        {
            Toast.makeText(_context, "Future dates will need to have the conversion rate manually specified", Toast.LENGTH_LONG);
        }
    }

    public float getConvertedAmount()
    {
        return _convertedAmount;
    }

    private void checkPermissions()
    {
        if (_context.checkSelfPermission(INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            _context.requestPermissions(new String[] {INTERNET}, CONNECT_TO_INTERNET_CODE);
        }
        else
        {
            _connectToInternet = true;
            getConversionRate(); // Set the flag to true and loop back around and try again
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        // If request is cancelled, the result arrays are empty
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            _connectToInternet = true;
            switch (requestCode)
            {
                case CONNECT_TO_INTERNET_CODE:
                    connectForConversionRate();
                    break;
            }
        }
        else
        {
            _connectToInternet = false;
            Toast.makeText(_context, "Failed to acquire permissions", Toast.LENGTH_SHORT).show();
            _conversionRateButton.setVisibility(View.VISIBLE);
            _conversionRateProgressBar.setVisibility(View.INVISIBLE);
            _conversionRateButton.setEnabled(false);
        }
    }

    private void connectForConversionRate()
    {
        _conversionRateStringMLD = new MutableLiveData<String>();
        final Observer<String> conversionObserver = new Observer<String>()
        {
            @Override
            public void onChanged(@Nullable String conversionRateString)
            {
                // Null check in the method
                onConversionRateReceived(conversionRateString);
            }
        };
        _conversionRateStringMLD.observe(_context, conversionObserver);
        new ConversionRateAsyncTask(
                _conversionRateStringMLD,
                _currencySpinner.getSelectedItemPosition(),
                _year, _month, _day).execute();
    }

    private void onConversionRateReceived(String conversionRateString)
    {
        _conversionRateButton.setVisibility(View.VISIBLE);
        _conversionRateProgressBar.setVisibility(View.INVISIBLE);

        try
        {
            String rate = conversionRateString.split(":")[2];
            rate = rate.substring(0, rate.length() - 2);

            float rateNumber = Float.parseFloat(rate);
            NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
            formatter.setRoundingMode(RoundingMode.HALF_UP);

            rate = formatter.format(rateNumber);
            rateNumber = formatter.parse(rate).floatValue();
            _conversionRateEditText.setAmount(rateNumber);

            Log.e(TAG, "Received: " + rateNumber);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to parse number");

            _conversionRateEditText.clearAmount();
        }
    }

    @Override
    public void valueChanged(Object tag, float value)
    {
        int tagInt = ((int)tag);
        switch (tagInt)
        {
            case BASE_AMOUNT_TAG:
                _baseAmount = value;
                break;
            case CONVERSION_RATE_TAG:
                _conversionRate = value;
                break;
            default:
                // Do nothing
                break;
        }
        _convertedAmount = _baseAmount * _conversionRate;
        _convertedAmountEditText.setText(Currencies.formatCurrency(Currencies.default_currency, _convertedAmount));
    }

    private void addToNote(View v)
    {
        DecimalFormat formatter = new DecimalFormat("###,###,###,###,##0.00");
        if (_note.length() > 0)
        {
            _modifiedNote = _note + "\n\n";
        }
        else
        {
            _modifiedNote = _note; // Empty String
        }
        _modifiedNote +=
                "Base amount: " + Currencies.formatCurrency(_currencySpinner.getSelectedItemPosition(), _baseAmount) + "\n" +
                "@ Conversion rate: " + formatter.format(_conversionRate);

        _callback.noteUpdated(_modifiedNote);
    }
}

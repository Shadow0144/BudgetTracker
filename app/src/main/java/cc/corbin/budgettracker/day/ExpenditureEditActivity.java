package cc.corbin.budgettracker.day;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.Locale;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.ConversionRateAsyncTask;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.MoneyValueFilter;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ExpenditureEditActivity extends AppCompatActivity
{
    private final String TAG = "ExpenditureEditActivity";

    public final static String TYPE_INTENT = "Type";
    public final static String DAY_INTENT = "Day";
    public final static String MONTH_INTENT = "Month";
    public final static String YEAR_INTENT = "Year";
    public final static String EXPENDITURE_INTENT = "Expenditure";
    public final static String INDEX_INTENT = "Index";

    private static final int CONNECT_TO_INTERNET_CODE = 0;
    private static boolean _connectToInternet = false;

    private int _day;
    private int _month;
    private int _year;

    private int _index;
    private ExpenditureEntity _expenditure;

    private PopupWindow _popupWindow;

    private Button _conversionRateButton;
    private ProgressBar _conversionRateProgressBar;

    private Spinner _symbolSpinner;

    private EditText _convertedAmountEditText;
    private TextView _totalConvertedAmountTextView;

    private EditText _generatedNoteEditText;

    private float _amount;
    private float _rateNumber;
    private float _totalConvertedAmount;

    private MutableLiveData<String> _conversionRateStringMLD;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenditure_edit_activity);

        Intent intent = getIntent();
        int type = intent.getIntExtra(TYPE_INTENT, -1);
        setResult(DayViewActivity.CANCEL); // In case of backing out
        switch (type)
        {
            case DayViewActivity.CREATE_EXPENDITURE:
                setupAmount();
                setupCategories();
                setupNote();

                _day = intent.getIntExtra(DAY_INTENT, 0);
                _month = intent.getIntExtra(MONTH_INTENT, 0);
                _year = intent.getIntExtra(YEAR_INTENT, 0);
                _expenditure = new ExpenditureEntity(_day, _month, _year);
                _index = 0;
                break;

            case DayViewActivity.EDIT_EXPENDITURE:
                _expenditure = intent.getParcelableExtra(EXPENDITURE_INTENT);
                _index = intent.getIntExtra(INDEX_INTENT, -1);

                findViewById(R.id.deleteButton).setEnabled(true); // Enable deleting

                if (_expenditure == null || _index == -1)
                {
                    Log.e(TAG, "Editing error");
                    setResult(DayViewActivity.FAILURE);
                    finish();
                }
                else { }

                _day = _expenditure.getDay();
                _month = _expenditure.getMonth();
                _year = _expenditure.getYear();

                setupAmount();
                setupCategories();
                setupNote();
                break;

            default:
                Log.e(TAG, "Error");
                setResult(DayViewActivity.FAILURE);
                finish();
                break;
        }

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
        _conversionRateStringMLD.observe(this, conversionObserver);
    }

    public void onAccept(View v)
    {
        final EditText amountEditText = findViewById(R.id.valueEditText);
        final RadioGroup categoriesHolder = findViewById(R.id.categoriesHolder);
        final RadioButton button = categoriesHolder.findViewById(categoriesHolder.getCheckedRadioButtonId());
        final EditText noteEditText = findViewById(R.id.noteEditText);

        // Setup the expenditure
        _expenditure.setCurrency(_symbolSpinner.getSelectedItemPosition());
        try
        {
            _expenditure.setAmount(Float.parseFloat(amountEditText.getText().toString()));
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failure to parse float");
            _expenditure.setAmount(0.0f);
        }
        _expenditure.setExpenseType(button.getText().toString());
        _expenditure.setNote(noteEditText.getText().toString());

        Intent intent = new Intent();
        intent.putExtra(EXPENDITURE_INTENT, _expenditure);
        intent.putExtra(INDEX_INTENT, _index);

        setResult(DayViewActivity.SUCCEED, intent);

        finish();
    }

    public void onCancel(View v)
    {
        setResult(DayViewActivity.CANCEL);
        finish();
    }

    public void onDelete(View v)
    {
        final View deleteView = getLayoutInflater().inflate(R.layout.popup_confirm_delete, null);

        _popupWindow = new PopupWindow(deleteView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);
    }

    public void onCancelDelete(View v)
    {
        _popupWindow.dismiss();
    }

    public void onConfirmDelete(View v)
    {
        _popupWindow.dismiss();

        Intent intent = new Intent();
        intent.putExtra(EXPENDITURE_INTENT, _expenditure);
        intent.putExtra(INDEX_INTENT, _index);

        setResult(DayViewActivity.DELETE, intent);

        finish();
    }

    private void setupAmount()
    {
        _amount = 0.0f;
        _rateNumber = 1.00f;
        _totalConvertedAmount = _amount * _rateNumber;

        _totalConvertedAmountTextView = findViewById(R.id.totalConvertedAmountTextView);
        _generatedNoteEditText = findViewById(R.id.generatedNoteEditText);

        // Setup the amount edit
        final EditText amountEditText = findViewById(R.id.valueEditText);
        final MoneyValueFilter moneyValueFilter = new MoneyValueFilter();
        if (_expenditure != null)
        {
            moneyValueFilter.setDigits(Currencies.integer[_expenditure.getCurrency()] ? 0 : 2);
            if (Currencies.integer[_expenditure.getCurrency()])
            {
                amountEditText.setHint("0");
            }
            else
            {
                amountEditText.setHint("0.00");
            }
        }
        else
        {
            moneyValueFilter.setDigits(Currencies.integer[Currencies.default_currency] ? 0 : 2);
            if (Currencies.integer[Currencies.default_currency])
            {
                amountEditText.setHint("0");
            }
            else
            {
                amountEditText.setHint("0.00");
            }
        }
        amountEditText.setFilters(new InputFilter[]{moneyValueFilter});

        amountEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0)
                {
                    _amount = Float.parseFloat(s.toString());
                }
                else
                {
                    _amount = 0.0f;
                }
                _totalConvertedAmount = _amount * _rateNumber;
                _totalConvertedAmountTextView.setText(Currencies.formatCurrency(Currencies.default_currency, _totalConvertedAmount));

                updateConversionViews();
            }
        });

        // Base currency, which will match the currency being entered
        // Always allow up to two digits of precision
        final TextView baseCurrencyTextView = findViewById(R.id.baseCurrencyTextView);
        baseCurrencyTextView.setText(Currencies.symbols[Currencies.default_currency]);

        // Converted currency, which will match the default currency
        final TextView convertedCurrencyTextView = findViewById(R.id.convertedCurrencyTextView);
        _convertedAmountEditText = findViewById(R.id.convertedAmountEditText);
        convertedCurrencyTextView.setText(Currencies.symbols[Currencies.default_currency]);
        final MoneyValueFilter convertedMoneyValueFilter = new MoneyValueFilter();
        convertedMoneyValueFilter.setDigits(2);
        _convertedAmountEditText.setFilters(new InputFilter[]{convertedMoneyValueFilter});

        _convertedAmountEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0)
                {
                    _rateNumber = Float.parseFloat(s.toString());
                }
                else
                {
                    _rateNumber = 1.00f;
                }
                _totalConvertedAmount = _amount * _rateNumber;
                _totalConvertedAmountTextView.setText(Currencies.formatCurrency(Currencies.default_currency, _totalConvertedAmount));

                updateConversionViews();
            }
        });

        // Setup the currency type spinner
        _symbolSpinner = findViewById(R.id.currencySelector);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, Currencies.symbols);
        _symbolSpinner.setAdapter(spinnerArrayAdapter);
        _symbolSpinner.setSelection(Currencies.default_currency);
        _symbolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                moneyValueFilter.setDigits(Currencies.integer[position] ? 0 : 2);
                if (Currencies.integer[position])
                {
                    amountEditText.setHint("0");
                }
                else
                {
                    amountEditText.setHint("0.00");
                }
                baseCurrencyTextView.setText(Currencies.symbols[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        updateConversionViews();

        _conversionRateButton = findViewById(R.id.conversionRateButton);
        _conversionRateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getConversionRate();
            }
        });
        _conversionRateProgressBar = findViewById(R.id.conversionRateProgressBar);

        // Setup the values if they exist already
        if (_expenditure != null)
        {
            _symbolSpinner.setSelection(_expenditure.getCurrency());
            String cost = Currencies.formatCurrency(Currencies.integer[_expenditure.getCurrency()], _expenditure.getAmount());
            amountEditText.setText(cost, TextView.BufferType.EDITABLE);
        }
        else { }
    }

    private void setupCategories()
    {
        // Setup the categories
        final RadioGroup categoriesHolder = findViewById(R.id.categoriesHolder);
        categoriesHolder.removeAllViews();
        categoriesHolder.clearCheck();
        final String[] categories = Categories.getCategories();
        int count = categories.length;
        for (int i = 0; i < count; i++)
        {
            final RadioButton button = new RadioButton(this);
            button.setText(categories[i]);
            categoriesHolder.addView(button);
            categoriesHolder.check(button.getId());
        }

        // Setup the values if they already exist
        if (_expenditure != null)
        {
            String expenseType = _expenditure.getExpenseType();
            for (int i = 0; i < count; i++)
            {
                RadioButton button = ((RadioButton) (categoriesHolder.getChildAt(i)));
                String category = button.getText().toString();
                if (expenseType.equals(category))
                {
                    categoriesHolder.check(button.getId());
                    break;
                }
                else { }
            }
        }
        else { }
    }

    private void setupNote()
    {
        if (_expenditure != null)
        {
            // Setup the note
            final EditText noteEditText = findViewById(R.id.noteEditText);
            noteEditText.setText(_expenditure.getNote());
        }
        else { }
    }

    private void getConversionRate()
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

    private void checkPermissions()
    {
        if (checkSelfPermission(INTERNET) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[] {INTERNET}, CONNECT_TO_INTERNET_CODE);
        }
        else
        {
            _connectToInternet = true;
            getConversionRate();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                                  String permissions[], int[] grantResults)
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
            Toast.makeText(this, "Failed to acquire permissions", Toast.LENGTH_SHORT).show();
            _conversionRateButton.setVisibility(View.VISIBLE);
            _conversionRateProgressBar.setVisibility(View.INVISIBLE);
            _conversionRateButton.setEnabled(false);
        }
    }

    private void connectForConversionRate()
    {
        ConversionRateAsyncTask conversionRateAsyncTask = new ConversionRateAsyncTask(_conversionRateStringMLD);

        String request = "https://free.currencyconverterapi.com/api/v5/convert?q=";
        request += Currencies.currencies.values()[_symbolSpinner.getSelectedItemPosition()];
        request += "_"+Currencies.currencies.values()[Currencies.default_currency];
        request += "&compact=ultra&date=" + _year + "-" + _month + "-" + _day;

        conversionRateAsyncTask.execute(request);
    }

    private void onConversionRateReceived(String conversionRateString)
    {
        _conversionRateButton.setVisibility(View.VISIBLE);
        _conversionRateProgressBar.setVisibility(View.INVISIBLE);

        try
        {
            String rate = conversionRateString.split(":")[2];
            rate = rate.substring(0, rate.length() - 2);

            _rateNumber = Float.parseFloat(rate);
            NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
            formatter.setRoundingMode(RoundingMode.HALF_UP);

            rate = formatter.format(_rateNumber);
            _rateNumber = formatter.parse(rate).floatValue();

            _convertedAmountEditText.setText(Currencies.formatCurrency(Currencies.default_currency, _rateNumber));

            updateConversionViews();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Failed to parse number");
            _rateNumber = 1.00f;
            _convertedAmountEditText.setText("");
            _totalConvertedAmountTextView.setText("");
        }
    }

    private void updateConversionViews()
    {
        _totalConvertedAmount = _amount * _rateNumber;
        _totalConvertedAmountTextView.setText(Currencies.formatCurrency(Currencies.default_currency, _totalConvertedAmount));

        String generatedNote = Currencies.formatCurrency(_symbolSpinner.getSelectedItemPosition(), _amount) + " -> " +
                Currencies.formatCurrency(Currencies.default_currency, _totalConvertedAmount) + "\n@ " +
                Currencies.formatCurrency(_symbolSpinner.getSelectedItemPosition(), 1.00f) + " -> " +
                Currencies.formatCurrency(Currencies.default_currency, _rateNumber);
        _generatedNoteEditText.setText(generatedNote);
    }
}

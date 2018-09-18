package cc.corbin.budgettracker.edit;

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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.ConversionRateAsyncTask;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.numericalformatting.MoneyValueFilter;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedEditText;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedCallback;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.year.YearViewActivity;

import static android.Manifest.permission.INTERNET;

public class ExpenditureEditActivity extends AppCompatActivity implements NumericalFormattedCallback
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

    private NumericalFormattedEditText _amountEditText;
    private LinearLayout _conversionLayout;
    private EditText _convertedAmountEditText;
    private TextView _totalConvertedAmountTextView;

    private float _amount;
    private float _rateNumber;
    private float _totalConvertedAmount;
    private int _baseCurrency;

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
        final TextView totalConvertedAmountTextView = findViewById(R.id.totalConvertedAmountTextView);
        final EditText valueEditText = findViewById(R.id.valueEditText);
        final EditText conversionRateEditText = findViewById(R.id.convertedAmountEditText);
        final RadioGroup categoriesHolder = findViewById(R.id.categoriesHolder);
        final EditText noteEditText = findViewById(R.id.noteEditText);

        NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());

        // Setup the expenditure
        String amount = totalConvertedAmountTextView.getText().toString();
        if (amount.length() > 0)
        {
            try
            {
                _expenditure.setAmount(formatter.parse(amount).floatValue());
            }
            catch (Exception e)
            {
                Log.e(TAG, "Failure to parse final amount float");
                _expenditure.setAmount(0.0f);
            }
        }
        else
        {
            _expenditure.setAmount(0.0f);
        }
        int category = categoriesHolder.getCheckedRadioButtonId();

        _expenditure.setCategory(category, Categories.getCategories()[category]);
        _expenditure.setBaseCurrency(_baseCurrency);
        String baseAmount = valueEditText.getText().toString();
        if (baseAmount.length() > 0)
        {
            try
            {
                _expenditure.setBaseAmount(formatter.parse(baseAmount).floatValue());
            }
            catch (Exception e)
            {
                Log.e(TAG, "Failure to parse base amount float");
                _expenditure.setBaseAmount(0.0f);
            }
        }
        else
        {
            _expenditure.setBaseAmount(0.0f);
        }
        String conversionRate = conversionRateEditText.getText().toString();
        if (conversionRate.length() > 0)
        {
            try
            {
                _expenditure.setConversionRate(formatter.parse(conversionRate).floatValue());
            }
            catch (Exception e)
            {
                Log.e(TAG, "Failure to parse conversion rate float");
                _expenditure.setConversionRate(1.0f);
            }
        }
        else
        {
            _expenditure.setConversionRate(1.0f);
        }
        _expenditure.setNote(noteEditText.getText().toString());

        Intent intent = new Intent();
        intent.putExtra(EXPENDITURE_INTENT, _expenditure);
        intent.putExtra(INDEX_INTENT, _index);

        setResult(DayViewActivity.SUCCEED, intent);

        DayViewActivity.dataInvalid = true;
        MonthViewActivity.dataInvalid = true;
        YearViewActivity.dataInvalid = true;
        TotalViewActivity.dataInvalid = true;

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

        DayViewActivity.dataInvalid = true;
        MonthViewActivity.dataInvalid = true;
        YearViewActivity.dataInvalid = true;
        TotalViewActivity.dataInvalid = true;

        finish();
    }

    private void setupAmount()
    {
        _amount = 0.0f;
        _rateNumber = 1.00f;
        _totalConvertedAmount = _amount * _rateNumber;

        _conversionLayout = findViewById(R.id.conversionLayout);
        _totalConvertedAmountTextView = findViewById(R.id.totalConvertedAmountTextView);
        final TextView totalCurrencyTextView = findViewById(R.id.totalCurrencyTextView);
        totalCurrencyTextView.setText(Currencies.symbols[Currencies.default_currency]);

        if (_expenditure != null)
        {
            _amount = _expenditure.getBaseAmount();
            _rateNumber = _expenditure.getConversionRate();
            _totalConvertedAmountTextView.setText(Currencies.formatCurrency(Currencies.integer[Currencies.default_currency], _expenditure.getAmount()));
        }
        else { }

        // Setup the amount edit
        _amountEditText = findViewById(R.id.valueEditText);
        if (_expenditure != null)
        {
            _amountEditText.setup(this, _expenditure.getBaseCurrency(), _expenditure.getAmount());
        }
        else
        {
            _amountEditText.setup(this);
        }

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

        if (_expenditure != null)
        {
            _convertedAmountEditText.setText("" + _expenditure.getConversionRate());
        }
        else { } // Else keep the hint

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

                updateConversionViews();
            }
        });

        // Setup the currency type spinner
        _symbolSpinner = findViewById(R.id.currencySelector);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, Currencies.symbols);
        _symbolSpinner.setAdapter(spinnerArrayAdapter);
        if (_expenditure != null)
        {
            _symbolSpinner.setSelection(_expenditure.getBaseCurrency());
        }
        else
        {
            _symbolSpinner.setSelection(Currencies.default_currency);
        }
        _symbolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                _baseCurrency = position;
                _amountEditText.setDigits(Currencies.integer[_baseCurrency] ? 0 : 2);
                if (Currencies.integer[_baseCurrency])
                {
                    _amountEditText.setHint("0");
                }
                else
                {
                    _amountEditText.setHint("0.00");
                }
                baseCurrencyTextView.setText(Currencies.symbols[_baseCurrency]);
                if (_baseCurrency == Currencies.default_currency)
                {
                    _conversionLayout.setVisibility(View.GONE);
                }
                else
                {
                    _conversionLayout.setVisibility(View.VISIBLE);
                }

                updateConversionViews();
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
            button.setId(i);
            button.setText(categories[i]);
            categoriesHolder.addView(button);
            categoriesHolder.check(button.getId());
        }

        // Setup the values if they already exist
        if (_expenditure != null)
        {
            String categoryName = _expenditure.getCategoryName();
            for (int i = 0; i < count; i++)
            { // TODO - Check if this is the best way to compare this
                RadioButton button = ((RadioButton) (categoriesHolder.getChildAt(i)));
                String category = button.getText().toString();
                if (categoryName.equals(category))
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
        Calendar currentDate = Calendar.getInstance();
        Calendar editDate = Calendar.getInstance();
        editDate.set(_year, _month-1, _day);

        long end = currentDate.getTimeInMillis();
        long start = editDate.getTimeInMillis();

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
                Toast.makeText(this, "Dates more than a year old will need to have the conversion rate manually specified", Toast.LENGTH_LONG);
            }
        }
        else
        {
            Toast.makeText(this, "Future dates will need to have the conversion rate manually specified", Toast.LENGTH_LONG);
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

            updateConversionViews();
        }
    }

    private void updateConversionViews()
    {
        _totalConvertedAmount = _amount * _rateNumber;
        _totalConvertedAmountTextView.setText(Currencies.formatCurrency(Currencies.integer[Currencies.default_currency], _totalConvertedAmount));
    }

    @Override
    public void valueChanged(int id, float value)
    {
        _amount = value;
        updateConversionViews();
    }
}

package cc.corbin.budgettracker.search;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.MutableLong;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.DatePickerFragment;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedEditText;

public class CreateSearchActivity extends AppCompatActivity
{
    private final String TAG = "CreateSearchActivity";

    private final String EXACT_DATE_INTENT = "Exact_Date";
    private final String START_DATE_INTENT = "Start_Date";
    private final String END_DATE_INTENT = "End_Date";
    private final String EXACT_AMOUNT_CURRENCY_INTENT = "Exact_Amount_Currency";
    private final String EXACT_AMOUNT_INTENT = "Exact_Amount";
    private final String AMOUNT_RANGE_CURRENCY_INTENT = "Amount_Range_Currency";
    private final String AMOUNT_RANGE_LOWER_INTENT = "Amount_Range_Lower";
    private final String AMOUNT_RANGE_UPPER_INTENT = "Amount_Range_Upper";
    private final String CATEGORIES_INTENT = "Categories";
    private final String CONTAINS_TEXT_INTENT = "Contains_Text";
    private final String EXACT_TEXT_INTENT = "Exact_Text";

    private CheckBox[] _categoryCheckBoxes;

    private RadioButton _anyDateRadioButton;
    private RadioButton _exactDateRadioButton;
    private RadioButton _dateRangeRadioButton;
    private RadioButton _anyAmountRadioButton;
    private RadioButton _exactAmountRadioButton;
    private RadioButton _amountRangeRadioButton;
    private RadioButton _anyTextRadioButton;
    private RadioButton _containsTextRadioButton;
    private RadioButton _exactTextRadioButton;

    private ImageButton _exactDateButton;
    private ImageButton _startDateButton;
    private ImageButton _endDateButton;

    private Spinner _exactAmountCurrencySpinner;
    private NumericalFormattedEditText _exactAmountEditText;
    private Spinner _amountRangeCurrencySpinner;
    private NumericalFormattedEditText _amountRangeLowerEditText;
    private NumericalFormattedEditText _amountRangeUpperEditText;
    private TextView _amountRangeCurrencyTextView;

    private EditText _containsTextEditText;
    private EditText _exactTextEditText;

    private TextView _exactDateYearTextView;
    private TextView _exactDateMonthTextView;
    private TextView _exactDateDayTextView;
    private TextView _startDateYearTextView;
    private TextView _startDateMonthTextView;
    private TextView _startDateDayTextView;
    private TextView _endDateYearTextView;
    private TextView _endDateMonthTextView;
    private TextView _endDateDayTextView;

    private MutableLiveData<Date> _exactDateLive;
    private MutableLiveData<Date> _startDateLive;
    private MutableLiveData<Date> _endDateLive;
    private Date _exactDate;
    private Date _startDate;
    private Date _endDate;

    private int _exactAmountCurrency;
    private int _amountRangeCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_search);

        LinearLayout categoriesLayout = findViewById(R.id.categoriesLinearLayout);
        String[] categories = Categories.getCategories();
        _categoryCheckBoxes = new CheckBox[categories.length];
        for (int i = 0; i < categories.length; i++)
        {
            _categoryCheckBoxes[i] = new CheckBox(this);
            _categoryCheckBoxes[i].setText(categories[i]);
            categoriesLayout.addView(_categoryCheckBoxes[i]);
        }

        _anyDateRadioButton = findViewById(R.id.anyDateRadioButton);
        _exactDateRadioButton = findViewById(R.id.exactDateRadioButton);
        _dateRangeRadioButton = findViewById(R.id.dateRangeRadioButton);
        _anyAmountRadioButton = findViewById(R.id.anyAmountRadioButton);
        _exactAmountRadioButton = findViewById(R.id.exactAmountRadioButton);
        _amountRangeRadioButton = findViewById(R.id.amountRangeRadioButton);
        _anyTextRadioButton = findViewById(R.id.anyTextRadioButton);
        _containsTextRadioButton = findViewById(R.id.containsTextRadioButton);
        _exactTextRadioButton = findViewById(R.id.exactTextRadioButton);

        _exactDateButton = findViewById(R.id.exactDateButton);
        _startDateButton = findViewById(R.id.startDateButton);
        _endDateButton = findViewById(R.id.endDateButton);
        _exactAmountCurrencySpinner = findViewById(R.id.exactAmountCurrencySpinner);
        _exactAmountEditText = findViewById(R.id.exactAmountEditText);
        _amountRangeCurrencySpinner = findViewById(R.id.amountRangeCurrencySpinner);
        _amountRangeLowerEditText = findViewById(R.id.amountRangeLowerEditText);
        _amountRangeUpperEditText = findViewById(R.id.amountRangeUpperEditText);
        _containsTextEditText = findViewById(R.id.containsTextEditText);
        _exactTextEditText = findViewById(R.id.exactTextEditText);

        _amountRangeCurrencyTextView = findViewById(R.id.amountRangeCurrencyTextView);

        _exactAmountEditText.setup();
        _amountRangeLowerEditText.setup();
        _amountRangeUpperEditText.setup();

        _exactAmountCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                _exactAmountCurrency = position;
                _exactAmountEditText.setDigits(Currencies.integer[_exactAmountCurrency] ? 0 : 2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // Select the default
                _exactAmountCurrencySpinner.setSelection(Currencies.default_currency);
            }
        });
        _exactAmountCurrencySpinner.setSelection(Currencies.default_currency);

        _amountRangeCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                _amountRangeCurrency = position;
                _amountRangeLowerEditText.setDigits(Currencies.integer[_amountRangeCurrency] ? 0 : 2);
                _amountRangeUpperEditText.setDigits(Currencies.integer[_amountRangeCurrency] ? 0 : 2);
                _amountRangeCurrencyTextView.setText(Currencies.symbols[_amountRangeCurrency]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // Select the default
                _amountRangeCurrencySpinner.setSelection(Currencies.default_currency);
            }
        });

        ((RadioGroup)findViewById(R.id.dateRadioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                updateDateViews();
            }
        });

        ((RadioGroup)findViewById(R.id.amountRadioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                updateAmountViews();
            }
        });

        ((RadioGroup)findViewById(R.id.noteRadioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                updateNoteViews();
            }
        });

        ArrayAdapter<String> currencySpinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, Currencies.symbols);
        _exactAmountCurrencySpinner.setAdapter(currencySpinnerArrayAdapter);
        _amountRangeCurrencySpinner.setAdapter(currencySpinnerArrayAdapter);
        _exactAmountCurrencySpinner.setSelection(Currencies.default_currency);
        _amountRangeCurrencySpinner.setSelection(Currencies.default_currency);

        _exactDateYearTextView = findViewById(R.id.exactYearTextView);
        _exactDateMonthTextView = findViewById(R.id.exactMonthTextView);
        _exactDateDayTextView = findViewById(R.id.exactDayTextView);
        _startDateYearTextView = findViewById(R.id.startYearTextView);
        _startDateMonthTextView = findViewById(R.id.startMonthTextView);
        _startDateDayTextView = findViewById(R.id.startDayTextView);
        _endDateYearTextView = findViewById(R.id.endYearTextView);
        _endDateMonthTextView = findViewById(R.id.endMonthTextView);
        _endDateDayTextView = findViewById(R.id.endDayTextView);

        _exactDateLive = new MutableLiveData<Date>();
        final Observer<Date> _exactDateObserver = new Observer<Date>()
        {
            @Override
            public void onChanged(@Nullable Date date)
            {
                _exactDate = date;
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(_exactDate);
                _exactDateYearTextView.setText("" + calendar.get(Calendar.YEAR));
                _exactDateMonthTextView.setText("" + (calendar.get(Calendar.MONTH)+1));
                _exactDateDayTextView.setText("" + calendar.get(Calendar.DATE));
            }
        };
        _exactDateLive.observe(this, _exactDateObserver);

        _startDateLive = new MutableLiveData<Date>();
        final Observer<Date> _startDateObserver = new Observer<Date>()
        {
            @Override
            public void onChanged(@Nullable Date date)
            {
                _startDate = date;
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(_startDate);
                _startDateYearTextView.setText("" + calendar.get(Calendar.YEAR));
                _startDateMonthTextView.setText("" + (calendar.get(Calendar.MONTH)+1));
                _startDateDayTextView.setText("" + calendar.get(Calendar.DATE));
            }
        };
        _startDateLive.observe(this, _startDateObserver);

        _endDateLive = new MutableLiveData<Date>();
        final Observer<Date> _endDateObserver = new Observer<Date>()
        {
            @Override
            public void onChanged(@Nullable Date date)
            {
                _endDate = date;
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(_endDate);
                _endDateYearTextView.setText("" + calendar.get(Calendar.YEAR));
                _endDateMonthTextView.setText("" + (calendar.get(Calendar.MONTH)+1));
                _endDateDayTextView.setText("" + calendar.get(Calendar.DATE));
            }
        };
        _endDateLive.observe(this, _endDateObserver);

        updateDateViews();
        updateAmountViews();
        updateNoteViews();
    }

    private void updateDateViews()
    {
        if (_anyDateRadioButton.isChecked())
        {
            _exactDateButton.setEnabled(false);
            _startDateButton.setEnabled(false);
            _endDateButton.setEnabled(false);
        }
        else if (_exactDateRadioButton.isChecked())
        {
            _exactDateButton.setEnabled(true);
            _startDateButton.setEnabled(false);
            _endDateButton.setEnabled(false);
        }
        else if (_dateRangeRadioButton.isChecked())
        {
            _exactDateButton.setEnabled(false);
            _startDateButton.setEnabled(true);
            _endDateButton.setEnabled(true);
        }
        else { }
    }

    private void updateAmountViews()
    {
        if (_anyAmountRadioButton.isChecked())
        {
            _exactAmountCurrencySpinner.setEnabled(false);
            _exactAmountEditText.setEnabled(false);
            _amountRangeCurrencySpinner.setEnabled(false);
            _amountRangeLowerEditText.setEnabled(false);
            _amountRangeUpperEditText.setEnabled(false);
        }
        else if (_exactAmountRadioButton.isChecked())
        {
            _exactAmountCurrencySpinner.setEnabled(true);
            _exactAmountEditText.setEnabled(true);
            _amountRangeCurrencySpinner.setEnabled(false);
            _amountRangeLowerEditText.setEnabled(false);
            _amountRangeUpperEditText.setEnabled(false);
        }
        else if (_amountRangeRadioButton.isChecked())
        {
            _exactAmountCurrencySpinner.setEnabled(false);
            _exactAmountEditText.setEnabled(false);
            _amountRangeCurrencySpinner.setEnabled(true);
            _amountRangeLowerEditText.setEnabled(true);
            _amountRangeUpperEditText.setEnabled(true);
        }
        else { }
    }

    private void updateNoteViews()
    {
        if (_anyTextRadioButton.isChecked())
        {
            _containsTextEditText.setEnabled(false);
            _exactTextEditText.setEnabled(false);
        }
        else if (_containsTextRadioButton.isChecked())
        {
            _containsTextEditText.setEnabled(true);
            _exactTextEditText.setEnabled(false);
        }
        else if (_exactTextRadioButton.isChecked())
        {
            _containsTextEditText.setEnabled(false);
            _exactTextEditText.setEnabled(true);
        }
        else { }
    }

    public void selectExactDate(View v)
    {
        DialogFragment fragment = new DatePickerFragment();
        ((DatePickerFragment) fragment).setLiveData(_exactDateLive);
        fragment.show(getSupportFragmentManager(), "exactDatePicker");
    }

    public void selectStartDate(View v)
    {
        DialogFragment fragment = new DatePickerFragment();
        ((DatePickerFragment) fragment).setLiveData(_startDateLive);
        fragment.show(getSupportFragmentManager(), "startDatePicker");
    }

    public void selectEndDate(View v)
    {
        DialogFragment fragment = new DatePickerFragment();
        ((DatePickerFragment) fragment).setLiveData(_endDateLive);
        fragment.show(getSupportFragmentManager(), "endDatePicker");
    }

    public void checkAllCategories(View v)
    {
        for (int i = 0; i < _categoryCheckBoxes.length; i++)
        {
            _categoryCheckBoxes[i].setChecked(true);
        }
    }

    public void clearAllCategories(View v)
    {
        for (int i = 0; i < _categoryCheckBoxes.length; i++)
        {
            _categoryCheckBoxes[i].setChecked(false);
        }
    }

    public void search(View v)
    {
        // Check for missing data // TODO

        // Create the intent
        Intent intent = new Intent(getApplicationContext(), SearchResultsActivity.class);

        // First get the date information
        if (_anyDateRadioButton.isChecked())
        {
            // Do nothing
        }
        else if (_exactDateRadioButton.isChecked())
        {
            intent.putExtra(EXACT_DATE_INTENT, _exactDate);
        }
        else if (_dateRangeRadioButton.isChecked())
        {
            intent.putExtra(START_DATE_INTENT, _startDate);
            intent.putExtra(END_DATE_INTENT, _endDate);
        }
        else { }

        // Next get the amount information
        if (_anyAmountRadioButton.isChecked())
        {
            // Do nothing
        }
        else if (_exactAmountRadioButton.isChecked())
        {
            intent.putExtra(EXACT_AMOUNT_CURRENCY_INTENT, _exactAmountCurrency);
            intent.putExtra(EXACT_AMOUNT_INTENT, _exactAmountEditText.getAmount());
        }
        else if (_amountRangeRadioButton.isChecked())
        {
            intent.putExtra(AMOUNT_RANGE_CURRENCY_INTENT, _amountRangeCurrency);
            intent.putExtra(AMOUNT_RANGE_LOWER_INTENT, _amountRangeLowerEditText.getAmount());
            intent.putExtra(AMOUNT_RANGE_UPPER_INTENT, _amountRangeUpperEditText.getAmount());
        }
        else { }

        // Next get the category information
        boolean[] categories = new boolean[_categoryCheckBoxes.length];
        for (int i = 0; i < _categoryCheckBoxes.length; i++)
        {
            categories[i] = _categoryCheckBoxes[i].isChecked();
        }
        intent.putExtra(CATEGORIES_INTENT, categories);

        // Finally get the note information
        if (_anyTextRadioButton.isChecked())
        {
            // Do nothing
        }
        else if (_containsTextRadioButton.isChecked())
        {
            intent.putExtra(CONTAINS_TEXT_INTENT, _containsTextEditText.getText());
        }
        else if (_exactTextRadioButton.isChecked())
        {
            intent.putExtra(EXACT_TEXT_INTENT, _exactTextEditText.getText());
        }
        else { }

        startActivity(intent);
    }
}

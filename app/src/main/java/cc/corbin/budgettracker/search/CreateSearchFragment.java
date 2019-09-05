package cc.corbin.budgettracker.search;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import cc.corbin.budgettracker.auxilliary.NavigationActivity;
import cc.corbin.budgettracker.auxilliary.NavigationDrawerHelper;
import cc.corbin.budgettracker.custom.CreateCustomViewActivity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.importexport.ImportExportActivity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedEditText;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.year.YearViewActivity;

public class CreateSearchFragment extends Fragment
{
    private final String TAG = "CreateSearchFragment";

    public static final String EXACT_DATE_INTENT = "Exact_Date";
    public static final String START_DATE_INTENT = "Start_Date";
    public static final String END_DATE_INTENT = "End_Date";
    public static final String INCLUDE_EXTRAS_INTENT = "Include_Extras";
    public static final String EXACT_AMOUNT_CURRENCY_INTENT = "Exact_Amount_Currency";
    public static final String EXACT_AMOUNT_INTENT = "Exact_Amount";
    public static final String AMOUNT_RANGE_CURRENCY_INTENT = "Amount_Range_Currency";
    public static final String AMOUNT_RANGE_LOWER_INTENT = "Amount_Range_Lower";
    public static final String AMOUNT_RANGE_UPPER_INTENT = "Amount_Range_Upper";
    public static final String CATEGORIES_INTENT = "Categories";
    public static final String CONTAINS_TEXT_INTENT = "Contains_Text";
    public static final String EXACT_TEXT_INTENT = "Exact_Text";

    private AppCompatActivity _context;

    private CheckBox _includeExtrasCheckBox;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved)
    {
        return inflater.inflate(R.layout.fragment_create_search, group);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        _context = ((AppCompatActivity)getActivity());

        _includeExtrasCheckBox = view.findViewById(R.id.includeExtrasCheckBox);

        LinearLayout categoriesLayout = view.findViewById(R.id.categoriesLinearLayout);
        String[] categories = Categories.getCategories();
        _categoryCheckBoxes = new CheckBox[categories.length];
        for (int i = 0; i < categories.length; i++)
        {
            _categoryCheckBoxes[i] = new CheckBox(_context);
            _categoryCheckBoxes[i].setText(categories[i]);
            _categoryCheckBoxes[i].setChecked(true);
            categoriesLayout.addView(_categoryCheckBoxes[i]);
        }

        _anyDateRadioButton = view.findViewById(R.id.anyDateRadioButton);
        _exactDateRadioButton = view.findViewById(R.id.exactDateRadioButton);
        _dateRangeRadioButton = view.findViewById(R.id.dateRangeRadioButton);
        _anyAmountRadioButton = view.findViewById(R.id.anyAmountRadioButton);
        _exactAmountRadioButton = view.findViewById(R.id.exactAmountRadioButton);
        _amountRangeRadioButton = view.findViewById(R.id.amountRangeRadioButton);
        _anyTextRadioButton = view.findViewById(R.id.anyTextRadioButton);
        _containsTextRadioButton = view.findViewById(R.id.containsTextRadioButton);
        _exactTextRadioButton = view.findViewById(R.id.exactTextRadioButton);

        _exactDateButton = view.findViewById(R.id.exactDateButton);
        _startDateButton = view.findViewById(R.id.startDateButton);
        _endDateButton = view.findViewById(R.id.endDateButton);
        _exactAmountCurrencySpinner = view.findViewById(R.id.exactAmountCurrencySpinner);
        _exactAmountEditText = view.findViewById(R.id.exactAmountEditText);
        _amountRangeCurrencySpinner = view.findViewById(R.id.amountRangeCurrencySpinner);
        _amountRangeLowerEditText = view.findViewById(R.id.amountRangeLowerEditText);
        _amountRangeUpperEditText = view.findViewById(R.id.amountRangeUpperEditText);
        _containsTextEditText = view.findViewById(R.id.containsTextEditText);
        _exactTextEditText = view.findViewById(R.id.exactTextEditText);

        _amountRangeCurrencyTextView = view.findViewById(R.id.amountRangeCurrencyTextView);

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

        ((RadioGroup)view.findViewById(R.id.dateRadioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                updateDateViews();
            }
        });

        ((RadioGroup)view.findViewById(R.id.amountRadioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                updateAmountViews();
            }
        });

        ((RadioGroup)view.findViewById(R.id.noteRadioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                updateNoteViews();
            }
        });

        ArrayAdapter<String> currencySpinnerArrayAdapter = new ArrayAdapter<String>(
                _context, android.R.layout.simple_spinner_item, Currencies.symbols);
        _exactAmountCurrencySpinner.setAdapter(currencySpinnerArrayAdapter);
        _amountRangeCurrencySpinner.setAdapter(currencySpinnerArrayAdapter);
        _exactAmountCurrencySpinner.setSelection(Currencies.default_currency);
        _amountRangeCurrencySpinner.setSelection(Currencies.default_currency);

        _exactDateYearTextView = view.findViewById(R.id.exactYearTextView);
        _exactDateMonthTextView = view.findViewById(R.id.exactMonthTextView);
        _exactDateDayTextView = view.findViewById(R.id.exactDayTextView);
        _startDateYearTextView = view.findViewById(R.id.startYearTextView);
        _startDateMonthTextView = view.findViewById(R.id.startMonthTextView);
        _startDateDayTextView = view.findViewById(R.id.startDayTextView);
        _endDateYearTextView = view.findViewById(R.id.endYearTextView);
        _endDateMonthTextView = view.findViewById(R.id.endMonthTextView);
        _endDateDayTextView = view.findViewById(R.id.endDayTextView);

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
            _exactDateButton.setAlpha(0.3f);
            _startDateButton.setEnabled(false);
            _startDateButton.setAlpha(0.3f);
            _endDateButton.setEnabled(false);
            _endDateButton.setAlpha(0.3f);
        }
        else if (_exactDateRadioButton.isChecked())
        {
            _exactDateButton.setEnabled(true);
            _exactDateButton.setAlpha(1.0f);
            _startDateButton.setEnabled(false);
            _startDateButton.setAlpha(0.3f);
            _endDateButton.setEnabled(false);
            _endDateButton.setAlpha(0.3f);
        }
        else if (_dateRangeRadioButton.isChecked())
        {
            _exactDateButton.setEnabled(false);
            _exactDateButton.setAlpha(0.3f);
            _startDateButton.setEnabled(true);
            _startDateButton.setAlpha(1.0f);
            _endDateButton.setEnabled(true);
            _endDateButton.setAlpha(1.0f);
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
        fragment.show(_context.getSupportFragmentManager(), "exactDatePicker");
    }

    public void selectStartDate(View v)
    {
        DialogFragment fragment = new DatePickerFragment();
        ((DatePickerFragment) fragment).setLiveData(_startDateLive);
        fragment.show(_context.getSupportFragmentManager(), "startDatePicker");
    }

    public void selectEndDate(View v)
    {
        DialogFragment fragment = new DatePickerFragment();
        ((DatePickerFragment) fragment).setLiveData(_endDateLive);
        fragment.show(_context.getSupportFragmentManager(), "endDatePicker");
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
        // Check for missing data
        boolean dataComplete = isDataComplete();

        if (dataComplete)
        {
            startActivity(getSearchIntent());
        }
        else { }
    }

    // Also launches a popup
    public boolean isDataComplete()
    {
        boolean dataComplete = true;
        String missingDataString = "";

        // Date check
        if (_exactDateRadioButton.isChecked())
        {
            if (_exactDate == null)
            {
                dataComplete = false;
                missingDataString = "Missing exact date";
            }
            else { }
        }
        else if (_dateRangeRadioButton.isChecked())
        {
            if (_startDate == null)
            {
                dataComplete = false;
                if (_endDate == null) // Check if both are missing
                {
                    missingDataString = "Missing start and end date";
                }
                else
                {
                    missingDataString = "Missing start date";
                }
            }
            else if (_endDate == null) // Check if only the end date is missing
            {
                dataComplete = false;
                missingDataString = "Missing end date";
            }
            else if (_startDate.after(_endDate))
            {
                dataComplete = false;
                missingDataString = "Start date must occur before end date";
            }
            else { }
        }
        else { }

        // Amount check
        if (_exactAmountRadioButton.isChecked())
        {
            if (_exactAmountEditText.getText().length() == 0)
            {
                dataComplete = false;
                missingDataString = "Exact amount is missing a value";
            }
            else { }
        }
        else if (_amountRangeRadioButton.isChecked())
        {
            if (_amountRangeLowerEditText.getText().length() == 0)
            {
                dataComplete = false;
                if (_amountRangeUpperEditText.getText().length() == 0)
                {
                    missingDataString = "Amount upper and lower bounds are missing values";
                }
                else
                {
                    missingDataString = "Amount lower bound is missing a value";
                }
            }
            else if (_amountRangeUpperEditText.getText().length() == 0)
            {
                dataComplete = false;
                missingDataString = "Amount upper bound is missing a value";
            }
            else if (_amountRangeLowerEditText.getAmount() > _amountRangeUpperEditText.getAmount())
            {
                dataComplete = false;
                missingDataString = "Amount lower bound should be not be greater than the upper bound";
            }
            else { }
        }
        else { }

        // Category check
        boolean categoryChecked = false;
        for (int i = 0; i < _categoryCheckBoxes.length; i++)
        {
            if (_categoryCheckBoxes[i].isChecked())
            {
                categoryChecked = true;
                break;
            }
            else { }
        }
        if (!categoryChecked)
        {
            dataComplete = false;
            missingDataString = "Select at least one category";
        }
        else { }

        // No checks necessary for the notes

        if (!dataComplete)
        {
            View missingDataView = View.inflate(_context, R.layout.popup_missing_search_data, null);
            final PopupWindow popupWindow = new PopupWindow(missingDataView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            TextView messageTextView = missingDataView.findViewById(R.id.messageTextView);
            messageTextView.setText(missingDataString);

            Button okButton = missingDataView.findViewById(R.id.okButton);
            okButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    popupWindow.dismiss();
                }
            });

            popupWindow.setFocusable(true);
            popupWindow.update();
            popupWindow.showAtLocation(_context.findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);
        }

        return dataComplete;
    }

    public Intent getSearchIntent()
    {
        // Create the intent
        Intent intent = new Intent(_context.getApplicationContext(), SearchResultsActivity.class);

        // First get the date information
        if (_anyDateRadioButton.isChecked())
        {
            // Do nothing
        }
        else if (_exactDateRadioButton.isChecked())
        {
            intent.putExtra(EXACT_DATE_INTENT, _exactDate.getTime());
        }
        else if (_dateRangeRadioButton.isChecked())
        {
            intent.putExtra(START_DATE_INTENT, _startDate.getTime());
            intent.putExtra(END_DATE_INTENT, _endDate.getTime());
        }
        else { }

        intent.putExtra(INCLUDE_EXTRAS_INTENT, _includeExtrasCheckBox.isChecked());

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
        else {  }

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
            intent.putExtra(CONTAINS_TEXT_INTENT, _containsTextEditText.getText().toString());
        }
        else if (_exactTextRadioButton.isChecked())
        {
            intent.putExtra(EXACT_TEXT_INTENT, _exactTextEditText.getText().toString());
        }
        else { }

        return intent;
    }
}

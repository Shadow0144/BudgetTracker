package cc.corbin.budgettracker.search;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.Calendar;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;

public class CreateSearchActivity extends AppCompatActivity
{
    private final String TAG = "CreateSearchActivity";

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

    private LinearLayout _exactDateLinearLayout;
    private LinearLayout _startDateLayout;
    private LinearLayout _endDateLayout;
    private Spinner _exactAmountCurrencySpinner;
    private EditText _exactAmountEditText;
    private Spinner _amountRangeCurrencySpinner;
    private EditText _amountRangeLowerEditText;
    private EditText _amountRangeUpperEditText;
    private EditText _containsTextEditText;
    private EditText _exactTextEditText;

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

        _exactDateLinearLayout = findViewById(R.id.exactDateLinearLayout);
        _startDateLayout = findViewById(R.id.startDateLayout);
        _endDateLayout = findViewById(R.id.endDateLayout);
        _exactAmountCurrencySpinner = findViewById(R.id.exactAmountCurrencySpinner);
        _exactAmountEditText = findViewById(R.id.exactAmountEditText);
        _amountRangeCurrencySpinner = findViewById(R.id.amountRangeCurrencySpinner);
        _amountRangeLowerEditText = findViewById(R.id.amountRangeLowerEditText);
        _amountRangeUpperEditText = findViewById(R.id.amountRangeUpperEditText);
        _containsTextEditText = findViewById(R.id.containsTextEditText);
        _exactTextEditText = findViewById(R.id.exactTextEditText);

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

        updateDateViews();
        updateAmountViews();
        updateNoteViews();
    }

    private void updateDateViews()
    {
        if (_anyDateRadioButton.isChecked())
        {
            _exactDateLinearLayout.setEnabled(false);
            _startDateLayout.setEnabled(false);
            _endDateLayout.setEnabled(false);
        }
        else if (_exactDateRadioButton.isChecked())
        {
            _exactDateLinearLayout.setEnabled(true);
            _startDateLayout.setEnabled(false);
            _endDateLayout.setEnabled(false);
        }
        else if (_dateRangeRadioButton.isChecked())
        {
            _exactDateLinearLayout.setEnabled(false);
            _startDateLayout.setEnabled(true);
            _endDateLayout.setEnabled(true);
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
}

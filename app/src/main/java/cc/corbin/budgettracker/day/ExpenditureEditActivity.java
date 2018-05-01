package cc.corbin.budgettracker.day;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.MoneyValueFilter;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class ExpenditureEditActivity extends AppCompatActivity
{
    private final String TAG = "ExpenditureEditActivity";

    public final static String TYPE_INTENT = "Type";
    public final static String DAY_INTENT = "Day";
    public final static String MONTH_INTENT = "Month";
    public final static String YEAR_INTENT = "Year";
    public final static String EXPENDITURE_INTENT = "Expenditure";
    public final static String INDEX_INTENT = "Index";

    private int _index;
    private ExpenditureEntity _expenditure;

    private PopupWindow _popupWindow;

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

                int day = intent.getIntExtra(DAY_INTENT, 0);
                int month = intent.getIntExtra(MONTH_INTENT, 0);
                int year = intent.getIntExtra(YEAR_INTENT, 0);
                _expenditure = new ExpenditureEntity(day, month, year);
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
    }

    public void onAccept(View v)
    {
        final Spinner symbolSpinner = findViewById(R.id.currencySelector);
        final EditText amountEditText = findViewById(R.id.valueEditText);
        final RadioGroup categoriesHolder = findViewById(R.id.categoriesHolder);
        final RadioButton button = categoriesHolder.findViewById(categoriesHolder.getCheckedRadioButtonId());
        final EditText noteEditText = findViewById(R.id.noteEditText);

        // Setup the expenditure
        _expenditure.setCurrency(symbolSpinner.getSelectedItemPosition());
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
        // Setup the currency type spinner
        final Spinner symbolSpinner = findViewById(R.id.currencySelector);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, Currencies.symbols);
        symbolSpinner.setAdapter(spinnerArrayAdapter);
        symbolSpinner.setSelection(Currencies.default_currency);
        // TODO Allow the symbolSpinner to change the number of decimals

        // Setup the amount edit
        final EditText amountEditText = findViewById(R.id.valueEditText);
        MoneyValueFilter moneyValueFilter = new MoneyValueFilter();
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

        // Setup the values if they exist already
        if (_expenditure != null)
        {
            symbolSpinner.setSelection(_expenditure.getCurrency());
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
        final String[] categories = DayViewActivity.getCategories();
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
}

package cc.corbin.budgettracker.edit;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
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
import android.widget.CalendarView;
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
import java.time.Month;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.ConversionRateAsyncTask;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
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

public class AdjustmentEditActivity extends AppCompatActivity implements NumericalFormattedCallback, TabLayout.OnTabSelectedListener
{
    private final String TAG = "AdjustmentEditActivity";

    public final static String TYPE_INTENT = "Type";
    public final static String MONTH_INTENT = "Month";
    public final static String YEAR_INTENT = "Year";
    public final static String CATEGORY_INTENT = "Category";
    public final static String BUDGET_INTENT = "Budget";
    public final static String LINKED_BUDGET_INTENT = "LinkedBudget";
    public final static String GROUP_INDEX_INTENT = "GroupIndex";
    public final static String CHILD_INDEX_INTENT = "ChildIndex";

    private int _month;
    private int _year;
    private int _category;

    private int _transferMonth;
    private int _transferYear;
    private int _transferCategory;

    private int _groupIndex;
    private int _childIndex;
    private BudgetEntity _adjustment;

    private TabLayout _typeTabLayout;
    private TextView _signTextView;
    private NumericalFormattedEditText _amountEditText;
    private EditText _noteEditText;

    private TextView _monthTextView;
    private TextView _yearTextView;

    private PopupWindow _popupWindow;

    private float _amount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjustment_edit);

        Intent intent = getIntent();
        int type = intent.getIntExtra(TYPE_INTENT, -1);
        setResult(MonthViewActivity.CANCEL); // In case of backing out
        switch (type)
        {
            case MonthViewActivity.CREATE_ADJUSTMENT:
                _month = intent.getIntExtra(MONTH_INTENT, 0);
                _year = intent.getIntExtra(YEAR_INTENT, 0);
                _transferMonth = _month;
                _transferYear = _year;
                String[] categories = Categories.getCategories();
                _category = intent.getIntExtra(CATEGORY_INTENT, (categories.length-1));
                _adjustment = new BudgetEntity(_month, _year, 0, _category, categories[_category]);
                _adjustment.setAdjustment(1); // All budgets created here are adjustments
                _groupIndex = 0;
                _childIndex = 0;
                break;

            case MonthViewActivity.EDIT_ADJUSTMENT:
                _adjustment = intent.getParcelableExtra(BUDGET_INTENT);
                _groupIndex = intent.getIntExtra(GROUP_INDEX_INTENT, -1);
                _childIndex = intent.getIntExtra(CHILD_INDEX_INTENT, -1);

                findViewById(R.id.deleteButton).setEnabled(true); // Enable deleting

                if (_adjustment == null || _groupIndex == -1 || _childIndex == -1)
                {
                    Log.e(TAG, "Editing error");
                    setResult(MonthViewActivity.FAILURE);
                    finish();
                }
                else { }

                _month = _adjustment.getMonth();
                _year = _adjustment.getYear();
                _transferMonth = _month;
                _transferYear = _year;
                break;

            default:
                Log.e(TAG, "Error");
                setResult(MonthViewActivity.FAILURE);
                finish();
                break;
        }

        _typeTabLayout = findViewById(R.id.typeTabLayout);
        _typeTabLayout.addOnTabSelectedListener(this);

        setupAmount();
        setupCategories();
        setupNote();
    }

    public void onAccept(View v)
    {
        if (_typeTabLayout.getSelectedTabPosition() == 1)
        {
            _amount = -_amount;
        }
        else { }
        _adjustment.setAmount(_amount);
        _adjustment.setNote(_noteEditText.getText().toString());

        Intent intent = new Intent();
        intent.putExtra(BUDGET_INTENT, _adjustment);
        intent.putExtra(GROUP_INDEX_INTENT, _groupIndex);
        intent.putExtra(CHILD_INDEX_INTENT, _childIndex);

        setResult(MonthViewActivity.SUCCEED, intent);

        DayViewActivity.dataInvalid = true;
        MonthViewActivity.dataInvalid = true;
        YearViewActivity.dataInvalid = true;
        TotalViewActivity.dataInvalid = true;

        finish();
    }

    public void onCancel(View v)
    {
        setResult(MonthViewActivity.CANCEL);
        finish();
    }

    public void onDelete(View v) // TODO - ask about linked budgets
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
        intent.putExtra(BUDGET_INTENT, _adjustment);
        intent.putExtra(GROUP_INDEX_INTENT, _groupIndex);
        intent.putExtra(CHILD_INDEX_INTENT, _childIndex);

        setResult(MonthViewActivity.DELETE, intent);

        DayViewActivity.dataInvalid = true;
        MonthViewActivity.dataInvalid = true;
        YearViewActivity.dataInvalid = true;
        TotalViewActivity.dataInvalid = true;

        finish();
    }

    private void setupAmount()
    {
        _signTextView = findViewById(R.id.signTextView);
        _amountEditText = findViewById(R.id.amountEditText);

        onTabSelected(null);

        _amount = 0.0f;
        if (_adjustment != null)
        {
            _amount = _adjustment.getAmount();
            _amountEditText.setup(this, Currencies.default_currency, _amount);
        }
        else
        {
            _amountEditText.setup(this);
        }
    }

    private void setupCategories()
    {
        TextView categoryTextView = findViewById(R.id.categoryTextView);
        categoryTextView.setText(_adjustment.getCategoryName());

        TextView dateTextView = findViewById(R.id.dateTextView);
        dateTextView.setText(_adjustment.getMonth() + "/" + _adjustment.getYear()); // TODO

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
        if (_adjustment != null)
        {
            String categoryName = _adjustment.getCategoryName();
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

        _yearTextView = findViewById(R.id.yearTextView);
        _yearTextView.setText("" + _transferYear);

        _monthTextView = findViewById(R.id.monthTextView);
        _monthTextView.setText("" + _transferMonth); // TODO
    }

    private void setupNote()
    {
        _noteEditText = findViewById(R.id.noteEditText);
        if (_adjustment != null)
        {
            // Setup the note
            _noteEditText.setText(_adjustment.getNote());
        }
        else { }
    }

    @Override
    public void valueChanged(int id, float value)
    {
        _amount = value;
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        String text = "";
        switch (_typeTabLayout.getSelectedTabPosition())
        {
            case 0:
               text = getString(R.string.positive);
                break;
            case 1:
                text = getString(R.string.negative);
                break;
            case 2:
                // Ambiguous TODO
                text = getString(R.string.positive);
                break;
        }
        text += Currencies.symbols[Currencies.default_currency];
        _signTextView.setText(text);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    public void previousMonth(View v)
    {
        _transferMonth--;
        if (_transferMonth < 1)
        {
            _transferMonth = 12;
        }
        else { }
        String text = String.format("%02d", _transferMonth);
        _monthTextView.setText(text);
    }

    public void nextMonth(View v)
    {
        _transferMonth++;
        if (_transferMonth > 12)
        {
            _transferMonth = 1;
        }
        else { }
        String text = String.format("%02d", _transferMonth);
        _monthTextView.setText(text);
    }

    public void previousYear(View v)
    {
        _transferYear--;
        String text = String.format("%02d", _transferYear);
        _yearTextView.setText(text);
    }

    public void nextYear(View v)
    {
        _transferYear++;
        String text = String.format("%02d", _transferYear);
        _yearTextView.setText(text);
    }
}

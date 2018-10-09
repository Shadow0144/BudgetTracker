package cc.corbin.budgettracker.edit;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Spinner;
import android.widget.TextView;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedEditText;
import cc.corbin.budgettracker.numericalformatting.NumericalFormattedCallback;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.year.YearViewActivity;

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
    public final static String TRANSFER_INTENT = "Transfer";

    private int _month;
    private int _year;
    private int _category;

    private boolean _editing;

    private int _transferMonth;
    private int _transferYear;
    private int _transferCategory;

    private int _groupIndex;
    private int _childIndex;
    private BudgetEntity _adjustment;

    private TabLayout _typeTabLayout;
    private TextView _signTextView;
    private TextView _currencyTextView;
    private NumericalFormattedEditText _amountEditText;
    private EditText _noteEditText;

    private TextView _monthTextView;
    private TextView _yearTextView;

    private PopupWindow _popupWindow;

    private float _amount;

    private boolean _transferTo;
    private LinearLayout _transferLayout;
    private TextView _transferHeaderTextView;
    private Spinner _transferCategorySpinner;
    private Button _transferDirectionButton;
    private TextView _currentDetailsTextView;
    private TextView _sisterDetailsTextView;

    private Button _signSwitchButton;
    private boolean _negative;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjustment_edit);

        _typeTabLayout = findViewById(R.id.typeTabLayout);
        _typeTabLayout.addOnTabSelectedListener(this);

        _signTextView = findViewById(R.id.signTextView);

        Intent intent = getIntent();
        int type = intent.getIntExtra(TYPE_INTENT, -1);
        setResult(MonthViewActivity.CANCEL); // In case of backing out
        switch (type)
        {
            case MonthViewActivity.CREATE_ADJUSTMENT:
                _editing = false;

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
                _editing = true;

                _adjustment = intent.getParcelableExtra(BUDGET_INTENT);
                _groupIndex = intent.getIntExtra(GROUP_INDEX_INTENT, -1);
                _childIndex = intent.getIntExtra(CHILD_INDEX_INTENT, -1);

                _typeTabLayout.setVisibility(View.GONE); // Disable the tabs
                findViewById(R.id.deleteButton).setEnabled(true); // Enable deleting
                _signSwitchButton = findViewById(R.id.signSwitchButton);
                _signSwitchButton.setVisibility(View.VISIBLE);
                _signTextView.setVisibility(View.GONE);

                // Show the linked editing text if this adjustment is linked to another one
                if (_adjustment.getSisterAdjustment() > -1)
                {
                    findViewById(R.id.linkedTextView).setVisibility(View.VISIBLE);
                }
                else { }

                float amount = _adjustment.getAmount();
                if (amount >= 0.0f)
                {
                    _negative = false;
                    _signSwitchButton.setText(R.string.positive);
                }
                else
                {
                    _negative = true;
                    _signSwitchButton.setText(R.string.negative);
                    _adjustment.setAmount(-amount);
                }

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

        _transferTo = true;
        _transferLayout = findViewById(R.id.transferLayout);
        _transferHeaderTextView = findViewById(R.id.transferHeaderTextView);
        _transferDirectionButton = findViewById(R.id.transferDirectionButton);
        _currentDetailsTextView = findViewById(R.id.currentDetailsTextView);
        _sisterDetailsTextView = findViewById(R.id.sisterDetailsTextView);

        setupAmount();
        setupCategories();
        setupNote();

        _yearTextView = findViewById(R.id.yearTextView);
        _yearTextView.setText("" + _transferYear);

        _monthTextView = findViewById(R.id.monthTextView);
        _monthTextView.setText(String.format("%02d", _transferMonth)); // TODO

        updateTransferInformation();
    }

    public void onAccept(View v)
    {
        if (!_editing)
        {
            acceptCreate();
        }
        else
        {
            acceptEdit();
        }
    }

    private void acceptCreate()
    {
        Intent intent = new Intent();
        intent.putExtra(GROUP_INDEX_INTENT, _groupIndex);
        intent.putExtra(CHILD_INDEX_INTENT, _childIndex);

        String note = _noteEditText.getText().toString();

        int tabPosition = _typeTabLayout.getSelectedTabPosition();
        switch (tabPosition)
        {
            case 0:
                // Do nothing
                break;
            case 1:
                _amount = -_amount;
                break;
            case 2:
                if (!_transferTo) // Flip the amount if transferring into this category and month
                {
                    _amount = -_amount;
                }
                else { }
                BudgetEntity sisterAdjustment = new BudgetEntity
                        (_transferMonth, _transferYear, _amount,
                                _transferCategory, Categories.getCategories()[_transferCategory]);
                sisterAdjustment.setNote(note); // Set the note
                sisterAdjustment.setAdjustment(1); // Make sure to set it as an adjustment
                intent.putExtra(LINKED_BUDGET_INTENT, sisterAdjustment);
                _amount = -_amount;
                break;
        }
        _adjustment.setAmount(_amount);
        _adjustment.setNote(note);

        intent.putExtra(BUDGET_INTENT, _adjustment);
        intent.putExtra(TRANSFER_INTENT, (tabPosition == 2));

        setResult(MonthViewActivity.SUCCEED, intent);

        DayViewActivity.dataInvalid = true;
        MonthViewActivity.dataInvalid = true;
        YearViewActivity.dataInvalid = true;
        TotalViewActivity.dataInvalid = true;

        finish();
    }

    private void acceptEdit()
    {
        Intent intent = new Intent();
        intent.putExtra(GROUP_INDEX_INTENT, _groupIndex);
        intent.putExtra(CHILD_INDEX_INTENT, _childIndex);

        String note = _noteEditText.getText().toString();

        if (_negative)
        {
            _amount *= -1;
        }
        else { }
        _adjustment.setAmount(_amount);
        _adjustment.setNote(note);

        intent.putExtra(BUDGET_INTENT, _adjustment);

        long linkedAdjustmentID = _adjustment.getSisterAdjustment();
        intent.putExtra(TRANSFER_INTENT, (linkedAdjustmentID > -1));
        if (linkedAdjustmentID > -1)
        {
            BudgetEntity sisterAdjustment = new BudgetEntity(-1, -1, -_amount,
                    -1, ""); // Only the amount and note values will be updated
            sisterAdjustment.setId(linkedAdjustmentID); // Remember to set the ID
            sisterAdjustment.setNote(note);
            intent.putExtra(LINKED_BUDGET_INTENT, sisterAdjustment);
        }
        else { }

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
        final View deleteView = getLayoutInflater().inflate(R.layout.popup_confirm_delete_adjustment, null);
        if (_adjustment.getSisterAdjustment() > -1)
        {
            deleteView.findViewById(R.id.linkedTextView).setVisibility(View.VISIBLE);
        }
        else
        {
            deleteView.findViewById(R.id.linkedTextView).setVisibility(View.GONE);
        }

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

        long linkedAdjustmentID = _adjustment.getSisterAdjustment();
        intent.putExtra(TRANSFER_INTENT, (linkedAdjustmentID > -1));
        if (linkedAdjustmentID > -1)
        {
            BudgetEntity sisterAdjustment = new BudgetEntity(); // Only the ID is required
            sisterAdjustment.setId(linkedAdjustmentID);
            intent.putExtra(LINKED_BUDGET_INTENT, sisterAdjustment);
        }
        else { }

        setResult(MonthViewActivity.DELETE, intent);

        DayViewActivity.dataInvalid = true;
        MonthViewActivity.dataInvalid = true;
        YearViewActivity.dataInvalid = true;
        TotalViewActivity.dataInvalid = true;

        finish();
    }

    private void setupAmount()
    {
        TextView categoryTextView = findViewById(R.id.categoryTextView);
        categoryTextView.setText(_adjustment.getCategoryName());

        TextView dateTextView = findViewById(R.id.dateTextView);
        dateTextView.setText(String.format("%02d", _month) + " / " + String.format("%04d", _year)); // TODO

        _currencyTextView = findViewById(R.id.currencyTextView);
        _currencyTextView.setText(Currencies.symbols[Currencies.default_currency]);

        _amountEditText = findViewById(R.id.amountEditText);
        if (_adjustment.getId() != 0)
        {
            _amount = _adjustment.getAmount();
            _amountEditText.setup(this, Currencies.default_currency, _amount);
        }
        else
        {
            _amount = 0.0f;
            _amountEditText.setup(this);
        }
    }

    private void setupCategories()
    {
        // Setup the categories
        _transferCategorySpinner = findViewById(R.id.categorySpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, Categories.getCategories());
        _transferCategorySpinner.setAdapter(spinnerArrayAdapter);
        _transferCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                _transferCategory = position;
                updateTransferInformation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // Do nothing
            }
        });
        _transferCategorySpinner.setSelection(Categories.getCategories().length-1);
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
        switch (_typeTabLayout.getSelectedTabPosition())
        {
            case 0:
               _signTextView.setText(R.string.positive);
               _transferLayout.setVisibility(View.GONE);
                break;
            case 1:
                _signTextView.setText(R.string.negative);
                _transferLayout.setVisibility(View.GONE);
                break;
            case 2:
                _signTextView.setText("");
                _transferLayout.setVisibility(View.VISIBLE);
                break;
        }
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
        _monthTextView.setText(String.format("%02d", _transferMonth));
        updateTransferInformation();
    }

    public void nextMonth(View v)
    {
        _transferMonth++;
        if (_transferMonth > 12)
        {
            _transferMonth = 1;
        }
        else { }
        _monthTextView.setText(String.format("%02d", _transferMonth));
        updateTransferInformation();
    }

    public void previousYear(View v)
    {
        _transferYear--;
        _yearTextView.setText(String.format("%04d", _transferYear));
        updateTransferInformation();
    }

    public void nextYear(View v)
    {
        _transferYear++;
        _yearTextView.setText(String.format("%04d", _transferYear));
        updateTransferInformation();
    }

    public void changeTransferDirection(View v)
    {
        _transferTo = !_transferTo;
        if (_transferTo)
        {
            _transferHeaderTextView.setText(R.string.transfer_to);
            _transferDirectionButton.setText(R.string.rightarrow);
        }
        else
        {
            _transferHeaderTextView.setText(R.string.transfer_from);
            _transferDirectionButton.setText(R.string.leftarrow);
        }
    }

    private void updateTransferInformation()
    {
        _currentDetailsTextView.setText(String.format("%02d", _month) + " / " + String.format("%04d", _year) + " : " + Categories.getCategories()[_category]);
        _sisterDetailsTextView.setText(String.format("%02d", _transferMonth) + " / " + String.format("%04d", _transferYear) + " : " + Categories.getCategories()[_transferCategory]);
    }

    public void switchSign(View v)
    {
        _negative = !_negative;
        if (_negative)
        {
            _signSwitchButton.setText(R.string.negative);
        }
        else
        {
            _signSwitchButton.setText(R.string.positive);
        }
    }
}

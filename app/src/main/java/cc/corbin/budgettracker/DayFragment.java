package cc.corbin.budgettracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Corbin on 1/26/2018.
 */

public class DayFragment extends Fragment
{
    private final String TAG = "DayFragment";

    public final static String DATE_INTENT = "Date";

    private LinearLayout _itemsContainer;

    private List<ExpenditureEntity> _expenditureEntities;

    private DayFragmentPagerAdapter _parent;

    private int _year;
    private int _month;
    private int _day;

    private boolean _visible;

    private long _date;
    private long _uid;
    private ExpenditureDatabase _db;

    private boolean _addingNewExpenditure;

    private PopupWindow _popupWindow;
    private ExpenditureEntity _expenditure;

    public void setParameters(DayFragmentPagerAdapter parent, int year, int month, int day)
    {
        _parent = parent;
        _year = year;
        _month = month;
        _day = day;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_day, container, false);

        _itemsContainer = v.findViewById(R.id.itemsContainer);

        _visible = true;

        Calendar c = Calendar.getInstance();
        c.set(_year, _month-1, _day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        _date = c.getTimeInMillis();

        _db = ExpenditureDatabase.getExpenditureDatabase(getContext());
        _expenditureEntities = _db.expenditureDao().getDay(_date);

        if (_expenditureEntities.size() > 0)
        {
            _uid = _expenditureEntities.get(_expenditureEntities.size() - 1).getDate() + 1;
        }
        else
        {
            _uid = _date;
        }

        _addingNewExpenditure = false;

        setUpExpenditures();

        return v;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        _visible = false;
    }

    public void updateExpenditureDatabase()
    {
        _db.expenditureDao().update(_expenditureEntities);
    }

    // Begin popups to add a new expenditure
    public void addNewExpenditure()
    {
        if (!_addingNewExpenditure)
        {
            _expenditure = new ExpenditureEntity(_uid++);
            selectExpenditureAmount(false, null);
        }
        else { } // Ignore
    }

    // Initial popup to select amount and currency type
    private void selectExpenditureAmount(final boolean editing, final ViewGroup parent)
    {
        _addingNewExpenditure = true;

        final View costView = getLayoutInflater().inflate(R.layout.amount, null);

        // Setup the currency type spinner
        final Spinner symbolSpinner = costView.findViewById(R.id.currencySelector);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, Currencies.symbols);
        symbolSpinner.setAdapter(spinnerArrayAdapter);
        symbolSpinner.setSelection(Currencies.default_currency);
        // TODO Allow the symbolSpinner to change the number of decimals

        // Setup the amount edit
        final EditText amountEditText = costView.findViewById(R.id.valueEditText);
        MoneyValueFilter moneyValueFilter = new MoneyValueFilter();
        moneyValueFilter.setDigits(Currencies.integer[Currencies.default_currency] ? 0 : 2);
        amountEditText.setFilters(new InputFilter[]{moneyValueFilter});
        if (Currencies.integer[Currencies.default_currency])
        {
            amountEditText.setHint("0");
        }
        else
        {
            amountEditText.setHint("0.00");
        }

        // Setup the cancel button
        final Button cancelButton = costView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelAddingExpenditure();
            }
        });

        // Setup the accept button
        final Button okButton = costView.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _expenditure.setCurrency(symbolSpinner.getSelectedItemPosition());
                try
                {
                    _expenditure.setAmount(Float.parseFloat(amountEditText.getText().toString()));
                }
                catch (Exception e) // TODO
                {
                    Log.e(TAG, "Failure to parse");
                    _expenditure.setAmount(0.0f);
                }
                _popupWindow.dismiss();
                if (!editing) // Launch the next step if not editing the amount or currency type
                {
                    selectExpenditureCategory(editing, null);
                }
                else
                {
                    refreshExpenditureView(parent);
                }
            }
        });

        // Setup the values if they exist already
        if (editing)
        {
            symbolSpinner.setSelection(_expenditure.getCurrency());
            amountEditText.setText(""+_expenditure.getAmount());
        }
        else { }

        _popupWindow = new PopupWindow(costView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(_itemsContainer, Gravity.CENTER, 0, 0);
    }

    // Final popup to select the expenditure category
    private void selectExpenditureCategory(final boolean editing, final ViewGroup parent)
    {
        final View categoryView = getLayoutInflater().inflate(R.layout.category, null);

        // Setup the categories
        final RadioGroup categoriesHolder = categoryView.findViewById(R.id.categoriesHolder);
        Context context = getContext();
        String[] categories = DayViewActivity.getCategories();
        int count = categories.length;
        for (int i = 0; i < count; i++)
        {
            final RadioButton button = new RadioButton(context);
            button.setText(categories[i]);
            categoriesHolder.addView(button);
            button.setChecked(true);
        }

        // Setup the cancel button
        final Button cancelButton = categoryView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelAddingExpenditure();
            }
        });

        // Setup the accept button
        final Button okButton = categoryView.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final RadioButton button = categoryView.findViewById(categoriesHolder.getCheckedRadioButtonId());
                _expenditure.setExpenseType(button.getText().toString());
                _popupWindow.dismiss();
                if (!editing)
                {
                    succeedAddingExpenditure();
                }
                else
                {
                    refreshExpenditureView(parent);
                }
            }
        });

        // Setup the values if they already exist
        if (editing)
        {
            // TODO
            //categoriesHolder.check(_expenditure.getExpenseType());
        }
        else { }

        _popupWindow = new PopupWindow(categoryView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(_itemsContainer, Gravity.CENTER, 0, 0);
    }

    private void cancelAddingExpenditure()
    {
        _popupWindow.dismiss();
        _addingNewExpenditure = false;
    }

    private void succeedAddingExpenditure()
    {
        _db.expenditureDao().insertAll(_expenditure);

        addExpenditure(_expenditure);

        _addingNewExpenditure = false;
    }

    public void addExpenditure(ExpenditureEntity exp)
    {
        _expenditureEntities.add(exp);

        if (_visible)
        {
            addExpenditureView(exp, (_expenditureEntities.size()-1));
        }
        else { }
    }

    private void setUpExpenditures()
    {
        int count = _expenditureEntities.size();

        for (int i = 0; i < count; i++)
        {
            ExpenditureEntity exp = _expenditureEntities.get(i);

            addExpenditureView(exp, i);
        }
    }

    private void addExpenditureView(ExpenditureEntity exp, int index)
    {
        final View view = getLayoutInflater().inflate(R.layout.item, null);
        view.setId(index);

        final TextView currencyView = view.findViewById(R.id.currencyView);
        currencyView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editExpenditureAmount(v);
            }
        });
        /*currSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                /// TODO
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });*/
        currencyView.setText(Currencies.symbols[exp.getCurrency()]);

        final TextView categoryView = view.findViewById(R.id.categoryView);
        categoryView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editExpenditureCategory(v);
            }
        });
        /*catSpinner.setAdapter(spinnerArrayAdapter);
        catSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                _expenditureEntities.get(((ViewGroup)(parent.getParent())).getId()).setExpenseType(((String)(((Spinner)(parent)).getItemAtPosition(position))));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });*/
        categoryView.setText(exp.getExpenseType());

        final TextView costView = view.findViewById(R.id.costView);
        costView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editExpenditureAmount(v);
            }
        });
        /*costText.setFilters(new InputFilter[]{new MoneyValueFilter()});
        costText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                int id = ((ViewGroup)(costText.getParent())).getId();
                if (s.toString().length() > 0)
                {
                    try
                    {
                        _expenditureEntities.get(id).setAmount(Float.parseFloat(s.toString()));
                    }
                    catch (Exception e)
                    {
                        _expenditureEntities.get(id).setAmount(0);
                    }
                }
                else
                {
                    _expenditureEntities.get(id).setAmount(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });*/
        //costView.setText(""+exp.getAmount());

        String cost;
        if (Currencies.integer[exp.getCurrency()])
        {
            cost = String.format("%.00f", exp.getAmount());
        }
        else
        {
            cost = String.format("%.02f", exp.getAmount());
        }
        costView.setText(cost);

        TextView removeButton = view.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.alert_title);
                builder.setMessage(R.string.alert_body);
                builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                acceptRemove(view);
                            }
                        });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cancelRemove(view);
                            }
                        });
                // Create the AlertDialog object and return it
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        _itemsContainer.addView(view);
        _parent.updateTotal();
    }

    private void cancelRemove(View v)
    {
        // Do nothing
    }

    private void acceptRemove(View v)
    {
        ViewGroup parent = ((ViewGroup) (v.getParent()));
        parent.removeView(v);
        ExpenditureEntity exp = _expenditureEntities.remove(v.getId());
        _db.expenditureDao().delete(exp);
        // Rename all the views
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++)
        {
            parent.getChildAt(i).setId(i);
        }
        _parent.updateTotal();
    }

    private void refreshExpenditureView(ViewGroup view)
    {
        final TextView currencyView = view.findViewById(R.id.currencyView);
        currencyView.setText(Currencies.symbols[_expenditure.getCurrency()]);

        final TextView categoryView = view.findViewById(R.id.categoryView);
        categoryView.setText(_expenditure.getExpenseType());

        final TextView costView = view.findViewById(R.id.costView);
        String cost;
        if (Currencies.integer[_expenditure.getCurrency()])
        {
            cost = String.format("%.02f", _expenditure.getAmount());
        }
        else
        {
            cost = String.format("%.00f", _expenditure.getAmount());
        }
        costView.setText(cost);
        _parent.updateTotal();
    }

    public List<ExpenditureEntity> getExpenditures()
    {
        return _expenditureEntities;
    }

    public void setExpenditures(List<ExpenditureEntity> expenditures)
    {
        int count = expenditures.size();
        for (int i = 0; i < count; i++)
        {
            addExpenditure(expenditures.get(i));
        }
    }

    public void editExpenditureAmount(View v)
    {
        ViewGroup parent = ((ViewGroup) (v.getParent()));
        _expenditure = _expenditureEntities.get(parent.getId());
        selectExpenditureAmount(true, parent);
    }

    public void editExpenditureCategory(View v)
    {
        ViewGroup parent = ((ViewGroup) (v.getParent()));
        _expenditure = _expenditureEntities.get(parent.getId());
        selectExpenditureCategory(true, parent);
    }
}

package cc.corbin.budgettracker;

import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
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
    private LiveData<List<ExpenditureEntity>> _entity;
    private ExpenditureViewModel _viewModel;

    private DayFragmentPagerAdapter _parent;

    private int _year;
    private int _month;
    private int _day;

    private boolean _visible;

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

        _viewModel = ViewModelProviders.of(getActivity()).get(ExpenditureViewModel.class);
        _entity = _viewModel.getDay(_year, _month, _day);

        final Observer<List<ExpenditureEntity>> entityObserver = new Observer<List<ExpenditureEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<ExpenditureEntity> expenditureEntities)
            {
                onLoadExpenses(expenditureEntities);
            }
        };

        _entity.observe(this, entityObserver);

        return v;
    }

    public void onLoadExpenses(@Nullable List<ExpenditureEntity> expenditureEntities)
    {
        if (expenditureEntities != null)
        {
            _expenditureEntities = expenditureEntities;

            onLoad();
        }
        else { }
    }

    public void onLoad()
    {
        ConstraintLayout rootLayout;

        FrameLayout progressFrame = getActivity().findViewById(R.id.progressFrame);
        if (progressFrame != null)
        {
            rootLayout = ((ConstraintLayout) progressFrame.getParent());
        }
        else
        {
            progressFrame = getActivity().findViewById(R.id.dayFrame);
            rootLayout = ((ConstraintLayout) progressFrame.getParent());
        }

        rootLayout.removeView(progressFrame);

        LayoutInflater inflater = getLayoutInflater();
        View dayView = inflater.inflate(R.layout.day, rootLayout, true);

        _itemsContainer = dayView.findViewById(R.id.itemsContainer);

        _visible = true;

        _addingNewExpenditure = false;

        setUpExpenditures();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        _visible = false;
    }

    public void updateExpenditureDatabase()
    {
        if (_expenditureEntities != null)
        {
            _viewModel.updateEntities(_expenditureEntities);
        }
        else { }
    }

    // Begin popups to add a new expenditure
    public void addNewExpenditure()
    {
        if (!_addingNewExpenditure)
        {
            _expenditure = new ExpenditureEntity(_day, _month, _year);
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
        if (editing)
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
            String cost = Currencies.formatCurrency(Currencies.integer[_expenditure.getCurrency()], _expenditure.getAmount());
            amountEditText.setText(cost, TextView.BufferType.EDITABLE);
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
        final String[] categories = DayViewActivity.getCategories();
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
                _expenditure.setExpenseTypeNumber(categoriesHolder.getCheckedRadioButtonId());
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
            for (int i = 0; i < count; i++)
            {
                Log.e(TAG, "" + i + " " + ((RadioButton)(categoriesHolder.getChildAt(i))).getText().toString());
                if (categories[i] == ((RadioButton)(categoriesHolder.getChildAt(i))).getText())
                {
                    categoriesHolder.check(i);
                }
            }
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
        _addingNewExpenditure = false; // TODO
    }

    private void succeedAddingExpenditure()
    {
        _viewModel.insertEntity(_expenditure);

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

        String cost = Currencies.formatCurrency(Currencies.integer[exp.getCurrency()], exp.getAmount());
        costView.setText(cost);

        final Button removeButton = view.findViewById(R.id.removeButton);
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

        final Button noteButton = view.findViewById(R.id.noteButton);
        noteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editNote(v);
            }
        });

        final String note = exp.getNote();
        if (note.length() == 0)
        {
            noteButton.setForegroundTintList(getContext().getColorStateList(R.color.translucent));
        }
        else
        {
            noteButton.setForegroundTintList(getContext().getColorStateList(R.color.black));
        }

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
        _viewModel.removeEntity(exp);
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
        String cost = Currencies.formatCurrency(Currencies.integer[_expenditure.getCurrency()], _expenditure.getAmount());
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

    public void editNote(View v)
    {
        final ViewGroup parent = ((ViewGroup) (v.getParent()));
        _expenditure = _expenditureEntities.get(parent.getId());

        final View noteView = getLayoutInflater().inflate(R.layout.note, null);

        // Setup the categories
        final EditText noteEditText = noteView.findViewById(R.id.noteEditText);

        noteEditText.setText(_expenditure.getNote());

        // Setup the cancel button
        final Button cancelButton = noteView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _popupWindow.dismiss();
            }
        });

        // Setup the accept button
        final Button okButton = noteView.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String note;
                if (noteEditText.getText() != null)
                {
                    note = noteEditText.getText().toString();
                }
                else
                {
                    note = "";
                }

                _expenditure.setNote(note);

                if (note.length() == 0)
                {
                    parent.findViewById(R.id.noteButton).setForegroundTintList(getContext().getColorStateList(R.color.translucent));
                }
                else
                {
                    parent.findViewById(R.id.noteButton).setForegroundTintList(getContext().getColorStateList(R.color.black));
                }

                _popupWindow.dismiss();
            }
        });

        _popupWindow = new PopupWindow(noteView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(_itemsContainer, Gravity.CENTER, 0, 0);
    }
}

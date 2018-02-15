package cc.corbin.budgettracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        _expenditureEntities = _db.expenditureDao().getAll(_date);

        if (_expenditureEntities.size() > 0)
        {
            _uid = _expenditureEntities.get(_expenditureEntities.size() - 1).getDate() + 1;
        }
        else
        {
            _uid = _date;
        }

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

    public void addExpenditure()
    {
        String category = DayViewActivity.getCategories()[DayViewActivity.getCategories().length-1];
        ExpenditureEntity exp = new ExpenditureEntity(_uid++, 0, 0, category);
        _db.expenditureDao().insertAll(exp);

        addExpenditure(exp, true);
    }

    public void addExpenditure(float cost, String category)
    {
        addExpenditure(new ExpenditureEntity(_uid++, 0, cost, category), false);
    }

    public void addExpenditure(ExpenditureEntity exp, boolean empty)
    {
        _expenditureEntities.add(exp);

        if (_visible)
        {
            addExpenditureView(exp, (_expenditureEntities.size()-1), empty);
        }
    }

    private void setUpExpenditures()
    {
        int count = _expenditureEntities.size();

        for (int i = 0; i < count; i++)
        {
            ExpenditureEntity exp = _expenditureEntities.get(i);

            addExpenditureView(exp, i, false);
        }
    }

    private void addExpenditureView(ExpenditureEntity exp, int index, boolean empty)
    {
        View view = getLayoutInflater().inflate(R.layout.item, null);
        view.setId(index);

        final Spinner currSpinner = view.findViewById(R.id.currencySelector);
        currSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
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
        });

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, DayViewActivity.getCategories());
        final Spinner catSpinner = view.findViewById(R.id.itemCategorySelector);
        catSpinner.setAdapter(spinnerArrayAdapter);
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
        });

        final EditText costText = view.findViewById(R.id.valueEditText);
        costText.setFilters(new InputFilter[]{new MoneyValueFilter()});
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
                    _expenditureEntities.get(id).setAmount(Float.parseFloat(s.toString()));
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
        });

        if (!empty)
        {
            String cost = String.format("%.02f", exp.getAmount());
            costText.setText("" + cost);
        }
        else { }

        Button removeButton = view.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ViewGroup parent = ((ViewGroup) (v.getParent()));
                ViewGroup pParent = ((ViewGroup) (parent.getParent()));
                pParent.removeView(parent);
                ExpenditureEntity exp = _expenditureEntities.remove(parent.getId());
                _db.expenditureDao().delete(exp);
                // Rename all the views
                int count = pParent.getChildCount();
                for (int i = 0; i < count; i++)
                {
                    pParent.getChildAt(i).setId(i);
                }
            }
        });

        int cats = DayViewActivity.getCategories().length;
        boolean set = false;
        for (int j = 0; j < cats; j++)
        {
            if (exp.getExpenseType().equals(DayViewActivity.getCategories()[j]))
            {
                catSpinner.setSelection(j);
                set = true;
                break;
            }
            else { }
        }
        if (!set)
        {
            exp.setExpenseType(DayViewActivity.getCategories()[cats-1]);
            catSpinner.setSelection(cats-1);
        }
        else { }

        _itemsContainer.addView(view);
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
            addExpenditure(expenditures.get(i), false);
        }
    }
}

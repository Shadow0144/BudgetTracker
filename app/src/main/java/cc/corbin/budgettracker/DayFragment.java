package cc.corbin.budgettracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Corbin on 1/26/2018.
 */

public class DayFragment extends Fragment
{
    private final String TAG = "DayFragment";

    public final static String DATE_INTENT = "Date";

    private LinearLayout _itemsContainer;

    private ArrayList<Expenditure> _expenditures;

    private DayFragmentPagerAdapter _parent;

    private int _year;
    private int _month;
    private int _day;

    private boolean _visible;

    public void setParameters(DayFragmentPagerAdapter parent, int year, int month, int day)
    {
        _parent = parent;
        _year = year;
        _month = month;
        _day = day;

        _expenditures = new ArrayList<Expenditure>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_day, container, false);

        _itemsContainer = v.findViewById(R.id.itemsContainer);

        _visible = true;

        setUpExpenditures();

        return v;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        _visible = false;
    }

    public void addExpenditure()
    {
        addExpenditure(new Expenditure());
    }

    public void addExpenditure(int cost, String category)
    {
        addExpenditure(new Expenditure(cost, category));
    }

    public void addExpenditure(Expenditure exp)
    {
        _expenditures.add(exp);

        if (_visible)
        {
            addExpenditureView(exp, (_expenditures.size()-1));
        }
    }

    private void setUpExpenditures()
    {
        int count = _expenditures.size();

        for (int i = 0; i < count; i++)
        {
            Expenditure exp = _expenditures.get(i);

            addExpenditureView(exp, i);
        }
    }

    private void addExpenditureView(Expenditure exp, int index)
    {
        View view = getLayoutInflater().inflate(R.layout.item, null);
        view.setId(index);

        // Add listener for currency changed

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, DayViewActivity.getCategories());
        Spinner catSpinner = view.findViewById(R.id.itemCategorySelector);
        catSpinner.setAdapter(spinnerArrayAdapter);
        // Add listener for item changed

        // Add listener for cost changed

        Button removeButton = view.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ViewGroup parent = ((ViewGroup) (v.getParent()));
                ((ViewGroup) (parent.getParent())).removeView(parent);
                _expenditures.remove(parent.getId());
            }
        });

        int cats = DayViewActivity.getCategories().length;
        boolean set = false;
        for (int j = 0; j < cats; j++)
        {
            if (exp.category.equals(DayViewActivity.getCategories()[j]))
            {
                catSpinner.setSelection(j);
                set = true;
                break;
            }
            else { }
        }
        if (!set)
        {
            exp.category = DayViewActivity.getCategories()[cats-1];
            catSpinner.setSelection(cats-1);
        }
        else { }

        _itemsContainer.addView(view);
    }

    public ArrayList<Expenditure> getExpenditures()
    {
        return _expenditures;
    }

    public void setExpenditures(ArrayList<Expenditure> expenditures)
    {
        int count = expenditures.size();
        for (int i = 0; i < count; i++)
        {
            addExpenditure(expenditures.get(i));
        }
    }
}

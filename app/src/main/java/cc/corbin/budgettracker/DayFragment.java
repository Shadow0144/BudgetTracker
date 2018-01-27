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

        _expenditures = new ArrayList<Expenditure>();

        return v;
    }

    public void addExpenditure()
    {
        View view = getLayoutInflater().inflate(R.layout.item, null);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, DayViewActivity.getCategories());
        ((Spinner)(view.findViewById(R.id.itemCategorySelector))).setAdapter(spinnerArrayAdapter);

        Button removeButton = view.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((ViewGroup)(v.getParent().getParent())).removeView(((ViewGroup)(v.getParent())));
            }
        });

        _expenditures.add(new Expenditure());

        _itemsContainer.addView(view);
    }

    private void loadDaysEvents()
    {

    }
}

package cc.corbin.budgettracker.day;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.List;
import java.util.Set;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.R;

/**
 * Created by Corbin on 1/26/2018.
 */

public class DayFragment extends Fragment
{
    private final String TAG = "DayFragment";

    private LinearLayout _itemsContainer;

    private List<ExpenditureEntity> _expenditureEntities;
    private MutableLiveData<List<ExpenditureEntity>> _entities;
    private ExpenditureViewModel _viewModel;

    private FrameLayout _progressFrame;
    private FrameLayout _dayFragmentFrame;

    private DayFragmentPagerAdapter _parent;
    private View _view;

    private int _year;
    private int _month;
    private int _day;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
         _view = inflater.inflate(R.layout.fragment_day, container, false);

         _progressFrame = _view.findViewById(R.id.progressFrame);
         _dayFragmentFrame = _view.findViewById(R.id.dayFragmentFrame);

        _viewModel = ViewModelProviders.of(getActivity()).get(ExpenditureViewModel.class);
        _entities = new MutableLiveData<List<ExpenditureEntity>>();

        final Observer<List<ExpenditureEntity>> entityObserver = new Observer<List<ExpenditureEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<ExpenditureEntity> expenditureEntities)
            {
                // Null check in the method
                onLoadExpenses(expenditureEntities);
            }
        };
        _entities.observe(this, entityObserver);

        // Make the call to getDay here if this occurs last
        if (_parent != null)
        {
            _viewModel.getDay(_entities, _year, _month, _day);
        }
        else { }

        return _view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    public void setParameters(DayFragmentPagerAdapter parent, int year, int month, int day)
    {
        _parent = parent;
        _year = year;
        _month = month;
        _day = day;

        // Make the call to getDay here if this occurs last
        if (_entities != null)
        {
            _viewModel.getDay(_entities, _year, _month, _day);
        }
        else { }
    }

    public void refreshView()
    {
        _dayFragmentFrame.removeAllViews();
        _progressFrame.setVisibility(FrameLayout.VISIBLE);
        _viewModel.getDay(_entities, _year, _month, _day);
    }

    private void onLoadExpenses(@Nullable List<ExpenditureEntity> expenditureEntities)
    {
        if (expenditureEntities != null)
        {
            _expenditureEntities = expenditureEntities;

            _progressFrame.setVisibility(FrameLayout.INVISIBLE);

            LayoutInflater inflater = getLayoutInflater();
            View dayView = inflater.inflate(R.layout.day, _dayFragmentFrame, false);
            _dayFragmentFrame.addView(dayView);
            _itemsContainer = dayView.findViewById(R.id.itemsContainer);

            setUpExpenditures();
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

        // Ensure the parent has been set first
        if (_parent != null && count == 0) // Set the total to zero here if no expenditures are getting added
        {
            _parent.updateTotal(_day, 0.0f);
        }
        else { }
    }

    private void addExpenditureView(ExpenditureEntity exp, final int index)
    {
        final ExpenditureItem view = new ExpenditureItem(getContext(), exp);
        view.setTag(index);
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((DayViewActivity)getActivity()).editItem((int)v.getTag(), ((ExpenditureItem)v).getExpenditure());
            }
        });

        _itemsContainer.addView(view);

        _parent.updateTotal(_day, calculateTotal());
    }

    public float calculateTotal()
    {
        float total = 0.0f;
        if (_expenditureEntities != null)
        {
            int size = _expenditureEntities.size();
            for (int i = 0; i < size; i++)
            {
                total += _expenditureEntities.get(i).getAmount();
            }
        }
        else { }
        return total;
    }
}

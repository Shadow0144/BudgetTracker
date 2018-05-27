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

        _viewModel = ViewModelProviders.of(getActivity()).get(ExpenditureViewModel.class);
        _viewModel.setDate(_year, _month, _day);
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
            _viewModel.getDay(_entities);
        }
        else { }

        Log.e(TAG, "onCreate");

        return _view;
    }

    @Override
    public void onResume()
    {
        if (SettingsActivity.dayNeedsUpdating > 0)
        {
            refreshView();
            SettingsActivity.dayNeedsUpdating--;
        }
        else { }
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
            _viewModel.getDay(_entities);
        }
        else { }
    }

    public void refreshView()
    {
        FrameLayout containerFrame = _view.findViewById(R.id.dayFrame);
        if (containerFrame != null) // While refreshing, remove everything and replace it with a loading bar
        {
            ConstraintLayout rootLayout = ((ConstraintLayout) containerFrame.getParent());
            rootLayout.removeView(containerFrame);

            LayoutInflater inflater = getLayoutInflater();
            View progressFrame = inflater.inflate(R.layout.progress_frame, rootLayout, false);
            rootLayout.addView(progressFrame);
        }
        else { }

        Log.e(TAG, "Day: " + _day + " Entities: " + _entities.hasActiveObservers() + " " + _entities.hasObservers());
        _viewModel.setDate(_year, _month, _day);
        _viewModel.getDay(_entities);
    }

    public void onLoadExpenses(@Nullable List<ExpenditureEntity> expenditureEntities)
    {
        Log.e(TAG, "Day: " + _day + " Loaded: " + (expenditureEntities != null));
        if (expenditureEntities != null)
        {
            _expenditureEntities = expenditureEntities;

            ConstraintLayout rootLayout;

            FrameLayout containerFrame = _view.findViewById(R.id.progressFrame);
            if (containerFrame == null)
            {
                containerFrame = _view.findViewById(R.id.dayFrame);
            }
            else { }
            rootLayout = ((ConstraintLayout) containerFrame.getParent());
            rootLayout.removeView(containerFrame);

            LayoutInflater inflater = getLayoutInflater();
            View dayView = inflater.inflate(R.layout.day, rootLayout, false);
            rootLayout.addView(dayView);

            _itemsContainer = dayView.findViewById(R.id.itemsContainer);

            setUpExpenditures();

            unlock();
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

        if (count == 0) // Set the total to zero here if no expenditures are getting added
        {
            _parent.updateTotal(_day, 0.0f);
        }
        else { }
    }

    private void addExpenditureView(ExpenditureEntity exp, final int index)
    {
        final ExpenditureItem view = new ExpenditureItem(getContext(), exp);
        view.setId(index);

        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((DayViewActivity)getActivity()).editItem(v.getId(), ((ExpenditureItem)v).getExpenditure());
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

    public void lock()
    {
        Log.e(TAG, "Lock");
        _itemsContainer.setEnabled(false);
        _view.findViewById(R.id.newProgressFrame).setVisibility(View.VISIBLE);
    }

    public void unlock()
    {
        Log.e(TAG, "Unlock");
        _itemsContainer.setEnabled(true);
        _view.findViewById(R.id.newProgressFrame).setVisibility(View.GONE);
    }
}

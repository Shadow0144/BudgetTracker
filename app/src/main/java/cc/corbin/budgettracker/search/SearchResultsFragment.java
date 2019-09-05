package cc.corbin.budgettracker.search;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.day.ExpenditureItem;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

public class SearchResultsFragment extends Fragment
{
    private final String TAG = "SearchResultsFragment";

    private LinearLayout _resultsContainer;

    private ProgressBar _progressBar;

    private TextView _totalTextView;
    private TextView _searchResultsTextView;

    private boolean _groupMode;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _expeditures;
    private List<ExpenditureEntity> _expenditureEntities;
    private MutableLiveData<List<BudgetEntity>> _budgets;
    private List<BudgetEntity> _budgetEntities;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved)
    {
        return inflater.inflate(R.layout.fragment_search_results, group);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        _viewModel = ExpenditureViewModel.getInstance();

        _progressBar = view.findViewById(R.id.searchProgressBar);
        _resultsContainer = view.findViewById(R.id.resultsLinearLayout);
        _totalTextView = view.findViewById(R.id.totalTextView);
        _searchResultsTextView = view.findViewById(R.id.searchResultsTextView);

        _progressBar.setVisibility(View.VISIBLE);
        _totalTextView.setText(getTotalString(0));

        _expeditures = new MutableLiveData<List<ExpenditureEntity>>();
        final Observer<List<ExpenditureEntity>> expenditureEntitiesObserver = new Observer<List<ExpenditureEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<ExpenditureEntity> expenditureEntities)
            {
                // List the results
                displayExpenditures(expenditureEntities);
            }
        };
        _expeditures.observe(this, expenditureEntitiesObserver);

        _groupMode = false;
    }

    public void enableGroupActivityMode()
    {
        _totalTextView.setVisibility(View.GONE);
        _searchResultsTextView.setVisibility(View.VISIBLE);
        _progressBar.setVisibility(View.GONE);
        _groupMode = true;
    }

    public void runQuery()
    {
        runQuery(getActivity().getIntent());
    }

    public void runQuery(Intent intent)
    {
        String query = new SearchHelper(intent).getQuery();
        runQuery(query);
    }

    public void runQuery(String query)
    {
        _resultsContainer.removeAllViews();
        _progressBar.setVisibility(View.VISIBLE);
        _totalTextView.setText(getTotalString(0));
        _viewModel.customExpenditureQuery(_expeditures, query);
    }

    private void displayExpenditures(List<ExpenditureEntity> expenditureEntities)
    {
        _progressBar.setVisibility(View.GONE);
        _resultsContainer.removeAllViews();
        float total = 0.0f;
        if (expenditureEntities != null)
        {
            _expenditureEntities = expenditureEntities;
            int count = _expenditureEntities.size();
            for (int i = 0; i < count; i++)
            {
                ExpenditureEntity exp = _expenditureEntities.get(i);
                total += exp.getAmount();
                final ExpenditureItem view = new ExpenditureItem(getActivity(), exp);
                view.setCheckable(_groupMode);
                // Add an listener to jump to the date of the item
                view.setTag(exp);
                if (_groupMode)
                {
                    view.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            ((ExpenditureItem)view).toggleChecked();
                        }
                    });
                }
                else
                {
                    view.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            jumpToExpDate((ExpenditureEntity) view.getTag());
                        }
                    });
                }
                _resultsContainer.addView(view);
            }
        }
        else { }
        _totalTextView.setText(getTotalString(total));
    }

    private String getTotalString(float total)
    {
        String totalString = getString(R.string.total_colon) + " " +
                Currencies.formatCurrency(Currencies.default_currency, total);
        return totalString;
    }

    private void jumpToExpDate(ExpenditureEntity exp)
    {
        if (exp.getDay() != 0)
        {
            Intent intent = new Intent(getActivity(), DayViewActivity.class);
            Calendar calendar = Calendar.getInstance();
            calendar.set(exp.getYear(), exp.getMonth() - 1, exp.getDay());
            intent.putExtra(DayViewActivity.YEAR_INTENT, exp.getYear());
            intent.putExtra(DayViewActivity.MONTH_INTENT, exp.getMonth());
            intent.putExtra(DayViewActivity.DAY_INTENT, exp.getDay());
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(getActivity(), MonthViewActivity.class);
            intent.putExtra(MonthViewActivity.YEAR_INTENT, exp.getYear());
            intent.putExtra(MonthViewActivity.MONTH_INTENT, exp.getMonth());
            startActivity(intent);
        }
    }

    private void jumpToBudDate(BudgetEntity bud)
    {

    }
}

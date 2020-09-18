package cc.corbin.budgettracker.day;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.paging.PagingActivity;
import cc.corbin.budgettracker.paging.PagingView;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

public class DayView extends PagingView
{
    private final static String TAG = "DayView";

    private final int TOTAL_TEXT_PADDING = 12;
    private final int TOTAL_TEXT_SIZE = 18;

    private final int PAGE_MARGIN = 4;
    private final int PROGRESS_BAR_MARGIN = 36;

    private List<ExpenditureEntity> _expenditureEntities;
    private MutableLiveData<List<ExpenditureEntity>> _entities;
    private ExpenditureViewModel _viewModel;

    protected TextView _totalTextView;
    protected ScrollView _dayListHolder;
    protected ProgressBar _progressBar;
    protected LinearLayout _expHolder;

    public DayView(Context context, PagingActivity activity)
    {
        super(context, activity);
        _context = context;
        _activity = activity;

        ignoreMonth = false;
        ignoreDay = false;

        setupTotalTextView();

        setupDayListHolder();

        _viewModel = ExpenditureViewModel.getInstance();
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
        _entities.observe((FragmentActivity)_context, entityObserver);
    }

    public void setDate(int year, int month, int day)
    {
        super.setDate(year, month, day);

        _expHolder.removeAllViews();
        _dayListHolder.removeView(_expHolder);
        _dayListHolder.addView(_progressBar);

        _viewModel.getDay(_entities, year, month, day);
    }

    private void onLoadExpenses(@Nullable List<ExpenditureEntity> expenditureEntities)
    {
        if (expenditureEntities != null)
        {
            _expenditureEntities = expenditureEntities;

            setUpExpenditures();
        }
        else { }
    }

    private void setupTotalTextView()
    {
        _totalTextView = new TextView(_context);
        _totalTextView.setText(R.string.total);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(1, 1, 1, 1);
        _totalTextView.setLayoutParams(params);
        _totalTextView.setPadding(0, TOTAL_TEXT_PADDING, 0, TOTAL_TEXT_PADDING);
        _totalTextView.setBackgroundColor(Color.WHITE);
        _totalTextView.setTextSize(TOTAL_TEXT_SIZE);
        _totalTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        addView(_totalTextView);
    }

    private void setupDayListHolder()
    {
        LayoutParams params;

        _dayListHolder = new ScrollView(_context);
        params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(PAGE_MARGIN, 0, PAGE_MARGIN, 0);
        _dayListHolder.setLayoutParams(params);
        _dayListHolder.setBackgroundColor(Color.WHITE);
        addView(_dayListHolder);

        params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, PROGRESS_BAR_MARGIN, 0, 0);
        _progressBar = new ProgressBar(_context);
        _progressBar.setLayoutParams(params);

        _expHolder = new LinearLayout(_context);
        _expHolder.setOrientation(LinearLayout.VERTICAL);
        params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        _expHolder.setLayoutParams(params);
    }

    private void setUpExpenditures()
    {
        int count = _expenditureEntities.size();

        for (int i = 0; i < count; i++)
        {
            ExpenditureEntity exp = _expenditureEntities.get(i);

            addExpenditureView(exp, i);
        }

        _dayListHolder.removeView(_progressBar);
        _dayListHolder.addView(_expHolder);
        _totalTextView.setText(getTotalString());
    }

    private void addExpenditureView(ExpenditureEntity exp, final int index)
    {
        final ExpenditureItem view = new ExpenditureItem(getContext(), exp);
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((DayViewActivity)_context).editExpenditure(((ExpenditureEntity)v.getTag()));
            }
        });
        _expHolder.addView(view);
    }

    private String getTotalString()
    {
        String totalString = _context.getString(R.string.total_colon) + " " +
                Currencies.formatCurrency(Currencies.default_currency, getCurrentTotal());
        return totalString;
    }

    public float getCurrentTotal()
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

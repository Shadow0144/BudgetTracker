package cc.corbin.budgettracker.day;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

// An individual day's worth of expenditures
public class DayList extends LinearLayout
{
    private final static String TAG = "DayList";

    private final int PAGE_MARGIN = 4;
    private final int PROGRESS_BAR_MARGIN = 36;

    private TextView _dateTextView;
    private TextView _totalTextView;
    private ScrollView _dayListHolder;
    private ProgressBar _progressBar;
    private LinearLayout _expHolder;

    private List<ExpenditureEntity> _expenditureEntities;
    private MutableLiveData<List<ExpenditureEntity>> _entities;
    private ExpenditureViewModel _viewModel;

    private Context _context;

    private Calendar _date;

    public DayList(Context context)
    {
        super(context);
        _context = context;

        setOrientation(VERTICAL);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(params);

        setupDateTextView();

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
        _expHolder.removeAllViews();
        _dayListHolder.removeView(_expHolder);
        _dayListHolder.addView(_progressBar);
        _date = Calendar.getInstance();
        _date.set(Calendar.YEAR, year);
        _date.set(Calendar.MONTH, month-1);
        _date.set(Calendar.DATE, day);
        _dateTextView.setText(getDateString());
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

    private void setupDateTextView()
    {
        _dateTextView = new TextView(_context);
        _dateTextView.setText("");
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(1, 1, 1, 0);
        _dateTextView.setLayoutParams(params);
        _dateTextView.setPadding(0, 24, 0, 36);
        _dateTextView.setBackgroundColor(_context.getColor(R.color.colorPrimaryDark));
        _dateTextView.setTextColor(Color.WHITE);
        _dateTextView.setTextSize(24);
        _dateTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        _dateTextView.setTypeface(_dateTextView.getTypeface(), Typeface.BOLD);
        addView(_dateTextView);
        _dateTextView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((DayViewActivity)_context).moveToMonthView(null);
            }
        });
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
        _totalTextView.setPadding(0, 12, 0, 12);
        _totalTextView.setBackgroundColor(Color.WHITE);
        _totalTextView.setTextSize(18);
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
        view.setTag(index);
        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((DayViewActivity)_context).editItem((int)v.getTag(), ((ExpenditureItem)v).getExpenditure());
            }
        });
        _expHolder.addView(view);
    }

    private String getDateString()
    {
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy/MM/dd");
        String dateString = simpleDate.format(getDate().getTime());
        return dateString;
    }

    private String getTotalString()
    {
        String totalString = _context.getString(R.string.total_colon) + " " +
                Currencies.formatCurrency(Currencies.default_currency, getCurrentTotal());
        return totalString;
    }

    public Calendar getDate()
    {
        return _date;
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

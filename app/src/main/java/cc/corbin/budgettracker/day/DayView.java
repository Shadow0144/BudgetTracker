package cc.corbin.budgettracker.day;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.PagingActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

public class DayView extends LinearLayout
{
    private final static String TAG = "DayView";

    private final int PAGE_MARGIN = 4;
    private final int PROGRESS_BAR_MARGIN = 36;
    private final int TOTAL_TEXT_PADDING = 12;

    private final int TOTAL_TEXT_SIZE = 18;

    private TextView _dateTextView;
    private TextView _totalTextView;
    private ScrollView _dayListHolder;
    private ProgressBar _progressBar;
    private LinearLayout _expHolder;
    private Button _todayLeftButton;
    private Button _todayRightButton;

    private List<ExpenditureEntity> _expenditureEntities;
    private MutableLiveData<List<ExpenditureEntity>> _entities;
    private ExpenditureViewModel _viewModel;

    private Context _context;
    private PagingActivity _activity;

    private Calendar _date;

    public DayView(Context context, PagingActivity activity)
    {
        super(context);
        _context = context;
        _activity = activity;

        setOrientation(VERTICAL);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(params);

        setupHeader();

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
        Calendar compare = ((Calendar)_date.clone());
        _date.set(Calendar.YEAR, year);
        _date.set(Calendar.MONTH, month-1);
        _date.set(Calendar.DATE, day);
        _dateTextView.setText(getDateString());

        if (compare.before(_date)) // Future
        {
            _activity.setHeaderColor(_context.getColor(R.color.colorPrimaryLight));
            _activity.showLeftCurrentButton();
            _activity.hideRightCurrentButton();
        }
        else if (compare.after(_date)) // Past
        {
            _activity.setHeaderColor(_context.getColor(R.color.colorPrimaryVeryDark));
            _activity.hideLeftCurrentButton();
            _activity.showRightCurrentButton();
        }
        else // Present
        {
            _activity.setHeaderColor(_context.getColor(R.color.colorPrimaryDark));
            _activity.hideLeftCurrentButton();
            _activity.hideRightCurrentButton();
        }

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

    private void setupHeader()
    {
        _dateTextView = _activity.findViewById(R.id.dateTextView);
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
                ((DayViewActivity)_context).editItem(((ExpenditureEntity)v.getTag()));
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

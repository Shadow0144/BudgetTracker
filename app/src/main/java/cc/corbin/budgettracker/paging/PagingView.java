package cc.corbin.budgettracker.paging;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import cc.corbin.budgettracker.R;

// Base class for any layout for use by a PagingViewHolder
public abstract class PagingView extends LinearLayout
{
    private final static String TAG = "PagingView";

    private final int DATE_TEXT_TOP_PADDING = 24;
    private final int DATE_TEXT_BOT_PADDING = 36;
    private final int DATE_TEXT_SIZE = 24;

    protected TextView _dateTextView;

    protected Context _context;
    protected PagingActivity _activity;

    protected LocalDate _date;
    protected int _day;
    protected int _month;
    protected int _year;
    protected boolean ignoreMonth;
    protected boolean ignoreDay;

    public PagingView(Context context, PagingActivity activity)
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
    }

    public void setDate(int year, int month, int day)
    {
        _year = year;
        _month = month;
        _day = day;

        // Provide the correct header text
        // Set the month and day to the current time when necessary so that we can compare if it is in the present
        if (!ignoreDay)
        {
            _date = LocalDate.of(_year, _month, _day);
            _dateTextView.setText(getDayString());
        }
        else if (!ignoreMonth)
        {
            _date = LocalDate.of(_year, _month, LocalDate.now().getDayOfMonth());
            _dateTextView.setText(getMonthString());
        }
        else
        {
            _date = LocalDate.of(_year, LocalDate.now().getMonth(), LocalDate.now().getDayOfMonth());
            _dateTextView.setText(getYearString());
        }

        if (_date.isAfter(LocalDate.now())) // Future
        {
            _dateTextView.setBackgroundColor(_context.getColor(R.color.colorPrimaryLight));
        }
        else if (_date.isBefore(LocalDate.now())) // Past
        {
            _dateTextView.setBackgroundColor(_context.getColor(R.color.colorPrimaryVeryDark));
        }
        else // Present
        {
            _dateTextView.setBackgroundColor(_context.getColor(R.color.colorPrimaryDark));
        }
    }

    protected void setupHeader()
    {
        _dateTextView = new TextView(_context);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(1, 1, 1, 0);
        _dateTextView.setLayoutParams(params);
        _dateTextView.setPadding(0, DATE_TEXT_TOP_PADDING, 0, DATE_TEXT_BOT_PADDING);
        _dateTextView.setTextColor(Color.WHITE);
        _dateTextView.setTextSize(DATE_TEXT_SIZE);
        _dateTextView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        _dateTextView.setTypeface(_dateTextView.getTypeface(), Typeface.BOLD);
        addView(_dateTextView);
    }

    protected String getDayString()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String dateString = getDate().format(formatter);
        return dateString;
    }

    protected String getMonthString()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
        String dateString = getDate().format(formatter);
        return dateString;
    }

    protected String getYearString()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        String dateString = getDate().format(formatter);
        return dateString;
    }

    public int getYear()
    {
        return _year;
    }

    public int getMonth()
    {
        return _month;
    }

    public int getDay()
    {
        return _day;
    }

    public boolean getIgnoreMonth()
    {
        return ignoreMonth;
    }

    public boolean getIgnoreDay()
    {
        return ignoreDay;
    }

    public LocalDate getDate()
    {
        return _date;
    }
}

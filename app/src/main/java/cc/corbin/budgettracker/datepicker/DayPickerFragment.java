package cc.corbin.budgettracker.datepicker;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.time.LocalDate;
import java.text.DateFormatSymbols;

import cc.corbin.budgettracker.R;

// Fragment for selecting a specific day from a calender
public class DayPickerFragment extends DialogFragment
{
    private final String TAG = "DayPickerFragment";

    // Selected date
    private int _selectedYear;
    private int _selectedMonth;
    private int _selectedDay;
    private int _selectedIndex;

    // Displayed month and year
    private int _displayedYear;
    private int _displayedMonth;

    private int _firstDay;

    private View _calendarView;

    private int _lightHeaderColor;
    private int _defaultHeaderColor;
    private int _darkHeaderColor;

    private LinearLayout _headerLayout;
    private TextView _monthYearTextView;
    private TextView[] _dayTextViews;
    private TableRow _week6Row;

    private int _highlightedIndex;
    private int _defaultTextColor;
    private int _highlightColor;

    private int _todayTextColor;

    private boolean _viewCreated;
    private boolean _dateSet;

    private MutableLiveData<LocalDate> _date;

    private View.OnClickListener _dayClickListener;

    public DayPickerFragment()
    {
        _viewCreated = false;
        _dateSet = false;
        _highlightedIndex = -1;
        _selectedYear = -1;
        _selectedMonth = -1;
        _selectedDay = -1;
        _selectedIndex = -1;
    }

    public void setDate(int year, int month)
    {
        _displayedYear = year;
        _displayedMonth = month;

        if (_viewCreated)
        {
            updateCalendar();
        }
        else { }
        _dateSet = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        _calendarView = inflater.inflate(R.layout.fragment_day_select, container);

        Context context = getContext();
        _todayTextColor = context.getColor(R.color.colorPrimaryVeryDark);
        _lightHeaderColor = context.getColor(R.color.colorPrimaryLight);
        _defaultHeaderColor = context.getColor(R.color.colorPrimaryDark);
        _darkHeaderColor = context.getColor(R.color.colorPrimaryVeryDark);
        _highlightColor = context.getColor(R.color.colorPrimaryLight);
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(new int[] { android.R.attr.textColorSecondary });
        _defaultTextColor = typedArray.getColor(0, 0);
        typedArray.recycle();

        _headerLayout = _calendarView.findViewById(R.id.headerLayout);

        _monthYearTextView = _calendarView.findViewById(R.id.monthYearTextView);

        _calendarView.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onCancel(v);
            }
        });

        _calendarView.findViewById(R.id.acceptButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onAccept(v);
            }
        });

        _calendarView.findViewById(R.id.previousMonthButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                previousMonth(v);
            }
        });

        _calendarView.findViewById(R.id.nextMonthButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                nextMonth(v);
            }
        });

        _calendarView.findViewById(R.id.monthYearTextView).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                moveToMonthView(v);
            }
        });

        _week6Row = _calendarView.findViewById(R.id.week6Row);

        setupDayTextViews();

        _viewCreated = true;
        if (_dateSet)
        {
            updateCalendar();
        }
        else { }

        return _calendarView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setLiveData(MutableLiveData<LocalDate> date)
    {
        _date = date;
    }

    private void updateCalendar()
    {
        // Color the header
        LocalDate date = LocalDate.of(_displayedYear, _displayedMonth, LocalDate.now().getDayOfMonth());
        LocalDate now = LocalDate.now();
        if (date.isAfter(now)) // Future
        {
            _headerLayout.setBackgroundColor(_lightHeaderColor);
        }
        else if (date.isBefore(now)) // Past
        {
            _headerLayout.setBackgroundColor(_darkHeaderColor);
        }
        else // Present
        {
            _headerLayout.setBackgroundColor(_defaultHeaderColor);
        }

        // Setup the header
        _monthYearTextView.setText(new DateFormatSymbols().getMonths()[_displayedMonth-1] + " " + _displayedYear);

        // Clear the selected day TextView
        if (_highlightedIndex > -1)
        {
            _dayTextViews[_highlightedIndex].setTextColor(_defaultTextColor);
        }
        else { }

        // Find the first day
        LocalDate start = LocalDate.of(_displayedYear, _displayedMonth, 1);
        int length = start.lengthOfMonth();
        _firstDay = start.getDayOfWeek().getValue() % 6; // Monday = 1, Sunday = 7 -> Sunday = 0, Monday = 1

        // Color the text of today if it is visible
        if (_displayedYear == now.getYear() && _displayedMonth == now.getMonthValue())
        {
            _highlightedIndex = _firstDay + now.getDayOfMonth() - 1;
            _dayTextViews[_highlightedIndex].setTextColor(_todayTextColor);
        }
        else { }

        // Color and set the text for every TextView
        int day = 1;
        for (int i = 0; i < 42; i++)
        {
            if (i >= _firstDay && day <= length)
            {
                _dayTextViews[i].setBackgroundColor(Color.WHITE);
                _dayTextViews[i].setEnabled(true);
                _dayTextViews[i].setText("" + day);
                day++;
            }
            else
            {
                _dayTextViews[i].setBackgroundColor(Color.LTGRAY);
                _dayTextViews[i].setEnabled(false);
                _dayTextViews[i].setText("");
            }
        }

        // Toggle the visibility of the last row
        if (_firstDay + length > 35)
        {
            _week6Row.setVisibility(View.VISIBLE);
        }
        else
        {
            _week6Row.setVisibility(View.INVISIBLE);
        }

        // Highlight the selected day
        if (_selectedYear == _displayedYear && _selectedMonth == _displayedMonth && _selectedIndex > -1)
        {
            _dayTextViews[_selectedIndex].setBackgroundColor(_highlightColor);
        }
        else { }
    }

    public void onDaySet(int year, int month, int day)
    {
        _selectedYear = year;
        _selectedMonth = month;
        _selectedDay = day;
        if (_selectedIndex > -1)
        {
            _dayTextViews[_selectedIndex].setBackgroundColor(Color.WHITE);
        }
        else { }
        _selectedIndex = _firstDay + _selectedDay - 1;
        _dayTextViews[_selectedIndex].setBackgroundColor(_highlightColor);
    }

    public void previousMonth(View v)
    {
        _displayedMonth--;
        if (_displayedMonth == 0)
        {
            _displayedYear--;
            _displayedMonth = 12;
        }
        else { }
        updateCalendar();
    }

    public void nextMonth(View v)
    {
        _displayedMonth++;
        if (_displayedMonth == 13)
        {
            _displayedYear++;
            _displayedMonth = 1;
        }
        else { }
        updateCalendar();
    }

    public void moveToMonthView(View v)
    {

    }

    public void onCancel(View v)
    {
        dismiss();
    }

    public void onAccept(View v)
    {
        if (_date != null)
        {
            LocalDate c = LocalDate.of(_selectedYear, _selectedMonth, _selectedDay);
            _date.postValue(c);
        }
        else { }
        dismiss();
    }

    private void setupDayTextViews()
    {
        _dayTextViews = new TextView[42];
        _dayTextViews[0] = _calendarView.findViewById(R.id.day1TextView);
        _dayTextViews[1] = _calendarView.findViewById(R.id.day2TextView);
        _dayTextViews[2] = _calendarView.findViewById(R.id.day3TextView);
        _dayTextViews[3] = _calendarView.findViewById(R.id.day4TextView);
        _dayTextViews[4] = _calendarView.findViewById(R.id.day5TextView);
        _dayTextViews[5] = _calendarView.findViewById(R.id.day6TextView);
        _dayTextViews[6] = _calendarView.findViewById(R.id.day7TextView);
        _dayTextViews[7] = _calendarView.findViewById(R.id.day8TextView);
        _dayTextViews[8] = _calendarView.findViewById(R.id.day9TextView);
        _dayTextViews[9] = _calendarView.findViewById(R.id.day10TextView);
        _dayTextViews[10] = _calendarView.findViewById(R.id.day11TextView);
        _dayTextViews[11] = _calendarView.findViewById(R.id.day12TextView);
        _dayTextViews[12] = _calendarView.findViewById(R.id.day13TextView);
        _dayTextViews[13] = _calendarView.findViewById(R.id.day14TextView);
        _dayTextViews[14] = _calendarView.findViewById(R.id.day15TextView);
        _dayTextViews[15] = _calendarView.findViewById(R.id.day16TextView);
        _dayTextViews[16] = _calendarView.findViewById(R.id.day17TextView);
        _dayTextViews[17] = _calendarView.findViewById(R.id.day18TextView);
        _dayTextViews[18] = _calendarView.findViewById(R.id.day19TextView);
        _dayTextViews[19] = _calendarView.findViewById(R.id.day20TextView);
        _dayTextViews[20] = _calendarView.findViewById(R.id.day21TextView);
        _dayTextViews[21] = _calendarView.findViewById(R.id.day22TextView);
        _dayTextViews[22] = _calendarView.findViewById(R.id.day23TextView);
        _dayTextViews[23] = _calendarView.findViewById(R.id.day24TextView);
        _dayTextViews[24] = _calendarView.findViewById(R.id.day25TextView);
        _dayTextViews[25] = _calendarView.findViewById(R.id.day26TextView);
        _dayTextViews[26] = _calendarView.findViewById(R.id.day27TextView);
        _dayTextViews[27] = _calendarView.findViewById(R.id.day28TextView);
        _dayTextViews[28] = _calendarView.findViewById(R.id.day29TextView);
        _dayTextViews[29] = _calendarView.findViewById(R.id.day30TextView);
        _dayTextViews[30] = _calendarView.findViewById(R.id.day31TextView);
        _dayTextViews[31] = _calendarView.findViewById(R.id.day32TextView);
        _dayTextViews[32] = _calendarView.findViewById(R.id.day33TextView);
        _dayTextViews[33] = _calendarView.findViewById(R.id.day34TextView);
        _dayTextViews[34] = _calendarView.findViewById(R.id.day35TextView);
        _dayTextViews[35] = _calendarView.findViewById(R.id.day36TextView);
        _dayTextViews[36] = _calendarView.findViewById(R.id.day37TextView);
        _dayTextViews[37] = _calendarView.findViewById(R.id.day38TextView);
        _dayTextViews[38] = _calendarView.findViewById(R.id.day39TextView);
        _dayTextViews[39] = _calendarView.findViewById(R.id.day40TextView);
        _dayTextViews[40] = _calendarView.findViewById(R.id.day41TextView);
        _dayTextViews[41] = _calendarView.findViewById(R.id.day42TextView);

        _dayClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onDaySet(_displayedYear, _displayedMonth, Integer.parseInt(v.getTag().toString()) - _firstDay);
            }
        };
        for (int i = 0; i < 42; i++)
        {
            _dayTextViews[i].setOnClickListener(_dayClickListener);
        }
    }
}

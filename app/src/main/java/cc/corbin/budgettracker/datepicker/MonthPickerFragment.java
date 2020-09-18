package cc.corbin.budgettracker.datepicker;

import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;

import cc.corbin.budgettracker.R;

// Fragment for selecting a specific month from a calendar
public class MonthPickerFragment extends DialogFragment
{
    private final String TAG = "MonthPickerFragment";

    private View _calendarView;

    private MutableLiveData<LocalDate> _date;
    private LocalDate _currentDate; // The current date the calendar is displaying

    public enum timeSpan
    {
        month,
        year,
        decade
    }
    private timeSpan _targetTimeSpan;
    private timeSpan _currentTimeSpan;

    private TextView[] _dayTextViews;
    private TextView[] _monthTextViews;
    private TextView[] _yearTextViews;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Check if it was set in the Bundle
        _targetTimeSpan = timeSpan.month;
        _currentTimeSpan = _targetTimeSpan;
        switch (_currentTimeSpan)
        {
            case month:
                _calendarView = inflater.inflate(R.layout.fragment_day_select, container, false);
                setupDayTextViews();
                break;
            case year:
                _calendarView = inflater.inflate(R.layout.fragment_day_select, container, false);
                setupMonthTextViews();
                break;
            case decade:
                _calendarView = inflater.inflate(R.layout.fragment_day_select, container, false);
                setupYearTextViews();
                break;
        }

        // Check if it was set in the Bundle
        _currentDate = LocalDate.now();

        updateDisplay();

        return _calendarView;
    }

    public void setDate(LocalDate date)
    {
        _currentDate = date;
        updateDisplay();
    }

    private void updateDisplay()
    {
        switch (_currentTimeSpan)
        {
            case month:
                updateMonthDisplay();
                break;
            case year:
                updateYearDisplay();
                break;
            case decade:
                updateDecadeDisplay();
                break;
        }
    }

    private void updateMonthDisplay()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
        String monthString = dateFormat.format(_currentDate);
        TextView monthTextView = _calendarView.findViewById(R.id.monthYearTextView);
        monthTextView.setText(monthString);

        // TODO
        /*LocalDate calendar = LocalDate.now();
        LocalDate previousMonth = LocalDate.of(Calendar.MONTH, calendar.getMonthValue()-1);
        calendar.setTime(_currentDate);
        int startDay = calendar.getFirstDayOfWeek();
        int endDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)+1; // Add one to fix the last date
        int previousEndDay = previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < 42; i++)
        {
            if (i <= startDay)
            {
                _dayTextViews[i].setText("" + (previousEndDay + i - startDay));
                _dayTextViews[i].setBackgroundColor(Color.LTGRAY);
            }
            else
            {
                if (i > endDay)
                {
                    _dayTextViews[i].setText("" + (i - endDay));
                    _dayTextViews[i].setBackgroundColor(Color.LTGRAY);
                }
                else
                {
                    _dayTextViews[i].setText("" + (i - startDay));
                }
            }
        }*/
    }

    private void updateYearDisplay()
    {

    }

    private void updateDecadeDisplay()
    {

    }

    public void setLiveData(MutableLiveData<LocalDate> date)
    {
        _date = date;
    }

    public void onDateSet(DatePicker view, int year, int month, int day)
    {
        if (_date != null)
        {
            LocalDate c = LocalDate.of(year, month, day);
            _date.postValue(c);
        }
        else { }
    }

    public void onDaySelect(View v)
    {
        Log.e(TAG, v.getTag().toString());
    }

    public void onMonthUp(View v)
    {

    }

    public void onMonthDown(View v)
    {

    }

    public void onMoveToYearView(View v)
    {

    }

    public void onMonthSelect(View v)
    {

    }

    public void onYearUp(View v)
    {

    }

    public void onYearDown(View v)
    {

    }

    public void onMoveToDecadeView(View v)
    {

    }

    public void onDecadeSelect(View v)
    {

    }

    public void onDecadeUp(View v)
    {

    }

    public void onDecadeDown(View v)
    {

    }

    public void onAccept(View v)
    {

    }

    public void onCancel(View v)
    {

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
    }

    private void setupMonthTextViews()
    {

    }

    private void setupYearTextViews()
    {

    }
}
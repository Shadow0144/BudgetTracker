package cc.corbin.budgettracker.auxilliary;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.time.LocalDate;

// Fragment for selecting a date (i.e. a day, month, and year) from a calender
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
{
    private final String TAG = "DatePickerFragment";

    private MutableLiveData<LocalDate> _date;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the current date as the default date in the picker
        final LocalDate c = LocalDate.now();
        int year = c.getYear();
        int month = c.getMonthValue();
        int day = c.getDayOfMonth();

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
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
}

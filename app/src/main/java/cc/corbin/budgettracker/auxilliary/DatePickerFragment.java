package cc.corbin.budgettracker.auxilliary;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.DatePicker;
import android.widget.FrameLayout;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
{
    private final String TAG = "DatePickerFragment";

    private MutableLiveData<Date> _date;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void setLiveData(MutableLiveData<Date> date)
    {
        _date = date;
    }

    public void onDateSet(DatePicker view, int year, int month, int day)
    {
        if (_date != null)
        {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            Date date = c.getTime();
            _date.postValue(date);
        }
        else { }
    }
}

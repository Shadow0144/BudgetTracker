package cc.corbin.budgettracker.year;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.time.LocalDate;

import cc.corbin.budgettracker.paging.PagingActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.R;

/**
 * Created by Corbin on 1/28/2018.
 */

public class YearViewActivity extends PagingActivity
{
    private final String TAG = "YearViewActivity";

    public final static String YEAR_INTENT = "Year";

    public final static int CREATE_EXT_EXPENDITURE = 0;
    public final static int EDIT_EXT_EXPENDITURE = 1;
    public final static int CREATE_ADJUSTMENT = 2;
    public final static int EDIT_ADJUSTMENT = 3;

    public final static int SUCCEED = 0;
    public final static int CANCEL = 1;
    public final static int DELETE = 2;
    public final static int FAILURE = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setup();

        int year = getIntent().getIntExtra(YEAR_INTENT, LocalDate.now().getYear());
        _currentDate = LocalDate.of(year, 1, 1);

        setupYearView();
    }

    private void setupYearView()
    {
        final YearRecyclerAdapter adapter = new YearRecyclerAdapter(this);
        setupAdapterView(adapter);
        int time = _currentDate.getYear();
        _recyclerView.scrollToPosition(time);
    }

    public void moveToTotalView(View v)
    {
        Intent intent = new Intent(getApplicationContext(), TotalViewActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == SettingsActivity.DATABASE_UPDATE_INTENT_FLAG)
        {
            _recyclerView.getAdapter().notifyDataSetChanged();
        }
        else if (requestCode == SettingsActivity.DATABASE_NO_UPDATE_INTENT_FLAG)
        {
            // Do nothing
        }
        else
        if (resultCode == FAILURE)
        {
            Toast toast = Toast.makeText(this, getString(R.string.failure_expense), Toast.LENGTH_LONG);
            toast.show();
        }
        else
        {
            if (requestCode == CREATE_EXT_EXPENDITURE || requestCode == CREATE_ADJUSTMENT ||
                    requestCode == EDIT_EXT_EXPENDITURE || requestCode == EDIT_ADJUSTMENT)
            {
                if (resultCode == SUCCEED || resultCode == DELETE)
                {
                    _recyclerView.getAdapter().notifyDataSetChanged();
                }
                else { }
            }
            else { }

            if (resultCode == CANCEL)
            {
                // Do nothing
            }
            else { }
        }
    }

    public void currentView(View v)
    {
        int time = _currentDate.getYear();
        _recyclerView.smoothScrollToPosition(time);
    }
}

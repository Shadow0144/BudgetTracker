package cc.corbin.budgettracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

/**
 * Created by Corbin on 1/28/2018.
 */

public class MonthViewActivity extends AppCompatActivity
{
    private final String TAG = "MonthViewActivity";

    private LinearLayout _monthsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view);

        _monthsContainer = findViewById(R.id.monthsLayout);
        MonthTable table = new MonthTable(this, 1, 2018);
        _monthsContainer.addView(table);
    }
}

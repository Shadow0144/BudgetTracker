package cc.corbin.budgettracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Corbin on 1/28/2018.
 */

public class MonthViewActivity extends AppCompatActivity
{
    private final String TAG = "MonthViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_view);
    }
}

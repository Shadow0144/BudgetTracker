package cc.corbin.budgettracker.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import cc.corbin.budgettracker.R;

public class SettingsActivity extends AppCompatActivity
{
    private final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}

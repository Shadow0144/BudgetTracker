package cc.corbin.budgettracker.setup;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.EnhancedViewPager;

public class SetupActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        EnhancedViewPager _setupViewPager = findViewById(R.id.setupViewPager);
        _setupViewPager.setAdapter(new SetupFragmentPagerAdapter(getSupportFragmentManager(), _setupViewPager));
        _setupViewPager.setFingerSwipingEnabled(false);
    }
}

package cc.corbin.budgettracker.navigation;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.settings.SettingsActivity;

// Activity with a navigation drawer, allowing access to other activities quickly
public abstract class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private final String TAG = "NavigationActivity";

    private DrawerLayout _drawerLayout;
    protected boolean _hasUpButton;
    protected MenuItem _upButton;

    protected void setup()
    {
        _hasUpButton = false; // Default to false
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try
        {
            ActionBar actionbar = getSupportActionBar();
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        catch (Exception e)
        {
            // Do nothing
        }
        _drawerLayout = findViewById(R.id.rootLayout);
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                _drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@Nullable MenuItem item)
    {
        Intent intent = NavigationDrawerHelper.handleNavigation(item);

        boolean handled = (intent != null);
        if (handled)
        {
            if (intent.hasExtra(SettingsActivity.SETTINGS_INTENT_FLAG))
            {
                startActivityForResult(intent, SettingsActivity.SETTINGS_REQUEST_CODE);
            }
            else
            {
                startActivity(intent);
            }
            _drawerLayout.closeDrawer(GravityCompat.START);
        }
        else { }

        return handled;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (_hasUpButton)
        {
            getMenuInflater().inflate(R.menu.nagivation_view, menu);
            _upButton = menu.getItem(0);
            setupUpButton();
        }
        else { }
        return true;
    }

    protected void setupUpButton()
    {
        // Override if needed
    }
}

package cc.corbin.budgettracker.custom;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.importexport.ImportExportActivity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.search.CreateSearchActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.year.YearViewActivity;

public class CreateCustomViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private final String TAG = "CreateCustomViewActivity";

    private DrawerLayout _drawerLayout;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _expenditures;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_custom_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        _drawerLayout = findViewById(R.id.rootLayout);
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        Calendar calendar = Calendar.getInstance();
        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(), BudgetDatabase.getBudgetDatabase());
        _viewModel.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DATE));
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
    public boolean onNavigationItemSelected(MenuItem item)
    {
        Intent intent;
        boolean handled = false;
        Calendar calendar = Calendar.getInstance();
        switch (item.getItemId())
        {
            case R.id.searchMenuItem:
                intent = new Intent(getApplicationContext(), CreateSearchActivity.class);
                startActivity(intent);
                handled = true;
                break;
            case R.id.dayMenuItem:
                intent = new Intent(getApplicationContext(), DayViewActivity.class);
                Calendar date = Calendar.getInstance();
                date.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DATE));
                intent.putExtra(DayViewActivity.DATE_INTENT, date.getTimeInMillis());
                startActivity(intent);
                handled = true;
                break;
            case R.id.monthMenuItem:
                intent = new Intent(getApplicationContext(), MonthViewActivity.class);
                intent.putExtra(MonthViewActivity.YEAR_INTENT, calendar.get(Calendar.YEAR));
                intent.putExtra(MonthViewActivity.MONTH_INTENT, calendar.get(Calendar.MONTH)+1);
                startActivity(intent);
                handled = true;
                break;
            case R.id.yearMenuItem:
                intent = new Intent(getApplicationContext(), YearViewActivity.class);
                intent.putExtra(YearViewActivity.YEAR_INTENT, calendar.get(Calendar.YEAR));
                startActivity(intent);
                handled = true;
                break;
            case R.id.totalMenuItem:
                intent = new Intent(getApplicationContext(), TotalViewActivity.class);
                startActivity(intent);
                handled = true;
                break;
            case R.id.customMenuItem:
                intent = new Intent(getApplicationContext(), CreateCustomViewActivity.class);
                startActivity(intent);
                handled = true;
                break;
            case R.id.settingsMenuItem:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                handled = true;
                break;
            case R.id.importExportMenuItem:
                intent = new Intent(getApplicationContext(), ImportExportActivity.class);
                startActivity(intent);
                handled = true;
                break;
        }

        if (handled)
        {
            _drawerLayout.closeDrawer(GravityCompat.START);
        }
        else { }

        return handled;
    }
}

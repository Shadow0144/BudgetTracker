package cc.corbin.budgettracker.group;

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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.NavigationActivity;
import cc.corbin.budgettracker.auxilliary.NavigationDrawerHelper;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.custom.CreateCustomViewActivity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.importexport.ImportExportActivity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.search.CreateSearchActivity;
import cc.corbin.budgettracker.search.CreateSearchFragment;
import cc.corbin.budgettracker.search.SearchResultsActivity;
import cc.corbin.budgettracker.search.SearchResultsFragment;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.year.YearViewActivity;

public class GroupActivity extends NavigationActivity
{
    private final String TAG = "GroupActivity";

    private CreateSearchFragment _createSearchFragment;
    private SearchResultsFragment _searchResultsFragment;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _expenditures;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        setup();

        _createSearchFragment = (CreateSearchFragment)getSupportFragmentManager().findFragmentById(R.id.createSearchFragment);
        _searchResultsFragment = (SearchResultsFragment)getSupportFragmentManager().findFragmentById(R.id.searchResultsFragment);

        _viewModel = ExpenditureViewModel.getInstance();

        _searchResultsFragment.enableGroupActivityMode();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == SettingsActivity.DATABASE_UPDATE_INTENT_FLAG)
        {
            // TODO Update outdated elements
        }
        else if (requestCode == SettingsActivity.DATABASE_NO_UPDATE_INTENT_FLAG)
        {
            // Do nothing
        }
        else { }
    }

    // From the Fragment
    public void search(View v)
    {
        if (_createSearchFragment.isDataComplete())
        {
            _searchResultsFragment.runQuery(_createSearchFragment.getSearchIntent());
        }
        else { }
    }
}
package cc.corbin.budgettracker.group;

import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.navigation.NavigationActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.search.CreateSearchFragment;
import cc.corbin.budgettracker.search.SearchResultsFragment;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

public class GroupActivity extends NavigationActivity implements TabLayout.OnTabSelectedListener
{
    private final String TAG = "GroupActivity";

    private TabLayout _groupTabLayout;
    private LinearLayout _createGroupLinearLayout;
    private LinearLayout _addItemsLinearLayout;

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

        _groupTabLayout = findViewById(R.id.groupTabLayout);
        _groupTabLayout.addOnTabSelectedListener(this);
        _createGroupLinearLayout = findViewById(R.id.createGroupLinearLayout);
        _addItemsLinearLayout = findViewById(R.id.addItemsLinearLayout);

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

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        switch (_groupTabLayout.getSelectedTabPosition())
        {
            case 0:
                _createGroupLinearLayout.setVisibility(View.VISIBLE);
                _addItemsLinearLayout.setVisibility(View.GONE);
                break;
            case 1:
                _createGroupLinearLayout.setVisibility(View.GONE);
                _addItemsLinearLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

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
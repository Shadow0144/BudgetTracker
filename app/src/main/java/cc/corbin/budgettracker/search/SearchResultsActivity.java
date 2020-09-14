package cc.corbin.budgettracker.search;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.navigation.NavigationActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;

public class SearchResultsActivity extends NavigationActivity
{
    private final String TAG = "SearchResultsActivity";

    private SearchResultsFragment _searchResultsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        setup();

        _searchResultsFragment = ((SearchResultsFragment)getSupportFragmentManager().findFragmentById(R.id.searchResultsFragment));
        _searchResultsFragment.runQuery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == SettingsActivity.DATABASE_UPDATE_INTENT_FLAG)
        {
            Toast.makeText(getApplicationContext(), "Data out of date; refreshing...", Toast.LENGTH_LONG).show();
            _searchResultsFragment.runQuery();
        }
        else if (requestCode == SettingsActivity.DATABASE_NO_UPDATE_INTENT_FLAG)
        {
            // Do nothing
        }
        else { }
    }
}

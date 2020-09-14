package cc.corbin.budgettracker.search;

import android.os.Bundle;
import android.view.View;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.navigation.NavigationActivity;

public class CreateSearchActivity extends NavigationActivity
{
    private final String TAG = "CreateSearchActivity";

    private CreateSearchFragment _createSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_search);
        setup();

        _createSearchFragment = ((CreateSearchFragment)getSupportFragmentManager().findFragmentById(R.id.createSearchFragment));
    }

    public void search(View v)
    {
        _createSearchFragment.search(v);
    }
}

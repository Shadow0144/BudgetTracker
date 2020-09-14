package cc.corbin.budgettracker.custom;

import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.Bundle;

import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.navigation.NavigationActivity;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

public class CreateCustomViewActivity extends NavigationActivity
{
    private final String TAG = "CreateCustomViewActivity";

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _expenditures;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_custom_view);
        setup();

        _viewModel = ExpenditureViewModel.getInstance();
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
}

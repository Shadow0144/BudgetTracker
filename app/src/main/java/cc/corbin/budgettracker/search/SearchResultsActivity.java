package cc.corbin.budgettracker.search;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.arch.lifecycle.Observer;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.NavigationActivity;
import cc.corbin.budgettracker.auxilliary.NavigationDrawerHelper;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.custom.CreateCustomViewActivity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.day.ExpenditureItem;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.importexport.ImportExportActivity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.year.YearViewActivity;

public class SearchResultsActivity extends NavigationActivity
{
    private final String TAG = "SearchResultsActivity";

    private LinearLayout _resultsContainer;

    private ProgressBar _progressBar;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _expeditures;
    private List<ExpenditureEntity> _expenditureEntities;
    private MutableLiveData<List<BudgetEntity>> _budgets;
    private List<BudgetEntity> _budgetEntities;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_search_results);
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        _viewModel = ExpenditureViewModel.getInstance();

        _progressBar = findViewById(R.id.searchProgressBar);
        _resultsContainer = findViewById(R.id.resultsLinearLayout);

        _progressBar.setVisibility(View.VISIBLE);

        String query = "SELECT * from expenditureentity";

        boolean whereAdded = false;

        // Date info
        boolean extrasIncluded = intent.getBooleanExtra(CreateSearchFragment.INCLUDE_EXTRAS_INTENT, true);
        if (intent.hasExtra(CreateSearchFragment.EXACT_DATE_INTENT))
        {
            Date date = new Date();
            date.setTime(intent.getLongExtra(CreateSearchFragment.EXACT_DATE_INTENT, -1));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            query += " WHERE ";
            whereAdded = true;
            query += "(" +
                    "year = " + calendar.get(Calendar.YEAR) +
                    " AND month = " + (calendar.get(Calendar.MONTH)+1) +
                    " AND day = " + (calendar.get(Calendar.DATE)) +
                    ")";
            // Do not need to worry about extras here
        }
        else if (intent.hasExtra(CreateSearchFragment.START_DATE_INTENT) /* && intent.hasExtra(CreateSearchFragment.END_DATE_INTENT) */)
        {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();

            date.setTime(intent.getLongExtra(CreateSearchFragment.START_DATE_INTENT, -1));
            calendar.setTime(date);
            int sYear = calendar.get(Calendar.YEAR);
            int sMonth = calendar.get(Calendar.MONTH)+1;
            int sDay = calendar.get(Calendar.DATE);

            date.setTime(intent.getLongExtra(CreateSearchFragment.END_DATE_INTENT, -1));
            calendar.setTime(date);
            int eYear = calendar.get(Calendar.YEAR);
            int eMonth = calendar.get(Calendar.MONTH)+1;
            int eDay = calendar.get(Calendar.DATE);

            query += " WHERE ";
            whereAdded = true;
            query += "(" +
                    "(year > " + (sYear) + " AND year < " + (eYear) + ") OR " +
                    "(year = " + (sYear) + " AND year < " + (eYear) + " AND month > " + (sMonth) + ") OR " +
                    "(year = " + (sYear) + " AND year < " + (eYear) + " AND month = " + (sMonth) + " AND day >= " + (sDay) + ") OR " +
                    "(year > " + (sYear) + " AND year = " + (eYear) + " AND month < " + (eMonth) + ") OR " +
                    "(year > " + (sYear) + " AND year = " + (eYear) + " AND month = " + (eMonth) + " AND day <= " + (eDay) + ") OR " +
                    "(year = " + (sYear) + " AND year = " + (eYear) + " AND month > " + (sMonth) + " AND month < " + (eMonth) + ") OR " +
                    "(year = " + (sYear) + " AND year = " + (eYear) + " AND month = " + (sMonth) + " AND month < " + (eMonth) + " AND day >= " + (sDay) + ") OR " +
                    "(year = " + (sYear) + " AND year = " + (eYear) + " AND month > " + (sMonth) + " AND month = " + (eMonth) + " AND day <= " + (eDay) + ") OR " +
                    "(year = " + (sYear) + " AND year = " + (eYear) + " AND month = " + (sMonth) + " AND month = " + (eMonth) + " AND day >= " + (sDay) + " AND day <= " + (eDay) + ")" +
                    ")";

            // Check the status of the extras
            if (extrasIncluded)
            {
                query += " OR ";
                query += "(" +
                        "(day = 0) AND (" +
                        "(year > " + (sYear) + " AND year < " + (eYear) + ") OR " +
                        "(year = " + (sYear) + " AND month >= " + (sMonth) + ") OR " +
                        "(year = " + (eYear) + " AND month <= " + (eMonth) + ")" +
                        ")" +
                        ")";
            }
            else
            {
                query += "(day != 0)";
            }
        }
        else // Any date
        {
            // Check the status of the extras
            if (!extrasIncluded)
            {
                query += " WHERE ";
                whereAdded = true;
                query += "(day != 0)";
            }
            else { }
        }

        // Amount info
        if (intent.hasExtra(CreateSearchFragment.EXACT_AMOUNT_INTENT))
        {
            int currency = intent.getIntExtra(CreateSearchFragment.EXACT_AMOUNT_CURRENCY_INTENT, Currencies.default_currency);
            float amount = intent.getFloatExtra(CreateSearchFragment.EXACT_AMOUNT_INTENT, 0.0f);

            if (whereAdded)
            {
                query += " AND ";
            }
            else
            {
                query += " WHERE ";
                whereAdded = true;
            }
            query += "(" +
                    "(currency = " + currency + ") AND " +
                    "(amount = " + amount + ")" +
                    ")";
        }
        else if (intent.hasExtra(CreateSearchFragment.AMOUNT_RANGE_CURRENCY_INTENT))
        {
            int currency = intent.getIntExtra(CreateSearchFragment.AMOUNT_RANGE_CURRENCY_INTENT, Currencies.default_currency);
            float lowerAmount = intent.getFloatExtra(CreateSearchFragment.AMOUNT_RANGE_LOWER_INTENT, 0.0f);
            float upperAmount = intent.getFloatExtra(CreateSearchFragment.AMOUNT_RANGE_UPPER_INTENT, 0.0f);

            if (whereAdded)
            {
                query += " AND ";
            }
            else
            {
                query += " WHERE ";
                whereAdded = true;
            }
            query += "(" +
                    "(currency = " + currency + ") AND " +
                    "(amount >= " + lowerAmount + ") AND " +
                    "(amount <= " + upperAmount + ")" +
                    ")";
        }
        else { }

        // Category info
        if (intent.hasExtra(CreateSearchFragment.CATEGORIES_INTENT))
        {
            boolean categoryAdded = false; // For adding a final parenthesis
            boolean[] categories = intent.getBooleanArrayExtra(CreateSearchFragment.CATEGORIES_INTENT);
            // If all categories, just leave it off
            boolean allCategories = true;
            for (int i = 0; i < categories.length; i++)
            {
                if (!categories[i])
                {
                    allCategories = false;
                    break;
                }
                else { }
            }
            if (!allCategories) // Only add a categories part if not all categories are considered
            {
                for (int i = 0; i < categories.length; i++)
                {
                    if (categories[i])
                    {
                        if (whereAdded)
                        {
                            if (i == 0)
                            {
                                query += " AND (";
                            }
                            else
                            {
                                query += " OR ";
                            }
                        }
                        else
                        {
                            query += " WHERE (";
                            whereAdded = true;
                        }
                        query += "category = " + i;
                        categoryAdded = true;
                    }
                    else
                    {
                    }
                }
                if (categoryAdded)
                {
                    query += ")";
                }
            }
            else { }
        }
        else { }

        // Note info
        if (intent.hasExtra(CreateSearchFragment.CONTAINS_TEXT_INTENT))
        {
            if (!whereAdded)
            {
                query += " WHERE ";
            }
            else
            {
                query += " AND ";
            }

            String note = intent.getStringExtra(CreateSearchFragment.CONTAINS_TEXT_INTENT);
            query += "(note LIKE '%" + note + "%')";
        }
        else if (intent.hasExtra(CreateSearchFragment.EXACT_TEXT_INTENT))
        {
            if (!whereAdded)
            {
                query += " WHERE ";
            }
            else
            {
                query += " AND ";
            }

            String note = intent.getStringExtra(CreateSearchFragment.EXACT_TEXT_INTENT);
            query += "(note = '" + note + "')";
        }
        else { }

        query += ";";

        _expeditures = new MutableLiveData<List<ExpenditureEntity>>();
        final Observer<List<ExpenditureEntity>> expenditureEntitiesObserver = new Observer<List<ExpenditureEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<ExpenditureEntity> expenditureEntities)
            {
                // List the results
                displayExpenditures(expenditureEntities);
            }
        };
        _expeditures.observe(this, expenditureEntitiesObserver);
        _viewModel.customExpenditureQuery(_expeditures, query);
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

    private void displayExpenditures(List<ExpenditureEntity> expenditureEntities)
    {
        _progressBar.setVisibility(View.GONE);
        _resultsContainer.removeAllViews();
        if (expenditureEntities != null)
        {
            _expenditureEntities = expenditureEntities;
            int count = _expenditureEntities.size();
            for (int i = 0; i < count; i++)
            {
                ExpenditureEntity exp = _expenditureEntities.get(i);
                final ExpenditureItem view = new ExpenditureItem(this, exp);
                // Add an listener to jump to the date of the item
                view.setTag(exp);
                view.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        jumpToExpDate((ExpenditureEntity)view.getTag());
                    }
                });
                _resultsContainer.addView(view);
            }
        }
        else { }
    }

    private void jumpToExpDate(ExpenditureEntity exp)
    {
        if (exp.getDay() != 0)
        {
            Intent intent = new Intent(this, DayViewActivity.class);
            Calendar calendar = Calendar.getInstance();
            calendar.set(exp.getYear(), exp.getMonth() - 1, exp.getDay());
            intent.putExtra(DayViewActivity.YEAR_INTENT, exp.getYear());
            intent.putExtra(DayViewActivity.MONTH_INTENT, exp.getMonth());
            intent.putExtra(DayViewActivity.DAY_INTENT, exp.getDay());
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(this, MonthViewActivity.class);
            intent.putExtra(MonthViewActivity.YEAR_INTENT, exp.getYear());
            intent.putExtra(MonthViewActivity.MONTH_INTENT, exp.getMonth());
            startActivity(intent);
        }
    }

    private void jumpToBudDate(BudgetEntity bud)
    {

    }
}

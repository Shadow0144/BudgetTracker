package cc.corbin.budgettracker.search;

import android.content.Intent;

import java.util.Calendar;
import java.util.Date;

import cc.corbin.budgettracker.auxilliary.Currencies;

public class SearchHelper
{
    private String _query;

    public SearchHelper(Intent intent)
    {
        _query = "SELECT * from expenditureentity";

        boolean whereAdded = false;

        // Date info
        boolean extrasIncluded = intent.getBooleanExtra(CreateSearchFragment.INCLUDE_EXTRAS_INTENT, true);
        if (intent.hasExtra(CreateSearchFragment.EXACT_DATE_INTENT))
        {
            Date date = new Date();
            date.setTime(intent.getLongExtra(CreateSearchFragment.EXACT_DATE_INTENT, -1));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            _query += " WHERE ";
            whereAdded = true;
            _query += "(" +
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

            _query += " WHERE ";
            whereAdded = true;
            _query += "(" +
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
                _query += " OR ";
                _query += "(" +
                        "(day = 0) AND (" +
                        "(year > " + (sYear) + " AND year < " + (eYear) + ") OR " +
                        "(year = " + (sYear) + " AND month >= " + (sMonth) + ") OR " +
                        "(year = " + (eYear) + " AND month <= " + (eMonth) + ")" +
                        ")" +
                        ")";
            }
            else
            {
                _query += "(day != 0)";
            }
        }
        else // Any date
        {
            // Check the status of the extras
            if (!extrasIncluded)
            {
                _query += " WHERE ";
                whereAdded = true;
                _query += "(day != 0)";
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
                _query += " AND ";
            }
            else
            {
                _query += " WHERE ";
                whereAdded = true;
            }
            _query += "(" +
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
                _query += " AND ";
            }
            else
            {
                _query += " WHERE ";
                whereAdded = true;
            }
            _query += "(" +
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
                                _query += " AND (";
                            }
                            else
                            {
                                _query += " OR ";
                            }
                        }
                        else
                        {
                            _query += " WHERE (";
                            whereAdded = true;
                        }
                        _query += "category = " + i;
                        categoryAdded = true;
                    }
                    else
                    {
                    }
                }
                if (categoryAdded)
                {
                    _query += ")";
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
                _query += " WHERE ";
            }
            else
            {
                _query += " AND ";
            }

            String note = intent.getStringExtra(CreateSearchFragment.CONTAINS_TEXT_INTENT);
            _query += "(note LIKE '%" + note + "%')";
        }
        else if (intent.hasExtra(CreateSearchFragment.EXACT_TEXT_INTENT))
        {
            if (!whereAdded)
            {
                _query += " WHERE ";
            }
            else
            {
                _query += " AND ";
            }

            String note = intent.getStringExtra(CreateSearchFragment.EXACT_TEXT_INTENT);
            _query += "(note = '" + note + "')";
        }
        else { }

        _query += ";";
    }

    public String getQuery()
    {
        return _query;
    }
}

package cc.corbin.budgettracker.auxilliary;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Set;

import cc.corbin.budgettracker.R;

// Helper class for loading and holding categories of expenses and budgets
public class Categories
{
    private final static String TAG = "Categories";

    private static boolean _categoriesSet = false;
    private static String[] _categories;

    public static boolean areCategoriesLoaded()
    {
        return _categoriesSet;
    }

    public static void loadCategories(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.budget_tracker_preferences_key), Context.MODE_PRIVATE);
        String categoriesSet = sharedPreferences.getString(context.getString(R.string.categories_list_key), null);
        if (categoriesSet == null)
        {
            _categories = context.getResources().getStringArray(R.array.default_categories);
        }
        else
        {
            _categories = categoriesSet.split("\\|");
        }

        _categoriesSet = true;
    }

    public static void setCategories(String[] categories)
    {
        _categories = categories;
    }

    public static String[] getCategories()
    {
        return _categories;
    }
}

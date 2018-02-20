package cc.corbin.budgettracker;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * Created by Corbin on 2/20/2018.
 */

public class BudgetTrackerApplication extends Application
{
    private final String TAG = "BudgetTrackerApplication";

    private final String SHARED_PREFERENCES = "CC.Corbin.BudgetTracker";
    @Override
    public void onCreate()
    {
        super.onCreate();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        Currencies.default_currency = sharedPreferences.getInt(Currencies.DEFAULT_CURRENCY_KEY, 3);
    }
}

package cc.corbin.budgettracker.navigation;

import android.content.Intent;
import android.view.MenuItem;

import java.time.LocalDate;

import cc.corbin.budgettracker.BudgetTrackerApplication;
import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.custom.CreateCustomViewActivity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.group.GroupActivity;
import cc.corbin.budgettracker.importexport.ImportExportActivity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.search.CreateSearchActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.year.YearViewActivity;

// Helper class for navigating to Activities from the navigation drawer
public class NavigationDrawerHelper
{
    // Set and pass back the intent
    public static Intent handleNavigation(MenuItem item)
    {
        Intent intent = null;
        LocalDate calendar = LocalDate.now();
        switch (item.getItemId())
        {
            case R.id.searchMenuItem:
                intent = new Intent(BudgetTrackerApplication.getInstance(), CreateSearchActivity.class);
                break;
            case R.id.dayMenuItem:
                intent = new Intent(BudgetTrackerApplication.getInstance(), DayViewActivity.class);
                intent.putExtra(DayViewActivity.YEAR_INTENT, calendar.getYear());
                intent.putExtra(DayViewActivity.MONTH_INTENT, calendar.getMonthValue());
                intent.putExtra(DayViewActivity.DAY_INTENT, calendar.getDayOfMonth());
                break;
            case R.id.monthMenuItem:
                intent = new Intent(BudgetTrackerApplication.getInstance(), MonthViewActivity.class);
                intent.putExtra(MonthViewActivity.YEAR_INTENT, calendar.getYear());
                intent.putExtra(MonthViewActivity.MONTH_INTENT, calendar.getMonthValue());
                break;
            case R.id.yearMenuItem:
                intent = new Intent(BudgetTrackerApplication.getInstance(), YearViewActivity.class);
                intent.putExtra(YearViewActivity.YEAR_INTENT, calendar.getYear());
                break;
            case R.id.totalMenuItem:
                intent = new Intent(BudgetTrackerApplication.getInstance(), TotalViewActivity.class);
                break;
            case R.id.customMenuItem:
                intent = new Intent(BudgetTrackerApplication.getInstance(), CreateCustomViewActivity.class);
                break;
            case R.id.groupMenuItem:
                intent = new Intent(BudgetTrackerApplication.getInstance(), GroupActivity.class);
                break;
            case R.id.settingsMenuItem:
                intent = new Intent(BudgetTrackerApplication.getInstance(), SettingsActivity.class);
                intent.putExtra(SettingsActivity.SETTINGS_INTENT_FLAG, true);
                break;
            case R.id.importExportMenuItem:
                intent = new Intent(BudgetTrackerApplication.getInstance(), ImportExportActivity.class);
                break;
        }

        return intent;
    }
}

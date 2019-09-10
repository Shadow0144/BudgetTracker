package cc.corbin.budgettracker.setup;

import android.support.v4.app.Fragment;

import cc.corbin.budgettracker.auxilliary.EnhancedViewPager;

public class SetupFragment extends Fragment
{
    private final String Tag = "WelcomeFragment";

    protected EnhancedViewPager _setupViewPager;

    public void setSetupViewPager(EnhancedViewPager setupViewPager)
    {
        _setupViewPager = setupViewPager;
    }

    protected void previous()
    {
        _setupViewPager.previousPage();
    }

    protected void next()
    {
        _setupViewPager.nextPage();
    }
}

package cc.corbin.budgettracker.setup;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.EnhancedViewPager;

public class SetupFragmentPagerAdapter extends FragmentPagerAdapter
{
    private final String TAG = "SetupFragmentPagerAdapter";

    private WelcomeFragment _welcomeFragment;
    private SelectLanguageFragment _selectLanguageFragment;
    private SetupBaseCurrencyFragment _setupBaseCurrencyFragment;
    private SetupAdditionalCurrenciesFragment _setupAdditionalCurrenciesFragment;
    private SetupCategoriesFragment _setupCategoriesFragment;

    public SetupFragmentPagerAdapter(FragmentManager fm, EnhancedViewPager viewPager)
    {
        super(fm);

        _welcomeFragment = new WelcomeFragment();
        _welcomeFragment.setSetupViewPager(viewPager);

        _selectLanguageFragment = new SelectLanguageFragment();
        _selectLanguageFragment.setSetupViewPager(viewPager);

        _setupBaseCurrencyFragment = new SetupBaseCurrencyFragment();
        _setupBaseCurrencyFragment.setSetupViewPager(viewPager);

        _setupAdditionalCurrenciesFragment = new SetupAdditionalCurrenciesFragment();
        _setupAdditionalCurrenciesFragment.setSetupViewPager(viewPager);

        _setupCategoriesFragment = new SetupCategoriesFragment();
        _setupCategoriesFragment.setSetupViewPager(viewPager);
    }

    @Override
    public Fragment getItem(int i)
    {
        Fragment fragment;
        switch (i)
        {
            case 0:
                fragment = _welcomeFragment;
                break;
            case 1:
                fragment = _selectLanguageFragment;
                break;
            case 2:
                fragment = _setupBaseCurrencyFragment;
                break;
            case 3:
                fragment = _setupAdditionalCurrenciesFragment;
                break;
            case 4:
                fragment = _setupCategoriesFragment;
                break;
                default:
                    fragment = null;
                    break;
        }
        return fragment;
    }

    @Override
    public int getCount()
    {
        return 5;
    }

    public SelectLanguageFragment getLanguageFragment()
    {
        return _selectLanguageFragment;
    }

    public SetupBaseCurrencyFragment getBaseCurrencyFragment()
    {
        return _setupBaseCurrencyFragment;
    }

    public SetupAdditionalCurrenciesFragment getAdditionalCurrenciesFragment()
    {
        return _setupAdditionalCurrenciesFragment;
    }

    public SetupCategoriesFragment getCategoriesFragment()
    {
        return _setupCategoriesFragment;
    }
}

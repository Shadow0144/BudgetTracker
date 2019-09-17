package cc.corbin.budgettracker.setup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.EnhancedViewPager;

public class SetupActivity extends AppCompatActivity
{
    private final String TAG = "SetupActivity";

    private SetupFragmentPagerAdapter _setupFragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        EnhancedViewPager _setupViewPager = findViewById(R.id.setupViewPager);
        _setupFragmentPagerAdapter = new SetupFragmentPagerAdapter(getSupportFragmentManager(), _setupViewPager);
        _setupViewPager.setAdapter(_setupFragmentPagerAdapter);
        _setupViewPager.setFingerSwipingEnabled(false);
        final LinearLayout pageImagesLinearLayout = findViewById(R.id.pageImagesLinearLayout);
        pageImagesLinearLayout.setVisibility(View.INVISIBLE);
        _setupViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int i, float v, int i1)
            {

            }

            @Override
            public void onPageSelected(int i)
            {
                switch (i)
                {
                    case 0:
                        pageImagesLinearLayout.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        pageImagesLinearLayout.setVisibility(View.VISIBLE);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page1ImageView)).setImageResource(android.R.drawable.radiobutton_on_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page2ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page3ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page4ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        break;
                    case 2:
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page1ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page2ImageView)).setImageResource(android.R.drawable.radiobutton_on_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page3ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page4ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        break;
                    case 3:
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page1ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page2ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page3ImageView)).setImageResource(android.R.drawable.radiobutton_on_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page4ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        break;
                    case 4:
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page1ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page2ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page3ImageView)).setImageResource(android.R.drawable.radiobutton_off_background);
                        ((ImageView)pageImagesLinearLayout.findViewById(R.id.page4ImageView)).setImageResource(android.R.drawable.radiobutton_on_background);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i)
            {

            }
        });
    }

    @Override
    public void onBackPressed()
    {
        finishAffinity();
    }

    public void commitSettings()
    {
        // TODO - Save settings
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.budget_tracker_preferences_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPreferences.edit();
        sharedEditor.putString(getString(R.string.language_key), _setupFragmentPagerAdapter.getLanguageFragment().getLanguage());
        sharedEditor.putString(getString(R.string.base_currency_key), _setupFragmentPagerAdapter.getBaseCurrencyFragment().getBaseCurrency());
        sharedEditor.putString(getString(R.string.additional_currencies_key), _setupFragmentPagerAdapter.getAdditionalCurrenciesFragment().getAdditionalCurrencies());
        String categories = _setupFragmentPagerAdapter.getCategoriesFragment().getCategories();
        sharedEditor.putString(getString(R.string.categories_list_key), categories);
        Categories.setCategories(categories.split("\\|"));
        //sharedEditor.apply();

        finish();
    }
}

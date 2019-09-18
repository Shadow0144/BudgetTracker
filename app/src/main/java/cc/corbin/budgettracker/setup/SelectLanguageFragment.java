package cc.corbin.budgettracker.setup;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Locale;

import cc.corbin.budgettracker.R;

public class SelectLanguageFragment extends SetupFragment
{
    private final String TAG = "SelectLanguageFragment";

    private String _language;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_setup_language, parent, false);

        Button previousButton = view.findViewById(R.id.previousButton);
        previousButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                previous();
            }
        });

        Button nextButton = view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                next();
            }
        });

        final RadioGroup languageRadioGroup = view.findViewById(R.id.languageRadioGroup);
        String language = Locale.getDefault().getLanguage();
        switch (language)
        {
            case "en":
                languageRadioGroup.check(R.id.englishRadioButton);
                break;
            case "de":
                languageRadioGroup.check(R.id.germanRadioButton);
                break;
            case "ja":
                languageRadioGroup.check(R.id.japaneseRadioButton);
                break;
            case "ko":
                languageRadioGroup.check(R.id.koreanRadioButton);
                break;
            case "ru":
                languageRadioGroup.check(R.id.russianRadioButton);
                break;
            default:
                languageRadioGroup.check(R.id.englishRadioButton);
                break;
        }
        languageRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.englishRadioButton:
                        _language = "en";
                        break;
                    case R.id.germanRadioButton:
                        _language = "de";
                        break;
                    case R.id.japaneseRadioButton:
                        _language = "ja";
                        break;
                    case R.id.koreanRadioButton:
                        _language = "ko";
                        break;
                    case R.id.russianRadioButton:
                        _language = "ru";
                        break;
                    default:
                        _language = "en";
                        break;
                }
                setLanguage(_language);
            }
        });

        return view;
    }

    public void setLanguage(String languageToLoad)
    {
        Log.e(TAG, "Setting language to: " + languageToLoad);
        String language = Locale.getDefault().getLanguage();
        if (!language.equals(languageToLoad))
        {
            Log.e(TAG, "Changing language");
            Activity context = getActivity();
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);

            Configuration configuration = context.getResources().getConfiguration();
            configuration.setLocale(locale);

            //context.createConfigurationContext(configuration);

            //context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

            context.recreate();
        }
        else
        {
            Log.e(TAG, "Already loaded: " + language);
        }
    }

    public String getLanguage()
    {
        return _language;
    }
}

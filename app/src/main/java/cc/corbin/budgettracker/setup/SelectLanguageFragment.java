package cc.corbin.budgettracker.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;

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

        final RadioButton englishRadioButton = view.findViewById(R.id.englishRadioButton);
        final RadioButton germanRadioButton = view.findViewById(R.id.germanRadioButton);
        final RadioButton japaneseRadioButton = view.findViewById(R.id.japaneseRadioButton);
        final RadioButton koreanRadioButton = view.findViewById(R.id.koreanRadioButton);
        final RadioButton russianRadioButton = view.findViewById(R.id.russianRadioButton);

        String language = Locale.getDefault().getISO3Language();
        switch (language)
        {
            case "eng":
                englishRadioButton.setChecked(true);
                _language = language;
                break;
            case "deu":
                germanRadioButton.setChecked(true);
                _language = language;
                break;
            case "jap":
                japaneseRadioButton.setChecked(true);
                _language = language;
                break;
            case "kor":
                koreanRadioButton.setChecked(true);
                _language = language;
                break;
            case "rus":
                russianRadioButton.setChecked(true);
                _language = language;
                break;
            default:
                englishRadioButton.setChecked(true);
                _language = "eng";
                break;
        }

        return view;
    }

    public String getLanguage()
    {
        return _language;
    }
}

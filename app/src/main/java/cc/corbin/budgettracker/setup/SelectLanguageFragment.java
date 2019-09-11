package cc.corbin.budgettracker.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import java.util.Locale;

import cc.corbin.budgettracker.R;

public class SelectLanguageFragment extends SetupFragment
{
    private final String TAG = "SelectLanguageFragment";

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
                break;
            case "deu":
                germanRadioButton.setChecked(true);
                break;
            case "jap":
                japaneseRadioButton.setChecked(true);
                break;
            case "kor":
                koreanRadioButton.setChecked(true);
                break;
            case "rus":
                russianRadioButton.setChecked(true);
                break;
            default:
                englishRadioButton.setChecked(true);
                break;
        }

        return view;
    }
}

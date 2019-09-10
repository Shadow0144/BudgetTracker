package cc.corbin.budgettracker.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import cc.corbin.budgettracker.R;

public class SetupCategoriesFragment extends SetupFragment
{
    private final String TAG = "SetupCategoriesFragment";

    private View _view;

    PopupWindow _popupWindow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        _view = inflater.inflate(R.layout.fragment_setup_categories, parent, false);

        Button previousButton = _view.findViewById(R.id.previousButton);
        previousButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                previous();
            }
        });

        Button nextButton = _view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        return _view;
    }

    private void finish()
    {
        View confirmSettingsView = getLayoutInflater().inflate(R.layout.popup_finish_setup, null);
        final Button cancelButton = confirmSettingsView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelFinish();
            }
        });
        final Button acceptButton = confirmSettingsView.findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                confirmFinish();
            }
        });
        _popupWindow = new PopupWindow(confirmSettingsView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(_view.findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);
    }

    private void cancelFinish()
    {
        _popupWindow.dismiss();
    }

    private void confirmFinish()
    {
        _popupWindow.dismiss();

        // TODO - Save settings

        getActivity().finish();
    }
}

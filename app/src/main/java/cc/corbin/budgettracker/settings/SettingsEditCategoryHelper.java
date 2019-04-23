package cc.corbin.budgettracker.settings;

import android.arch.lifecycle.MutableLiveData;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.SortableItem;
import cc.corbin.budgettracker.auxilliary.SortableLinearLayout;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

public class SettingsEditCategoryHelper
{
    private final String TAG = "SettingsEditCategoryHelper";

    private SettingsActivity _parent;

    private int _categoryEditIndex;
    private int _newCategoryIndex;
    private String[] _categories;
    private String _newCategoryString;

    private PopupWindow _popupWindow;

    public SettingsEditCategoryHelper(SettingsActivity parent, View v)
    {
        _parent = parent;
        _categoryEditIndex = v.getId();
        _categories = Categories.getCategories();

        final View categoryEditView = _parent.getLayoutInflater().inflate(R.layout.popup_edit_category, null);

        final EditText categoryEditText = categoryEditView.findViewById(R.id.categoryEditText);
        categoryEditText.setText(((SortableItem)v).getText());

        final Button confirmButton = categoryEditView.findViewById(R.id.confirmButton);
        categoryEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                String text = s.toString();
                boolean unmatched = true;
                for (int i = 0; i < _categories.length; i++)
                {
                    if (text.equals(_categories[i]))
                    {
                        unmatched = false;
                        break;
                    }
                    else { }
                }
                confirmButton.setEnabled(unmatched && (text.length() > 0));
            }
        });
        categoryEditText.setFilters(new InputFilter[] { new InputFilter()
        {
            private final String bannedCharacters = "|";

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend)
            {
                if (source != null && bannedCharacters.contains(source))
                {
                    return "";
                }
                else
                {
                    return source;
                }
            }
        } });

        _popupWindow = new PopupWindow(categoryEditView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(_parent.findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);
    }

    public PopupWindow confirmCategoryEdit(ExpenditureViewModel viewModel, MutableLiveData<Boolean> processing, SortableLinearLayout sortableCategoriesTable)
    {
        final EditText categoryEditText = _popupWindow.getContentView().findViewById(R.id.categoryEditText);

        String newCategoryName = categoryEditText.getText().toString();

        viewModel.renameCategory(_categoryEditIndex, newCategoryName, processing);

        sortableCategoriesTable.updateItemText(_categoryEditIndex, newCategoryName);

        _popupWindow.dismiss();

        return null; // Window is dismissed
    }

    public PopupWindow recategorizeCategory()
    {
        _popupWindow.dismiss();

        final View recategorizeView = _parent.getLayoutInflater().inflate(R.layout.popup_recategorize, null);

        final TextView currentCategoryTextView = recategorizeView.findViewById(R.id.originalCategoryTextView);
        currentCategoryTextView.setText(_categories[_categoryEditIndex]);

        String[] updatedCategories = new String[_categories.length-1];
        int j = 0;
        for (int i = 0; i < _categories.length; i++)
        {
            if (!_categories[i].equals(_categories[_categoryEditIndex]))
            {
                updatedCategories[j] = _categories[i];
                j++;
            }
            else { }
        }

        final Spinner newCategorySpinner = recategorizeView.findViewById(R.id.newCategorySpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                _parent, android.R.layout.simple_spinner_item, updatedCategories);
        newCategorySpinner.setAdapter(spinnerArrayAdapter);
        newCategorySpinner.setSelection(updatedCategories.length-1); // Set to Other

        _popupWindow = new PopupWindow(recategorizeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(_parent.findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);

        return  _popupWindow;
    }

    public PopupWindow confirmRecategorize()
    {
        // Grab all the info before dismissing the popup
        final Spinner newCategorySpinner = _popupWindow.getContentView().findViewById(R.id.newCategorySpinner);
        _newCategoryIndex = newCategorySpinner.getSelectedItemPosition();
        _newCategoryString = newCategorySpinner.getSelectedItem().toString();
        _popupWindow.dismiss();

        final View confirmRemoveCategoryLayout = _parent.getLayoutInflater().inflate(R.layout.popup_confirm_remove_category, null);

        final TextView originalCategoryTextView = confirmRemoveCategoryLayout.findViewById(R.id.originalCategoryTextView);
        final TextView newCategoryTextView = confirmRemoveCategoryLayout.findViewById(R.id.newCategoryTextView);

        originalCategoryTextView.setText(_categories[_categoryEditIndex]);
        newCategoryTextView.setText(_newCategoryString);

        _popupWindow = new PopupWindow(confirmRemoveCategoryLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(_parent.findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);

        return  _popupWindow;
    }

    public PopupWindow confirmRemoveAndRecategorize(ExpenditureViewModel viewModel, MutableLiveData<Boolean> processing,
                                                    SortableLinearLayout sortableCategoriesTable)
    {
        // Update all the expenditures and budgets
        viewModel.mergeCategory(_categoryEditIndex, _newCategoryIndex, _newCategoryString, processing);

        // Remove from the list and commit the change
        String[] newCategories = new String[_categories.length-1];
        int j = 0;
        for (int i = 0; i < _categories.length; i++)
        {
            if (i != _categoryEditIndex)
            {
                newCategories[j] = _categories[i];
                j++;
            }
            else { }
        }

        // Remove the view
        sortableCategoriesTable.removeSortableView(_categoryEditIndex);

        _popupWindow.dismiss();

        return null; // Window is dismissed
    }

    public PopupWindow getPopupWindow()
    {
        return _popupWindow;
    }
}

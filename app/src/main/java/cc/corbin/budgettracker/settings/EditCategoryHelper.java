package cc.corbin.budgettracker.settings;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
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

public class EditCategoryHelper
{
    private final String TAG = "EditCategoryHelper";

    private Context _context;
    private SortableCategoryListInterface _parent;
    private LayoutInflater _inflater;
    private View _rootView;

    private int _categoryEditIndex;
    private int _newCategoryIndex;
    private String[] _categories;
    private String _newCategoryString;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<Boolean> _processing;
    private SortableLinearLayout _sortableCategoriesTable;
    private SortableItem _item;

    private PopupWindow _popupWindow;

    public EditCategoryHelper(Context context, SortableCategoryListInterface parent, LayoutInflater inflater, View rootView,
                              ExpenditureViewModel viewModel, MutableLiveData<Boolean> processing,
                              SortableLinearLayout sortableCategoriesTable, SortableItem item)
    {
        _context = context;
        _parent = parent;
        _inflater = inflater;
        _rootView = rootView;
        _viewModel = viewModel;
        _processing = processing;
        _sortableCategoriesTable = sortableCategoriesTable;
        _item = item;
        _categoryEditIndex = _item.getId();
        String categoryString = _item.getText().toString();
        _categories = Categories.getCategories();

        final View categoryEditView = _inflater.inflate(R.layout.popup_edit_category, null);

        final EditText categoryEditText = categoryEditView.findViewById(R.id.categoryEditText);
        categoryEditText.setText(categoryString);

        final Button cancelButton = categoryEditView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelEditCategory();
            }
        });

        final Button removeButton = categoryEditView.findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                promptRecategorizeCategory();
            }
        });

        final Button confirmButton = categoryEditView.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                confirmCategoryEdit();
            }
        });

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
        _popupWindow.showAtLocation(_rootView, Gravity.CENTER, 0, 0);
    }

    private void cancelEditCategory()
    {
        _popupWindow.dismiss();
        _parent.cancelEditCategory();
    }

    // If removing a category, begin the process to recategorize
    // This popup will let the user select which category to dump the category being removed into
    private void promptRecategorizeCategory()
    {
        _popupWindow.dismiss();

        final View recategorizeView = _inflater.inflate(R.layout.popup_recategorize_category, null);

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
                _context, android.R.layout.simple_spinner_item, updatedCategories);
        newCategorySpinner.setAdapter(spinnerArrayAdapter);
        newCategorySpinner.setSelection(updatedCategories.length-1); // Set to Other

        final Button cancelButton = recategorizeView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelEditCategory();
            }
        });

        final Button acceptButton = recategorizeView.findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                askToConfirmRemoveAndRecategorizeCategory();
            }
        });

        _popupWindow = new PopupWindow(recategorizeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(_rootView, Gravity.CENTER, 0, 0);
    }

    private void askToConfirmRemoveAndRecategorizeCategory()
    {
        // Grab all the info before dismissing the popup
        final Spinner newCategorySpinner = _popupWindow.getContentView().findViewById(R.id.newCategorySpinner);
        _newCategoryIndex = newCategorySpinner.getSelectedItemPosition();
        _newCategoryString = newCategorySpinner.getSelectedItem().toString();
        _popupWindow.dismiss();

        final View confirmRemoveCategoryLayout = _inflater.inflate(R.layout.popup_confirm_remove_category, null);

        final TextView originalCategoryTextView = confirmRemoveCategoryLayout.findViewById(R.id.originalCategoryTextView);
        final TextView newCategoryTextView = confirmRemoveCategoryLayout.findViewById(R.id.newCategoryTextView);

        originalCategoryTextView.setText(_categories[_categoryEditIndex]);
        newCategoryTextView.setText(_newCategoryString);

        final Button cancelButton = confirmRemoveCategoryLayout.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelEditCategory();
            }
        });

        final Button acceptButton = confirmRemoveCategoryLayout.findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                confirmRemoveAndRecategorizeCategory();
            }
        });

        _popupWindow = new PopupWindow(confirmRemoveCategoryLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(_rootView, Gravity.CENTER, 0, 0);
    }

    private void confirmRemoveAndRecategorizeCategory()
    {
        // Update all the expenditures and budgets
        _viewModel.mergeCategory(_categoryEditIndex, _newCategoryIndex, _newCategoryString, _processing);

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
        _sortableCategoriesTable.removeSortableView(_categoryEditIndex);

        _parent.removeAndRecategorizeCategory();

        _popupWindow.dismiss();
    }

    private void confirmCategoryEdit()
    {
        final EditText categoryEditText = _popupWindow.getContentView().findViewById(R.id.categoryEditText);

        String newCategoryName = categoryEditText.getText().toString();

        _viewModel.renameCategory(_categoryEditIndex, newCategoryName, _processing);

        _sortableCategoriesTable.updateItemText(_categoryEditIndex, newCategoryName);

        _parent.confirmEditCategory();

        _popupWindow.dismiss();
    }

    public PopupWindow getPopupWindow()
    {
        return _popupWindow;
    }
}

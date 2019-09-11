package cc.corbin.budgettracker.settings;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.SortableItem;
import cc.corbin.budgettracker.auxilliary.SortableLinearLayout;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

public class AddCategoryHelper
{
    private final String TAG = "AddCategoryHelper";

    private Context _context;
    private SortableCategoryListInterface _parent;
    private ExpenditureViewModel _viewModel;
    private MutableLiveData<Boolean> _processing;
    private SortableLinearLayout _sortableCategoriesTable;

    private String[] _categories;

    private PopupWindow _popupWindow;

    public AddCategoryHelper(Context context, SortableCategoryListInterface parent, LayoutInflater layoutInflator, View rootView,
                             ExpenditureViewModel viewModel, MutableLiveData<Boolean> processing, SortableLinearLayout sortableCategoriesTable)
    {
        _context = context;
        _parent = parent;
        _categories = Categories.getCategories();
        _viewModel = viewModel;
        _processing = processing;
        _sortableCategoriesTable = sortableCategoriesTable;

        // Reuse the edit popup, but disable the remove button
        final View categoryEditView = layoutInflator.inflate(R.layout.popup_edit_category, null);

        final Button confirmButton = categoryEditView.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                confirmCategoryAdd();
            }
        });

        final Button cancelButton = categoryEditView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancelCategoryAdd();
            }
        });

        final EditText categoryEditText = categoryEditView.findViewById(R.id.categoryEditText);
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

        final Button removeButton = categoryEditView.findViewById(R.id.removeButton);
        removeButton.setVisibility(View.INVISIBLE);

        _popupWindow = new PopupWindow(categoryEditView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
    }

    private void cancelCategoryAdd()
    {
        _popupWindow.dismiss();
        _parent.cancelAddCategory();
    }

    private void confirmCategoryAdd()
    {
        String[] categoriesNew = new String[_categories.length+1];

        int i;
        int end = _categories.length-1;
        for (i = 0; i < end; i++)
        {
            categoriesNew[i] = _categories[i];
        }

        categoriesNew[i++] = ((EditText)(_popupWindow.getContentView().findViewById(R.id.categoryEditText))).getText().toString();
        categoriesNew[i] = _categories[end];

        SortableItem categoryCell = new SortableItem(_context);
        categoryCell.setText(categoriesNew[end]);
        categoryCell.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _parent.editCategory(v);
            }
        });
        categoryCell.setId(end);

        _sortableCategoriesTable.insertSortableView(categoryCell);

        _viewModel.addCategory(end, _processing);

        _parent.confirmAddCategory();

        _popupWindow.dismiss();
    }

    public PopupWindow getPopupWindow()
    {
        return  _popupWindow;
    }
}

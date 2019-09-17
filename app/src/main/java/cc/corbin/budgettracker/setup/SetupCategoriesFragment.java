package cc.corbin.budgettracker.setup;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.SortableItem;
import cc.corbin.budgettracker.auxilliary.SortableLinearLayout;
import cc.corbin.budgettracker.settings.AddCategoryHelper;
import cc.corbin.budgettracker.settings.EditCategoryHelper;
import cc.corbin.budgettracker.settings.SortableCategoryListInterface;
import cc.corbin.budgettracker.tables.TableCell;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

public class SetupCategoriesFragment extends SetupFragment implements SortableCategoryListInterface
{
    private final String TAG = "SetupCategoriesFragment";

    private View _view;

    private String[] _categories;

    private SortableLinearLayout _sortableCategoriesTable;
    private Button _resortButton;
    private Button _confirmResortButton;
    private Button _cancelResortButton;
    private SortableItem[] _originalCategoriesSortedList;

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

        setupCategories();

        return _view;
    }

    private void setupCategories()
    {
        _categories = Categories.getCategories();
        final LinearLayout categoriesLayout = _view.findViewById(R.id.categoriesLinearLayout);

        boolean addTitle = false;
        if (addTitle)
        {
            TableCell categoryCell = new TableCell(this.getContext(), TableCell.TITLE_CELL);
            categoryCell.setText(R.string.categories);
            categoriesLayout.addView(categoryCell);
        }
        else { }

        _sortableCategoriesTable = new SortableLinearLayout(this.getContext());
        _sortableCategoriesTable.setSortingEnabled(false);
        int size = _categories.length;//-1;
        SortableItem categoryCellSort = null;
        for (int i = 0; i < size; i++)
        {
            boolean sortable = i < (size-1);
            categoryCellSort = new SortableItem(this.getContext(), sortable);
            categoryCellSort.setText(_categories[i]);
            categoryCellSort.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    editCategory(v);
                }
            });
            categoryCellSort.setId(i);
            _sortableCategoriesTable.insertSortableView(categoryCellSort, sortable);
        }
        categoriesLayout.addView(_sortableCategoriesTable);

        //_otherCategoryCell = new TableCell(this.getContext(), TableCell.SEMI_SPECIAL_CELL);
        //_otherCategoryCell.setText(_categories[size]); // The last item
        //categoriesLayout.addView(_otherCategoryCell);

        // Add the add button
        final Button addButton = new Button(this.getContext());
        addButton.setPadding(0, 0, 0, 0); // TODO
        addButton.setText("Add");
        addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addCategory();
            }
        });
        categoriesLayout.addView(addButton);

        // Add the resort button
        _resortButton = new Button(this.getContext());
        _resortButton.setPadding(0, 0, 0, 0); // TODO
        _resortButton.setText("Resort");
        _resortButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _originalCategoriesSortedList = _sortableCategoriesTable.getSortableItemList();
                _sortableCategoriesTable.setSortingEnabled(true);
                _resortButton.setVisibility(View.GONE);
                _confirmResortButton.setVisibility(View.VISIBLE);
                _cancelResortButton.setVisibility(View.VISIBLE);
            }
        });
        categoriesLayout.addView(_resortButton);

        // Add the confirm resort button and hide it
        _confirmResortButton = new Button(this.getContext());
        _confirmResortButton.setPadding(0, 0, 0, 0); // TODO
        _confirmResortButton.setText("Confirm Resort");
        _confirmResortButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _sortableCategoriesTable.setSortingEnabled(false);
                _resortButton.setVisibility(View.VISIBLE);
                _confirmResortButton.setVisibility(View.GONE);
                _cancelResortButton.setVisibility(View.GONE);
                // TODO ?
            }
        });
        _confirmResortButton.setVisibility(View.GONE);
        categoriesLayout.addView(_confirmResortButton);

        // Add the cancel resort button and hide it
        _cancelResortButton = new Button(this.getContext());
        _cancelResortButton.setMinHeight(0);
        _cancelResortButton.setText("Cancel Resort");
        _cancelResortButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _sortableCategoriesTable.setSortingEnabled(false);
                _resortButton.setVisibility(View.VISIBLE);
                _confirmResortButton.setVisibility(View.GONE);
                _cancelResortButton.setVisibility(View.GONE);
                // TODO ?
            }
        });
        _cancelResortButton.setVisibility(View.GONE);
        categoriesLayout.addView(_cancelResortButton);
    }

    @Override
    public void addCategory()
    {
        new AddCategoryHelper(this.getContext(), this, this.getLayoutInflater(),
                _view.findViewById(R.id.rootLayout), ExpenditureViewModel.getInstance(), new MutableLiveData<Boolean>(),
                _sortableCategoriesTable); // This will create and popup the editor
    }

    // Called by the items in the list
    @Override
    public void editCategory(View v)
    {
        new EditCategoryHelper(this.getContext(), this, this.getLayoutInflater(),
                _view.findViewById(R.id.rootLayout), ExpenditureViewModel.getInstance(), new MutableLiveData<Boolean>(),
                _sortableCategoriesTable, ((SortableItem)v)); // This will create and popup the editor
    }

    @Override
    public void cancelAddCategory()
    {
        // Do nothing
    }

    @Override
    public void cancelEditCategory()
    {
        // Do nothing
    }

    @Override
    public void confirmAddCategory()
    {
        // TODO
    }

    @Override
    public void confirmEditCategory()
    {
        // TODO
    }

    @Override
    public void removeAndRecategorizeCategory()
    {
        // TODO
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

        ((SetupActivity)getActivity()).commitSettings();
    }

    public String getCategories()
    {
        return "Food|Travel|Other"; // TODO
    }
}

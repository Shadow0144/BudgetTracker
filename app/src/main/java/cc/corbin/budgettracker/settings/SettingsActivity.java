package cc.corbin.budgettracker.settings;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.NavigationActivity;
import cc.corbin.budgettracker.auxilliary.SortableItem;
import cc.corbin.budgettracker.auxilliary.SortableLinearLayout;
import cc.corbin.budgettracker.tables.TableCell;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

public class SettingsActivity extends NavigationActivity implements SortableCategoryListInterface
{
    private final String TAG = "SettingsActivity";

    public final static String SETTINGS_INTENT_FLAG = "SettingsIntent";
    public final static int SETTINGS_REQUEST_CODE = 0;
    public final static int DATABASE_NO_UPDATE_INTENT_FLAG = 254;
    public final static int DATABASE_UPDATE_INTENT_FLAG = 255;

    private String[] _categories;

    private SortableLinearLayout _sortableCategoriesTable;
    private TableCell _otherCategoryCell;
    private TableCell _resortButton;
    private TableCell _confirmResortButton;
    private TableCell _cancelResortButton;
    private SortableItem[] _originalCategoriesSortedList;

    private ExpenditureViewModel _viewModel;

    private MutableLiveData<Boolean> _processing;
    private PopupWindow _processingPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setup();

        setResult(SettingsActivity.DATABASE_NO_UPDATE_INTENT_FLAG);

        _viewModel = ExpenditureViewModel.getInstance();

        // Setup the processing flag and popup
        setupProcessing();

        // Setup the categories
        setupCategoriesLayout();

        // Setup the default currencies
        setupDefaultCurrencies();
    }

    @Override
    public void onBackPressed()
    {
        if (_processing.getValue())
        {
            Toast.makeText(this, getString(R.string.wait_for_processing), Toast.LENGTH_LONG).show();
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == SettingsActivity.DATABASE_UPDATE_INTENT_FLAG)
        {
            // TODO Update outdated elements
        }
        else if (requestCode == SettingsActivity.DATABASE_NO_UPDATE_INTENT_FLAG)
        {
            // Do nothing
        }
        else { }
    }

    public void onProcessingChanged(boolean processing)
    {
        if (processing)
        {
            _processingPopup.showAtLocation(findViewById(R.id.rootLayout), Gravity.CENTER, 0, 0);
        }
        else
        {
            _processingPopup.dismiss();
        }
    }

    private void setupProcessing()
    {
        _processing = new MutableLiveData<Boolean>();
        _processing.setValue(false);
        final Observer<Boolean> processingObserver = new Observer<Boolean>()
        {
            @Override
            public void onChanged(@Nullable Boolean processing)
            {
                onProcessingChanged(processing);
            }
        };
        _processing.observe(this, processingObserver);
        final View processingView = getLayoutInflater().inflate(R.layout.popup_processing_event, null);
        _processingPopup = new PopupWindow(processingView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _processingPopup.setFocusable(false);
        _processingPopup.update();
    }

    private void setupCategoriesLayout()
    {
        _categories = Categories.getCategories();

        final LinearLayout categoriesLayout = findViewById(R.id.categoriesLayout);
        TableCell categoryCell = new TableCell(this, TableCell.TITLE_CELL);
        categoryCell.setText(R.string.categories);
        categoriesLayout.addView(categoryCell);

        _sortableCategoriesTable = new SortableLinearLayout(this);
        _sortableCategoriesTable.setSortingEnabled(false);
        int size = _categories.length-1;
        for (int i = 0; i < size; i++)
        {
            SortableItem categoryCellSort = new SortableItem(this);
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
            _sortableCategoriesTable.insertSortableView(categoryCellSort);
        }
        categoriesLayout.addView(_sortableCategoriesTable);

        _otherCategoryCell = new TableCell(this, TableCell.SEMI_SPECIAL_CELL);
        _otherCategoryCell.setText(_categories[size]); // The last item
        categoriesLayout.addView(_otherCategoryCell);

        // Add the add button
        categoryCell = new TableCell(this, TableCell.SPECIAL_CELL);
        categoryCell.setText("<Add>");
        categoryCell.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addCategory();
            }
        });
        categoriesLayout.addView(categoryCell);

        // Add the resort button
        _resortButton = new TableCell(this, TableCell.SPECIAL_CELL);
        _resortButton.setText("<Resort>");
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
        _confirmResortButton = new TableCell(this, TableCell.SPECIAL_CELL);
        _confirmResortButton.setText("<Confirm Resort>");
        _confirmResortButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _sortableCategoriesTable.setSortingEnabled(false);
                _resortButton.setVisibility(View.VISIBLE);
                _confirmResortButton.setVisibility(View.GONE);
                _cancelResortButton.setVisibility(View.GONE);
                categoriesResorted();
            }
        });
        _confirmResortButton.setVisibility(View.GONE);
        categoriesLayout.addView(_confirmResortButton);

        // Add the cancel resort button and hide it
        _cancelResortButton = new TableCell(this, TableCell.SEMI_SPECIAL_CELL);
        _cancelResortButton.setText("<Cancel Resort>");
        _cancelResortButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _sortableCategoriesTable.setSortingEnabled(false);
                _resortButton.setVisibility(View.VISIBLE);
                _confirmResortButton.setVisibility(View.GONE);
                _cancelResortButton.setVisibility(View.GONE);
                categoriesResortCancel();
            }
        });
        _cancelResortButton.setVisibility(View.GONE);
        categoriesLayout.addView(_cancelResortButton);
    }

    private void setupDefaultCurrencies()
    {
        final TableLayout defaultCurrencyTable = findViewById(R.id.defaultCurrencyTable);
        defaultCurrencyTable.setColumnStretchable(0, true);
        defaultCurrencyTable.setColumnStretchable(1, true);

        final TableRow defaultCurrencyTitleRow = new TableRow(this);
        final TableCell defaultCurrencyTitle = new TableCell(this, TableCell.TITLE_CELL);
        defaultCurrencyTitle.setText("Default Currency");
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 2;
        defaultCurrencyTitle.setLayoutParams(params);
        defaultCurrencyTitleRow.addView(defaultCurrencyTitle);
        defaultCurrencyTable.addView(defaultCurrencyTitleRow);

        final TableRow defaultCurrencyContentRow = new TableRow(this);
        final TableCell defaultCurrencySymbolCell = new TableCell(this, TableCell.DEFAULT_CELL);
        final TableCell defaultCurrencyNameCell = new TableCell(this, TableCell.DEFAULT_CELL);

        defaultCurrencySymbolCell.setText(Currencies.symbols[Currencies.default_currency]);
        defaultCurrencyNameCell.setText(Currencies.currencies.values()[Currencies.default_currency].name());

        defaultCurrencyContentRow.addView(defaultCurrencySymbolCell);
        defaultCurrencyContentRow.addView(defaultCurrencyNameCell);
        defaultCurrencyTable.addView(defaultCurrencyContentRow);
    }

    /// Edit

    public void editCategory(View v)
    {
        new EditCategoryHelper(this, this, this.getLayoutInflater(),
                findViewById(R.id.rootLayout), _viewModel, _processing,
                _sortableCategoriesTable, ((SortableItem)v));
    }

    @Override
    public void cancelEditCategory()
    {
        // Do nothing
    }

    @Override
    public void confirmEditCategory()
    {
        saveUpdatedCategories();
        setResult(SettingsActivity.DATABASE_UPDATE_INTENT_FLAG);
    }

    @Override
    public void removeAndRecategorizeCategory()
    {
        saveUpdatedCategories();
        setResult(SettingsActivity.DATABASE_UPDATE_INTENT_FLAG);
    }

    /// /Remove

    /// Add

    @Override
    public void addCategory()
    {
        new AddCategoryHelper(this, this, this.getLayoutInflater(),
                findViewById(R.id.rootLayout), _viewModel, _processing,
                _sortableCategoriesTable);
    }

    @Override
    public void cancelAddCategory()
    {
        // Do nothing
    }

    @Override
    public void confirmAddCategory()
    {
        saveUpdatedCategories();
        setResult(SettingsActivity.DATABASE_UPDATE_INTENT_FLAG);
    }

    /// /Add

    /// Resort

    private void categoriesResortCancel()
    {
        _sortableCategoriesTable.setSortableItemList(_originalCategoriesSortedList);
    }

    private void categoriesResorted()
    {
        saveUpdatedCategories();
        _viewModel.updateCategories(_categories, _processing); // Other updates are called in their helpers
        setResult(SettingsActivity.DATABASE_UPDATE_INTENT_FLAG);
    }

    private void saveUpdatedCategories()
    {
        SortableItem[] sortableItems = _sortableCategoriesTable.getSortableItemList();
        String other = _categories[_categories.length-1]; // Store "Other"
        _categories = new String[sortableItems.length+1];
        int i = 0;
        for (; i < (_categories.length-1); i++)
        {
            _categories[i] = sortableItems[i].getText().toString();
        }
        _categories[i] = other;

        StringBuilder sb = new StringBuilder();
        for (i = 0; i < _categories.length; i++)
        {
            sb.append(_categories[i]).append("|");
        }
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.budget_tracker_preferences_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.categories_list_key), sb.toString());
        Categories.setCategories(_categories);
        //editor.apply(); // TODO
    }

    /// /Resort
}

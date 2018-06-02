package cc.corbin.budgettracker.settings;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.tables.TableCell;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.year.YearViewActivity;

public class SettingsActivity extends AppCompatActivity
{
    private final String TAG = "SettingsActivity";

    private String[] _categories;

    private TableLayout _categoriesTable;
    private TableCell _otherCategoryCell;

    private int _categoryEditIndex;
    private int _newCategoryIndex;
    private String _newCategoryString;

    private PopupWindow _popupWindow;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _exps;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        _categories = Categories.getCategories();

        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(this), BudgetDatabase.getBudgetDatabase(this));
        _viewModel.setDate(0, 0, 0);

        _exps = new MutableLiveData<List<ExpenditureEntity>>();
        _budgets = new MutableLiveData<List<BudgetEntity>>();

        final Observer<List<ExpenditureEntity>> entityObserver = new Observer<List<ExpenditureEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<ExpenditureEntity> expenditureEntities)
            {
                expendituresUpdated();
            }
        };

        final Observer<List<BudgetEntity>> budgetObserver = new Observer<List<BudgetEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<BudgetEntity> budgetEntities)
            {
                budgetsUpdated();
            }
        };

        _exps.observe(this, entityObserver);
        _budgets.observe(this, budgetObserver);

        final LinearLayout categoriesLayout = findViewById(R.id.categoriesLayout);

        _categoriesTable = new TableLayout(this);
        _categoriesTable.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        _categoriesTable.setStretchAllColumns(true);
        _categoriesTable.setColumnShrinkable(0, true);

        TableRow categoryRow = new TableRow(this);
        TableCell categoryCell = new TableCell(this, TableCell.TITLE_CELL);
        categoryCell.setText("Categories");
        categoryRow.addView(categoryCell);
        _categoriesTable.addView(categoryRow);

        String[] categories = Categories.getCategories();
        int size = categories.length;
        for (int i = 0; i < size; i++)
        {
            categoryRow = new TableRow(this);
            categoryCell = new TableCell(this, TableCell.DEFAULT_CELL);
            categoryCell.setText(categories[i]);
            if (i < (size-1)) // Do not add a listener for editing or removing "Other"
            {
                categoryCell.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        editCategory(v);
                    }
                });
            }
            else { }
            categoryCell.setId(i);
            categoryRow.addView(categoryCell);
            _categoriesTable.addView(categoryRow);
        }

        _otherCategoryCell = categoryCell; // The last item
        _otherCategoryCell.setType(TableCell.SEMI_SPECIAL_CELL);

        // Add the add button
        categoryRow = new TableRow(this);
        categoryCell = new TableCell(this, TableCell.SPECIAL_CELL);
        categoryCell.setText("<Add>");
        categoryCell.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addCategory(v);
            }
        });
        categoryRow.addView(categoryCell);
        _categoriesTable.addView(categoryRow);

        // Add the resort button
        categoryRow = new TableRow(this);
        categoryCell = new TableCell(this, TableCell.SPECIAL_CELL);
        categoryCell.setText("<Resort>");
        categoryCell.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                resortCategories(v);
            }
        });
        categoryRow.addView(categoryCell);
        _categoriesTable.addView(categoryRow);

        categoriesLayout.addView(_categoriesTable);

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

    private void expendituresUpdated()
    {
        TotalViewActivity.dataInvalid = true;
        YearViewActivity.dataInvalid = true;
        MonthViewActivity.dataInvalid = true;
        DayViewActivity.dataInvalid = true;
    }

    private void budgetsUpdated()
    {
        TotalViewActivity.dataInvalid = true;
        YearViewActivity.dataInvalid = true;
        MonthViewActivity.dataInvalid = true;
        DayViewActivity.dataInvalid = true;
    }

    ///
    /// Cancel
    ///

    public void cancel(View v)
    {
        _popupWindow.dismiss();
    }

    ///
    /// / Cancel
    ///

    ///
    /// Edit
    ///

    private void editCategory(View v)
    {
        _categoryEditIndex = v.getId();

        final View categoryEditView = getLayoutInflater().inflate(R.layout.popup_edit_category, null);

        final EditText categoryEditText = categoryEditView.findViewById(R.id.categoryEditText);
        categoryEditText.setText(((TableCell)v).getText());

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
        categoryEditText.setFilters(new InputFilter [] { new InputFilter()
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
        _popupWindow.showAtLocation(findViewById(R.id.settingsRootLayout), Gravity.CENTER, 0, 0);
    }

    public void confirmCategoryEdit(View v)
    {
        final EditText categoryEditText = _popupWindow.getContentView().findViewById(R.id.categoryEditText);

        String newCategoryName = categoryEditText.getText().toString();

        _viewModel.renameExpenditureCategory(_exps, _categoryEditIndex, newCategoryName);
        _viewModel.renameBudgetCategory(_budgets, _categoryEditIndex, newCategoryName);

        _categories[_categoryEditIndex] = newCategoryName;

        final TableCell editedCategory = _categoriesTable.findViewById(_categoryEditIndex);
        editedCategory.setText(_categories[_categoryEditIndex]);

        // Save the updated categories
        saveUpdatedCategories();

        _popupWindow.dismiss();
    }

    ///
    /// / Edit
    ///

    ///
    /// Remove
    ///

    // Called from the Remove button
    public void recategorizeCategory(View v)
    {
        _popupWindow.dismiss();

        final View recategorizeView = getLayoutInflater().inflate(R.layout.popup_recategorize, null);

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
                this, android.R.layout.simple_spinner_item, updatedCategories);
        newCategorySpinner.setAdapter(spinnerArrayAdapter);
        newCategorySpinner.setSelection(updatedCategories.length-1); // Set to Other

        _popupWindow = new PopupWindow(recategorizeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(findViewById(R.id.settingsRootLayout), Gravity.CENTER, 0, 0);
    }

    // Called from the Confirm button of the recategorization popup
    public void confirmRecategorize(View v)
    {
        // Grab all the info before dismissing the popup
        final Spinner newCategorySpinner = _popupWindow.getContentView().findViewById(R.id.newCategorySpinner);
        _newCategoryIndex = newCategorySpinner.getSelectedItemPosition();
        _newCategoryString = newCategorySpinner.getSelectedItem().toString();
        _popupWindow.dismiss();

        final View confirmRemoveCategoryLayout = getLayoutInflater().inflate(R.layout.popup_confirm_remove_category, null);

        final TextView originalCategoryTextView = confirmRemoveCategoryLayout.findViewById(R.id.originalCategoryTextView);
        final TextView newCategoryTextView = confirmRemoveCategoryLayout.findViewById(R.id.newCategoryTextView);

        originalCategoryTextView.setText(_categories[_categoryEditIndex]);
        newCategoryTextView.setText(_newCategoryString);

        _popupWindow = new PopupWindow(confirmRemoveCategoryLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(findViewById(R.id.settingsRootLayout), Gravity.CENTER, 0, 0);
    }

    // Called from the final warning screen
    public void confirmRemoveAndRecategorize(View v)
    {
        // Update all the expenditures and budgets
        _viewModel.mergeExpenditureCategory(_exps, _categoryEditIndex, _newCategoryIndex, _newCategoryString);
        _viewModel.mergeBudgetCategory(_budgets, _categoryEditIndex, _newCategoryIndex, _newCategoryString);
        _viewModel.removeExpenditureCategory(_exps, _categoryEditIndex);
        _viewModel.removeBudgetCategory(_budgets, _categoryEditIndex);

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
        _categories = newCategories;

        // Remove the view
        _categoriesTable.removeViewAt(_categoryEditIndex+1); // +1 for the column header

        // Save the updated categories
        saveUpdatedCategories();

        _popupWindow.dismiss();
    }

    ///
    /// / Remove
    ///

    ///
    /// Add
    ///

    private void addCategory(View v)
    {
        final View categoryEditView = getLayoutInflater().inflate(R.layout.popup_edit_category, null);

        final Button confirmButton = categoryEditView.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                confirmCategoryAdd(v);
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
        _popupWindow.showAtLocation(findViewById(R.id.settingsRootLayout), Gravity.CENTER, 0, 0);
    }

    public void confirmCategoryAdd(View v)
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

        TableRow categoryRow = new TableRow(this);
        TableCell categoryCell = new TableCell(this, TableCell.DEFAULT_CELL);
        categoryCell.setText(categoriesNew[end]);
        categoryCell.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editCategory(v);
            }
        });
        categoryRow.setId(end);

        categoryRow.addView(categoryCell);
        _categoriesTable.addView(categoryRow, end+1);

        _categories = categoriesNew;

        _viewModel.addExpenditureCategory(_exps, end+1);
        _viewModel.addBudgetCategory(_budgets, end+1);

        // Save the updated categories
        saveUpdatedCategories();

        _popupWindow.dismiss();
    }

    ///
    /// / Add
    ///

    ///
    /// Resort
    ///

    private void resortCategories(View v)
    {

    }

    ///
    /// / Resort
    ///

    private void saveUpdatedCategories()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < _categories.length; i++)
        {
            sb.append(_categories[i]).append("|");
        }
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.budget_tracker_preferences_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.categories_list_key), sb.toString());
        Categories.setCategories(_categories);
        editor.commit();

        expendituresUpdated();
        budgetsUpdated();
    }
}

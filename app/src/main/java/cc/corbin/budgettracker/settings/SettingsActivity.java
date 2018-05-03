package cc.corbin.budgettracker.settings;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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


import org.w3c.dom.Text;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.TableCell;

public class SettingsActivity extends AppCompatActivity
{
    private final String TAG = "SettingsActivity";

    private String[] _categories;

    private TableLayout _categoriesTable;
    private TableCell _otherCategoryCell;

    private int _categoryEditIndex;
    private int _newCategoryIndex;
    private PopupWindow _popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        _categories = Categories.getCategories();

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
        _otherCategoryCell.setup(this, TableCell.SEMI_SPECIAL_CELL);

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

        categoriesLayout.addView(_categoriesTable);
    }

    public void cancel(View v)
    {
        _popupWindow.dismiss();
    }

    private void editCategory(View v)
    {
        _categoryEditIndex = v.getId();

        final View categoryEditView = getLayoutInflater().inflate(R.layout.popup_edit_category, null);

        final EditText categoryEditText = categoryEditView.findViewById(R.id.categoryEditText);
        categoryEditText.setHint(((TextView)v).getText());

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
                // TODO - Check if it matches any other category
                confirmButton.setEnabled(categoryEditText.getText().length() > 0);
            }
        });

        _popupWindow = new PopupWindow(categoryEditView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(findViewById(R.id.settingsRootLayout), Gravity.CENTER, 0, 0);
    }

    public void confirmCategoryEdit(View v)
    {

        _popupWindow.dismiss();
    }

    // Called from the Remove button
    public void recategorizeCategory(View v)
    {
        _popupWindow.dismiss();

        final View recategorizeView = getLayoutInflater().inflate(R.layout.popup_recategorize, null);

        final TextView currentCategoryTextView = recategorizeView.findViewById(R.id.originalCategoryTextView);
        currentCategoryTextView.setText(_categories[_categoryEditIndex]);

        final Spinner newCategorySpinner = recategorizeView.findViewById(R.id.newCategorySpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, _categories);
        newCategorySpinner.setAdapter(spinnerArrayAdapter);
        newCategorySpinner.setSelection(_categories.length-1);
        // TODO - Remove original category from list and make spinner set new category index

        _popupWindow = new PopupWindow(recategorizeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(findViewById(R.id.settingsRootLayout), Gravity.CENTER, 0, 0);
    }

    // Called from the Confirm button of the recategorization popup
    public void confirmRecategorize(View v)
    {
        _popupWindow.dismiss();

        final View confirmRemoveCategoryLayout = getLayoutInflater().inflate(R.layout.popup_confirm_remove_category, null);

        _popupWindow = new PopupWindow(confirmRemoveCategoryLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        _popupWindow.setFocusable(true);
        _popupWindow.update();
        _popupWindow.showAtLocation(findViewById(R.id.settingsRootLayout), Gravity.CENTER, 0, 0);
    }

    // Called from the final warning screen
    public void confirmRemoveAndRecategorize(View v)
    {
        _popupWindow.dismiss();
    }

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
                // TODO - Check if it matches any other category
                confirmButton.setEnabled(categoryEditText.getText().length() > 0);
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
        categoryRow.setId(end);

        categoryRow.addView(categoryCell);
        _categoriesTable.addView(categoryRow, end+1);

        _popupWindow.dismiss();
    }
}

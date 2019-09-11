package cc.corbin.budgettracker.settings;

import android.view.View;

public interface SortableCategoryListInterface
{
    // For adding
    public void cancelAddCategory();

    public void addCategory();

    public void confirmAddCategory();

    // Added as a callback to each item as they are added to the table
    public void editCategory(View v);

    // For editing
    public void cancelEditCategory();

    public void confirmEditCategory();

    public void removeAndRecategorizeCategory();
}

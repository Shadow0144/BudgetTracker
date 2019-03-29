package cc.corbin.budgettracker.workerthread.expenditureevent;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;

public class ExpMergeCategoryEvent implements ExpDatabaseEvent
{
    private final String TAG = "ExpMergeCategoryEvent";

    private int _category;
    private int _newCategory;
    private String _newCategoryName;

    public ExpMergeCategoryEvent(int category, int newCategory, String newCategoryName)
    {
        _category = category;
        _newCategory = newCategory;
        _newCategoryName = newCategoryName;
    }

    @Override
    public void processEvent(ExpenditureDatabase dbE)
    {
        dbE.expenditureDao().mergeCategory(_category, _newCategory, _newCategoryName);
    }
}

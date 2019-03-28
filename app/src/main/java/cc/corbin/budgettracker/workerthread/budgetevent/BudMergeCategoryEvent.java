package cc.corbin.budgettracker.workerthread.budgetevent;

import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudMergeCategoryEvent implements BudDatabaseEvent
{
    private final String TAG = "BudMergeCategoryEvent";

    private int _category;
    private int _newCategory;
    private String _newCategoryName;

    public BudMergeCategoryEvent(int category, int newCategory, String newCategoryName)
    {
        _category = category;
        _newCategory = newCategory;
        _newCategoryName = newCategoryName;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        dbB.budgetDao().mergeCategory(_category, _newCategory, _newCategoryName);
        new BudDBFuncHelper().updateTotalBudget(dbB, _newCategory);
    }
}

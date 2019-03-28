package cc.corbin.budgettracker.workerthread.budgetevent;

import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudRenameCategoryEvent implements BudDatabaseEvent
{
    private final String TAG = "BudRenameCategoryEvent";

    private int _category;
    private String _newCategoryName;

    public BudRenameCategoryEvent(int category, String newCategoryName)
    {
        _category = category;
        _newCategoryName = newCategoryName;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        dbB.budgetDao().renameCategory(_category, _newCategoryName);
    }
}

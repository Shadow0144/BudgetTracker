package cc.corbin.budgettracker.workerthread.budgetevent;

import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudAddCategoryEvent implements BudDatabaseEvent
{
    private final String TAG = "BudAddCategoryEvent";

    private int _category;

    public BudAddCategoryEvent(int category)
    {
        _category = _category;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        dbB.budgetDao().increaseCategoryNumbers(_category);
    }
}

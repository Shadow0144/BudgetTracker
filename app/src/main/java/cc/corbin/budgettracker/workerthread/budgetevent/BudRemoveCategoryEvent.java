package cc.corbin.budgettracker.workerthread.budgetevent;

import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudRemoveCategoryEvent implements BudDatabaseEvent
{
    private final String TAG = "BudRemoveCategoryEvent";

    private int _category;

    public BudRemoveCategoryEvent(int category)
    {
        _category = category;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        dbB.budgetDao().decreaseCategoryNumbers(_category);
    }
}

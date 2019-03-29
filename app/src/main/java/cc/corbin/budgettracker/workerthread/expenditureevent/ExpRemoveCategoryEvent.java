package cc.corbin.budgettracker.workerthread.expenditureevent;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;

public class ExpRemoveCategoryEvent implements ExpDatabaseEvent
{
    private final String TAG = "ExpRemoveCategoryEvent";

    private int _category;

    public ExpRemoveCategoryEvent(int category)
    {
        _category = category;
    }

    @Override
    public void processEvent(ExpenditureDatabase dbE)
    {
        dbE.expenditureDao().decreaseCategoryNumbers(_category);
    }
}

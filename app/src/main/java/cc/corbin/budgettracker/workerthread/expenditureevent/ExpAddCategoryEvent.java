package cc.corbin.budgettracker.workerthread.expenditureevent;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;

public class ExpAddCategoryEvent implements ExpDatabaseEvent
{
    private final String TAG = "ExpAddCategoryEvent";

    private int _category;

    public ExpAddCategoryEvent(int category)
    {
        _category = category;
    }

    @Override
    public void processEvent(ExpenditureDatabase dbE)
    {
        dbE.expenditureDao().increaseCategoryNumbers(_category);
    }
}

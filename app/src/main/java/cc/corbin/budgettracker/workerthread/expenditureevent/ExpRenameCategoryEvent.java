package cc.corbin.budgettracker.workerthread.expenditureevent;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class ExpRenameCategoryEvent implements ExpDatabaseEvent
{
    private final String TAG = "ExpRenameCategoryEvent";

    private int _category;
    private String _newCategoryName;

    public ExpRenameCategoryEvent(int category, String newCategoryName)
    {
        _category = category;
        _newCategoryName = newCategoryName;
    }

    @Override
    public void processEvent(ExpenditureDatabase dbE)
    {
        dbE.expenditureDao().renameCategory(_category, _newCategoryName);
    }
}

package cc.corbin.budgettracker.workerthread.expenditureevent;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;

public class ExpResortCategoriesEvent implements ExpDatabaseEvent
{
    private final String TAG = "ExpResortCategoriesEvent";

    private String[] _newCategoryNames;

    public ExpResortCategoriesEvent(String[] newCategoryNames)
    {
        _newCategoryNames = newCategoryNames;
    }

    @Override
    public void processEvent(ExpenditureDatabase dbE)
    {
        String[] newCategoryNames = _newCategoryNames;
        for (int i = 0; i < newCategoryNames.length; i++)
        {
            dbE.expenditureDao().updateCategoryNumber(newCategoryNames[i], i);
        }
    }
}

package cc.corbin.budgettracker.workerthread.budgetevent;

import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudResortCategoriesEvent implements BudDatabaseEvent
{
    private final String TAG = "BudResortCategoriesEvent";

    private String[] _newCategoryNames;

    public BudResortCategoriesEvent(String[] categoryNames)
    {
        _newCategoryNames = categoryNames;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        String[] newCategoryNames = _newCategoryNames;
        for (int i = 0; i < newCategoryNames.length; i++)
        {
            dbB.budgetDao().updateCategoryNumber(newCategoryNames[i], i);
        }
    }
}

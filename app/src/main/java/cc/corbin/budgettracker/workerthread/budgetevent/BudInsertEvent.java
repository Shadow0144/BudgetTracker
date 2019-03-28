package cc.corbin.budgettracker.workerthread.budgetevent;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudInsertEvent implements BudDatabaseEvent
{
    private final String TAG = "BudInsertEvent";

    private int _year;
    private int _month;
    private BudgetEntity _entity;

    public BudInsertEvent(int year, int month, BudgetEntity budgetEntity)
    {
        _year = year;
        _month = month;
        _entity = budgetEntity;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        BudgetEntity entity = _entity;
        long id = dbB.budgetDao().insert(entity);
        entity.setId(id);
        new BudDBFuncHelper().updateYearBudget(dbB, entity);
    }
}

package cc.corbin.budgettracker.workerthread.budgetevent;

import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudUpdateEvent implements BudDatabaseEvent
{
    private final String TAG = "BudUpdateEvent";

    private BudgetEntity _entity;

    public BudUpdateEvent(BudgetEntity budgetEntity)
    {
        _entity = budgetEntity;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        dbB.budgetDao().update(_entity);
        new BudDBFuncHelper().updateYearBudget(dbB, _entity);
    }
}

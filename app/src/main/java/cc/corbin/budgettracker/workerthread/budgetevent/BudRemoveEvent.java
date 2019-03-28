package cc.corbin.budgettracker.workerthread.budgetevent;

import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudRemoveEvent implements BudDatabaseEvent
{
    private final String TAG = "BudRemoveEvent";

    private BudgetEntity _entity;

    public BudRemoveEvent(BudgetEntity budgetEntity)
    {
        _entity = budgetEntity;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        dbB.budgetDao().delete(_entity);
        new BudDBFuncHelper().updateYearBudget(dbB, _entity);
    }
}

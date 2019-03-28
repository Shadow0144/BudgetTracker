package cc.corbin.budgettracker.workerthread.budgetevent;

import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudUpdateTransferEvent implements BudDatabaseEvent
{
    private final String TAG = "BudUpdateTransferEvent";

    private BudgetEntity _entity;
    private BudgetEntity _linkedEntity;

    public BudUpdateTransferEvent(BudgetEntity budgetEntity, BudgetEntity linkedBudgetEntity)
    {
        _entity = budgetEntity;
        _linkedEntity = linkedBudgetEntity;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        dbB.budgetDao().updateAmountAndNote(_entity.getId(), _entity.getAmount(), _entity.getNote());
        dbB.budgetDao().updateAmountAndNote(_linkedEntity.getId(), _linkedEntity.getAmount(), _linkedEntity.getNote());
        new BudDBFuncHelper().updateLinkedYearBudget(dbB, _entity, _linkedEntity);
    }
}

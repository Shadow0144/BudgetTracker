package cc.corbin.budgettracker.workerthread.budgetevent;

import java.util.ArrayList;
import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudRemoveTransferEvent implements BudDatabaseEvent
{
    private final String TAG = "BudRemoveTransferEvent";

    private BudgetEntity _entity;
    private BudgetEntity _linkedEntity;

    public BudRemoveTransferEvent(BudgetEntity budgetEntity, BudgetEntity linkedBudgetEntity)
    {
        _entity = budgetEntity;
        _linkedEntity = linkedBudgetEntity;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        ArrayList<BudgetEntity> entities = new ArrayList<BudgetEntity>();
        entities.add(_entity);
        entities.add(_linkedEntity);
        dbB.budgetDao().delete(entities);
        new BudDBFuncHelper().updateLinkedYearBudget(dbB, _entity, _linkedEntity);
    }
}

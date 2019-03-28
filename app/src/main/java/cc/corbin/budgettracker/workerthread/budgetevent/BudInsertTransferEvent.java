package cc.corbin.budgettracker.workerthread.budgetevent;

import java.util.ArrayList;
import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudInsertTransferEvent implements BudDatabaseEvent
{
    private final String TAG = "BudInsertTransferEvent";

    private int _year;
    private int _month;
    private BudgetEntity _entity;
    private BudgetEntity _linkedEntity;

    public BudInsertTransferEvent(int year, int month, BudgetEntity budgetEntity, BudgetEntity linkedBudgetEntity)
    {
        _year = year;
        _month = month;
        _entity = budgetEntity;
        _linkedEntity = linkedBudgetEntity;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        // Insert the entities to get IDs, then update them with their new links
        ArrayList<BudgetEntity> entities = new ArrayList<BudgetEntity>();
        entities.add(_entity);
        entities.add(_linkedEntity);
        long[] ids = dbB.budgetDao().insertAll(entities);

        // Need to set the IDs and link data to update the linkage
        _entity.setId(ids[0]);
        _linkedEntity.setId(ids[1]);
        _entity.setLinkedAdjustment(ids[1], _linkedEntity.getMonth(), _linkedEntity.getYear(), _linkedEntity.getCategory());
        _linkedEntity.setLinkedAdjustment(ids[0], _entity.getMonth(), _entity.getYear(), _entity.getCategory());

        dbB.budgetDao().update(entities);
        new BudDBFuncHelper().updateLinkedYearBudget(dbB, _entity, _linkedEntity);
    }
}

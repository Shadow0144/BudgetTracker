package cc.corbin.budgettracker.workerthread.expenditureevent;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class ExpRemoveEvent implements ExpDatabaseEvent
{
    private final String TAG = "ExpRemoveEvent";

    private ExpenditureEntity _entity;

    public ExpRemoveEvent(ExpenditureEntity entity)
    {
        _entity = entity;
    }

    @Override
    public void processEvent(ExpenditureDatabase dbE)
    {
        dbE.expenditureDao().delete(_entity);
    }
}

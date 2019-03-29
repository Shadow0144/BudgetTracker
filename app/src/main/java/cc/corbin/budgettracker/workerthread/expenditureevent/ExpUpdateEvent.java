package cc.corbin.budgettracker.workerthread.expenditureevent;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class ExpUpdateEvent implements ExpDatabaseEvent
{
    private final String TAG = "ExpUpdateEvent";

    private ExpenditureEntity _entity;

    public ExpUpdateEvent(ExpenditureEntity entity)
    {
        _entity = entity;
    }

    @Override
    public void processEvent(ExpenditureDatabase dbE)
    {
        dbE.expenditureDao().update(_entity);
    }
}

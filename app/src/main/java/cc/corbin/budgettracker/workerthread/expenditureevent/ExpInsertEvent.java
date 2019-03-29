package cc.corbin.budgettracker.workerthread.expenditureevent;

import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class ExpInsertEvent implements ExpDatabaseEvent
{
    private final String TAG = "ExpInsertEvent";

    private ExpenditureEntity _entity;

    public ExpInsertEvent(ExpenditureEntity entity)
    {
        _entity = entity;
    }

    @Override
    public void processEvent(ExpenditureDatabase dbE)
    {
        long id = dbE.expenditureDao().insert(_entity);
        _entity.setId(id);
    }
}

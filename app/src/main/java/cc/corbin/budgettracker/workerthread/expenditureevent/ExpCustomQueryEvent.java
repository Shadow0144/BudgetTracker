package cc.corbin.budgettracker.workerthread.expenditureevent;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SimpleSQLiteQuery;

import java.util.List;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class ExpCustomQueryEvent implements ExpDatabaseEvent
{
    private final String TAG = "ExpCustomQueryEvent";

    private String _query;
    private MutableLiveData<List<ExpenditureEntity>> _mutableLiveData;

    public ExpCustomQueryEvent(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, String query)
    {
        _mutableLiveData = mutableLiveData;
        _query = query;
    }

    @Override
    public void processEvent(ExpenditureDatabase dbE)
    {
        List<ExpenditureEntity> entities = dbE.expenditureDao().customQuery(new SimpleSQLiteQuery(_query));
        _mutableLiveData.postValue(entities);
    }
}

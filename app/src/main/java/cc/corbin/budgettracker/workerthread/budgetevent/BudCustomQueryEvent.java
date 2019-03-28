package cc.corbin.budgettracker.workerthread.budgetevent;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SimpleSQLiteQuery;

import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudCustomQueryEvent implements BudDatabaseEvent
{
    private final String TAG = "BudCustomQueryEvent";

    private String _query;
    private MutableLiveData<List<BudgetEntity>> _mutableLiveData;

    public BudCustomQueryEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, String query)
    {
        _mutableLiveData = mutableLiveData;
        _query = query;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        _mutableLiveData.postValue(dbB.budgetDao().customQuery(new SimpleSQLiteQuery(_query)));
    }
}

package cc.corbin.budgettracker.workerthread.expenditureevent;

import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class ExpQueryEvent implements ExpDatabaseEvent
{
    private final String TAG = "ExpQueryEvent";

    public enum QueryType
    {
        day,
        month,
        year,
        total
    };

    private int _year;
    private int _month;
    private int _day;
    private QueryType _queryType;
    private MutableLiveData<List<ExpenditureEntity>> _mutableLiveData;

    public ExpQueryEvent(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int year, int month, int day)
    {
        _year = year;
        _month = month;
        _day = day;
        _mutableLiveData = mutableLiveData;
        _queryType = QueryType.day;
    }

    public ExpQueryEvent(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int year, int month)
    {
        _year = year;
        _month = month;
        _mutableLiveData = mutableLiveData;
        _queryType = QueryType.month;
    }

    public ExpQueryEvent(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int year)
    {
        _year = year;
        _mutableLiveData = mutableLiveData;
        _queryType = QueryType.year;
    }

    public ExpQueryEvent(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        _mutableLiveData = mutableLiveData;
        _queryType = QueryType.total;
    }

    @Override
    public void processEvent(ExpenditureDatabase dbE)
    {
        List<ExpenditureEntity> entities = null;
        switch (_queryType)
        {
            case day:
                entities = dbE.expenditureDao().getDay(_year, _month, _day);
                break;
            case month:
                entities = dbE.expenditureDao().getMonth(_year, _month);
                break;
            case year:
                entities = dbE.expenditureDao().getYear(_year);
                break;
            case total:
                entities = dbE.expenditureDao().getTotal();
                break;
        }
        _mutableLiveData.postValue(entities);
    }
}

package cc.corbin.budgettracker.workerthread;

import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class ExpDatabaseEvent
{
    private final String TAG = "ExpDatabaseEvent";

    private static int id = 0;

    public enum EventType
    {
        query,
        insert,
        update,
        remove,
        recategorize
    };

    public enum QueryType
    {
        day,
        month,
        year,
        total
    }

    private MutableLiveData<List<ExpenditureEntity>> _mutableLiveData;

    private int _id;
    private int _year;
    private int _month;
    private int _day;
    private int _category;
    private String _newCategoryName;
    private EventType _eventType;
    private ExpenditureEntity _entity;
    private QueryType _queryType;
    private List<ExpenditureEntity> _entities;

    public ExpDatabaseEvent(MutableLiveData<List<ExpenditureEntity>> mutableLiveData,
                            EventType eventType, int year, int month, int day, ExpenditureEntity entity)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _year = year;
        _month = month;
        _day = day;
        _entity = entity;
        _id = id++;
    }

    public ExpDatabaseEvent(MutableLiveData<List<ExpenditureEntity>> mutableLiveData,
                            EventType eventType, int year, int month, int day, QueryType queryType)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _year = year;
        _month = month;
        _day = day;
        _queryType = queryType;
        _id = id++;
    }

    public ExpDatabaseEvent(MutableLiveData<List<ExpenditureEntity>> mutableLiveData,
                            EventType eventType, int category, String newCategoryName)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _category = category;
        _newCategoryName = newCategoryName;
        _id = id++;
    }

    public MutableLiveData<List<ExpenditureEntity>> getMutableLiveData()
    {
        return _mutableLiveData;
    }

    public ExpenditureEntity getEntity()
    {
        return _entity;
    }

    public void setEntities(List<ExpenditureEntity> entities)
    {
        _entities = entities;
    }

    public List<ExpenditureEntity> getEntities()
    {
        return _entities;
    }

    public EventType getEventType()
    {
        return _eventType;
    }

    public QueryType getQueryType()
    {
        return _queryType;
    }

    public int getId()
    {
        return _id;
    }

    public int getYear()
    {
        return _year;
    }

    public int getMonth()
    {
        return _month;
    }

    public int getDay()
    {
        return _day;
    }

    public int getCategory()
    {
        return _category;
    }

    public String getNewCategoryName()
    {
        return _newCategoryName;
    }
}
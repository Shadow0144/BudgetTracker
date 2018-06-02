package cc.corbin.budgettracker.workerthread;

import android.arch.lifecycle.MutableLiveData;
import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudgetDatabaseEvent
{
    private final String TAG = "BudgetDatabaseEvent";

    private static int id = 0;

    public enum EventType
    {
        query,
        insert,
        update,
        remove,
        recategorize,
        addcategory,
        removecategory
    };

    public enum QueryType
    {
        month,
        year,
        total
    };

    private MutableLiveData<List<BudgetEntity>> _mutableLiveData;

    private int _id;
    private int _year;
    private int _month;
    private int _category;
    private String _newCategoryName;
    private EventType _eventType;
    private QueryType _queryType;
    private BudgetEntity _entity;
    private List<BudgetEntity> _entities;

    public BudgetDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
                            int year, int month, BudgetEntity entity)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _year = year;
        _month = month;
        _entity = entity;
        _id = id++;
        _entities = null;
    }

    public BudgetDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
                            int year, int month, QueryType queryType)
    {
        _mutableLiveData = mutableLiveData;
        _queryType = queryType;
        _eventType = eventType;
        _year = year;
        _month = month; // Can also be used as a final year
        _id = id++;
        _entities = null;
    }

    public BudgetDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
                               int category, String newCategoryName)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _category = category;
        _newCategoryName = newCategoryName;
        _id = id++;
        _entities = null;
    }

    public BudgetDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
                               int category)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _category = category;
        _id = id++;
        _entities = null;
    }

    public MutableLiveData<List<BudgetEntity>> getMutableLiveData()
    {
        return _mutableLiveData;
    }

    public void setEntities(List<BudgetEntity> entities)
    {
        _entities = entities;
    }

    public List<BudgetEntity> getEntities()
    {
        return _entities;
    }

    public BudgetEntity getEntity()
    {
        return _entity;
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

    public int getCategory()
    {
        return _category;
    }

    public String getNewCategoryName()
    {
        return _newCategoryName;
    }
}
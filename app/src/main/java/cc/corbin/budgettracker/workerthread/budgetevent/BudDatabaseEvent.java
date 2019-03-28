package cc.corbin.budgettracker.workerthread.budgetevent;

import android.arch.lifecycle.MutableLiveData;
import java.util.List;

import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudDatabaseEvent
{
    private final String TAG = "BudDatabaseEvent";

    private static int id = 0;

    public enum EventType
    {
        query,
        insert,
        insertTransfer,
        update,
        updateTransfer,
        remove,
        removeTransfer,
        renameCategory,
        mergeCategory,
        addCategory,
        removeCategory,
        resortCategories,
        customQuery
    };

    public enum QueryType
    {
        month,
        months,
        year,
        total
    };

    private MutableLiveData<List<BudgetEntity>> _mutableLiveData;

    private int _id;
    private int _year;
    private int _month;
    private int _category;
    private int _newCategory;
    private String _newCategoryName;
    private String[] _newCategoryNames;
    private String _query;
    private EventType _eventType;
    private QueryType _queryType;
    private BudgetEntity _entity;
    private BudgetEntity _linkedEntity;
    private List<BudgetEntity> _entities;

    public BudDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
                            int year, int month, BudgetEntity entity)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _year = year;
        _month = month;
        _entity = entity;
        _category = entity.getCategory(); // Set this in case of updating
        _id = id++;
        _entities = null;
    }

    public BudDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
                            int year, int month, BudgetEntity entity, BudgetEntity linkedEntity)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _year = year;
        _month = month;
        _entity = entity;
        _linkedEntity = linkedEntity;
        _id = id++;
    }

    public BudDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
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

    // Rename
    public BudDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
                            int category, String newCategoryName)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _category = category;
        _newCategoryName = newCategoryName;
        _id = id++;
        _entities = null;
    }

    // Add or remove
    public BudDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
                            int category)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _category = category;
        _id = id++;
        _entities = null;
    }

    // Merge
    public BudDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
                            int category, int newCategory, String newCategoryName)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _category = category;
        _newCategory = newCategory;
        _newCategoryName = newCategoryName;
        _id = id++;
        _entities = null;
    }

    // Categories resorted
    public BudDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
                            String[] newCategoryNames)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _newCategoryNames = newCategoryNames;
        _id = id++;
        _entities = null;
    }

    public BudDatabaseEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, EventType eventType,
                            String query)
    {
        _mutableLiveData = mutableLiveData;
        _eventType = eventType;
        _query = query;
        _id++;
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

    public BudgetEntity getLinkedEntity()
    {
        return _linkedEntity;
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

    public int getNewCategory()
    {
        return _newCategory;
    }

    public String getNewCategoryName()
    {
        return _newCategoryName;
    }

    public String[] getNewCategoryNames()
    {
        return _newCategoryNames;
    }

    public String getQuery()
    {
        return _query;
    }
}
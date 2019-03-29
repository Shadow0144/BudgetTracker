package cc.corbin.budgettracker.workerthread.expenditureevent;

import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public interface ExpDatabaseEvent
{
    /*private final String TAG = "ExpDatabaseEvent";

    private static int id = 0;

    public enum EventType
    {
        query,
        insert,
        update,
        remove,
        renameCategory,
        mergeCategory,
        addCategory,
        removeCategory,
        resortCategories,
        customQuery
    };

    private int _id;
    private int _year;
    private int _month;
    private int _day;
    private int _category;
    private int _newCategory;
    private String _newCategoryName;
    private String[] _newCategoryNames;
    private String _query;
    private EventType _eventType;
    private ExpenditureEntity _entity;
    private QueryType _queryType;
    private List<ExpenditureEntity> _entities;
    private MutableLiveData<List<ExpenditureEntity>> _mutableLiveData;
    */

    void processEvent(ExpenditureDatabase dbE);
}
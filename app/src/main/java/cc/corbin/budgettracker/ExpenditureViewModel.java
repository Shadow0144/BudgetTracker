package cc.corbin.budgettracker;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.util.Log;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Corbin on 3/14/2018.
 */

class ExpDatabaseEvent
{
    private final String TAG = "ExpDatabaseEvent";

    private static int id = 0;

    public enum EventType
    {
        query,
        insert,
        update,
        remove
    };

    public enum QueryType
    {
        day,
        month,
        year
    }

    private MutableLiveData<List<ExpenditureEntity>> _mutableLiveData;

    private int _id;
    private int _year;
    private int _month;
    private int _day;
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
}

class BudDatabaseEvent
{
    private final String TAG = "BudDatabaseEvent";

    private static int id = 0;

    public enum EventType
    {
        insert,
        update,
        remove
    };

    private int _id;
    private int _year;
    private int _month;
    private int _day;
    private EventType _eventType;
    private BudgetEntity _entity;

    public BudDatabaseEvent(EventType eventType, int year, int month, int day, BudgetEntity entity)
    {
        _eventType = eventType;
        _year = year;
        _month = month;
        _day = day;
        _entity = entity;
        _id = id++;
    }

    public BudgetEntity getEntity()
    {
        return _entity;
    }

    public EventType getEventType()
    {
        return _eventType;
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
}

class DatabaseThread extends Thread
{
    private final String TAG = "DatabaseThread";

    private ConcurrentLinkedQueue<ExpDatabaseEvent> _expEvents;
    private ConcurrentLinkedQueue<BudDatabaseEvent> _budEvents;
    private ConcurrentLinkedQueue<ExpDatabaseEvent> _completedExpEvents;
    private ConcurrentLinkedQueue<BudDatabaseEvent> _completedBudEvents;

    private ExpenditureDatabase _dbE;
    private BudgetDatabase _dbB;

    private Handler _handler;

    private volatile boolean _running;

    public DatabaseThread(ExpenditureDatabase dbE, BudgetDatabase dbB,
                          ConcurrentLinkedQueue<ExpDatabaseEvent> eEvents,
                          ConcurrentLinkedQueue<BudDatabaseEvent> bEvents,
                          ConcurrentLinkedQueue<ExpDatabaseEvent> completedExpEvents,
                          ConcurrentLinkedQueue<BudDatabaseEvent> completedBudEvents,
                          Handler handler)
    {
        _dbE = dbE;
        _dbB = dbB;
        _expEvents = eEvents;
        _budEvents = bEvents;
        _completedExpEvents = completedExpEvents;
        _completedBudEvents = completedBudEvents;
        _handler = handler;
    }

    @Override
    public void run()
    {
        _running = true;
        while (_running)
        {
            if (!_expEvents.isEmpty())
            {
                processExpEvent(_expEvents.poll());
            }
            else { }

            if (!_budEvents.isEmpty())
            {
                processBudEvent(_budEvents.poll());
            }
            else { }
        }
    }

    public void finish()
    {
        while (!_expEvents.isEmpty() || !_budEvents.isEmpty());
        _running = false;
    }

    private void processExpEvent(ExpDatabaseEvent event)
    {
        switch (event.getEventType())
        {
            case query:
                List<ExpenditureEntity> entities = null;
                switch (event.getQueryType())
                {
                    case day:
                        entities = _dbE.expenditureDao().getDay(event.getYear(), event.getMonth(), event.getDay());
                        break;
                    case month:
                        entities = _dbE.expenditureDao().getMonth(event.getYear(), event.getMonth());
                        break;
                    case year:
                        entities = _dbE.expenditureDao().getYear(event.getYear());
                        break;
                }
                event.setEntities(entities);
                _completedExpEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;

            case insert:
                long id = _dbE.expenditureDao().insert(event.getEntity());
                event.getEntity().setId(id);
                _completedExpEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;

            case update:
                _dbE.expenditureDao().update(event.getEntity());
                _completedExpEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;

            case remove:
                _dbE.expenditureDao().delete(event.getEntity());
                _completedExpEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;
        }
    }

    private void processBudEvent(BudDatabaseEvent event)
    {
        switch (event.getEventType())
        {
            case insert:
                _dbB.budgetDao().insertAll(event.getEntity());
                _handler.post(new ExpenditureRunnable());
                break;
            case update:
                _dbB.budgetDao().update(event.getEntity());
                _handler.post(new ExpenditureRunnable());
                break;
            case remove:
                _dbB.budgetDao().delete(event.getEntity());
                _handler.post(new ExpenditureRunnable());
                break;
        }
    }
}

class ExpenditureRunnable implements Runnable
{
    private final String TAG = "ExpenditureRunnable";

    public static ExpenditureViewModel viewModel;

    @Override
    public void run()
    {
        viewModel.checkQueue();
    }
}

public class ExpenditureViewModel extends ViewModel
{
    private final String TAG = "ExpenditureViewModel";

    private DatabaseThread _dataBaseThread;

    private ExpenditureDatabase _dbE;
    private BudgetDatabase _dbB;

    private int _year;
    private int _month;
    private int _day;

    private ConcurrentLinkedQueue<ExpDatabaseEvent> _expEvents;
    private ConcurrentLinkedQueue<BudDatabaseEvent> _budEvents;
    private ConcurrentLinkedQueue<ExpDatabaseEvent> _completedExpEvents;
    private ConcurrentLinkedQueue<BudDatabaseEvent> _completedBudEvents;

    private Handler _handler;

    public ExpenditureViewModel()
    {
        _expEvents = new ConcurrentLinkedQueue<ExpDatabaseEvent>();
        _budEvents = new ConcurrentLinkedQueue<BudDatabaseEvent>();
        _completedExpEvents = new ConcurrentLinkedQueue<ExpDatabaseEvent>();
        _completedBudEvents = new ConcurrentLinkedQueue<BudDatabaseEvent>();
        _dbE = null;
        _dbB = null;
        ExpenditureRunnable.viewModel = this;
    }

    public void setDatabases(ExpenditureDatabase dbE, BudgetDatabase dbB)
    {
        if (_dbE == null || _dbB == null)
        {
            _handler = new Handler();
            _dbE = dbE;
            _dbB = dbB;
            _dataBaseThread = new DatabaseThread(_dbE, _dbB,
                    _expEvents, _budEvents,
                    _completedExpEvents, _completedBudEvents,
                    _handler);
            _dataBaseThread.start();
        }
        else { }
    }

    public void setDate(int year, int month, int day)
    {
        _year = year;
        _month = month;
        _day = day;
    }

    @Override
    public void onCleared()
    {
        _dataBaseThread.finish();
        try
        {
            _dataBaseThread.join();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }
    }

    // When a database event has finished
    public void checkQueue()
    {
        while (!_completedExpEvents.isEmpty())
        {
            ExpDatabaseEvent expDatabaseEvent = _completedExpEvents.poll();
            expDatabaseEvent.getMutableLiveData().postValue(expDatabaseEvent.getEntities());
        }
    }

    public void getDay(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.query, _year, _month, _day, ExpDatabaseEvent.QueryType.day);
        _expEvents.add(event);
    }

    public void getMonth(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.query, _year, _month, _day, ExpDatabaseEvent.QueryType.month);
        _expEvents.add(event);
    }

    public void getYear(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.query, _year, _month, _day, ExpDatabaseEvent.QueryType.year);
        _expEvents.add(event);
    }

    public void insertExpEntity(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.insert, _year, _month, _day, entity);
        _expEvents.add(event);
    }

    public void insertExpEntities(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, List<ExpenditureEntity> entities)
    {
        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.insert, _year, _month, _day, entities.get(i));
            _expEvents.add(event);
        }
    }

    public void updateExpEntity(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.update, _year, _month, _day, entity);
        _expEvents.add(event);
    }

    public void updateExpEntities(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, List<ExpenditureEntity> entities)
    {
        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.update, _year, _month, _day, entities.get(i));
            _expEvents.add(event);
        }
    }

    public void removeExpEntity(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.remove, _year, _month, _day, entity);
        _expEvents.add(event);
    }

    public void insertBudgetEntity(BudgetEntity budgetEntity)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(BudDatabaseEvent.EventType.insert, _year, _month, _day, budgetEntity);
        _budEvents.add(event);
    }

    public void insertBudgetEntities(List<BudgetEntity> budgetEntities)
    {
        int size = budgetEntities.size();
        for (int i = 0; i < size; i++)
        {
            BudDatabaseEvent event = new BudDatabaseEvent(BudDatabaseEvent.EventType.insert, _year, _month, _day, budgetEntities.get(i));
            _budEvents.add(event);
        }
    }
}

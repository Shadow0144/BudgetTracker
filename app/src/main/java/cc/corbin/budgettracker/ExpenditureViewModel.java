package cc.corbin.budgettracker;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Corbin on 3/14/2018.
 */

class DatabaseEvent
{
    private final String TAG = "DatabaseEvent";

    private static int id = 0;

    public enum EventType
    {
        insert,
        update,
        remove
    };

    private int _id;
    private EventType _eventType;
    private ExpenditureEntity _entity;

    public DatabaseEvent(EventType eventType, ExpenditureEntity entity)
    {
        _eventType = eventType;
        _entity = entity;
        _id = id++;
    }

    public ExpenditureEntity getEntity()
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
}

class DatabaseThread extends Thread
{
    private final String TAG = "DatabaseThread";

    private ConcurrentLinkedQueue<DatabaseEvent> _events;
    private ConcurrentLinkedQueue<Integer> _completedEvents;

    private ExpenditureDatabase _db;

    private boolean _running;

    public DatabaseThread(ExpenditureDatabase db, ConcurrentLinkedQueue<DatabaseEvent> events, ConcurrentLinkedQueue<Integer> completedEvents)
    {
        _db = db;
        _events = events;
        _completedEvents = completedEvents;
    }

    @Override
    public void run()
    {
        _running = true;
        while (_running)
        {
            if (!_events.isEmpty())
            {
                Log.e(TAG, "Event!");
                processEvent(_events.poll());
            }
            else { }
        }
    }

    public void finish()
    {
        _running = false;
    }

    private void processEvent(DatabaseEvent event)
    {
        switch (event.getEventType())
        {
            case insert:
                _db.expenditureDao().insertAll(event.getEntity());
                Log.e(TAG, "Inserted: "+event.getEntity().getYear()+" "+event.getEntity().getMonth()+" "+event.getEntity().getDay());
                break;
            case update:
                _db.expenditureDao().update(event.getEntity());
                Log.e(TAG, "Updated");
                break;
            case remove:
                _db.expenditureDao().delete(event.getEntity());
                Log.e(TAG, "Removed");
                break;
        }
    }
}

public class ExpenditureViewModel extends ViewModel
{
    private final String TAG = "ExpenditureViewModel";

    private DatabaseThread _dataBaseThread;

    private ExpenditureDatabase _db;

    private ConcurrentLinkedQueue<DatabaseEvent> _events;
    private ConcurrentLinkedQueue<Integer> _completedEvents;

    public ExpenditureViewModel()
    {
        _events = new ConcurrentLinkedQueue<DatabaseEvent>();
        _completedEvents = new ConcurrentLinkedQueue<Integer>();
        _db = null;
    }

    public void setDatabase(ExpenditureDatabase db)
    {
        if (_db == null)
        {
            _db = db;
            _dataBaseThread = new DatabaseThread(_db, _events, _completedEvents);
            _dataBaseThread.start();
            Log.e(TAG, "Started");
        }
        else { }
    }

    @Override
    public void onCleared()
    {
        _dataBaseThread.finish();
        Log.e(TAG, "Finished");
        try
        {
            _dataBaseThread.join();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }
    }

    public LiveData<List<ExpenditureEntity>> getDay(int year, int month, int day)
    {
        return _db.expenditureDao().getDay(year, month, day);
    }

    public LiveData<List<ExpenditureEntity>> getWeek(int year, int month, int week)
    {
        return _db.expenditureDao().getTimeSpan(year, month, (((week-1)*7)+1), (((week)*7)));
    }

    public LiveData<List<ExpenditureEntity>> getMonth(int year, int month)
    {
        return _db.expenditureDao().getTimeSpan(year, month, 0, 32);
    }

    public void insertEntity(ExpenditureEntity entity)
    {
        DatabaseEvent event = new DatabaseEvent(DatabaseEvent.EventType.insert, entity);
        _events.add(event);
    }

    public void insertEntities(List<ExpenditureEntity> entities)
    {
        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            DatabaseEvent event = new DatabaseEvent(DatabaseEvent.EventType.insert, entities.get(i));
            _events.add(event);
        }
    }

    public void updateEntity(ExpenditureEntity entity)
    {
        DatabaseEvent event = new DatabaseEvent(DatabaseEvent.EventType.update, entity);
        _events.add(event);
    }

    public void updateEntities(List<ExpenditureEntity> entities)
    {
        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            DatabaseEvent event = new DatabaseEvent(DatabaseEvent.EventType.update, entities.get(i));
            _events.add(event);
        }
    }

    public void removeEntity(ExpenditureEntity entity)
    {
        DatabaseEvent event = new DatabaseEvent(DatabaseEvent.EventType.remove, entity);
        _events.add(event);
    }
}

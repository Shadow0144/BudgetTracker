package cc.corbin.budgettracker.workerthread;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.util.Log;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 3/14/2018.
 */

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
    private ConcurrentLinkedQueue<BudgetDatabaseEvent> _budEvents;
    private ConcurrentLinkedQueue<ExpDatabaseEvent> _completedExpEvents;
    private ConcurrentLinkedQueue<BudgetDatabaseEvent> _completedBudEvents;

    private Handler _handler;

    public ExpenditureViewModel()
    {
        _expEvents = new ConcurrentLinkedQueue<ExpDatabaseEvent>();
        _budEvents = new ConcurrentLinkedQueue<BudgetDatabaseEvent>();
        _completedExpEvents = new ConcurrentLinkedQueue<ExpDatabaseEvent>();
        _completedBudEvents = new ConcurrentLinkedQueue<BudgetDatabaseEvent>();
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
            if (expDatabaseEvent.getMutableLiveData() != null)
            {
                expDatabaseEvent.getMutableLiveData().postValue(expDatabaseEvent.getEntities());
            }
            else { }
        }

        while (!_completedBudEvents.isEmpty())
        {
            BudgetDatabaseEvent budgetDatabaseEvent = _completedBudEvents.poll();
            if (budgetDatabaseEvent.getMutableLiveData() != null)
            {
                budgetDatabaseEvent.getMutableLiveData().postValue(budgetDatabaseEvent.getEntities());
            }
            else { }
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

    public void getTotal(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.query, _year, _month, _day, ExpDatabaseEvent.QueryType.total);
        _expEvents.add(event);

    }

    public void insertExpEntity(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.insert, _year, _month, _day, entity);
        _expEvents.add(event);
    }

    public void updateExpEntity(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.update, _year, _month, _day, entity);
        _expEvents.add(event);
    }

    public void removeExpEntity(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.remove, _year, _month, _day, entity);
        _expEvents.add(event);
    }

    public void recategorizeExpenditureEntities(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, String oldCategory, String newCategory)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.recategorize, oldCategory, newCategory);
        _expEvents.add(event);
    }

    public void getMonthBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData)
    {
        BudgetDatabaseEvent event = new BudgetDatabaseEvent(mutableLiveData, BudgetDatabaseEvent.EventType.query, _year, _month, BudgetDatabaseEvent.QueryType.month);
        _budEvents.add(event);
    }

    public void getYearBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData)
    {
        BudgetDatabaseEvent event = new BudgetDatabaseEvent(mutableLiveData, BudgetDatabaseEvent.EventType.query, _year, _month, BudgetDatabaseEvent.QueryType.year);
        _budEvents.add(event);
    }

    public void insertBudgetEntity(MutableLiveData<List<BudgetEntity>> mutableLiveData, BudgetEntity budgetEntity)
    {
        BudgetDatabaseEvent event = new BudgetDatabaseEvent(mutableLiveData, BudgetDatabaseEvent.EventType.insert, _year, _month, budgetEntity);
        _budEvents.add(event);
    }

    public void updateBudgetEntity(MutableLiveData<List<BudgetEntity>> mutableLiveData, BudgetEntity budgetEntity)
    {
        BudgetDatabaseEvent event = new BudgetDatabaseEvent(mutableLiveData, BudgetDatabaseEvent.EventType.update, _year, _month, budgetEntity);
        _budEvents.add(event);
    }

    public void removeBudgetEntity(MutableLiveData<List<BudgetEntity>> mutableLiveData, BudgetEntity budgetEntity)
    {
        BudgetDatabaseEvent event = new BudgetDatabaseEvent(mutableLiveData, BudgetDatabaseEvent.EventType.remove, _year, _month, budgetEntity);
        _budEvents.add(event);
    }

    public void recategorizeBudgetEntities(MutableLiveData<List<BudgetEntity>> mutableLiveData, String oldCategory, String newCategory)
    {
        BudgetDatabaseEvent event = new BudgetDatabaseEvent(mutableLiveData, BudgetDatabaseEvent.EventType.recategorize, oldCategory, newCategory);
        _budEvents.add(event);
    }
}

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

    private static DatabaseThread _dataBaseThread;

    private static ExpenditureDatabase _dbE;
    private static BudgetDatabase _dbB;

    private static int _year;
    private static int _month;
    private static int _day;

    private static ConcurrentLinkedQueue<ExpDatabaseEvent> _expEvents;
    private static ConcurrentLinkedQueue<BudgetDatabaseEvent> _budEvents;
    private static ConcurrentLinkedQueue<ExpDatabaseEvent> _completedExpEvents;
    private static ConcurrentLinkedQueue<BudgetDatabaseEvent> _completedBudEvents;

    private static Handler _handler;

    public ExpenditureViewModel()
    {
        if (_expEvents == null || _budEvents == null)
        {
            _expEvents = new ConcurrentLinkedQueue<ExpDatabaseEvent>();
            _budEvents = new ConcurrentLinkedQueue<BudgetDatabaseEvent>();
            _completedExpEvents = new ConcurrentLinkedQueue<ExpDatabaseEvent>();
            _completedBudEvents = new ConcurrentLinkedQueue<BudgetDatabaseEvent>();
            _dbE = null;
            _dbB = null;
            _handler = new Handler();
        }
        else { }
        ExpenditureRunnable.viewModel = this;
    }

    public void setDatabases(ExpenditureDatabase dbE, BudgetDatabase dbB)
    {
        if (_dataBaseThread == null || _dbE == null || _dbB == null)
        {
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
        Log.e(TAG, "Cleared");
        if (_dataBaseThread != null)
        {
            _dataBaseThread.finish();
            try
            {
                _dataBaseThread.join();
                _dataBaseThread = null;
                _dbE = null;
                _dbB = null;
            }
            catch (Exception e)
            {
                Log.e(TAG, e.toString());
            }
        }
        else { }
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
        Log.e(TAG, "Adding day query");
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

    public void recategorizeExpenditureEntities(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int category, String newCategoryName)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.recategorize, category, newCategoryName);
        _expEvents.add(event);
    }

    public void addExpenditureCategory(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int category)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.addcategory, category);
        _expEvents.add(event);
    }

    public void removeExpenditureCategory(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int category)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.removecategory, category);
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

    public void getTotalBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData, int startYear, int endYear)
    {
        BudgetDatabaseEvent event = new BudgetDatabaseEvent(mutableLiveData, BudgetDatabaseEvent.EventType.query, startYear, endYear, BudgetDatabaseEvent.QueryType.total);
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

    public void recategorizeBudgetEntities(MutableLiveData<List<BudgetEntity>> mutableLiveData, int category, String newCategoryName)
    {
        BudgetDatabaseEvent event = new BudgetDatabaseEvent(mutableLiveData, BudgetDatabaseEvent.EventType.recategorize, category, newCategoryName);
        _budEvents.add(event);
    }

    public void addBudgetCategory(MutableLiveData<List<BudgetEntity>> mutableLiveData, int category)
    {
        BudgetDatabaseEvent event = new BudgetDatabaseEvent(mutableLiveData, BudgetDatabaseEvent.EventType.addcategory, category);
        _budEvents.add(event);
    }

    public void removeBudgetCategory(MutableLiveData<List<BudgetEntity>> mutableLiveData, int category)
    {
        BudgetDatabaseEvent event = new BudgetDatabaseEvent(mutableLiveData, BudgetDatabaseEvent.EventType.removecategory, category);
        _budEvents.add(event);
    }
}

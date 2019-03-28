package cc.corbin.budgettracker.workerthread;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.workerthread.budgetevent.BudDatabaseEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpDatabaseEvent;

/**
 * Created by Corbin on 3/14/2018.
 */

public class ExpenditureViewModel extends ViewModel
{
    private static final String TAG = "ExpenditureViewModel";

    private static ExpenditureDatabase _dbE;
    private static BudgetDatabase _dbB;

    private static int _year;
    private static int _month;
    private static int _day;

    private static ConcurrentLinkedQueue<ExpDatabaseEvent> _expEvents;
    private static ConcurrentLinkedQueue<BudDatabaseEvent> _budEvents;
    private static ConcurrentLinkedQueue<ExpDatabaseEvent> _completedExpEvents;
    private static ConcurrentLinkedQueue<BudDatabaseEvent> _completedBudEvents;

    private static AsyncTask<Void, Void, Void> _queuer;
    private static DatabaseThread _thread;

    public ExpenditureViewModel()
    {
        if (_expEvents == null || _budEvents == null)
        {
            _expEvents = new ConcurrentLinkedQueue<ExpDatabaseEvent>();
            _budEvents = new ConcurrentLinkedQueue<BudDatabaseEvent>();
            _completedExpEvents = new ConcurrentLinkedQueue<ExpDatabaseEvent>();
            _completedBudEvents = new ConcurrentLinkedQueue<BudDatabaseEvent>();
            _dbE = null;
            _dbB = null;
        }
        else { }
    }

    public void setDatabases(ExpenditureDatabase dbE, BudgetDatabase dbB)
    {
        _dbE = dbE;
        _dbB = dbB;
        _thread = new DatabaseThread(_dbE, _dbB,
                _expEvents, _budEvents,
                _completedExpEvents, _completedBudEvents);
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
        super.onCleared();
    }

    public static void shutdown()
    {
        _dbE = null;
        _dbB = null;
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
            BudDatabaseEvent budDatabaseEvent = _completedBudEvents.poll();
            if (budDatabaseEvent.getMutableLiveData() != null)
            {
                budDatabaseEvent.getMutableLiveData().postValue(budDatabaseEvent.getEntities());
            }
            else { }
        }
    }

    public void processQueue()
    {
        // Ensure the thread is setup with the databases and that the queuer is either null or not running
        if ((_thread != null) && ((_queuer == null) || (_queuer.getStatus() != AsyncTask.Status.RUNNING)))
        {
            _queuer = new DatabaseAsyncTask(this, _thread);
            _queuer.execute();
        }
        else { }
    }

    public void getDay(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.query, _year, _month, _day, ExpDatabaseEvent.QueryType.day);
        _expEvents.add(event);
        processQueue();
    }

    public void getMonth(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.query, _year, _month, _day, ExpDatabaseEvent.QueryType.month);
        _expEvents.add(event);
        processQueue();
    }

    public void getYear(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.query, _year, _month, _day, ExpDatabaseEvent.QueryType.year);
        _expEvents.add(event);
        processQueue();
    }

    public void getTotal(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.query, _year, _month, _day, ExpDatabaseEvent.QueryType.total);
        _expEvents.add(event);
        processQueue();
    }

    public void insertExpEntity(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.insert, _year, _month, _day, entity);
        _expEvents.add(event);
        processQueue();
    }

    public void updateExpEntity(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.update, _year, _month, _day, entity);
        _expEvents.add(event);
        processQueue();
    }

    public void removeExpEntity(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.remove, _year, _month, _day, entity);
        _expEvents.add(event);
        processQueue();
    }

    public void renameExpenditureCategory(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int category, String newCategoryName)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.renameCategory, category, newCategoryName);
        _expEvents.add(event);
        processQueue();
    }

    public void mergeExpenditureCategory(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int category, int newCategory, String newCategoryName)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.mergeCategory, category, newCategory, newCategoryName);
        _expEvents.add(event);
        processQueue();
    }

    public void addExpenditureCategory(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int category)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.addCategory, category);
        _expEvents.add(event);
        processQueue();
    }

    public void removeExpenditureCategory(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int category)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.removeCategory, category);
        _expEvents.add(event);
        processQueue();
    }


    public void updateExpenditureCategories(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, String[] categoryNames)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.resortCategories, categoryNames);
        _expEvents.add(event);
        processQueue();
    }

    public void getMonthBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.query, _year, _month, BudDatabaseEvent.QueryType.month);
        _budEvents.add(event);
        processQueue();
    }

    public void getMonthsBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.query, _year, _month, BudDatabaseEvent.QueryType.months);
        _budEvents.add(event);
        processQueue();
    }

    public void getYearBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.query, _year, _month, BudDatabaseEvent.QueryType.year);
        _budEvents.add(event);
        processQueue();
    }

    public void getTotalBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData, int startYear, int endYear)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.query, startYear, endYear, BudDatabaseEvent.QueryType.total);
        _budEvents.add(event);
        processQueue();
    }

    public void insertBudgetEntity(MutableLiveData<List<BudgetEntity>> mutableLiveData, BudgetEntity budgetEntity)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.insert, _year, _month, budgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void insertLinkedBudgetEntities(MutableLiveData<List<BudgetEntity>> mutableLiveData, BudgetEntity budgetEntity, BudgetEntity linkedBudgetEntity)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.insertTransfer, _year, _month, budgetEntity, linkedBudgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void updateBudgetEntity(MutableLiveData<List<BudgetEntity>> mutableLiveData, BudgetEntity budgetEntity)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.update, _year, _month, budgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void updateLinkedBudgetEntities(MutableLiveData<List<BudgetEntity>> mutableLiveData, BudgetEntity budgetEntity, BudgetEntity linkedBudgetEntity)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.updateTransfer, _year, _month, budgetEntity, linkedBudgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void removeBudgetEntity(MutableLiveData<List<BudgetEntity>> mutableLiveData, BudgetEntity budgetEntity)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.remove, _year, _month, budgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void removeLinkedBudgetEntities(MutableLiveData<List<BudgetEntity>> mutableLiveData, BudgetEntity budgetEntity, BudgetEntity linkedBudgetEntity)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.removeTransfer, _year, _month, budgetEntity, linkedBudgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void renameBudgetCategory(MutableLiveData<List<BudgetEntity>> mutableLiveData, int category, String newCategoryName)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.renameCategory, category, newCategoryName);
        _budEvents.add(event);
        processQueue();
    }

    public void mergeBudgetCategory(MutableLiveData<List<BudgetEntity>> mutableLiveData, int category, int newCategory, String newCategoryName)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.mergeCategory, category, newCategory, newCategoryName);
        _budEvents.add(event);
        processQueue();
    }

    public void addBudgetCategory(MutableLiveData<List<BudgetEntity>> mutableLiveData, int category)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.addCategory, category);
        _budEvents.add(event);
        processQueue();
    }

    public void removeBudgetCategory(MutableLiveData<List<BudgetEntity>> mutableLiveData, int category)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.removeCategory, category);
        _budEvents.add(event);
        processQueue();
    }

    public void updateBudgetCategories(MutableLiveData<List<BudgetEntity>> mutableLiveData, String[] categoryNames)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.resortCategories, categoryNames);
        _budEvents.add(event);
        processQueue();
    }

    public void customExpenditureQuery(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, String query)
    {
        ExpDatabaseEvent event = new ExpDatabaseEvent(mutableLiveData, ExpDatabaseEvent.EventType.customQuery, query);
        _expEvents.add(event);
        processQueue();
    }

    public void customBudgetQuery(MutableLiveData<List<BudgetEntity>> mutableLiveData, String query)
    {
        BudDatabaseEvent event = new BudDatabaseEvent(mutableLiveData, BudDatabaseEvent.EventType.customQuery, query);
        _budEvents.add(event);
        processQueue();
    }
}

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
import cc.corbin.budgettracker.workerthread.budgetevent.BudAddCategoryEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudCustomQueryEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudDatabaseEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudInsertEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudInsertTransferEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudMergeCategoryEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudQueryEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudRemoveCategoryEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudRemoveEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudRemoveTransferEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudRenameCategoryEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudResortCategoriesEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudUpdateEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudUpdateTransferEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpAddCategoryEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpCustomQueryEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpDatabaseEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpInsertEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpMergeCategoryEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpQueryEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpRemoveCategoryEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpRemoveEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpRenameCategoryEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpResortCategoriesEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpUpdateEvent;

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
        // TODO ?
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
        ExpDatabaseEvent event = new ExpQueryEvent(mutableLiveData, _year, _month, _day);
        _expEvents.add(event);
        processQueue();
    }

    public void getMonth(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        ExpDatabaseEvent event = new ExpQueryEvent(mutableLiveData, _year, _month);
        _expEvents.add(event);
        processQueue();
    }

    public void getYear(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        ExpDatabaseEvent event = new ExpQueryEvent(mutableLiveData, _year);
        _expEvents.add(event);
        processQueue();
    }

    public void getTotal(MutableLiveData<List<ExpenditureEntity>> mutableLiveData)
    {
        ExpDatabaseEvent event = new ExpQueryEvent(mutableLiveData);
        _expEvents.add(event);
        processQueue();
    }

    public void insertExpEntity(ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpInsertEvent(entity);
        _expEvents.add(event);
        processQueue();
    }

    public void updateExpEntity(ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpUpdateEvent(entity);
        _expEvents.add(event);
        processQueue();
    }

    public void removeExpEntity(ExpenditureEntity entity)
    {
        ExpDatabaseEvent event = new ExpRemoveEvent(entity);
        _expEvents.add(event);
        processQueue();
    }

    public void renameExpenditureCategory(int category, String newCategoryName)
    {
        ExpDatabaseEvent event = new ExpRenameCategoryEvent(category, newCategoryName);
        _expEvents.add(event);
        processQueue();
    }

    public void mergeExpenditureCategory(int category, int newCategory, String newCategoryName)
    {
        ExpDatabaseEvent event = new ExpMergeCategoryEvent(category, newCategory, newCategoryName);
        _expEvents.add(event);
        processQueue();
    }

    public void addExpenditureCategory(int category)
    {
        ExpDatabaseEvent event = new ExpAddCategoryEvent(category);
        _expEvents.add(event);
        processQueue();
    }

    public void removeExpenditureCategory(int category)
    {
        ExpDatabaseEvent event = new ExpRemoveCategoryEvent(category);
        _expEvents.add(event);
        processQueue();
    }


    public void updateExpenditureCategories(String[] categoryNames)
    {
        ExpDatabaseEvent event = new ExpResortCategoriesEvent(categoryNames);
        _expEvents.add(event);
        processQueue();
    }

    public void getMonthBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData)
    {
        BudDatabaseEvent event = new BudQueryEvent(mutableLiveData, _year, _month, BudQueryEvent.QueryType.month);
        _budEvents.add(event);
        processQueue();
    }

    public void getMonthsBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData)
    {
        BudDatabaseEvent event = new BudQueryEvent(mutableLiveData, _year, _month, BudQueryEvent.QueryType.months);
        _budEvents.add(event);
        processQueue();
    }

    public void getYearBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData)
    {
        BudDatabaseEvent event = new BudQueryEvent(mutableLiveData, _year, _month, BudQueryEvent.QueryType.year);
        _budEvents.add(event);
        processQueue();
    }

    public void getTotalBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData, int startYear, int endYear)
    {
        BudDatabaseEvent event = new BudQueryEvent(mutableLiveData, _year, _month, startYear, endYear, BudQueryEvent.QueryType.total);
        _budEvents.add(event);
        processQueue();
    }

    public void insertBudgetEntity(BudgetEntity budgetEntity)
    {
        BudDatabaseEvent event = new BudInsertEvent(_year, _month, budgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void insertLinkedBudgetEntities(BudgetEntity budgetEntity, BudgetEntity linkedBudgetEntity)
    {
        BudDatabaseEvent event = new BudInsertTransferEvent(_year, _month, budgetEntity, linkedBudgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void updateBudgetEntity(BudgetEntity budgetEntity)
    {
        BudDatabaseEvent event = new BudUpdateEvent(budgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void updateLinkedBudgetEntities(BudgetEntity budgetEntity, BudgetEntity linkedBudgetEntity)
    {
        BudDatabaseEvent event = new BudUpdateTransferEvent(budgetEntity, linkedBudgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void removeBudgetEntity(BudgetEntity budgetEntity)
    {
        BudDatabaseEvent event = new BudRemoveEvent(budgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void removeLinkedBudgetEntities(BudgetEntity budgetEntity, BudgetEntity linkedBudgetEntity)
    {
        BudDatabaseEvent event = new BudRemoveTransferEvent(budgetEntity, linkedBudgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void renameBudgetCategory(int category, String newCategoryName)
    {
        BudDatabaseEvent event = new BudRenameCategoryEvent(category, newCategoryName);
        _budEvents.add(event);
        processQueue();
    }

    public void mergeBudgetCategory(int category, int newCategory, String newCategoryName)
    {
        BudDatabaseEvent event = new BudMergeCategoryEvent(category, newCategory, newCategoryName);
        _budEvents.add(event);
        processQueue();
    }

    public void addBudgetCategory(int category)
    {
        BudDatabaseEvent event = new BudAddCategoryEvent(category);
        _budEvents.add(event);
        processQueue();
    }

    public void removeBudgetCategory(int category)
    {
        BudDatabaseEvent event = new BudRemoveCategoryEvent(category);
        _budEvents.add(event);
        processQueue();
    }

    public void updateBudgetCategories(String[] categoryNames)
    {
        BudDatabaseEvent event = new BudResortCategoriesEvent(categoryNames);
        _budEvents.add(event);
        processQueue();
    }

    public void customExpenditureQuery(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, String query)
    {
        ExpDatabaseEvent event = new ExpCustomQueryEvent(mutableLiveData, query);
        _expEvents.add(event);
        processQueue();
    }

    public void customBudgetQuery(MutableLiveData<List<BudgetEntity>> mutableLiveData, String query)
    {
        BudDatabaseEvent event = new BudCustomQueryEvent(mutableLiveData, query);
        _budEvents.add(event);
        processQueue();
    }
}

package cc.corbin.budgettracker.workerthread;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.workerthread.budgetevent.BudCustomQueryEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudDatabaseEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudInsertEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudInsertTransferEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudQueryEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudRemoveEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudRemoveTransferEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudUpdateEvent;
import cc.corbin.budgettracker.workerthread.budgetevent.BudUpdateTransferEvent;
import cc.corbin.budgettracker.workerthread.combinedevent.ComAddCategoryEvent;
import cc.corbin.budgettracker.workerthread.combinedevent.ComDatabaseEvent;
import cc.corbin.budgettracker.workerthread.combinedevent.ComExportDatabasesEvent;
import cc.corbin.budgettracker.workerthread.combinedevent.ComMergeCategoryEvent;
import cc.corbin.budgettracker.workerthread.combinedevent.ComRemoveCategoryEvent;
import cc.corbin.budgettracker.workerthread.combinedevent.ComRenameCategoryEvent;
import cc.corbin.budgettracker.workerthread.combinedevent.ComResortCategoriesEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpCustomQueryEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpDatabaseEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpInsertEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpMonthCategoryEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpQueryEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpRemoveEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpUpdateEvent;

/**
 * Created by Corbin on 3/14/2018.
 */

public class ExpenditureViewModel
{
    private static final String TAG = "ExpenditureViewModel";

    private static ExpenditureViewModel _instance;

    private static ConcurrentLinkedQueue<ExpDatabaseEvent> _expEvents;
    private static ConcurrentLinkedQueue<BudDatabaseEvent> _budEvents;
    private static ConcurrentLinkedQueue<ComDatabaseEvent> _comEvents;

    private static DatabaseThread _thread;

    private ExpenditureViewModel()
    {
        _expEvents = new ConcurrentLinkedQueue<ExpDatabaseEvent>();
        _budEvents = new ConcurrentLinkedQueue<BudDatabaseEvent>();
        _comEvents = new ConcurrentLinkedQueue<ComDatabaseEvent>();
        _thread = new DatabaseThread(_expEvents, _budEvents, _comEvents);
    }

    public static ExpenditureViewModel getInstance()
    {
        if (_instance == null)
        {
            _instance = new ExpenditureViewModel();
        }
        else { }
        return _instance;
    }

    private void processQueue()
    {
        // Ensure the thread is setup with the databases
        if ((_thread != null))// && ((_queuer == null) || (_queuer.getStatus() != AsyncTask.Status.RUNNING)))
        {
            new DatabaseAsyncTask(this, _thread).execute();
        }
        else { }
    }

    public void getDay(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int year, int month, int day)
    {
        ExpDatabaseEvent event = new ExpQueryEvent(mutableLiveData, year, month, day);
        _expEvents.add(event);
        processQueue();
    }

    public void getMonth(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int year, int month)
    {
        ExpDatabaseEvent event = new ExpQueryEvent(mutableLiveData, year, month);
        _expEvents.add(event);
        processQueue();
    }

    public void getYear(MutableLiveData<List<ExpenditureEntity>> mutableLiveData, int year)
    {
        ExpDatabaseEvent event = new ExpQueryEvent(mutableLiveData, year);
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

    public void getMonthCategoryExpenses(MutableLiveData<Float> mutableLiveData, int category, int year, int month)
    {
        ExpDatabaseEvent event = new ExpMonthCategoryEvent(mutableLiveData, category, year, month);
        _expEvents.add(event);
        processQueue();
    }

    public void getMonthBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData, int year, int month)
    {
        BudDatabaseEvent event = new BudQueryEvent(mutableLiveData, year, month, BudQueryEvent.QueryType.month);
        _budEvents.add(event);
        processQueue();
    }

    // For use in adjustments
    public void getMonthCategoryBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData, int category, int year, int month)
    {
        BudDatabaseEvent event = new BudQueryEvent(mutableLiveData, category, year, month, BudQueryEvent.QueryType.month_category);
        _budEvents.add(event);
        processQueue();
    }

    public void getMonthsBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData, int year)
    {
        BudDatabaseEvent event = new BudQueryEvent(mutableLiveData, year, BudQueryEvent.QueryType.months);
        _budEvents.add(event);
        processQueue();
    }

    public void getYearBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData, int year)
    {
        BudDatabaseEvent event = new BudQueryEvent(mutableLiveData, year, BudQueryEvent.QueryType.year);
        _budEvents.add(event);
        processQueue();
    }

    public void getTotalBudget(MutableLiveData<List<BudgetEntity>> mutableLiveData, int startYear, int endYear)
    {
        BudDatabaseEvent event = new BudQueryEvent(mutableLiveData, startYear, 0, endYear, 12, BudQueryEvent.QueryType.total);
        _budEvents.add(event);
        processQueue();
    }

    public void insertBudgetEntity(BudgetEntity budgetEntity, int year, int month)
    {
        BudDatabaseEvent event = new BudInsertEvent(year, month, budgetEntity);
        _budEvents.add(event);
        processQueue();
    }

    public void insertLinkedBudgetEntities(BudgetEntity budgetEntity, BudgetEntity linkedBudgetEntity, int year, int month)
    {
        BudDatabaseEvent event = new BudInsertTransferEvent(year, month, budgetEntity, linkedBudgetEntity);
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

    public void renameCategory(int category, String newCategoryName, MutableLiveData<Boolean> processing)
    {
        ComDatabaseEvent event = new ComRenameCategoryEvent(category, newCategoryName, processing);
        _comEvents.add(event);
        processQueue();
    }

    public void mergeCategory(int category, int newCategory, String newCategoryName, MutableLiveData<Boolean> processing)
    {
        ComDatabaseEvent event = new ComMergeCategoryEvent(category, newCategory, newCategoryName, processing);
        _comEvents.add(event);
        processQueue();
    }

    public void addCategory(int category, MutableLiveData<Boolean> processing)
    {
        ComDatabaseEvent event = new ComAddCategoryEvent(category, processing);
        _comEvents.add(event);
        processQueue();
    }

    public void removeCategory(int category, MutableLiveData<Boolean> processing)
    {
        ComDatabaseEvent event = new ComRemoveCategoryEvent(category, processing);
        _comEvents.add(event);
        processQueue();
    }

    public void updateCategories(String[] categoryNames, MutableLiveData<Boolean> processing)
    {
        ComDatabaseEvent event = new ComResortCategoriesEvent(categoryNames, processing);
        _comEvents.add(event);
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

    public void exportDatabases(MutableLiveData<Boolean> complete,
                                String whereQuery, String dstFolder,
                                String srcExpFileName, String dstExpFileName,
                                String srcBudFileName, String dstBudFileName,
                                boolean exportBudgets)
    {
        ComDatabaseEvent event = new ComExportDatabasesEvent(complete, whereQuery, dstFolder, srcExpFileName, dstExpFileName,
                                                            srcBudFileName, dstBudFileName, exportBudgets);
        _comEvents.add(event);
        processQueue();
    }
}

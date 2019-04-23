package cc.corbin.budgettracker.workerthread;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.workerthread.budgetevent.BudDatabaseEvent;
import cc.corbin.budgettracker.workerthread.combinedevent.ComDatabaseEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpDatabaseEvent;

public class DatabaseThread
{
    private final String TAG = "DatabaseThread";

    private ConcurrentLinkedQueue<ExpDatabaseEvent> _expEvents;
    private ConcurrentLinkedQueue<BudDatabaseEvent> _budEvents;
    private ConcurrentLinkedQueue<ComDatabaseEvent> _comEvents;

    private ExpenditureDatabase _dbE;
    private BudgetDatabase _dbB;

    public DatabaseThread(ConcurrentLinkedQueue<ExpDatabaseEvent> eEvents,
                          ConcurrentLinkedQueue<BudDatabaseEvent> bEvents,
                          ConcurrentLinkedQueue<ComDatabaseEvent> cEvents)
    {
        _dbE = ExpenditureDatabase.getExpenditureDatabase();
        _dbB = BudgetDatabase.getBudgetDatabase();
        _expEvents = eEvents;
        _budEvents = bEvents;
        _comEvents = cEvents;
    }

    public void run()
    {
        while (!_expEvents.isEmpty())
        {
            processExpEvent(_expEvents.poll());
        }

        while (!_budEvents.isEmpty())
        {
            processBudEvent(_budEvents.poll());
        }

        while (!_comEvents.isEmpty())
        {
            processComEvent(_comEvents.poll());
        }
    }

    private void processExpEvent(ExpDatabaseEvent event)
    {
        event.processEvent(_dbE);
        //_completedExpEvents.add(event);
    }

    private void processBudEvent(BudDatabaseEvent event)
    {
        event.processEvent(_dbB);
        //_completedBudEvents.add(event);
    }

    private void processComEvent(ComDatabaseEvent event)
    {
        event.processEvent(_dbB, _dbE);
        //_completedComEvents.add(event);
    }
}
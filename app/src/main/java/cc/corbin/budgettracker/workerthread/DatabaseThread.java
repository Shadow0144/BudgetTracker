package cc.corbin.budgettracker.workerthread;

import android.arch.persistence.db.SimpleSQLiteQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.workerthread.budgetevent.BudDatabaseEvent;
import cc.corbin.budgettracker.workerthread.expenditureevent.ExpDatabaseEvent;

public class DatabaseThread
{
    private final String TAG = "DatabaseThread";

    private ConcurrentLinkedQueue<ExpDatabaseEvent> _expEvents;
    private ConcurrentLinkedQueue<BudDatabaseEvent> _budEvents;
    private ConcurrentLinkedQueue<ExpDatabaseEvent> _completedExpEvents;
    private ConcurrentLinkedQueue<BudDatabaseEvent> _completedBudEvents;

    private ExpenditureDatabase _dbE;
    private BudgetDatabase _dbB;

    public DatabaseThread(ExpenditureDatabase dbE, BudgetDatabase dbB,
                          ConcurrentLinkedQueue<ExpDatabaseEvent> eEvents,
                          ConcurrentLinkedQueue<BudDatabaseEvent> bEvents,
                          ConcurrentLinkedQueue<ExpDatabaseEvent> completedExpEvents,
                          ConcurrentLinkedQueue<BudDatabaseEvent> completedBudEvents)
    {
        _dbE = dbE;
        _dbB = dbB;
        _expEvents = eEvents;
        _budEvents = bEvents;
        _completedExpEvents = completedExpEvents;
        _completedBudEvents = completedBudEvents;
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
    }

    private void processExpEvent(ExpDatabaseEvent event)
    {
        event.processEvent(_dbE);
        _completedExpEvents.add(event);
    }

    private void processBudEvent(BudDatabaseEvent event)
    {
        event.processEvent(_dbB);
        _completedBudEvents.add(event);
    }
}
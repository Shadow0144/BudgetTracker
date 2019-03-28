package cc.corbin.budgettracker.workerthread.budgetevent;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;

public interface BudDatabaseEvent
{
    void processEvent(BudgetDatabase dbB);
}
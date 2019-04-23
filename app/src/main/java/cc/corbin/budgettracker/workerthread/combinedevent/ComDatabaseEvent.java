package cc.corbin.budgettracker.workerthread.combinedevent;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;

public interface ComDatabaseEvent
{
    void processEvent(BudgetDatabase dbB, ExpenditureDatabase dbE);
}

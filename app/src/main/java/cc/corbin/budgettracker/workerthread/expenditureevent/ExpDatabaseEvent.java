package cc.corbin.budgettracker.workerthread.expenditureevent;

import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public interface ExpDatabaseEvent
{
    void processEvent(ExpenditureDatabase dbE);
}
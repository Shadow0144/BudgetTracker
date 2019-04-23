package cc.corbin.budgettracker.workerthread.combinedevent;

import android.arch.lifecycle.MutableLiveData;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;

public class ComRemoveCategoryEvent implements ComDatabaseEvent
{
    private final String TAG = "ComRemoveCategoryEvent";

    private int _category;
    private MutableLiveData<Boolean> _processing;

    public ComRemoveCategoryEvent(int category, MutableLiveData<Boolean> processing)
    {
        _category = category;
        _processing = processing;
        _processing.postValue(true);
    }

    @Override
    public void processEvent(BudgetDatabase dbB, ExpenditureDatabase dbE)
    {
        dbB.budgetDao().decreaseCategoryNumbers(_category);
        dbE.expenditureDao().decreaseCategoryNumbers(_category);
        _processing.postValue(false);
    }
}
package cc.corbin.budgettracker.workerthread.combinedevent;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;

public class ComAddCategoryEvent implements ComDatabaseEvent
{
    private final String TAG = "ComAddCategoryEvent";

    private int _category;
    private MutableLiveData<Boolean> _processing;

    public ComAddCategoryEvent(int category, MutableLiveData<Boolean> processing)
    {
        _category = category;
        _processing = processing;
        _processing.postValue(true);
    }

    @Override
    public void processEvent(BudgetDatabase dbB, ExpenditureDatabase dbE)
    {
        dbB.budgetDao().increaseCategoryNumbers(_category);
        dbE.expenditureDao().increaseCategoryNumbers(_category);
        _processing.postValue(false);
    }
}
package cc.corbin.budgettracker.workerthread.combinedevent;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;

public class ComResortCategoriesEvent implements ComDatabaseEvent
{
    private final String TAG = "ComResortCategoriesEvent";

    private String[] _newCategoryNames;
    private MutableLiveData<Boolean> _processing;

    public ComResortCategoriesEvent(String[] newCategoryNames, MutableLiveData<Boolean> processing)
    {
        _newCategoryNames = newCategoryNames;
        _processing = processing;
        _processing.postValue(true);
    }

    @Override
    public void processEvent(BudgetDatabase dbB, ExpenditureDatabase dbE)
    {
        for (int i = 0; i < _newCategoryNames.length; i++)
        {
            dbB.budgetDao().updateCategoryNumber(_newCategoryNames[i], i);
            dbE.expenditureDao().updateCategoryNumber(_newCategoryNames[i], i);
        }
        _processing.postValue(false);
    }
}
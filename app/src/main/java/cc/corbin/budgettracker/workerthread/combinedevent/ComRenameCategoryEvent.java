package cc.corbin.budgettracker.workerthread.combinedevent;

import android.arch.lifecycle.MutableLiveData;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;

public class ComRenameCategoryEvent implements ComDatabaseEvent
{
    private final String TAG = "ComRenameCategoryEvent";

    private int _category;
    private String _newCategoryName;
    private MutableLiveData<Boolean> _processing;

    public ComRenameCategoryEvent(int category, String newCategoryName, MutableLiveData<Boolean> processing)
    {
        _category = category;
        _newCategoryName = newCategoryName;
        _processing = processing;
        _processing.postValue(true);
    }

    @Override
    public void processEvent(BudgetDatabase dbB, ExpenditureDatabase dbE)
    {
        dbB.budgetDao().renameCategory(_category, _newCategoryName);
        dbE.expenditureDao().renameCategory(_category, _newCategoryName);
        _processing.postValue(false);
    }
}

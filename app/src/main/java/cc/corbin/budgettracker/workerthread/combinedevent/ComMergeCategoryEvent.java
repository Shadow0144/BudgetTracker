package cc.corbin.budgettracker.workerthread.combinedevent;

import android.arch.lifecycle.MutableLiveData;

import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;

public class ComMergeCategoryEvent implements ComDatabaseEvent
{
    private final String TAG = "ComMergeCategoryEvent";

    private int _category;
    private int _newCategory;
    private String _newCategoryName;
    private MutableLiveData<Boolean> _processing;

    public ComMergeCategoryEvent(int category, int newCategory, String newCategoryName, MutableLiveData<Boolean> processing)
    {
        _category = category;
        _newCategory = newCategory;
        _newCategoryName = newCategoryName;
        _processing = processing;
        _processing.postValue(true);
    }

    @Override
    public void processEvent(BudgetDatabase dbB, ExpenditureDatabase dbE)
    {
        // Merge and remove
        dbB.budgetDao().mergeCategory(_category, _newCategory, _newCategoryName);
        dbE.expenditureDao().mergeCategory(_category, _newCategory, _newCategoryName);
        dbB.budgetDao().decreaseCategoryNumbers(_category);
        dbE.expenditureDao().decreaseCategoryNumbers(_category);
        _processing.postValue(false);
    }
}
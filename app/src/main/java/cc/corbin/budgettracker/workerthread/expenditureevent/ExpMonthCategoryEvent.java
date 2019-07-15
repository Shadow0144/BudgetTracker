package cc.corbin.budgettracker.workerthread.expenditureevent;

import android.arch.lifecycle.MutableLiveData;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class ExpMonthCategoryEvent implements ExpDatabaseEvent
{
    private final String TAG = "ExpMonthCategoryEvent";

    private MutableLiveData<Float> _mutableLiveData;
    private int _category;
    private int _year;
    private int _month;

    public ExpMonthCategoryEvent(MutableLiveData<Float> mutableLiveData, int category, int year, int month)
    {
        _mutableLiveData = mutableLiveData;
        _category = category;
        _year = year;
        _month = month;
    }

    @Override
    public void processEvent(ExpenditureDatabase dbE)
    {
        float amount = dbE.expenditureDao().getMonthCategory(_category, _year, _month);
        _mutableLiveData.postValue(amount);
    }
}

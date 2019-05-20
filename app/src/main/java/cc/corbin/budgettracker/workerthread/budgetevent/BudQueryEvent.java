package cc.corbin.budgettracker.workerthread.budgetevent;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SimpleSQLiteQuery;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudQueryEvent implements BudDatabaseEvent
{
    private final String TAG = "BudQueryEvent";

    public enum QueryType
    {
        month,
        months,
        year,
        total
    };

    private int _year;
    private int _month;
    private int _startYear;
    private int _startMonth;
    private int _endYear;
    private int _endMonth;
    private QueryType _queryType;
    private MutableLiveData<List<BudgetEntity>> _mutableLiveData;

    // Month
    public BudQueryEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData,
                         int year, int month, QueryType queryType)
    {
        _mutableLiveData = mutableLiveData;
        _year = year;
        _month = month;
        _queryType = queryType;
    }

    // Year
    public BudQueryEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData, int year, QueryType queryType)
    {
        _mutableLiveData = mutableLiveData;
        _year = year;
        _queryType = queryType;
    }

    // Total or Months (range)
    public BudQueryEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData,
                         int startYear, int startMonth, int endYear, int endMonth, QueryType queryType)
    {
        _mutableLiveData = mutableLiveData;
        _startYear = startYear;
        _startMonth = startMonth;
        _endYear = endYear;
        _endMonth = endMonth;
        _queryType = queryType;
    }

    @Override
    public void processEvent(BudgetDatabase dbB)
    {
        String[] categories = Categories.getCategories();
        BudDBFuncHelper helper = new BudDBFuncHelper();
        ArrayList<BudgetEntity> entities = new ArrayList<BudgetEntity>();

        switch (_queryType)
        {
            case month:
                for (int i = 0; i < categories.length; i++)
                {
                    entities.add(helper.getMonthCategoryBudget(dbB, _month, _year, i));
                }
                entities.addAll(dbB.budgetDao().getMonthAdjustments(_year, _month));
                break;
            case months:
                int y = _startYear; // TODO - Broken!
                int m = _startMonth;
                for (++y; y < (_endYear-1); y++)
                {
                    for (int i = 1; i < categories.length; i++)
                    {
                        entities.add(helper.getYearCategoryBudget(dbB, _year, i));
                        for (int j = 1; j <= 12; j++)
                        {
                            entities.add(helper.getMonthCategoryBudget(dbB, j, _year, i));
                        }
                    }
                }
                m = _endMonth;
                break;
            case year:
                for (int i = 1; i <= 12; i++)
                {
                    for (int j = 0; j < categories.length; j++)
                    {
                        entities.add(helper.getMonthCategoryBudget(dbB, i, _year, j));
                    }
                }
                break;
            case total:
                for (int i = _startYear; i <= _endYear; i++)
                {
                    for (int j = 0; j < categories.length; j++)
                    {
                        entities.add(helper.getYearCategoryBudget(dbB, i, j));
                    }
                }
                break;
        }
        _mutableLiveData.postValue(entities);
    }
}

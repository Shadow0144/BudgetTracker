package cc.corbin.budgettracker.workerthread.budgetevent;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SimpleSQLiteQuery;

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
    private int _endYear;
    private int _endMonth;
    private QueryType _queryType;
    private MutableLiveData<List<BudgetEntity>> _mutableLiveData;

    public BudQueryEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData,
                         int year, int month, QueryType queryType)
    {
        _mutableLiveData = mutableLiveData;
        _year = year;
        _month = month;
        _queryType = queryType;
    }

    public BudQueryEvent(MutableLiveData<List<BudgetEntity>> mutableLiveData,
                         int year, int month, int startYear, int endYear, QueryType queryType)
    {
        _mutableLiveData = mutableLiveData;
        _year = year;
        _month = month;
        _startYear = startYear;
        _endYear = endYear;
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
                for (int i = 0; i < categories.length; i++)
                {
                    entities.add(helper.getYearCategoryBudget(dbB, _year, i));
                    for (int j = 1; j <= 12; j++)
                    {
                        entities.add(helper.getMonthCategoryBudget(dbB, j, _year, i));
                        // TODO Add adjustments
                    }
                }
                break;
            case year:
                for (int i = 0; i < categories.length; i++)
                {
                    entities.add(helper.getYearCategoryBudget(dbB, _year, i));
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
                // TODO Add adjustments
                break;
        }
        _mutableLiveData.postValue(entities);
    }
}

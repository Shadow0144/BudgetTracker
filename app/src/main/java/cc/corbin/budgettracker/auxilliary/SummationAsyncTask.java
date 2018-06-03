package cc.corbin.budgettracker.auxilliary;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class SummationAsyncTask extends AsyncTask<List<ExpenditureEntity>, Void, float[][]>
{
    private final String TAG = "SummationAsyncTask";

    public enum summationType
    {
        weekly,
        monthly,
        yearly,
        categorically
    };
    private summationType _summationType;

    private MutableLiveData<float[]> _amounts;
    private MutableLiveData<float[]> _categoryAmounts;
    private boolean _dualAmounts;

    public SummationAsyncTask(summationType summation, MutableLiveData<float[]> amounts)
    {
        _summationType = summation;
        _amounts = amounts;
        _dualAmounts = false;
    }

    public SummationAsyncTask(summationType summation, MutableLiveData<float[]> timeAmounts, MutableLiveData<float[]> categoryAmounts)
    {
        _summationType = summation;
        _amounts = timeAmounts;
        _categoryAmounts = categoryAmounts;
        _dualAmounts = true;
    }

    @Override
    protected float[][] doInBackground(List<ExpenditureEntity>... entitiesList)
    {
        List<ExpenditureEntity> entities = entitiesList[0];
        float[] sums = new float[0];
        float[] catSums;

        float[][] amounts;
        if (_dualAmounts)
        {
            amounts = new float[2][];
            catSums = new float[Categories.getCategories().length];
        }
        else
        {
            amounts = new float[1][];
            catSums = new float[0];
        }

        switch (_summationType)
        {
            case weekly:
                sums = new float[7];
                getWeeklySummations(sums, catSums, entities);
                break;

            case monthly:
                sums = new float[14];
                getMonthlySummations(sums, catSums, entities);
                break;

            case yearly:
                if (entities.size() > 0)
                {
                    // Assume entities are sorted
                    int minYear = entities.get(0).getYear();
                    int maxYear = entities.get(entities.size() - 1).getYear();
                    sums = new float[maxYear - minYear + 1];
                    getYearlySummations(sums, catSums, entities, minYear);
                }
                else
                {
                    // Do nothing
                    sums = new float[0];
                }
                break;

            case categorically:
                sums = new float[Categories.getCategories().length];
                getCategoricalSummations(sums, entities);
                break;
        }

        amounts[0] = sums;
        if (_dualAmounts)
        {
            amounts[1] = catSums;
        }
        else { }

        return amounts;
    }

    @Override
    protected void onPostExecute(float[][] result)
    {
        _amounts.postValue(result[0]);
        if (_dualAmounts)
        {
            _categoryAmounts.postValue(result[1]);
        }
        else { }
    }

    private void getWeeklySummations(float[] weeks, float[] categories, List<ExpenditureEntity> entities)
    {
        for (int i = 0; i < weeks.length; i++)
        {
            weeks[i] = 0.0f;
        }
        for (int i = 0; i < categories.length; i++)
        {
            categories[i] = 0.0f;
        }

        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = entities.get(i);
            int day = entity.getDay();
            if (day == 0)
            {
                weeks[0] += entity.getAmount();
            }
            else if (day < 8)
            {
                weeks[1] += entity.getAmount();
            }
            else if (day < 15)
            {
                weeks[2] += entity.getAmount();
            }
            else if (day < 22)
            {
                weeks[3] += entity.getAmount();
            }
            else if (day < 29)
            {
                weeks[4] += entity.getAmount();
            }
            else if (day < 32)
            {
                weeks[5] += entity.getAmount();
            }
            else // day == 32
            {
                weeks[6] += entity.getAmount();
            }
            if (_dualAmounts)
            {
                int category = entity.getCategory();
                categories[category] += entity.getAmount();
            }
            else { }
        }
    }

    private void getMonthlySummations(float[] months, float[] categories, List<ExpenditureEntity> entities)
    {
        for (int i = 0; i < months.length; i++)
        {
            months[i] = 0.0f;
        }
        for (int i = 0; i < categories.length; i++)
        {
            categories[i] = 0.0f;
        }
        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = entities.get(i);
            int month = entity.getMonth();
            months[month] += entity.getAmount();
            if (_dualAmounts)
            {
                int category = entity.getCategory();
                categories[category] += entity.getAmount();
            }
            else { }
        }
    }

    private void getYearlySummations(float[] years, float[] categories, List<ExpenditureEntity> entities, int minYear)
    {
        for (int i = 0; i < years.length; i++)
        {
            years[i] = 0.0f;
        }
        for (int i = 0; i < categories.length; i++)
        {
            categories[i] = 0.0f;
        }
        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = entities.get(i);
            int year = entity.getYear();
            years[year - minYear] += entity.getAmount();
            if (_dualAmounts)
            {
                int category = entity.getCategory();
                categories[category] += entity.getAmount();
            }
            else { }
        }
    }

    private void getCategoricalSummations(float[] categories, List<ExpenditureEntity> entities)
    {
        for (int i = 0; i < categories.length; i++)
        {
            categories[i] = 0.0f;
        }
        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = entities.get(i);
            int category = entity.getCategory();
            categories[category] += entity.getAmount();
        }
    }
}
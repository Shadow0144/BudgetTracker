package cc.corbin.budgettracker.auxilliary;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class SummationAsyncTask extends AsyncTask<List<ExpenditureEntity>, SummationAsyncTask.SummationResult, float[]>
{
    private final String TAG = "SummationAsyncTask";

    public class SummationResult
    {
        private String _title;
        private float _amount;
        private int _ID;
        private boolean _isLast;

        public SummationResult(String title, float amount, int ID, boolean isLast)
        {
            _title = title;
            _amount = amount;
            _ID = ID;
            _isLast = isLast;
        }

        public String getTitle()
        {
            return _title;
        }

        public float getAmount()
        {
            return _amount;
        }

        public int getID()
        {
            return _ID;
        }

        public boolean getIsLast()
        {
            return _isLast;
        }
    }

    public enum summationType
    {
        daily,
        weekly,
        monthly,
        yearly,
        categorically
    };
    private summationType _summationType;

    private MutableLiveData<float[]> _amounts;

    private AsyncSummationCallback _callback;
    private boolean _usingCallback;

    public SummationAsyncTask(summationType summation, MutableLiveData<float[]> amounts)
    {
        _summationType = summation;
        _amounts = amounts;
        _usingCallback = false;
    }

    public SummationAsyncTask(summationType summation, MutableLiveData<float[]> amounts, AsyncSummationCallback callback)
    {
        _summationType = summation;
        _amounts = amounts;
        _callback = callback;
        _usingCallback = true;
    }

    @Override
    protected float[] doInBackground(List<ExpenditureEntity>... entitiesList)
    {
        List<ExpenditureEntity> entities = entitiesList[0];
        float[] sums = new float[0];

        switch (_summationType)
        {
            case daily:
                if (entities.size() > 0)
                {
                    // Assume entities are sorted
                    ExpenditureEntity startEntity = entities.get(0);
                    ExpenditureEntity endEntity = entities.get(entities.size() - 1);
                    Calendar startDate = Calendar.getInstance();
                    Calendar endDate = Calendar.getInstance();
                    startDate.set(startEntity.getYear(), startEntity.getMonth()-1, startEntity.getDay());
                    endDate.set(endEntity.getYear(), endEntity.getMonth()-1, endEntity.getDay());
                    long days = TimeUnit.DAYS.convert((endDate.getTimeInMillis() - startDate.getTimeInMillis()), TimeUnit.MILLISECONDS);
                    sums = new float[(int)(days + 1)];
                    getDailySummations(sums, entities, startDate);
                }
                else
                {
                    // Do nothing
                    sums = new float[0];
                }
                break;

            case weekly:
                sums = new float[6]; // + 1 for extras
                getWeeklySummations(sums, entities);
                break;

            case monthly:
                sums = new float[12];
                getMonthlySummations(sums, entities);
                break;

            case yearly:
                if (entities.size() > 0)
                {
                    // Assume entities are sorted
                    int minYear = entities.get(0).getYear();
                    int maxYear = entities.get(entities.size() - 1).getYear();
                    sums = new float[maxYear - minYear + 1];
                    getYearlySummations(sums, entities, minYear);
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

        return sums;
    }

    @Override
    protected void onProgressUpdate(SummationResult... results)
    {
        for (int i = 0; i < results.length; i++)
        {
            if (_usingCallback)
            {
                _callback.rowComplete(_summationType, results[i].getTitle(), results[i].getAmount(),
                        results[i].getID(), results[i].getIsLast());
            }
            else { }
        }
    }

    @Override
    protected void onPostExecute(float[] result)
    {
        _amounts.postValue(result);
    }

    private void getWeeklySummations(float[] weeks, List<ExpenditureEntity> entities)
    {
        for (int i = 0; i < weeks.length; i++)
        {
            weeks[i] = 0.0f;
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
            else // if (day < 32)
            {
                weeks[5] += entity.getAmount();
            }
            /*else // day == 32
            {
                weeks[6] += entity.getAmount();
            }*/
        }
    }

    private void getDailySummations(float[] days, List<ExpenditureEntity> entities, Calendar startDate)
    {
        for (int i = 0; i < days.length; i++)
        {
            days[i] = 0.0f;
        }
        int size = entities.size();
        int index = 0;
        int currentDay = startDate.get(Calendar.DATE);
        int currentMonth = startDate.get(Calendar.MONTH)+1;
        int currentYear = startDate.get(Calendar.YEAR);
        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = entities.get(i);
            int day = entity.getDay();
            int month = entity.getMonth();
            int year = entity.getYear();
            if (day != currentDay || month != currentMonth || year != currentYear)
            {
                currentDay = day;
                currentMonth = month;
                currentYear = year;
                index++;
            }
            days[index] += entity.getAmount();
        }
    }

    private void getMonthlySummations(float[] months, List<ExpenditureEntity> entities)
    {
        for (int i = 0; i < months.length; i++)
        {
            months[i] = 0.0f;
        }
        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = entities.get(i);
            int month = entity.getMonth();
            months[month-1] += entity.getAmount();
        }
    }

    // Warning: Results must be sorted by year
    private void getYearlySummations(float[] years, List<ExpenditureEntity> entities, int minYear)
    {
        for (int i = 0; i < years.length; i++)
        {
            years[i] = 0.0f;
        }
        int size = entities.size();
        int year = minYear;
        for (int i = 0; i < size; i++) // Loop through every entity
        {
            ExpenditureEntity entity = entities.get(i);
            int entityYear = entity.getYear(); // Check if the entity is the year currently being summed
            while (entityYear > year) // When we've reached an entity for a new year, publish
            {
                publishProgress(new SummationResult("" + year, years[year - minYear], year, false));
                year++;
            }
            years[year - minYear] += entity.getAmount(); // Add the amount to the year
        }
        publishProgress(new SummationResult("" + year, years[year - minYear], year, true));
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
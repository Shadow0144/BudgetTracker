package cc.corbin.budgettracker.auxilliary;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

// Performs summations of data and returns results periodically
public class SummationAsyncTask extends AsyncTask<List<ExpenditureEntity>, SummationAsyncTask.SummationResult, SummationAsyncTask.SummationResult[]>
{
    private final String TAG = "SummationAsyncTask";

    public class SummationResult
    {
        private String _title;
        private float _amount;
        private int _ID;
        private boolean _isLast;

        public SummationResult()
        {
            _title = "";
            _amount = 0.0f;
            _ID = 0;
            _isLast = false;
        }

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

        private void add(float amount)
        {
            _amount += amount;
        }
    }

    public enum SummationType
    {
        daily,
        weekly,
        monthly,
        yearly,
        categorically
    };
    private SummationType _summationType;

    private MutableLiveData<SummationResult[]> _summations;

    private AsyncSummationCallback _callback;
    private boolean _usingCallback;

    public SummationAsyncTask(SummationType summationType, MutableLiveData<SummationResult[]> summations)
    {
        _summationType = summationType;
        _summations = summations;
        _usingCallback = false;
    }

    public SummationAsyncTask(SummationType summationType, MutableLiveData<SummationResult[]> summations, AsyncSummationCallback callback)
    {
        _summationType = summationType;
        _summations = summations;
        _callback = callback;
        _usingCallback = true;
    }

    @Override
    protected SummationResult[] doInBackground(List<ExpenditureEntity>... entitiesList)
    {
        List<ExpenditureEntity> entities = entitiesList[0];
        SummationResult[] sums = new SummationResult[0];

        switch (_summationType)
        {
            case daily:
                if (entities.size() > 0)
                {
                    // Assume entities are sorted
                    ExpenditureEntity startEntity = entities.get(0);
                    ExpenditureEntity endEntity = entities.get(entities.size() - 1);
                    LocalDate startDate = LocalDate.of(startEntity.getYear(), startEntity.getMonth(), startEntity.getDay());
                    LocalDate endDate = LocalDate.of(endEntity.getYear(), endEntity.getMonth(), endEntity.getDay());
                    long days = TimeUnit.DAYS.convert((endDate.toEpochDay() - startDate.toEpochDay()), TimeUnit.DAYS);
                    sums = new SummationResult[(int)(days + 1)];
                    getDailySummations(sums, entities, startDate);
                }
                else
                {
                    // Do nothing
                    sums = new SummationResult[0];
                }
                break;

            case weekly:
                sums = new SummationResult[6]; // + 1 for extras
                getWeeklySummations(sums, entities);
                break;

            case monthly:
                sums = new SummationResult[12];
                getMonthlySummations(sums, entities);
                break;

            case yearly:
                if (entities.size() > 0)
                {
                    // Assume entities are sorted
                    int minYear = entities.get(0).getYear();
                    int maxYear = entities.get(entities.size() - 1).getYear();
                    sums = new SummationResult[maxYear - minYear + 1];
                    getYearlySummations(sums, entities, minYear);
                }
                else
                {
                    // Do nothing
                    sums = new SummationResult[0];
                }
                break;

            case categorically:
                sums = new SummationResult[Categories.getCategories().length];
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
    protected void onPostExecute(SummationResult[] result)
    {
        if (_summations != null)
        {
            _summations.postValue(result);
        }
        else { }
    }

    private void getWeeklySummations(SummationResult[] weeks, List<ExpenditureEntity> entities)
    {
        for (int i = 0; i < weeks.length; i++) // TODO
        {
            weeks[i] = new SummationResult("Week " + (i + 1), 0.0f, 0, false);
        }

        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = entities.get(i);
            int day = entity.getDay();
            if (day == 0)
            {
                weeks[i].add(entity.getAmount());
            }
            else if (day < 8)
            {
                weeks[1].add(entity.getAmount());
            }
            else if (day < 15)
            {
                weeks[2].add(entity.getAmount());
            }
            else if (day < 22)
            {
                weeks[3].add(entity.getAmount());
            }
            else if (day < 29)
            {
                weeks[4].add(entity.getAmount());
            }
            else // if (day < 32)
            {
                weeks[5].add(entity.getAmount());
            }
            /*else // day == 32
            {
                weeks[6].add(entity.getAmount());
            }*/
        }
    }

    private void getDailySummations(SummationResult[] days, List<ExpenditureEntity> entities, LocalDate startDate)
    {
        for (int i = 0; i < days.length; i++)
        {
            days[i] = new SummationResult();
        }
        int size = entities.size();
        int index = 0;
        int currentYear = startDate.getYear();
        int currentMonth = startDate.getMonthValue();
        int currentDay = startDate.getDayOfMonth();
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
            days[index].add(entity.getAmount());
        }
    }

    private void getMonthlySummations(SummationResult[] months, List<ExpenditureEntity> entities)
    {
        for (int i = 0; i < months.length; i++)
        {
            months[i] = new SummationResult();
        }
        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = entities.get(i);
            int month = entity.getMonth();
            months[month-1].add(entity.getAmount());
        }
    }

    // Warning: Results must be sorted by year
    private void getYearlySummations(SummationResult[] years, List<ExpenditureEntity> entities, int minYear)
    {
        for (int i = 0; i < years.length; i++)
        {
            years[i] = new SummationResult();
        }
        int size = entities.size();
        int year = minYear;
        for (int i = 0; i < size; i++) // Loop through every entity
        {
            ExpenditureEntity entity = entities.get(i);
            int entityYear = entity.getYear(); // Check if the entity is the year currently being summed
            while (entityYear > year) // When we've reached an entity for a new year, publish
            {
                publishProgress(new SummationResult("" + year, years[year - minYear].getAmount(), year, false));
                year++;
            }
            years[year - minYear].add(entity.getAmount()); // Add the amount to the year
        }
        publishProgress(new SummationResult("" + year, years[year - minYear].getAmount(), year, true));
    }

    private void getCategoricalSummations(SummationResult[] categories, List<ExpenditureEntity> entities)
    {
        for (int i = 0; i < categories.length; i++)
        {
            categories[i] = new SummationResult();
        }
        int size = entities.size();
        for (int i = 0; i < size; i++)
        {
            ExpenditureEntity entity = entities.get(i);
            int category = entity.getCategory();
            categories[category].add(entity.getAmount());
        }
    }
}
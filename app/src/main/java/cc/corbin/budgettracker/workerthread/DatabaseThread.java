package cc.corbin.budgettracker.workerthread;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

public class DatabaseThread extends Thread
{
    private final String TAG = "DatabaseThread";

    private ConcurrentLinkedQueue<ExpDatabaseEvent> _expEvents;
    private ConcurrentLinkedQueue<BudgetDatabaseEvent> _budEvents;
    private ConcurrentLinkedQueue<ExpDatabaseEvent> _completedExpEvents;
    private ConcurrentLinkedQueue<BudgetDatabaseEvent> _completedBudEvents;

    private ExpenditureDatabase _dbE;
    private BudgetDatabase _dbB;

    private Handler _handler;

    private volatile boolean _running;

    public DatabaseThread(ExpenditureDatabase dbE, BudgetDatabase dbB,
                          ConcurrentLinkedQueue<ExpDatabaseEvent> eEvents,
                          ConcurrentLinkedQueue<BudgetDatabaseEvent> bEvents,
                          ConcurrentLinkedQueue<ExpDatabaseEvent> completedExpEvents,
                          ConcurrentLinkedQueue<BudgetDatabaseEvent> completedBudEvents,
                          Handler handler)
    {
        _dbE = dbE;
        _dbB = dbB;
        _expEvents = eEvents;
        _budEvents = bEvents;
        _completedExpEvents = completedExpEvents;
        _completedBudEvents = completedBudEvents;
        _handler = handler;
    }

    @Override
    public void run()
    {
        _running = true;
        while (_running)
        {
            if (!_expEvents.isEmpty())
            {
                processExpEvent(_expEvents.poll());
            }
            else { }

            if (!_budEvents.isEmpty())
            {
                processBudEvent(_budEvents.poll());
            }
            else { }
        }
    }

    public void finish()
    {
        while (!_expEvents.isEmpty() || !_budEvents.isEmpty());
        _running = false;
    }

    private void processExpEvent(ExpDatabaseEvent event)
    {
        switch (event.getEventType())
        {
            case query:
                List<ExpenditureEntity> entities = null;
                switch (event.getQueryType())
                {
                    case day:
                        entities = _dbE.expenditureDao().getDay(event.getYear(), event.getMonth(), event.getDay());
                        break;
                    case month:
                        entities = _dbE.expenditureDao().getMonth(event.getYear(), event.getMonth());
                        break;
                    case year:
                        entities = _dbE.expenditureDao().getYear(event.getYear());
                        break;
                }
                event.setEntities(entities);
                _completedExpEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;

            case insert:
                long id = _dbE.expenditureDao().insert(event.getEntity());
                event.getEntity().setId(id);
                _completedExpEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;

            case update:
                _dbE.expenditureDao().update(event.getEntity());
                _completedExpEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;

            case remove:
                _dbE.expenditureDao().delete(event.getEntity());
                _completedExpEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;
        }
    }

    private void processBudEvent(BudgetDatabaseEvent event)
    {
        Log.e(TAG, ""+event.getEventType().name());
        switch (event.getEventType())
        {
            case query:
                List<BudgetEntity> entities = null;
                switch (event.getQueryType())
                {
                    case month:
                        entities = new ArrayList<BudgetEntity>();
                        String[] categories = DayViewActivity.getCategories();
                        for (int i = 0; i < categories.length; i++)
                        {
                            List<BudgetEntity> catEntities = _dbB.budgetDao().getCategoryBeforeMonth(event.getYear(), event.getMonth(), categories[i]);
                            if (catEntities.size() == 0)
                            {
                                BudgetEntity newEntity = new BudgetEntity();
                                newEntity.setExpenseType(categories[i]);
                                newEntity.setCurrency(Currencies.default_currency);
                                entities.add(newEntity);
                            }
                            else
                            {
                                BudgetEntity maxEntity = catEntities.get(0);
                                int maxYear = maxEntity.getYear();
                                int maxMonth = maxEntity.getMonth();
                                int size = catEntities.size();
                                for (int j = 1; j < size; j++)
                                {
                                    BudgetEntity catEntity = catEntities.get(i);
                                    if (catEntity.getYear() > maxYear)
                                    {
                                        maxMonth = catEntity.getMonth();
                                        maxYear = catEntity.getYear();
                                        maxEntity = catEntity;
                                    }
                                    else if (catEntity.getYear() == maxYear)
                                    {
                                        if (catEntity.getMonth() > maxMonth)
                                        {
                                            maxMonth = catEntity.getMonth();
                                            maxEntity = catEntity;
                                        }
                                        else { }
                                    }
                                    else { }
                                }
                                entities.add(maxEntity);
                            }
                        }
                        break;
                    case year:
                        entities = _dbB.budgetDao().getYear(event.getYear());
                        break;
                }
                event.setEntities(entities);
                _completedBudEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;
            case insert:
                long id = _dbB.budgetDao().insert(event.getEntity());
                event.getEntity().setId(id);
                _completedBudEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;
            case update:
                _dbB.budgetDao().update(event.getEntity());
                _completedBudEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;
            case remove:
                _dbB.budgetDao().delete(event.getEntity());
                _completedBudEvents.add(event);
                _handler.post(new ExpenditureRunnable());
                break;
        }
    }
}
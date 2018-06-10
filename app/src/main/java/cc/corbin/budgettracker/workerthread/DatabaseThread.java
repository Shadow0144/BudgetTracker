package cc.corbin.budgettracker.workerthread;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cc.corbin.budgettracker.auxilliary.Categories;
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
                    case total:
                        entities = _dbE.expenditureDao().getTotal();
                        break;
                }
                event.setEntities(entities);
                break;

            case insert:
                long id = _dbE.expenditureDao().insert(event.getEntity());
                event.getEntity().setId(id);
                break;

            case update:
                _dbE.expenditureDao().update(event.getEntity());
                break;

            case remove:
                _dbE.expenditureDao().delete(event.getEntity());
                break;

            case renameCategory:
                _dbE.expenditureDao().renameCategory(event.getCategory(), event.getNewCategoryName());
                break;

            case mergeCategory:
                _dbE.expenditureDao().mergeCategory(event.getCategory(), event.getNewCategory(), event.getNewCategoryName());
                break;

            case addCategory:
                _dbE.expenditureDao().increaseCategoryNumbers(event.getCategory());
                break;

            case removeCategory:
                _dbE.expenditureDao().decreaseCategoryNumbers(event.getCategory());
                break;

            case resortCategories:
                String[] newCategoryNames = event.getNewCategoryNames();
                for (int i = 0; i < newCategoryNames.length; i++)
                {
                    _dbE.expenditureDao().updateCategoryNumber(newCategoryNames[i], i);
                }
                break;
        }
        _completedExpEvents.add(event);
        _handler.post(new ExpenditureRunnable());
    }

    private BudgetEntity getMonthCategoryBudget(int month, int year, int category)
    {
        BudgetEntity entity;

        List<BudgetEntity> catEntities = _dbB.budgetDao().getCategoryBeforeMonth(year, month, category);
        if (catEntities.size() == 0)
        {
            BudgetEntity newEntity = new BudgetEntity();
            newEntity.setCategory(category, Categories.getCategories()[category]);
            entity = newEntity;
        }
        else
        {
            BudgetEntity maxEntity = catEntities.get(0);
            int maxYear = maxEntity.getYear();
            int maxMonth = maxEntity.getMonth();
            int size = catEntities.size();
            for (int i = 0; i < size; i++)
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
            entity = maxEntity;
        }

        return entity;
    }

    private BudgetEntity getYearCategoryBudget(int year, int category)
    {
        BudgetEntity budgetEntity = _dbB.budgetDao().getYear(year, category);
        if (budgetEntity == null)
        {
            budgetEntity = new BudgetEntity();
        }
        else { }

        return budgetEntity;
    }

    private void updateYearBudget(int year, int category)
    {
        float total = 0.0f;
        for (int i = 1; i <= 12; i++)
        {
            BudgetEntity entity = getMonthCategoryBudget(i, year, category);
            total += entity.getAmount();
        }
        BudgetEntity yearBudget = _dbB.budgetDao().getYear(year, category);
        if (yearBudget == null)
        {
            yearBudget = new BudgetEntity(0, year, total, category, Categories.getCategories()[category]);
            _dbB.budgetDao().insert(yearBudget);
        }
        else
        {
            yearBudget.setAmount(total);
            _dbB.budgetDao().update(yearBudget);
        }
    }

    private void processBudEvent(BudgetDatabaseEvent event)
    {
        switch (event.getEventType())
        {
            case query:
                List<BudgetEntity> entities = null;
                String[] categories = Categories.getCategories();
                int month;
                int year;
                int endYear;
                switch (event.getQueryType())
                {
                    case month:
                        entities = new ArrayList<BudgetEntity>();
                        month = event.getMonth();
                        year = event.getYear();
                        for (int i = 0; i < categories.length; i++)
                        {
                            entities.add(getMonthCategoryBudget(month, year, i));
                        }
                        break;
                    case months:
                        entities = new ArrayList<BudgetEntity>();
                        year = event.getYear();
                        for (int i = 0; i < categories.length; i++)
                        {
                            entities.add(getYearCategoryBudget(year, i));
                            for (int j = 1; j <= 12; j++)
                            {
                                entities.add(getMonthCategoryBudget(j, year, i));
                            }
                        }
                        break;
                    case year:
                        entities = new ArrayList<BudgetEntity>();
                        year = event.getYear();
                        for (int i = 0; i < categories.length; i++)
                        {
                            entities.add(getYearCategoryBudget(year, i));
                        }
                        break;
                    case total:
                        entities = new ArrayList<BudgetEntity>();
                        year = event.getYear();
                        endYear = event.getMonth();
                        for (int i = year; i <= endYear; i++)
                        {
                            for (int j = 0; j < categories.length; j++)
                            {
                                entities.add(getYearCategoryBudget(i, j));
                            }
                        }
                        break;
                }
                event.setEntities(entities);
                break;

            case insert:
                long id = _dbB.budgetDao().insert(event.getEntity());
                event.getEntity().setId(id);
                updateYearBudget(event.getYear(), event.getCategory());
                break;

            case update:
                _dbB.budgetDao().update(event.getEntity());
                updateYearBudget(event.getYear(), event.getCategory());
                break;

            case remove:
                _dbB.budgetDao().delete(event.getEntity());
                updateYearBudget(event.getYear(), event.getCategory());
                break;

            case renameCategory:
                _dbB.budgetDao().renameCategory(event.getCategory(), event.getNewCategoryName());
                break;

            case mergeCategory:
                _dbB.budgetDao().mergeCategory(event.getCategory(), event.getNewCategory(), event.getNewCategoryName());
                updateYearBudget(event.getYear(), event.getNewCategory());
                break;

            case addCategory:
                _dbB.budgetDao().increaseCategoryNumbers(event.getCategory());
                break;

            case removeCategory:
                _dbB.budgetDao().decreaseCategoryNumbers(event.getCategory());
                break;

            case resortCategories:
                String[] newCategoryNames = event.getNewCategoryNames();
                for (int i = 0; i < newCategoryNames.length; i++)
                {
                    _dbB.budgetDao().updateCategoryNumber(newCategoryNames[i], i);
                }
                break;
        }
        _completedBudEvents.add(event);
        _handler.post(new ExpenditureRunnable());
    }
}
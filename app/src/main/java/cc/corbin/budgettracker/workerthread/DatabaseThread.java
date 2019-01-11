package cc.corbin.budgettracker.workerthread;

import android.arch.persistence.db.SimpleSQLiteQuery;
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

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class DatabaseThread
{
    private final String TAG = "DatabaseThread";

    private ConcurrentLinkedQueue<ExpDatabaseEvent> _expEvents;
    private ConcurrentLinkedQueue<BudgetDatabaseEvent> _budEvents;
    private ConcurrentLinkedQueue<ExpDatabaseEvent> _completedExpEvents;
    private ConcurrentLinkedQueue<BudgetDatabaseEvent> _completedBudEvents;

    private ExpenditureDatabase _dbE;
    private BudgetDatabase _dbB;

    public DatabaseThread(ExpenditureDatabase dbE, BudgetDatabase dbB,
                          ConcurrentLinkedQueue<ExpDatabaseEvent> eEvents,
                          ConcurrentLinkedQueue<BudgetDatabaseEvent> bEvents,
                          ConcurrentLinkedQueue<ExpDatabaseEvent> completedExpEvents,
                          ConcurrentLinkedQueue<BudgetDatabaseEvent> completedBudEvents)
    {
        _dbE = dbE;
        _dbB = dbB;
        _expEvents = eEvents;
        _budEvents = bEvents;
        _completedExpEvents = completedExpEvents;
        _completedBudEvents = completedBudEvents;
    }

    public void run()
    {
        while (!_expEvents.isEmpty())
        {
            processExpEvent(_expEvents.poll());
        }

        while (!_budEvents.isEmpty())
        {
            processBudEvent(_budEvents.poll());
        }
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

            case customQuery:
                String query = event.getQuery();
                entities = _dbE.expenditureDao().customQuery(new SimpleSQLiteQuery(query));
                event.setEntities(entities);
                break;
        }
        _completedExpEvents.add(event);
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

    // Called by insert, update, delete
    private void updateYearBudget(BudgetEntity entity)
    {
        int year = entity.getYear();
        int category = entity.getCategory();
        updateYearBudget(year, category);
    }

    // Called by merging
    private void updateYearBudget(int year, int category)
    {
        float total = 0.0f;
        for (int i = 1; i <= 12; i++)
        {
            BudgetEntity monthEntity = getMonthCategoryBudget(i, year, category);
            total += monthEntity.getAmount();
            List<BudgetEntity> entities = _dbB.budgetDao().getMonthAdjustments(year, i);
            int length = entities.size();
            for (int j = 0; j < length; j++)
            {
                total += entities.get(j).getAmount();
            }
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

    private void updateLinkedYearBudget(BudgetEntity entity, BudgetEntity linkedEntity)
    {
        int year = entity.getYear();
        int category = entity.getCategory();
        int linkedYear = linkedEntity.getYear();
        int linkedCategory = linkedEntity.getCategory();
        updateYearBudget(entity);
        if ((year != linkedYear) || (category != linkedCategory))
        {
            updateYearBudget(linkedEntity);
        }
        else { }
    }

    private void processBudEvent(BudgetDatabaseEvent event)
    {
        BudgetEntity entity = null;
        BudgetEntity linkedEntity = null;
        List<BudgetEntity> entities = null;
        String[] categories = Categories.getCategories();
        switch (event.getEventType())
        {
            case query:
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
                        entities.addAll(_dbB.budgetDao().getMonthAdjustments(year, month));
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
                                // TODO Add adjustments
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
                        // TODO Add adjustments
                        break;
                }
                event.setEntities(entities);
                break;

            case insert:
                entity = event.getEntity();
                long id = _dbB.budgetDao().insert(entity);
                entity.setId(id);
                updateYearBudget(entity);
                break;

            case insertTransfer:
                // Insert the entities to get IDs, then update them with their new links
                entities = new ArrayList<BudgetEntity>();
                entity = event.getEntity();
                linkedEntity = event.getLinkedEntity();
                entities.add(entity);
                entities.add(linkedEntity);
                long[] ids = _dbB.budgetDao().insertAll(entities);

                // Need to set the IDs and link data to update the linkage
                entity.setId(ids[0]);
                linkedEntity.setId(ids[1]);
                entity.setLinkedAdjustment(ids[1], linkedEntity.getMonth(), linkedEntity.getYear(), linkedEntity.getCategory());
                linkedEntity.setLinkedAdjustment(ids[0], entity.getMonth(), entity.getYear(), entity.getCategory());

                _dbB.budgetDao().update(entities);
                updateLinkedYearBudget(entity, linkedEntity);
                break;

            case update:
                entity = event.getEntity();
                _dbB.budgetDao().update(entity);
                updateYearBudget(entity);
                break;

            case updateTransfer:
                entity = event.getEntity();
                linkedEntity = event.getLinkedEntity();
                _dbB.budgetDao().updateAmountAndNote(entity.getId(), entity.getAmount(), entity.getNote());
                _dbB.budgetDao().updateAmountAndNote(linkedEntity.getId(), linkedEntity.getAmount(), linkedEntity.getNote());
                updateLinkedYearBudget(entity, linkedEntity);
                break;

            case remove:
                entity = event.getEntity();
                _dbB.budgetDao().delete(entity);
                updateYearBudget(entity);
                break;

            case removeTransfer:
                entities = new ArrayList<BudgetEntity>();
                entity = event.getEntity();
                linkedEntity = event.getLinkedEntity();
                entities.add(entity);
                entities.add(linkedEntity);
                _dbB.budgetDao().delete(entities);
                updateLinkedYearBudget(entity, linkedEntity);
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

            case customQuery:
                String query = event.getQuery();
                entities = _dbB.budgetDao().customQuery(new SimpleSQLiteQuery(query));
                event.setEntities(entities);
                break;
        }
        _completedBudEvents.add(event);
    }
}
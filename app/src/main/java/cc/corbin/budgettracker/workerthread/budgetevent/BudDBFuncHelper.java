package cc.corbin.budgettracker.workerthread.budgetevent;

import java.util.List;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudDBFuncHelper
{
    public BudgetEntity getMonthCategoryBudget(BudgetDatabase dbB, int month, int year, int category)
    {
        BudgetEntity entity;

        List<BudgetEntity> catEntities = dbB.budgetDao().getCategoryBeforeMonth(year, month, category);
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

    public BudgetEntity getYearCategoryBudget(BudgetDatabase dbB, int year, int category)
    {
        BudgetEntity budgetEntity = dbB.budgetDao().getYear(year, category);
        if (budgetEntity == null)
        {
            budgetEntity = new BudgetEntity(year, category, Categories.getCategories()[category]);
        }
        else { }

        return budgetEntity;
    }

    // Not used
    public void updateTotalBudget(BudgetDatabase dbB, int category)
    {
        int min = dbB.budgetDao().getMinYear(category);
        int max = dbB.budgetDao().getMaxYear(category);
        for (int i = min; i <= max; i++)
        {
            updateYearBudget(dbB, i, category);
        }
    }

    // Called by insert, update, delete
    public void updateYearBudget(BudgetDatabase dbB, BudgetEntity entity)
    {
        int year = entity.getYear();
        int category = entity.getCategory();
        updateYearBudget(dbB, year, category);
    }

    // Called by merging
    public void updateYearBudget(BudgetDatabase dbB, int year, int category)
    {
        float total = 0.0f;
        for (int i = 1; i <= 12; i++)
        {
            BudgetEntity monthEntity = getMonthCategoryBudget(dbB, i, year, category);
            total += monthEntity.getAmount();
            List<BudgetEntity> entities = dbB.budgetDao().getMonthAdjustments(year, i);
            int length = entities.size();
            for (int j = 0; j < length; j++)
            {
                total += entities.get(j).getAmount();
            }
        }
        BudgetEntity yearBudget = dbB.budgetDao().getYear(year, category);
        if (yearBudget == null)
        {
            yearBudget = new BudgetEntity(0, year, total, category, Categories.getCategories()[category]);
            dbB.budgetDao().insert(yearBudget);
        }
        else
        {
            yearBudget.setAmount(total);
            dbB.budgetDao().update(yearBudget);
        }
    }

    public void updateLinkedYearBudget(BudgetDatabase dbB, BudgetEntity entity, BudgetEntity linkedEntity)
    {
        int year = entity.getYear();
        int category = entity.getCategory();
        int linkedYear = linkedEntity.getYear();
        int linkedCategory = linkedEntity.getCategory();
        updateYearBudget(dbB, entity);
        if ((year != linkedYear) || (category != linkedCategory))
        {
            updateYearBudget(dbB, linkedEntity);
        }
        else { }
    }
}

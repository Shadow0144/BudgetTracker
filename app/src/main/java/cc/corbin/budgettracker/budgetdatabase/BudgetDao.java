package cc.corbin.budgettracker.budgetdatabase;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;
import android.arch.persistence.room.Update;

import java.util.List;

import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 4/15/2018.
 */

@Dao
public interface BudgetDao
{
    @Query("SELECT * FROM budgetentity")
    List<BudgetEntity> getAll();

    @Query("SELECT * FROM budgetentity " +
            "WHERE year = (:year) AND month = (:month) AND isAdjustment = 1 " +
            "ORDER BY category ASC")
    List<BudgetEntity> getMonthAdjustments(int year, int month);

    @Query("SELECT * FROM budgetentity " +
            "WHERE year < (:year) OR (year = (:year) AND month <= (:month))") /// TODO Fix
    List<BudgetEntity> getMonth(int year, int month);

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND month = (:month)")
    BudgetEntity getExactMonth(int year, int month);

    @Query("SELECT * FROM budgetentity WHERE " +
            "year = (SELECT MAX(year) FROM " +
            "(SELECT * FROM budgetentity WHERE " +
            "(((year < (:year)) OR (year = (:year) AND month <= (:month))) AND category = (:category))" +
            ")) " +
            "ORDER BY month DESC " +
            "LIMIT 1")
    BudgetEntity getMonth(int year, int month, int category); // TODO BROKEN!

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND month = (:month) AND category = (:category)")
    BudgetEntity getExactMonth(int year, int month, int category);

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND NOT month = 0")
    List<BudgetEntity> getYearAsMonths(int year);

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND month = 0")
    BudgetEntity getYear(int year);

    @Query("SELECT * FROM budgetentity WHERE year = (:year)  AND month = 0 AND category = (:category)")
    BudgetEntity getYear(int year, int category);

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "year = (:year) AND " +
            "month BETWEEN (:sMonth) AND (:eMonth)" +
            " )")
    List<BudgetEntity> getTimeSpan(int year, int sMonth, int eMonth);

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "year = (:year) AND " +
            "month BETWEEN (:sMonth) AND (:eMonth) AND " +
            "category = (:category)" +
            " )")
    List<BudgetEntity> getTimeSpan(int year, int sMonth, int eMonth, int category);

    // Query for finding the budgets for month view
    @Query("SELECT * FROM budgetentity WHERE ( " +
            "category = (:category)  AND isAdjustment = 0 " +
            "AND (year < (:year) OR (year = (:year) AND month <= (:month))) AND month != 0" +
            " )")
    List<BudgetEntity> getCategoryBeforeMonth(int year, int month, int category);

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "category = (:category)" +
            " )")
    List<BudgetEntity> getCategory(int category);

    @Query("SELECT * FROM budgetentity WHERE id IN (:budgetsIds)")
    List<BudgetEntity> loadAllByIds(int[] budgetsIds);

    @Query("UPDATE budgetentity SET categoryName = (:newCategoryName) WHERE category = (:category)")
    void renameCategory(int category, String newCategoryName);

    @Query("UPDATE budgetentity SET category = (:newCategory), categoryName = (:newCategoryName) WHERE category = (:category)")
    void mergeCategory(int category, int newCategory, String newCategoryName);

    @Query("UPDATE budgetentity SET category = (:newCategory) WHERE category = (:category)")
    void updateCategoryNumber(int category, int newCategory);

    @Query("UPDATE budgetentity SET category = (:newCategory) WHERE categoryName = (:categoryName)")
    void updateCategoryNumber(String categoryName, int newCategory);

    @Query("UPDATE budgetentity SET category = (category+1) WHERE category >= (:category)")
    void increaseCategoryNumbers(int category);

    @Query("UPDATE budgetentity SET category = (category-1) WHERE category >= (:category)")
    void decreaseCategoryNumbers(int category);

    @Insert
    long insert(BudgetEntity budget);

    @Insert
    long[] insertAll(BudgetEntity... budgets);

    @Insert
    long[] insertAll(List<BudgetEntity> budgets);

    @Delete
    void delete(BudgetEntity budget);

    @Delete
    void delete(List<BudgetEntity> budgets);

    @Query("DELETE FROM budgetentity WHERE id = (:id)")
    void delete(long id);

    @Update
    void update(BudgetEntity... budgets);

    @Update
    void update(List<BudgetEntity> budgets);

    @Query("UPDATE budgetentity SET amount = (:amount), note = (:note) WHERE id = (:id)")
    void updateAmountAndNote(long id, float amount, String note);

    @RawQuery
    List<BudgetEntity> customQuery(SupportSQLiteQuery query);

    @Query("SELECT MIN(year) FROM budgetentity WHERE category = (:category)")
    int getMinYear(int category);

    @Query("SELECT MAX(year) FROM budgetentity WHERE category = (:category)")
    int getMaxYear(int category);
}

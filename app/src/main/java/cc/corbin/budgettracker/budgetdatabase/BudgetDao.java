package cc.corbin.budgettracker.budgetdatabase;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Corbin on 4/15/2018.
 */

@Dao
public interface BudgetDao
{
    @Query("SELECT * FROM budgetentity")
    List<BudgetEntity> getAll();

    @Query("SELECT * FROM budgetentity " +
            "WHERE year < (:year) OR (year = (:year) AND month <= (:month))") /// TODO Fix
    List<BudgetEntity> getMonth(int year, int month);

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND month = (:month)")
    List<BudgetEntity> getOnlyMonth(int year, int month);

    @Query("SELECT * FROM budgetentity WHERE " +
            "year = (SELECT MAX(year) FROM " +
            "(SELECT * FROM budgetentity WHERE " +
            "(((year < (:year)) OR (year = (:year) AND month <= (:month))) AND category = (:category))" +
            ")) " +
            "ORDER BY month DESC " +
            "LIMIT 1")
    List<BudgetEntity> getMonth(int year, int month, int category); // TODO BROKEN!

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND month = (:month) AND category = (:category)")
    List<BudgetEntity> getOnlyMonth(int year, int month, int category);

    @Query("SELECT * FROM budgetentity WHERE year = (:year)")  /// TODO Fix
    List<BudgetEntity> getYear(int year);

    @Query("SELECT * FROM budgetentity WHERE year = (:year)")
    List<BudgetEntity> getOnlyYear(int year);

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND category = (:category)")  /// TODO Fix
    List<BudgetEntity> getYear(int year, int category);

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND category = (:category)")
    List<BudgetEntity> getOnlyYear(int year, int category);

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

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "category = (:category) " +
            "AND (year < (:year) OR (year = (:year) AND month <= (:month))) AND month != 0" +
            " )")
    List<BudgetEntity> getCategoryBeforeMonth(int year, int month, int category);

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "category = (:category)" +
            " )")
    List<BudgetEntity> getCategory(int category);

    @Query("SELECT * FROM budgetentity WHERE id IN (:budgetsIds)")
    List<BudgetEntity> loadAllByIds(int[] budgetsIds);

    @Query("UPDATE budgetentity SET categoryName = (:newCategoryName)  WHERE category = (:category)")
    void recategorize(int category, String newCategoryName);

    @Insert
    long insert(BudgetEntity budget);

    @Insert
    long[] insertAll(BudgetEntity... budgets);

    @Insert
    long[] insertAll(List<BudgetEntity> budgets);

    @Delete
    void delete(BudgetEntity budget);

    @Update
    void update(BudgetEntity... budgets);

    @Update
    void update(List<BudgetEntity> budgets);
}

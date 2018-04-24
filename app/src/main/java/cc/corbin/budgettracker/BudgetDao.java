package cc.corbin.budgettracker;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
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
            "(((year < (:year)) OR (year = (:year) AND month <= (:month))) AND expenseType = (:category))" +
            ")) " +
            "ORDER BY month DESC " +
            "LIMIT 1")
    List<BudgetEntity> getMonth(int year, int month, String category); // TODO BROKEN!

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND month = (:month) AND expenseType = (:category)")
    List<BudgetEntity> getOnlyMonth(int year, int month, String category);

    @Query("SELECT * FROM budgetentity WHERE year = (:year)")  /// TODO Fix
    List<BudgetEntity> getYear(int year);

    @Query("SELECT * FROM budgetentity WHERE year = (:year)")
    List<BudgetEntity> getOnlyYear(int year);

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND expenseType = (:category)")  /// TODO Fix
    List<BudgetEntity> getYear(int year, String category);

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND expenseType = (:category)")
    List<BudgetEntity> getOnlyYear(int year, String category);

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "year = (:year) AND " +
            "month BETWEEN (:sMonth) AND (:eMonth)" +
            " )")
    List<BudgetEntity> getTimeSpan(int year, int sMonth, int eMonth);

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "year = (:year) AND " +
            "month BETWEEN (:sMonth) AND (:eMonth) AND " +
            "expenseType = (:category)" +
            " )")
    List<BudgetEntity> getTimeSpan(int year, int sMonth, int eMonth, String category);

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "expenseType = (:category) " +
            "AND (year < (:year) OR (year = (:year) AND month <= (:month))) AND month != 0" +
            " )")
    List<BudgetEntity> getCategoryBeforeMonth(int year, int month, String category);

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "expenseType = (:category)" +
            " )")
    List<BudgetEntity> getCategory(String category);

    @Query("SELECT * FROM budgetentity WHERE id IN (:budgetsIds)")
    List<BudgetEntity> loadAllByIds(int[] budgetsIds);

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

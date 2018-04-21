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

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND month = (:month)")
    LiveData<List<BudgetEntity>> getMonth(int year, int month);

    @Query("SELECT * FROM budgetentity WHERE year = (:year) AND month = (:month) AND expenseType = (:category)")
    LiveData<List<BudgetEntity>> getMonth(int year, int month, String category);

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "year = (:year) AND " +
            "month BETWEEN (:sMonth) AND (:eMonth)" +
            " )")
    LiveData<List<BudgetEntity>> getTimeSpan(int year, int sMonth, int eMonth);

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "year = (:year) AND " +
            "month BETWEEN (:sMonth) AND (:eMonth) AND " +
            "expenseType = (:category)" +
            " )")
    LiveData<List<BudgetEntity>> getTimeSpan(int year, int sMonth, int eMonth, String category);

    @Query("SELECT * FROM budgetentity WHERE ( " +
            "expenseType = (:category)" +
            " )")
    LiveData<List<BudgetEntity>> getCategory(String category);

    @Query("SELECT * FROM budgetentity WHERE id IN (:budgetsIds)")
    LiveData<List<BudgetEntity>> loadAllByIds(int[] budgetsIds);

    @Insert
    void insertAll(BudgetEntity... budgets);

    @Insert
    void insertAll(List<BudgetEntity> budgets);

    @Delete
    void delete(BudgetEntity budget);

    @Update
    void update(BudgetEntity... budgets);

    @Update
    void update(List<BudgetEntity> budgets);
}

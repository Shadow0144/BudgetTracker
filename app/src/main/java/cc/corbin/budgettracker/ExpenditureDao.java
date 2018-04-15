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
 * Created by Corbin on 2/14/2018.
 */

@Dao
public interface ExpenditureDao
{
    @Query("SELECT * FROM expenditureentity")
    List<ExpenditureEntity> getAll();

    @Query("SELECT * FROM expenditureentity WHERE year = (:year) AND month = (:month) AND day = (:day)")
    LiveData<List<ExpenditureEntity>> getDay(int year, int month, int day);

    @Query("SELECT * FROM expenditureentity WHERE (" +
            "year = (:year) AND " +
            "month = (:month) AND " +
            "day BETWEEN (:sDay) AND (:eDay))")
    LiveData<List<ExpenditureEntity>> getTimeSpan(int year, int month, int sDay, int eDay);

    @Query("SELECT * FROM expenditureentity WHERE (" +
            "(year > (:sYear) AND year < (:eYear)) OR " +
            "(year = (:sYear) AND year < (:eYear) AND month > (:sMonth)) OR " +
            "(year = (:sYear) AND year < (:eYear) AND month = (:sMonth) AND day >= (:sDay)) OR " +
            "(year > (:sYear) AND year = (:eYear) AND month < (:eMonth) OR " +
            "(year > (:sYear) AND year = (:eYear) AND month = (:eMonth) AND day <= (:eDay)) OR " +
            "(year = (:sYear) AND year = (:eYear) AND month > (:sMonth) AND month < (:eMonth)) OR " +
            "(year = (:sYear) AND year = (:eYear) AND month = (:sMonth) AND month < (:eMonth) AND day >= (:sDay)) OR " +
            "(year = (:sYear) AND year = (:eYear) AND month > (:sMonth) AND month = (:eMonth) AND day <= (:eDay)) OR " +
            "(year = (:sYear) AND year = (:eYear) AND month = (:sMonth) AND month = (:eMonth) AND day >= (:sDay) AND day <= (:eDay)))" +
            ")")
    LiveData<List<ExpenditureEntity>> getTimeSpan(int sYear, int sMonth, int sDay, int eYear, int eMonth, int eDay);

    @Query("SELECT * FROM expenditureentity WHERE (" +
            "year = (:year) AND " +
            "month = (:month) AND " +
            "day BETWEEN (:sDay) AND (:eDay)) AND " +
            "expenseType = (:category)")
    LiveData<List<ExpenditureEntity>> getTimeSpan(int year, int month, int sDay, int eDay, String category);

    @Query("SELECT * FROM expenditureentity WHERE (" +
            "(" +
            "(year > (:sYear) AND year < (:eYear)) OR " +
            "(year = (:sYear) AND year < (:eYear) AND month > (:sMonth)) OR " +
            "(year = (:sYear) AND year < (:eYear) AND month = (:sMonth) AND day >= (:sDay)) OR " +
            "(year > (:sYear) AND year = (:eYear) AND month < (:eMonth) OR " +
            "(year > (:sYear) AND year = (:eYear) AND month = (:eMonth) AND day <= (:eDay)) OR " +
            "(year = (:sYear) AND year = (:eYear) AND month > (:sMonth) AND month < (:eMonth)) OR " +
            "(year = (:sYear) AND year = (:eYear) AND month = (:sMonth) AND month < (:eMonth) AND day >= (:sDay)) OR " +
            "(year = (:sYear) AND year = (:eYear) AND month > (:sMonth) AND month = (:eMonth) AND day <= (:eDay)) OR " +
            "(year = (:sYear) AND year = (:eYear) AND month = (:sMonth) AND month = (:eMonth) AND day >= (:sDay) AND day <= (:eDay)))" +
            ") AND " +
            " expenseType = (:category) " +
            ")")
    LiveData<List<ExpenditureEntity>> getTimeSpan(int sYear, int sMonth, int sDay, int eYear, int eMonth, int eDay, String category);

    @Query("SELECT * FROM expenditureentity WHERE id IN (:expenditureIds)")
    LiveData<List<ExpenditureEntity>> loadAllByIds(int[] expenditureIds);

    @Insert
    void insertAll(ExpenditureEntity... expenditures);

    @Insert
    void insertAll(List<ExpenditureEntity> expenditures);

    @Delete
    void delete(ExpenditureEntity expenditure);

    @Update
    void update(ExpenditureEntity... expenditures);

    @Update
    void update(List<ExpenditureEntity> expenditures);
}

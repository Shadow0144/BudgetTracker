package cc.corbin.budgettracker.expendituredatabase;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.arch.persistence.room.RawQuery;

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
    List<ExpenditureEntity> getDay(int year, int month, int day);

    @Query("SELECT * FROM expenditureentity WHERE year = (:year) AND month = (:month) ORDER BY day ASC")
    List<ExpenditureEntity> getMonth(int year, int month);

    @Query("SELECT * FROM expenditureentity WHERE year = (:year) ORDER BY month, day ASC")
    List<ExpenditureEntity> getYear(int year);

    @Query("SELECT * FROM expenditureentity ORDER BY year, month, day ASC")
    List<ExpenditureEntity> getTotal();

    @Query("SELECT * FROM expenditureentity WHERE (" +
            "year = (:year) AND " +
            "month = (:month) AND " +
            "day BETWEEN (:sDay) AND (:eDay))")
    List<ExpenditureEntity> getTimeSpan(int year, int month, int sDay, int eDay);

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
    List<ExpenditureEntity> getTimeSpan(int sYear, int sMonth, int sDay, int eYear, int eMonth, int eDay);

    @Query("SELECT * FROM expenditureentity WHERE (" +
            "year = (:year) AND " +
            "month = (:month) AND " +
            "day BETWEEN (:sDay) AND (:eDay)) AND " +
            "category = (:category)")
    List<ExpenditureEntity> getTimeSpan(int year, int month, int sDay, int eDay, int category);

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
            " category = (:category) " +
            ")")
    List<ExpenditureEntity> getTimeSpan(int sYear, int sMonth, int sDay, int eYear, int eMonth, int eDay, int category);

    @Query("SELECT * FROM expenditureentity WHERE id IN (:expenditureIds)")
    List<ExpenditureEntity> loadAllByIds(long[] expenditureIds);

    @Query("SELECT SUM (amount) FROM expenditureentity WHERE ( category = (:category) AND year = (:year) AND month = (:month) )")
    float getMonthCategory(int category, int year, int month);

    @Query("UPDATE expenditureentity SET categoryName = (:newCategoryName) WHERE category = (:category)")
    void renameCategory(int category, String newCategoryName);

    @Query("UPDATE expenditureentity SET category = (:newCategory), categoryName = (:newCategoryName) WHERE category = (:category)")
    void mergeCategory(int category, int newCategory, String newCategoryName);

    @Query("UPDATE expenditureentity SET category = (:newCategory) WHERE category = (:category)")
    void updateCategoryNumber(int category, int newCategory);

    @Query("UPDATE expenditureentity SET category = (:newCategory) WHERE categoryName = (:categoryName)")
    void updateCategoryNumber(String categoryName, int newCategory);

    @Query("UPDATE expenditureentity SET category = (category+1) WHERE category >= (:category)")
    void increaseCategoryNumbers(int category);

    @Query("UPDATE expenditureentity SET category = (category-1) WHERE category >= (:category)")
    void decreaseCategoryNumbers(int category);

    @Insert
    long insert(ExpenditureEntity expenditures);

    @Insert
    long[] insertAll(ExpenditureEntity... expenditures);

    @Insert
    long[] insertAll(List<ExpenditureEntity> expenditures);

    @Delete
    void delete(ExpenditureEntity expenditure);

    @Update
    void update(ExpenditureEntity... expenditures);

    @Update
    void update(List<ExpenditureEntity> expenditures);

    @RawQuery
    List<ExpenditureEntity> customQuery(SupportSQLiteQuery query);
}

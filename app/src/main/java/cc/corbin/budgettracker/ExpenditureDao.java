package cc.corbin.budgettracker;

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

    @Query("SELECT * FROM expenditureentity WHERE date BETWEEN (:date) AND (:date+86400000-1)")
    List<ExpenditureEntity> getDay(long date);

    @Query("SELECT * FROM expenditureentity WHERE date BETWEEN (:sDate) AND (:eDate+86400000-1)")
    List<ExpenditureEntity> getTimeSpan(long sDate, long eDate);

    @Query("SELECT * FROM expenditureentity WHERE date BETWEEN (:sDate) AND (:eDate+86400000-1) AND expenseType = (:category)")
    List<ExpenditureEntity> getTimeSpan(long sDate, long eDate, String category);

    @Query("SELECT * FROM expenditureentity WHERE date IN (:expenditureIds)")
    List<ExpenditureEntity> loadAllByIds(int[] expenditureIds);

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

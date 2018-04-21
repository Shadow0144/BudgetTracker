package cc.corbin.budgettracker;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Corbin on 4/15/2018.
 */

@Database(entities = {BudgetEntity.class}, version = 1)
public abstract class BudgetDatabase extends RoomDatabase
{
    public abstract BudgetDao budgetDao();

    private static BudgetDatabase INSTANCE;

    public static BudgetDatabase getBudgetDatabase(Context context)
    {
        BudgetDatabase r;
        if (INSTANCE == null)
        {
            r = INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), BudgetDatabase.class, "budgets")
                            //.fallbackToDestructiveMigration()
                            //.allowMainThreadQueries()
                            .build();
        }
        else
        {
            r = INSTANCE;
        }
        return r;
    }

    public static void destroyInstance()
    {
        INSTANCE = null;
    }
}
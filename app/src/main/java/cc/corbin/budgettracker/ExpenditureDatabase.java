package cc.corbin.budgettracker;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by Corbin on 2/14/2018.
 */

@Database(entities = {ExpenditureEntity.class}, version = 5)
public abstract class ExpenditureDatabase extends RoomDatabase
{
    public abstract ExpenditureDao expenditureDao();

    private static ExpenditureDatabase INSTANCE;

    public static ExpenditureDatabase getExpenditureDatabase(Context context)
    {
        ExpenditureDatabase r;
        if (INSTANCE == null)
        {
            r = INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), ExpenditureDatabase.class, "expenditures")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
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

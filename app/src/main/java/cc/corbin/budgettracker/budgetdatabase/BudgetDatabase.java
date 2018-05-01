package cc.corbin.budgettracker.budgetdatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

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
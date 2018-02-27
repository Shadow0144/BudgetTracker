package cc.corbin.budgettracker;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by Corbin on 2/14/2018.
 */

@Database(entities = {ExpenditureEntity.class}, version = 6)
public abstract class ExpenditureDatabase extends RoomDatabase
{
    public abstract ExpenditureDao expenditureDao();

    private static ExpenditureDatabase INSTANCE;

    static final Migration MIGRATION_5_6 = new Migration(5, 6)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE ExpenditureEntity"
                    + " ADD COLUMN note TEXT NOT NULL DEFAULT ''");
        }
    };

    public static ExpenditureDatabase getExpenditureDatabase(Context context)
    {
        ExpenditureDatabase r;
        if (INSTANCE == null)
        {
            r = INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), ExpenditureDatabase.class, "expenditures")
                            //.fallbackToDestructiveMigration()
                            .addMigrations(MIGRATION_5_6)
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
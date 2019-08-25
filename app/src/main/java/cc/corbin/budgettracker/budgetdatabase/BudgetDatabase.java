package cc.corbin.budgettracker.budgetdatabase;

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
import android.util.Log;

import cc.corbin.budgettracker.BudgetTrackerApplication;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;

/**
 * Created by Corbin on 4/15/2018.
 */

@Database(entities = {BudgetEntity.class}, version = 5)
public abstract class BudgetDatabase extends RoomDatabase
{
    private final static String TAG = "BudgetDatabase";

    public abstract BudgetDao budgetDao();

    private static BudgetDatabase INSTANCE;

    static final Migration MIGRATION_1_2 = new Migration(1, 2)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            // Get the entities
            Cursor cursor = database.query("SELECT * From BudgetEntity");

            // long id;
            // int month;
            // int year;
            // int currency;
            // float amount;
            // String categoryName;

            // v v v

            // long id;
            // int month;
            // int year;
            // x
            // float amount;
            // ---> int category;
            // String categoryName;

            // Store them as objects
            int i;
            String[] categories = Categories.getCategories();
            BudgetEntity[] entities = new BudgetEntity[cursor.getCount()];
            cursor.moveToFirst();
            for (i = 0; i < entities.length; i++)
            {
                BudgetEntity entity = new BudgetEntity(
                        cursor.getLong(0), // id
                        cursor.getInt(1), // month
                        cursor.getInt(2), // year
                        cursor.getFloat(4), // amount
                        0, // ---> category
                        cursor.getString(5) // categoryName
                );
                entities[i] = entity;
                for (int j = 0; j < categories.length; j++)
                {
                    if (entity.getCategoryName().equals(categories[j]))
                    {
                        entity.setCategory(j, categories[j]);
                        break;
                    }
                    else { }
                }
                cursor.moveToNext();
            }

            // Create the new table
            database.execSQL(
                    "CREATE TABLE NewBudgetEntity " +
                            "( " +
                            "id INTEGER NOT NULL UNIQUE PRIMARY KEY, " +
                            "month INTEGER NOT NULL DEFAULT 0, " +
                            "year INTEGER NOT NULL DEFAULT 0, " +
                            "amount REAL NOT NULL DEFAULT 0.0, " +
                            "category INTEGER NOT NULL DEFAULT 0, " +
                            "categoryName TEXT " +
                            ");"
            );

            // Fill the new table
            for (i = 0; i < entities.length; i++)
            {
                ContentValues values = new ContentValues();
                values.put("id", i);
                values.put("month", entities[i].getMonth());
                values.put("year", entities[i].getYear());
                values.put("amount", entities[i].getAmount());
                values.put("category", entities[i].getCategory());
                values.put("categoryName", entities[i].getCategoryName());
                database.insert("NewBudgetEntity", SQLiteDatabase.CONFLICT_ABORT, values);
            }

            // Delete the old table
            database.execSQL("DROP TABLE BudgetEntity;");

            // Rename the table
            database.execSQL("ALTER TABLE NewBudgetEntity " +
                    "RENAME TO BudgetEntity;");

            /*// Set the text columns to be not nullable
            database.execSQL("ALTER TABLE ExpenditureEntity"
                    + " ALTER COLUMN categoryName TEXT NOT NULL DEFAULT '';");*/
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE BudgetEntity"
                    + " ADD COLUMN adjustment INTEGER NOT NULL DEFAULT 0;");

            database.execSQL("ALTER TABLE BudgetEntity"
                    + " ADD COLUMN sisterAdjustment INTEGER NOT NULL DEFAULT -1;");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE BudgetEntity"
                    + " ADD COLUMN note TEXT;");

            database.execSQL("UPDATE BudgetEntity " +
                "SET note = '';");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            // Get the entities
            Cursor cursor = database.query("SELECT * From BudgetEntity");

            // Store them as objects
            int i = 0;
            BudgetEntity[] entities = new BudgetEntity[cursor.getColumnCount()];
            cursor.moveToFirst();
            for (i = 0; i < entities.length; i++)
            {
                BudgetEntity entity = new BudgetEntity(
                        cursor.getLong(0), // ID
                        cursor.getInt(1), // month
                        cursor.getInt(2), // year
                        cursor.getFloat(3), // amount
                        cursor.getInt(4), // category
                        cursor.getString(5), // categoryName
                        cursor.getInt(6), // adjustment
                        cursor.getLong(7), // sisterID
                        cursor.getString(8) // note
                );
                entities[i] = entity;
                cursor.moveToNext();
            }

            // Create the new table
            database.execSQL(
                    "CREATE TABLE NewBudgetEntity " +
                            "( " +
                            "id INTEGER NOT NULL UNIQUE PRIMARY KEY, " +
                            "month INTEGER NOT NULL, " +
                            "year INTEGER NOT NULL, " +
                            "amount REAL NOT NULL, " +
                            "category INTEGER NOT NULL, " +
                            "categoryName TEXT, " +
                            "isAdjustment INTEGER NOT NULL DEFAULT 0, " +
                            "linkedID INTEGER NOT NULL DEFAULT -1, " +
                            "linkedMonth INTEGER NOT NULL DEFAULT -1, " +
                            "linkedYear INTEGER NOT NULL DEFAULT -1, " +
                            "linkedCategory INTEGER NOT NULL DEFAULT -1, " +
                            "note TEXT " +
                            ");"
            );

            // Fill the new table
            for (i = 0; i < entities.length; i++)
            {
                ContentValues values = new ContentValues();
                values.put("id", i);
                values.put("month", entities[i].getMonth());
                values.put("year", entities[i].getYear());
                values.put("amount", entities[i].getAmount());
                values.put("category", entities[i].getCategory());
                values.put("categoryName", entities[i].getCategoryName());
                values.put("isAdjustment", entities[i].getIsAdjustment());
                values.put("linkedID", entities[i].getLinkedID());
                values.put("linkedMonth", entities[i].getLinkedMonth());
                values.put("linkedYear", entities[i].getLinkedYear());
                values.put("linkedCategory", entities[i].getLinkedCategory());
                values.put("note", entities[i].getNote());
                database.insert("NewBudgetEntity", SQLiteDatabase.CONFLICT_ABORT, values);
            }

            // Delete the old table
            database.execSQL("DROP TABLE BudgetEntity;");

            // Rename the table
            database.execSQL("ALTER TABLE NewBudgetEntity " +
                    "RENAME TO BudgetEntity;");
        }
    };

    public static void createDatabaseFile(String originalDatabase, String folder,
                                          String databaseName, String whereQuery)
    {
        try
        {
            SQLiteDatabase db =
                    SQLiteDatabase.openOrCreateDatabase((folder + databaseName), null);

            db.execSQL("DROP TABLE IF EXISTS "
                    + "BudgetEntity;");

            // Create the new table
            db.execSQL("ATTACH DATABASE '" + originalDatabase + "' AS transfer_db");
            db.execSQL("CREATE TABLE " + "BudgetEntity " +
                    "AS SELECT * FROM transfer_db."+ "BudgetEntity " +
                    whereQuery + ";" );

            db.execSQL("DETACH transfer_db");

            db.close();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    public static BudgetDatabase getBudgetDatabase()
    {
        BudgetDatabase r;
        if (INSTANCE == null)
        {
            r = INSTANCE =
                    Room.databaseBuilder(BudgetTrackerApplication.getInstance(), BudgetDatabase.class, "budgets")
                            //.fallbackToDestructiveMigration()
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .addMigrations(MIGRATION_4_5)
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
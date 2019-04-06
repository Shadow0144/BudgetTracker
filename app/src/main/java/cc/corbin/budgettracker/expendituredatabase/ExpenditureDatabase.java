package cc.corbin.budgettracker.expendituredatabase;

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

import cc.corbin.budgettracker.BudgetTrackerApplication;
import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;

/**
 * Created by Corbin on 2/14/2018.
 */

@Database(entities = {ExpenditureEntity.class}, version = 10)
public abstract class ExpenditureDatabase extends RoomDatabase
{
    public abstract ExpenditureDao expenditureDao();

    private static ExpenditureDatabase INSTANCE;

    static final Migration MIGRATION_5_6 = new Migration(5, 6)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE ExpenditureEntity "
                    + "ADD COLUMN note TEXT NOT NULL DEFAULT '';");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) // Failure!!!
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            // Get the entities
            Cursor cursor = database.query("SELECT * From ExpenditureEntity");

            // Store them as objects
            int i = 0;
            ExpenditureEntity[] entities = new ExpenditureEntity[cursor.getColumnCount()];
            cursor.moveToFirst();
            for (i = 0; i < entities.length; i++)
            {
                ExpenditureEntity entity = new ExpenditureEntity(
                    cursor.getLong(0), // date
                    cursor.getInt(1), // currency
                    cursor.getFloat(2), // amount
                    cursor.getString(3), // expenseType
                    cursor.getString(4) // note
                );
                entities[i] = entity;
                cursor.moveToNext();
            }

            // Create the new table
            database.execSQL(
                    "CREATE TABLE NewExpenditureEntity " +
                            "( " +
                            "id INTEGER NOT NULL UNIQUE PRIMARY KEY, " +
                            "day INTEGER NOT NULL, " +
                            "month INTEGER NOT NULL, " +
                            "year INTEGER NOT NULL, " +
                            "currency INTEGER NOT NULL, " +
                            "amount REAL NOT NULL, " +
                            "expenseType TEXT, " +
                            "note TEXT " +
                            ");"
            );

            // Fill the new table
            for (i = 0; i < entities.length; i++)
            {
                ContentValues values = new ContentValues();
                values.put("id", i);
                values.put("day", entities[i].getDay());
                values.put("month", entities[i].getMonth());
                values.put("year", entities[i].getYear());
                values.put("currency", Currencies.default_currency); //entities[i].getCurrency());
                values.put("amount", entities[i].getAmount());
                values.put("expenseType", entities[i].getCategoryName()); //entities[i].getExpenseType());
                values.put("note", entities[i].getNote());
                database.insert("NewExpenditureEntity", SQLiteDatabase.CONFLICT_ABORT, values);
            }

            // Delete the old table
            database.execSQL("DROP TABLE ExpenditureEntity;");

            // Rename the table
            database.execSQL("ALTER TABLE NewExpenditureEntity " +
                    "RENAME TO ExpenditureEntity;");

            /*// Set the text columns to be not nullable
            database.execSQL("ALTER TABLE ExpenditureEntity"
                    + " ALTER COLUMN expenseType TEXT NOT NULL DEFAULT '';");
            database.execSQL("ALTER TABLE ExpenditureEntity"
                    + " ALTER COLUMN note TEXT NOT NULL DEFAULT '';");*/
        }
    };

    static final Migration MIGRATION_7_8 = new Migration(7, 8)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE ExpenditureEntity"
                    + " ADD COLUMN generatedNote TEXT DEFAULT '';");
        }
    };

    static final Migration MIGRATION_8_9 = new Migration(8, 9)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE ExpenditureEntity " +
                    "ADD COLUMN baseCurrency INTEGER NOT NULL DEFAULT " + Currencies.default_currency + "; ");
            database.execSQL("ALTER TABLE ExpenditureEntity " +
                    "ADD COLUMN baseAmount REAL NOT NULL DEFAULT 0; ");
            database.execSQL("ALTER TABLE ExpenditureEntity " +
                    "ADD COLUMN conversionRate REAL NOT NULL DEFAULT 1.0; ");
            database.execSQL("UPDATE ExpenditureEntity " +
                    "SET baseAmount = amount; ");
            database.execSQL(
                    "CREATE TABLE ExpenditureEntity_backup " +
                            "( " +
                            "id INTEGER NOT NULL UNIQUE PRIMARY KEY, " +
                            "day INTEGER NOT NULL, " +
                            "month INTEGER NOT NULL, " +
                            "year INTEGER NOT NULL, " +
                            "amount REAL NOT NULL, " +
                            "expenseType TEXT, " +
                            "baseCurrency INTEGER NOT NULL DEFAULT "  + Currencies.default_currency + ", " +
                            "baseAmount REAL NOT NULL DEFAULT 0, " +
                            "conversionRate REAL NOT NULL DEFAULT 1.0, " +
                            "note TEXT " +
                            ");"
            );
            database.execSQL("INSERT INTO ExpenditureEntity_backup SELECT " +
                    "id, day, month, year, amount, expenseType, baseCurrency, baseAmount, conversionRate, note " +
                    "FROM ExpenditureEntity");
            database.execSQL("DROP TABLE ExpenditureEntity; ");
            database.execSQL("ALTER TABLE ExpenditureEntity_backup RENAME TO ExpenditureEntity; ");
        }
    };

    static final Migration MIGRATION_9_10 = new Migration(9, 10)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            // Get the entities
            Cursor cursor = database.query("SELECT * From ExpenditureEntity");

            // long id;
            // int day;
            // int month;
            // int year;
            // float amount;
            // ---> // int category;
            // String categoryName;
            // int baseCurrency;
            // float baseAmount;
            // float conversionRate;
            // String note;

            // Store them as objects
            int i;
            String[] categories = Categories.getCategories();
            ExpenditureEntity[] entities = new ExpenditureEntity[cursor.getCount()];
            cursor.moveToFirst();
            for (i = 0; i < entities.length; i++)
            {
                ExpenditureEntity entity = new ExpenditureEntity(
                        cursor.getLong(0), // id
                        cursor.getInt(1), // day
                        cursor.getInt(2), // month
                        cursor.getInt(3), // year
                        cursor.getFloat(4), // amount
                        0, // ---> category
                        cursor.getString(5), // categoryName
                        cursor.getInt(6), // baseCurrency
                        cursor.getFloat(7), // baseAmount
                        cursor.getFloat(8), // conversionRate
                        cursor.getString(9) // note
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
                    "CREATE TABLE NewExpenditureEntity " +
                            "( " +
                            "id INTEGER NOT NULL UNIQUE PRIMARY KEY, " +
                            "day INTEGER NOT NULL DEFAULT 0, " +
                            "month INTEGER NOT NULL DEFAULT 0, " +
                            "year INTEGER NOT NULL DEFAULT 0, " +
                            "amount REAL NOT NULL DEFAULT 0.0, " +
                            "category INTEGER NOT NULL DEFAULT 0," +
                            "categoryName TEXT, " +
                            "baseCurrency INTEGER NOT NULL DEFAULT 0," +
                            "baseAmount REAL NOT NULL DEFAULT 0.0," +
                            "conversionRate REAL NOT NULL DEFAULT 0.0, " +
                            "note TEXT " +
                            ");"
            );

            // Fill the new table
            for (i = 0; i < entities.length; i++)
            {
                ContentValues values = new ContentValues();
                values.put("id", i);
                values.put("day", entities[i].getDay());
                values.put("month", entities[i].getMonth());
                values.put("year", entities[i].getYear());
                values.put("amount", entities[i].getAmount());
                values.put("category", entities[i].getCategory());
                values.put("categoryName", entities[i].getCategoryName());
                values.put("baseCurrency", entities[i].getBaseCurrency());
                values.put("baseAmount", entities[i].getBaseAmount());
                values.put("conversionRate", entities[i].getConversionRate());
                values.put("note", entities[i].getNote());
                database.insert("NewExpenditureEntity", SQLiteDatabase.CONFLICT_ABORT, values);
            }

            // Delete the old table
            database.execSQL("DROP TABLE ExpenditureEntity;");

            // Rename the table
            database.execSQL("ALTER TABLE NewExpenditureEntity " +
                    "RENAME TO ExpenditureEntity;");

            /*// Set the text columns to be not nullable
            database.execSQL("ALTER TABLE ExpenditureEntity"
                    + " ALTER COLUMN categoryName TEXT NOT NULL DEFAULT '';");
            database.execSQL("ALTER TABLE ExpenditureEntity"
                    + " ALTER COLUMN note TEXT NOT NULL DEFAULT '';");*/
        }
    };

    public static ExpenditureDatabase getExpenditureDatabase()
    {
        ExpenditureDatabase r;
        if (INSTANCE == null)
        {
            r = INSTANCE =
                    Room.databaseBuilder(BudgetTrackerApplication.getInstance(), ExpenditureDatabase.class, "expenditures")
                            //.fallbackToDestructiveMigration()
                            .addMigrations(MIGRATION_5_6)
                            .addMigrations(MIGRATION_6_7)
                            .addMigrations(MIGRATION_7_8)
                            .addMigrations(MIGRATION_8_9)
                            .addMigrations(MIGRATION_9_10)
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
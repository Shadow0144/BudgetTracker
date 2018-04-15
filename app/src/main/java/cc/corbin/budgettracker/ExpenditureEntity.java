package cc.corbin.budgettracker;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Corbin on 2/14/2018.
 */

@Entity
public class ExpenditureEntity
{
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo
    private int day;

    @ColumnInfo
    private int month;

    @ColumnInfo
    private int year;

    @ColumnInfo
    private int currency;

    @ColumnInfo
    private float amount;

    @ColumnInfo
    private String expenseType;

    @Ignore
    private int expenseTypeNumber;

    @ColumnInfo
    private String note;

    @Ignore
    public ExpenditureEntity()
    {
        this.id = 0;
        this.day = 0;
        this.month = 0;
        this.year = 0;
        this.currency = 0;
        this.amount = 0.0f;
        this.expenseType = "";
        this.note = "";
    }

    @Ignore
    public ExpenditureEntity(long date, int currency, float amount, String expenseType, String note)
    {
        this.id = 0;
        this.currency = currency;
        this.amount = amount;
        this.expenseType = expenseType;
        this.note = note;
        checkNullStrings();
        convertDateToDMY(date);
    }

    @Ignore
    public ExpenditureEntity(int day, int month, int year)
    {
        this.id = 0;
        this.day = day;
        this.month = month;
        this.year = year;
        this.currency = 0;
        this.amount = 0.0f;
        this.expenseType = "";
        this.note = "";
        checkNullStrings();
    }

    @Ignore
    public ExpenditureEntity(int day, int month, int year, int currency, float amount, String expenseType, String note)
    {
        this.id = 0;
        this.day = day;
        this.month = month;
        this.year = year;
        this.currency = currency;
        this.amount = amount;
        this.expenseType = expenseType;
        this.note = note;
        checkNullStrings();
    }

    public ExpenditureEntity(int id, int day, int month, int year, int currency, float amount, String expenseType, String note)
    {
        this.id = id;
        this.day = day;
        this.month = month;
        this.year = year;
        this.currency = currency;
        this.amount = amount;
        this.expenseType = expenseType;
        this.note = note;
        checkNullStrings();
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getDay()
    {
        return day;
    }

    public void setDay(int day)
    {
        this.day = day;
    }

    public int getMonth()
    {
        return month;
    }

    public void setMonth(int month)
    {
        this.month = month;
    }

    public int getYear()
    {
        return year;
    }

    public void setYear(int year)
    {
        this.year = year;
    }

    public int getCurrency()
    {
        return currency;
    }

    public void setCurrency(int currency)
    {
        this.currency = currency;
    }

    public float getAmount()
    {
        return amount;
    }

    public void setAmount(float amount)
    {
        this.amount = amount;
    }

    public String getExpenseType()
    {
        return expenseType;
    }

    public void setExpenseType(String expenseType)
    {
        this.expenseType = expenseType;
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }

    @Ignore
    public int getExpenseTypeNumber()
    {
        return expenseTypeNumber;
    }

    @Ignore
    public void setExpenseTypeNumber(int expenseTypeNumber)
    {
        this.expenseTypeNumber = expenseTypeNumber;
    }

    @Ignore
    private void checkNullStrings()
    {
        if (this.expenseType == null) this.expenseType = "";
        if (this.note == null) this.note = "";
    }

    @Ignore
    private void convertDateToDMY(long date)
    {
        Calendar currentDate = Calendar.getInstance();

        currentDate.setTimeInMillis(date);

        day = currentDate.get(Calendar.DATE);
        month = currentDate.get(Calendar.MONTH)+1;
        year = currentDate.get(Calendar.YEAR);
    }
}

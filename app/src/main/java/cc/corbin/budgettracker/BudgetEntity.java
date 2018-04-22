package cc.corbin.budgettracker;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Corbin on 4/15/2018.
 */

@Entity
public class BudgetEntity
{
    @PrimaryKey(autoGenerate = true)
    private long id;

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
    public BudgetEntity()
    {
        this.id = 0;
        this.month = 0;
        this.year = 0;
        this.currency = 0;
        this.amount = 0.0f;
        this.expenseType = "";
    }

    @Ignore
    public BudgetEntity(int month, int year, int currency, float amount, String expenseType)
    {
        this.id = 0;
        this.month = month;
        this.year = year;
        this.currency = currency;
        this.amount = amount;
        this.expenseType = expenseType;
    }

    public BudgetEntity(long id, int month, int year, int currency, float amount, String expenseType)
    {
        this.id = id;
        this.month = month;
        this.year = year;
        this.currency = currency;
        this.amount = amount;
        this.expenseType = expenseType;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
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
}

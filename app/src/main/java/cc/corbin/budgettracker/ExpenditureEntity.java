package cc.corbin.budgettracker;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Corbin on 2/14/2018.
 */

@Entity
public class ExpenditureEntity
{
    @PrimaryKey
    private long date;

    @ColumnInfo
    private int currency;

    @ColumnInfo
    private float amount;

    @ColumnInfo
    private String expenseType;

    // Must have a date set as this is the PRIMARY_KEY

    @Ignore
    public ExpenditureEntity(long date)
    {
        this.date = date;
        this.currency = 0;
        this.amount = 0.0f;
        this.expenseType = "";
    }

    public ExpenditureEntity(long date, int currency, float amount, String expenseType)
    {
        this.date = date;
        this.currency = currency;
        this.amount = amount;
        this.expenseType = expenseType;
    }

    public long getDate()
    {
        return date;
    }

    public void setDate(long date)
    {
        this.date = date;
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

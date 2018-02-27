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

    @Ignore
    private int expenseTypeNumber;

    @ColumnInfo
    private String note;

    // Must have a date set as this is the PRIMARY_KEY

    @Ignore
    public ExpenditureEntity(long date)
    {
        this.date = date;
        this.currency = 0;
        this.amount = 0.0f;
        this.expenseType = "";
        this.note = "";
    }

    public ExpenditureEntity(long date, int currency, float amount, String expenseType, String note)
    {
        this.date = date;
        this.currency = currency;
        this.amount = amount;
        this.expenseType = expenseType;
        this.note = note;
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
}
